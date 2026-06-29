# ADR-001: Service Layer Boundary

| Field | Value |
|-------|-------|
| **Status** | Accepted |
| **Date** | 2026-06-26 |
| **Input** | [Task 1 — Service Layer Architecture Audit](./task-1-service-layer-audit.md) |
| **Scope** | Project-wide service-layer boundaries |

---

## Context

The XML Generation System uses a feature-based layered architecture on Spring Boot 3:

```text
Controller  →  Service  →  Repository  →  Database
```

[Task 1](./task-1-service-layer-audit.md) audited the implemented codebase and recorded the following factual baseline:

- Two modules contain production logic: **Authentication** and **Template**.
- Controllers are thin: they delegate to services and wrap results in `ApiResponse`.
- Service public interfaces use request/response DTOs and do not expose JPA entity classes or Spring Data types.
- Mapping is performed inline inside service implementations; no `Mapper` classes exist.
- `@Transactional` appears only on service public methods.
- `PageMeta` and `ApiResponse.ok(data, meta)` exist in `common.api`, but no paginated endpoint or service method uses them.
- `SecurityContextHolder` is read in one service operation (`TemplateServiceImpl.create`) via a private unchecked helper.
- JPA models reside in `{module}.entity`; non-JPA type `AuthenticatedUser` resides in `authentication.domain`.
- Project documentation (`develop-standards.md`, `project-structure.md`) does not fully align with several implemented practices.

Task 1 identified boundary questions (§16) and risks from missing standards (§17). This ADR resolves those questions at project level.

---

## Problem Statement

Without explicit service-layer boundaries:

1. Each new module may resolve the same design questions independently (pagination return type, mapping approach, identity resolution).
2. Framework types such as Spring Data `Page` may leak into service public contracts.
3. Feature-specific result wrappers may proliferate per endpoint.
4. Implementation and documentation may continue to diverge.
5. Security and error-handling behavior may become inconsistent across modules.

A single project-level decision record is required before further feature work proceeds on paginated lists and additional business modules.

---

## Decision Drivers

1. **Consistency** — All modules follow the same layer contracts.
2. **Principle over heuristic** — Rules express architectural intent, not arbitrary numeric thresholds.
3. **Framework independence at service boundaries** — Public service interfaces must not expose Spring Data or Spring Web types.
4. **Alignment with existing HTTP model** — `ApiResponse<T>` and `PageMeta` already exist in `common.api` per API design.
5. **Audit evidence** — Decisions build on observed implementation strengths and close documented gaps.
6. **Reusability** — Contracts must apply to Authentication, Template, Master Data, Export History, Saved Input, and XML Generation modules.
7. **Testability** — Boundaries must support unit testing with mocked dependencies, as existing service tests demonstrate.

---

## Alternatives Considered

### Service public contract

| Alternative | Summary | Not chosen because |
|-------------|---------|-------------------|
| Expose JPA entities on service interfaces | Service methods return `*Entity` | Leaks persistence model; contradicts current practice (Task 1 §2.2, §6) |
| Expose Spring `Page<T>` on service interfaces | Service returns `Page<Dto>` | Framework type on public contract; couples consumers to Spring Data |
| Domain-only service contracts (no DTOs) | Separate domain models for all I/O | Additional mapping layer not justified at current complexity |
| **DTOs on service interfaces** | Request/response records as service API | **Chosen** — matches established implementation (Task 1 §5) |

### Pagination

| Alternative | Summary | Not chosen because |
|-------------|---------|-------------------|
| Spring `Page<T>` on service interface | Direct pass-through from repository | Framework leakage on service boundary |
| Feature-specific wrappers | Per-endpoint result types (e.g. module-local list results) | Not reusable across modules |
| Controller builds `PageMeta` from multiple service calls | Separate `list()` and `count()` | Splits atomic query; inefficient |
| Out-parameter holders | Caller supplies mutable meta container | Non-idiomatic; poor test ergonomics |
| Defer all paginated endpoints | No list APIs until multiple modules exist | Blocks documented API requirements unnecessarily |
| **`PageResult<T>` in `common.api`** | `PageResult<T>(List<T> content, PageMeta meta)` | **Chosen** — see Decision §2 |

### Mapping

| Alternative | Summary | Not chosen because |
|-------------|---------|-------------------|
| Mapper required always | Per literal reading of `develop-standards.md` §6 | Over-engineering for current flat 1:1 mappings (Task 1 §9) |
| Inline only, never Mapper | No Mapper classes ever | Does not scale to nested graphs and engine integration |
| **Inline default; Mapper by complexity/reuse** | Mapper when objective criteria met | **Chosen** — see Decision §3 |

### SecurityContext

| Alternative | Summary | Not chosen because |
|-------------|---------|-------------------|
| Controllers read identity, pass `userId` to service | Explicit parameter on every write operation | Pollutes service API; duplicates security concern at HTTP layer |
| Shared provider introduced immediately | Central abstraction before duplication exists | Premature; only one service operation reads context today (Task 1 §11) |
| **Services read SecurityContext; shared abstraction when justified** | Private helper until centralization warranted | **Chosen** — see Decision §6 |

---

## Decisions

### 1. Service interface contract

**Decision**

| Rule | Policy |
|------|--------|
| JPA entity classes on service interfaces | **Forbidden** |
| Spring framework types on service interfaces | **Forbidden** — includes `Page`, `Pageable`, `Sort`, and other Spring Data / Spring Web types |
| Request/response DTOs on service interfaces | **Allowed and standard** |
| Domain types on service interfaces | **Allowed** — non-JPA types from `{module}.domain` |
| Shared API types on service interfaces | **Allowed** — `common.api` contracts designed for cross-layer use (e.g. `PageResult<T>`) |
| Primitives and collections | **Allowed** — as carriers of the above |

**Allowed on service public interfaces:**

```text
DTOs (request/response)
Domain types (non-JPA)
common.api contracts (PageResult<T>)
Primitives, collections, Optional of the above
```

**Forbidden on service public interfaces:**

```text
JPA Entity classes
Spring Data / Spring Web framework types
```

**Rationale:** Task 1 confirms service interfaces already use DTOs and do not expose entities or Spring types (§2.2, §12). This decision codifies that practice project-wide.

**Consequences:**

- Service interfaces remain stable and testable without Spring Data knowledge.
- Service implementations bear responsibility for mapping persistence results to permitted return types.
- Future project documentation should be aligned with this ADR regarding DTO usage on service interfaces.

**Task 1 reference:** §2.2, §6, §12 (strength 3), §15 (service public contract gap), §16 (questions 1–3).

---

### 2. Pagination strategy

**Decision:** Adopt `PageResult<T>` in `common.api` as the **sole** service-layer return type for paginated queries.

```java
public record PageResult<T>(List<T> content, PageMeta meta) {}
```

#### Layer contracts

| Layer | Contract | Notes |
|-------|----------|-------|
| **Repository** | `Page<Entity>` or equivalent persistence result | Spring Data `Page` / `Pageable` permitted in repository and service **implementation** only |
| **Service (public)** | `PageResult<Dto>` | Never `Page<Dto>` or `Page<Entity>` |
| **Controller** | `ApiResponse.ok(result.content(), result.meta())` | Assembles HTTP envelope; does not import Spring `Page` |
| **`PageMeta` ownership** | Built in **service implementation**; carried via `PageResult` | Controller reads `meta` from `PageResult` |

#### Service implementation flow

```text
1. Validate pagination parameters according to the API specification
2. Convert API page semantics to persistence page semantics — inside service impl
3. Execute repository query returning Page<Entity>
4. Map Entity → Dto
5. Build PageMeta from persistence page totals
6. return new PageResult<>(mappedContent, meta)
```

**Rationale:** Task 1 records that `PageMeta` and `ApiResponse.ok(data, meta)` exist but no paginated flow is implemented (§5, §14 item 10, §15). `PageResult<T>` composes existing HTTP types without introducing a second pagination model. Spring `Page` remains a persistence concern.

**Consequences:**

- All paginated list endpoints share one service → controller pattern.
- Controllers remain thin with no Spring Data imports.
- One shared type in `common.api` serves all modules.
- Closes Task 1 boundary question 2 and mitigates pagination-related risks (§17).

**Task 1 reference:** §5, §7, §12 (strength 10), §14 (item 10), §15, §16 (questions 2–3), §17 (blocked paginated features).

---

### 3. Mapping strategy

**Decision:** **Inline mapping is the default.** A dedicated `{Module}Mapper` is introduced when mapping complexity or reuse warrants centralization.

#### Inline mapping is appropriate when ALL are true

1. Flat, one-to-one field correspondence.
2. Mapping is local to a single service operation (not reused elsewhere).
3. No nested object graphs requiring sub-mapping.
4. No non-trivial transformation logic (conditional assembly, aggregation, business-rule-driven field derivation).

#### Dedicated Mapper is required when ANY is true

1. The same entity↔DTO conversion is **reused** across multiple service methods or modules.
2. Mapping involves **nested collections or object graphs**.
3. Mapping contains **non-trivial transformation logic** beyond direct field assignment and enum serialization.
4. Mapping is shared with or delegated to the **XML Engine** or other infrastructure-independent components.

#### Where mapping lives

| Location | Policy |
|----------|--------|
| Service implementation | Default — inline or delegate to Mapper |
| Controller | **Forbidden** |
| Repository | **Forbidden** |
| Engine | Receives domain/engine types only — **never DTOs** |

**Rationale:** Task 1 documents inline mapping as the only implemented approach; no Mapper classes exist (§9). Criteria are based on complexity and reuse, not field counts.

**Consequences:**

- Simple metadata mappings remain inline.
- Complex schema, compile, and multi-site mappings will use Mapper classes.
- Future project documentation should be aligned with this ADR regarding mapping strategy.

**Task 1 reference:** §4, §9, §14 (items 2, 5), §15, §16 (question 4).

---

### 4. DTO ownership

| Question | Policy |
|----------|--------|
| May service interfaces use DTOs? | **Yes** — DTOs are the **API boundary contract** shared by controller and service |
| Relationship to presentation layer | DTOs reside in `{module}.dto` and serve both controller and service interface layers |
| May response DTOs reference enums? | **Yes, conditionally** — see below |

#### Enum reference rule

Response DTOs may reference **stable business enums** when:

- The enum represents a fixed, API-documented value set.
- The enum is **not tightly coupled to persistence behavior** (no ORM lifecycle logic or persistence-specific semantics exposed to callers).

Response DTOs must **not** reference JPA entity **classes**.

**Rationale:** Task 1 shows service interfaces already use DTOs (§5) while standards describe DTOs as presentation-layer only (§14 item 3). Enum rule is expressed by business semantics, not package location, to remain valid if types are relocated.

**Consequences:**

- Service and controller share the same request/response types.
- Enum placement decisions are independent of DTO coupling rules.
- Future project documentation should be aligned with this ADR regarding DTO ownership.

**Task 1 reference:** §5, §14 (items 3, 5), §15, §16 (questions 5–6).

---

### 5. Entity vs domain package roles

**Decision:** Both `entity` and `domain` packages are canonical per module.

| Package | Responsibility | Contains |
|---------|----------------|----------|
| `{module}.entity` | **Persistence model** | JPA entity classes and types whose primary role is ORM mapping |
| `{module}.domain` | **Non-JPA domain model** | Business identity records, value types, and stable enums not tied to ORM |

#### Rules

1. JPA annotations appear **only** in `entity`.
2. `domain` types must not depend on `entity`.
3. `entity` may depend on `domain` types.
4. Services use entities internally; domain types may appear in service contracts where appropriate.
5. Engine must not depend on `entity` or DTOs.

**Rationale:** Task 1 records JPA models in `entity` and `AuthenticatedUser` in `domain` (§2.4, §6). `project-structure.md` lists `domain` but not `entity` (§14 item 4).

**Consequences:**

- Clear placement rules for new modules.
- Future project documentation should be aligned with this ADR to include both `entity` and `domain` in module structure.

**Task 1 reference:** §2.4, §6, §14 (item 4), §15, §16 (question 7).

---

### 6. SecurityContext usage

| Layer | May access `SecurityContext`? |
|-------|-------------------------------|
| Controller | **No** |
| Service | **Yes** — when the operation requires authenticated identity |
| Repository | **No** |
| Infrastructure (filters) | **Yes** — establishes context |

#### Current-user resolution

1. Services that require identity read `AuthenticatedUser` from `SecurityContext` (principal set by `JwtAuthenticationFilter`).
2. Resolution may be implemented as a **private service helper** while identity logic is localized.
3. A **shared current-user abstraction** should be introduced when duplicated identity-resolution logic becomes significant enough to justify centralization.

#### Required behavior

When a service operation requires authentication and the context is missing, anonymous, or the principal is not `AuthenticatedUser`:

```text
throw new UnauthorizedException(CommonErrorCode.UNAUTHORIZED)
```

Services must **not** parse JWT tokens directly.

**Rationale:** Task 1 records controllers and repositories do not access `SecurityContext`; one service reads it via unchecked cast (§11, §14 item 6). Identity belongs in the service layer for business operations that require it. Centralization is driven by duplication, not module count.

**Consequences:**

- Controllers and repositories remain free of security context.
- All services requiring identity follow the same error behavior.
- Shared abstraction timing is a maintainability judgment, not a fixed trigger.

**Task 1 reference:** §2.1, §2.2, §7, §11, §14 (item 6), §15, §16 (questions 8–10), §17 (security handling variance).

---

### 7. Transaction boundaries

| Rule | Policy |
|------|--------|
| `@Transactional` placement | **Service public methods only** |
| Read operations | `@Transactional(readOnly = true)` |
| Write operations | `@Transactional` (default read-write) |
| Repository | **No** `@Transactional` |
| Controller | **No** `@Transactional` |
| Engine | **No** `@Transactional` |

**Rationale:** Task 1 shows all implemented service methods are transactional; repositories and controllers are not (§8, §12 strength 4).

**Consequences:** Uniform transaction ownership across all future services.

**Task 1 reference:** §8, §12 (strength 4), §15.

---

### 8. Repository boundary

| Rule | Policy |
|------|--------|
| Responsibility | Persistence and query semantics **only** |
| Return types | `Entity`, `Optional<Entity>`, `Page<Entity>`, `List<Entity>`, primitives |
| DTO return | **Forbidden** |
| Business validation | **Forbidden** |
| SecurityContext access | **Forbidden** |
| Transaction management | **Forbidden** |

**Rationale:** Task 1 confirms repositories return entities only, with no DTOs, transactions, or security access (§7, §12 strength 2).

**Consequences:** Business rules and API types remain in the service layer.

**Task 1 reference:** §7, §12 (strength 2), §15, §16 (question 13 implied).

---

### 9. Exception ownership

| Category | When to use | Examples |
|----------|-------------|----------|
| **`CommonErrorCode`** | Cross-cutting, non-resource-specific | `UNAUTHORIZED`, `FORBIDDEN`, `VALIDATION_FAILED`, `INTERNAL_SERVER_ERROR` |
| **Module `{Module}ErrorCode`** | Resource-specific errors defined in API design | `TEMPLATE_NOT_FOUND`, `INVALID_CREDENTIALS` |
| **Application exceptions** | Shared hierarchy in `exception` package | `NotFoundException`, `ConflictException`, etc. |

#### Selection rules

1. API design names a **specific error code** for a module resource → **module enum**.
2. Error is **not tied to a specific resource** → **CommonErrorCode**.
3. `GlobalExceptionHandler` is the **sole** HTTP translator; controllers do not build error bodies.

**Rationale:** Task 1 documents module codes for resource-specific errors and common codes for security/infrastructure (§10, §14 item 8).

**Consequences:** Predictable error code ownership across modules.

**Task 1 reference:** §10, §14 (item 8), §15, §16 (question 11).

---

### 10. Controller responsibilities

Controllers are **thin HTTP adapters**.

#### Required

- Map HTTP inputs to service calls.
- Apply Bean Validation on request DTOs where applicable.
- Wrap results in `ApiResponse.ok(...)` (with `PageMeta` from `PageResult` for paginated lists).
- Constructor injection of services.

#### Forbidden

- Business logic.
- Repository access.
- `SecurityContext` access.
- Entity construction or persistence.
- Manual error response construction.
- `@Transactional`.

**Rationale:** Task 1 confirms implemented controllers follow this pattern (§2.1, §12 strength 1).

**Consequences:** Controllers remain simple HTTP boundaries across all modules.

**Task 1 reference:** §2.1, §4, §12 (strength 1), §15.

---

## Consequences (Summary)

| Area | Benefit | Trade-off |
|------|---------|-----------|
| Service contract | Clear allowed/forbidden types | Implementation mapping responsibility in service impl |
| `PageResult<T>` | Unified pagination across modules | One shared type to introduce in `common.api` |
| Mapping by complexity/reuse | Right-sized abstraction per feature | Requires judgment at implementation time |
| DTO on service API | Consistent with existing implementation | Documentation alignment needed |
| entity/domain split | Clear package roles | Documentation alignment needed |
| SecurityContext in service | Identity near business rules | Shared abstraction deferred until duplication warrants it |

---

## Migration Impact

This ADR does not require immediate code changes. It governs future work and provides the reference for aligning existing code when subsequently modified.

| Area | Relationship to current code |
|------|------------------------------|
| Service interfaces using DTOs | Aligned |
| Inline mapping for simple operations | Aligned |
| Service/repository layering | Aligned |
| `PageResult<T>` | Not yet in codebase; introduced when first paginated service is implemented |
| `SecurityContext` error behavior | Defines required behavior; existing private helpers should conform when next modified |
| Project documentation | Should be aligned with this ADR in a separate documentation task |

No module rewrites are required solely because of this ADR.

---

## Documentation Alignment

The following project documents should be reviewed and aligned with this ADR in a **separate documentation task**:

- `docs/10-development-standard/develop-standards.md` — DTO ownership, mapping strategy, transaction conventions
- `docs/12-project-structure/project-structure.md` — `entity` and `domain` package roles

This ADR records architectural decisions. Documentation updates are explicit follow-up work, not part of this ADR.

---

## References

- [Task 1 — Service Layer Architecture Audit](./task-1-service-layer-audit.md)
- `docs/10-development-standard/develop-standards.md`
- `docs/12-project-structure/project-structure.md`
- `docs/06-api-design/p1_overview.md` — response envelope
- `docs/06-api-design/p8_error-model.md` — pagination and error model

---

## Verification

| Check | Result |
|-------|--------|
| Production code modified | **No** |
| Documentation only | **Yes** |
| Implementation tasks included | **No** |
| Feature-specific decisions included | **No** |

---
## Persistence Layer Purity

### Status

Accepted

---

### Context

XMLGen follows a layered architecture where each layer has a clear responsibility.

As the project grows (Master Module, Template Metadata Engine, Mapping Engine, XML Generator), it becomes increasingly important to prevent business logic from leaking into the persistence layer.

Mixing persistence and business logic leads to:

* Fat entities
* Hidden side effects
* Difficult testing
* Tight coupling between persistence and domain logic

---

### Decision

Entities are persistence models only.

Their responsibility is limited to representing the database schema.

Entities must not contain:

* business rules
* validation logic
* XML generation logic
* mapping logic
* helper methods with business meaning
* cross-entity coordination

Business behavior belongs to the Service / Application layer.

---

### Responsibilities

#### Entity

Responsible for:

* database column mapping
* persistence annotations
* enum persistence
* audit fields
* foreign key identifiers

Not responsible for:

* validation
* default value calculation
* business decisions
* XML tree construction
* metadata compilation
* runtime mapping

---

#### Repository

Responsible for:

* persistence
* querying
* pagination
* filtering

Repositories must not contain business decisions.

---

#### Service

Responsible for:

* business rules
* validation orchestration
* transaction boundaries
* metadata compilation
* XML generation workflow
* Mapping Engine execution

---

### Design Principles

1. Migration defines the database contract.

2. Entity reflects the database contract.

3. Repository provides persistence access.

4. Service implements business behavior.

5. Controllers orchestrate HTTP only.

---

### Examples

Good

```text
TemplateFieldEntity

↓

stores

field_name

xml_name

node_type

value_type
```

Bad

```text
TemplateFieldEntity

↓

compileSchema()

generateXml()

resolveMasterData()

validateOccurrenceRule()
```

Those operations belong to dedicated services.

---

### Benefits

* Clear separation of concerns
* Easier unit testing
* Better maintainability
* Simpler future refactoring
* Consistent architecture across all modules
* Alignment with ADR-001 and ADR-002

---

### Project-wide Rule

Every new module must follow this principle.

This applies to:

* Master Module
* Template Metadata Engine
* Mapping Engine
* Input Engine
* XML Generator
* Import / Export Engine

No business logic should ever be implemented inside Entity classes.

