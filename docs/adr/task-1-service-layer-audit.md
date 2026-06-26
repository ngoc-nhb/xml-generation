# Task 1 — Service Layer Architecture Audit

| Field | Value |
|-------|-------|
| **Type** | Architecture audit (as-implemented) |
| **Date** | 2026-06-26 |
| **Scope** | Implemented production code in Authentication and Template modules |
| **Production code modified** | None |

---

## 1. Summary

This document records the **current implementation** of the XML Generation System service-layer boundaries as observed in the codebase at the time of audit.

Two business modules contain implemented production code: **Authentication** and **Template**. Five additional modules exist as package skeletons only (`masterdata`, `savedinput`, `exporthistory`, `xmlgeneration`, `engine`).

The project follows a feature-based layered structure:

```text
Controller  →  Service  →  Repository  →  Database (JPA Entity)
```

Shared infrastructure includes `common.api`, `common.persistence`, `config`, `exception`, and `infrastructure.security`.

The audit identifies what exists, where it exists, observed gaps, and boundary questions. It does **not** propose solutions or architectural decisions.

---

## 2. Current Layered Architecture

### 2.1 Controller layer

**Location:** `{module}/controller`

**Implemented classes:**

| Class | Module | Base path |
|-------|--------|-----------|
| `AuthenticationController` | authentication | `/api/v1/auth` |
| `TemplateController` | template | `/api/v1/templates` |

**Implemented endpoints:**

| HTTP | Path | Handler |
|------|------|---------|
| `POST` | `/api/v1/auth/login` | `AuthenticationController.login` |
| `GET` | `/api/v1/templates/{id}` | `TemplateController.findById` |

**Observed patterns:**

- `@RestController` and `@RequestMapping` on each controller.
- Constructor injection of the corresponding service interface.
- Successful responses wrapped in `ApiResponse.ok(...)`.
- `AuthenticationController` applies `@Valid` on `LoginRequest`.
- No controller injects or calls a repository.
- No controller accesses `SecurityContext` or `SecurityContextHolder`.
- No controller contains `@Transactional`.

### 2.2 Service layer

**Location:** `{module}/service`

**Implemented interfaces and classes:**

| Interface | Implementation |
|-----------|----------------|
| `AuthenticationService` | `AuthenticationServiceImpl` |
| `TemplateService` | `TemplateServiceImpl` |
| `TokenProvider` | `JwtTokenProvider` (in `authentication.security`) |

**Implemented public service methods:**

| Service | Method | Return type |
|---------|--------|-------------|
| `AuthenticationService` | `login(LoginRequest)` | `LoginResponse` |
| `TemplateService` | `create(CreateTemplateRequest)` | `CreateTemplateResponse` |
| `TemplateService` | `findById(Long)` | `TemplateResponse` |

**Observed patterns:**

- `@Service` on implementations.
- `@Transactional` on all implemented public service methods.
- Business rules (credential check, duplicate code check, not-found handling) are in service implementations.
- Service public interfaces use request/response DTO types; they do not declare JPA entity types or Spring Data types.
- `TemplateServiceImpl` uses entities internally (`TemplateEntity`).
- `AuthenticationServiceImpl` uses `UserEntity` internally.
- Mapping from entity to response DTO is performed inside service implementations (inline).
- `TemplateServiceImpl.create` reads `SecurityContextHolder` via a private `getCurrentUser()` method.
- `TemplateServiceImpl.findById` does not access `SecurityContext`.
- `AuthenticationServiceImpl` does not access `SecurityContext`.

### 2.3 Repository layer

**Location:** `{module}/repository`

**Implemented interfaces:**

| Repository | Extends | Entity type |
|------------|---------|-------------|
| `UserRepository` | `JpaRepository<UserEntity, Long>` | `UserEntity` |
| `TemplateRepository` | `JpaRepository<TemplateEntity, Long>` | `TemplateEntity` |

**Custom query methods:**

| Repository | Method | Return type |
|------------|--------|-------------|
| `UserRepository` | `findByUsername(String)` | `Optional<UserEntity>` |
| `TemplateRepository` | `findByCode(String)` | `Optional<TemplateEntity>` |
| `TemplateRepository` | `existsByCode(String)` | `boolean` |

**Observed patterns:**

- Repositories return entities, `Optional` of entities, or primitives.
- No repository returns DTOs.
- No repository contains `@Transactional`.
- No repository accesses `SecurityContext`.
- No `Page` or `Pageable` usage in repository interfaces.

### 2.4 Entity layer

**Location:** `{module}/entity` (JPA models), `{module}/domain` (non-JPA types)

**Implemented JPA entities:**

| Class | Table | Extends |
|-------|-------|---------|
| `UserEntity` | `users` | `BaseEntity` |
| `TemplateEntity` | `templates` | `BaseEntity` |

**Other persistence-related types:**

| Class | Package | Role |
|-------|---------|------|
| `TemplateStatus` | `template.entity` | JPA enum column on `TemplateEntity` |
| `BaseEntity` | `common.persistence` | Mapped superclass (`id`, `createdAt`, `updatedAt`) |

**Implemented domain types (non-JPA):**

| Class | Package | Role |
|-------|---------|------|
| `AuthenticatedUser` | `authentication.domain` | Record; security principal |

---

## 3. Implemented Modules

### 3.1 Authentication

| Layer | Status | Artifacts |
|-------|--------|-----------|
| Controller | Implemented | `AuthenticationController` |
| Service | Implemented | `AuthenticationService`, `AuthenticationServiceImpl` |
| Repository | Implemented | `UserRepository` |
| Entity | Implemented | `UserEntity` |
| Domain | Implemented | `AuthenticatedUser` |
| DTO | Implemented | `LoginRequest`, `LoginResponse` |
| Exception codes | Implemented | `AuthenticationErrorCode` |
| Mapper | Not implemented | `mapper/package-info.java` only |
| Security | Implemented | `JwtTokenProvider`, `JwtProperties`, `TokenProvider` |

### 3.2 Template

| Layer | Status | Artifacts |
|-------|--------|-----------|
| Controller | Partial | `TemplateController` — detail endpoint only; no create endpoint |
| Service | Implemented | `TemplateService`, `TemplateServiceImpl` — `create` and `findById` |
| Repository | Implemented | `TemplateRepository` |
| Entity | Implemented | `TemplateEntity`, `TemplateStatus` |
| Domain | Not implemented | `domain/package-info.java` only |
| DTO | Partial | `CreateTemplateRequest`, `CreateTemplateResponse`, `TemplateResponse` implemented; `UpdateTemplateRequest` defined but unused |
| Exception codes | Implemented | `TemplateErrorCode` |
| Mapper | Not implemented | `mapper/package-info.java` only |

### 3.3 Skeleton modules (no production logic)

`masterdata`, `savedinput`, `exporthistory`, `xmlgeneration`, and `engine` contain package structure and `package-info.java` files only.

---

## 4. Responsibilities of Each Layer (As Implemented)

| Layer | Observed responsibilities |
|-------|---------------------------|
| **Controller** | HTTP mapping, request validation (`@Valid`), delegate to service, wrap success in `ApiResponse` |
| **Service** | Business rules, transaction boundaries, entity construction, persistence orchestration, inline DTO mapping, JWT generation (auth), `SecurityContext` read (template create only) |
| **Repository** | JPA persistence and Spring Data query methods |
| **Entity** | ORM mapping to database tables |
| **Infrastructure security** | JWT filter, security filter chain, REST 401/403 handlers |
| **Global exception** | `GlobalExceptionHandler` translates exceptions to HTTP responses |

---

## 5. DTO Usage

**Location convention:** `{module}/dto/request`, `{module}/dto/response`

**Implemented DTOs:**

| DTO | Module | Used by controller | Used by service interface |
|-----|--------|--------------------|---------------------------|
| `LoginRequest` | authentication | Yes | Yes |
| `LoginResponse` | authentication | Yes | Yes |
| `CreateTemplateRequest` | template | No | Yes |
| `CreateTemplateResponse` | template | No | Yes |
| `TemplateResponse` | template | Yes | Yes |
| `UpdateTemplateRequest` | template | No | No |

**Shared API types:**

| Type | Package | Usage in production code |
|------|---------|--------------------------|
| `ApiResponse<T>` | `common.api` | All implemented controllers |
| `PageMeta` | `common.api` | Defined; `ApiResponse.ok(data, meta)` exists; no live endpoint passes `PageMeta` |
| `ApiError` | `common.api` | Error envelope via `GlobalExceptionHandler` / `ErrorResponseWriter` |

**Observed patterns:**

- DTOs are Java `record` types.
- Request DTOs use Jakarta Bean Validation annotations where defined (`@NotBlank`, `@Size`).
- Service public interfaces accept request DTOs and return response DTOs.
- `TemplateResponse` references `TemplateStatus` from `template.entity`.

**Reference in project documentation:**

- `docs/10-development-standard/develop-standards.md` §6 states DTOs belong to the presentation layer and shall not be passed into Domain or Engine.

---

## 6. Entity Usage

**Observed patterns:**

- JPA entities live in `{module}.entity`.
- Entities are used inside service implementations and repositories.
- Service public interfaces do not expose entity types.
- `UserEntity` fields used in login: `username`, `passwordHash`, `isActive`, `isAdmin`, `id`. Field `deletedAt` exists; login flow contains a `TODO(authentication-phase-2)` comment and does not evaluate `deletedAt`.
- `TemplateEntity` fields set on create: `code`, `name`, `description`, `status` (defaults to `ACTIVE`), `createdById`. Field `compiledSchemaJson` is not set on create (remains null).
- `BaseEntity` provides `id`, `createdAt`, `updatedAt` with Spring Data JPA auditing annotations.
- `PersistenceConfig` defines `AuditorAware<Long>` returning `Optional.empty()`. `created_by` on templates is assigned manually in `TemplateServiceImpl.create` from `AuthenticatedUser.id()`.

---

## 7. Repository Responsibilities (As Implemented)

| Observation | Detail |
|-------------|--------|
| Persistence only | No business validation beyond query semantics |
| Return types | `UserEntity`, `TemplateEntity`, `Optional`, `boolean` |
| No DTOs | No repository method returns a DTO |
| No transactions | No `@Transactional` on repositories |
| No security | No `SecurityContext` usage |
| No pagination queries | No `Page` or `Pageable` methods defined |

---

## 8. Transaction Boundaries (As Implemented)

| Method | Class | Annotation |
|--------|-------|------------|
| `login` | `AuthenticationServiceImpl` | `@Transactional(readOnly = true)` |
| `create` | `TemplateServiceImpl` | `@Transactional` |
| `findById` | `TemplateServiceImpl` | `@Transactional(readOnly = true)` |

**Observed patterns:**

- `@Transactional` appears only on service public methods.
- Read operations use `readOnly = true`.
- Write operation (`create`) uses default read-write transaction.
- No `@Transactional` on controllers, repositories, or infrastructure components.

**Reference in project documentation:**

- `docs/10-development-standard/develop-standards.md` §7 states transactions shall be managed only by the Service layer.

---

## 9. Mapping Strategy (As Implemented)

| Observation | Detail |
|-------------|--------|
| Mapper classes | None implemented; all `mapper/` packages contain `package-info.java` only |
| Inline mapping | `AuthenticationServiceImpl` builds `LoginResponse` from `UserEntity` fields inline |
| Inline mapping | `TemplateServiceImpl.findById` constructs `TemplateResponse` from `TemplateEntity` getters inline |
| Inline mapping | `TemplateServiceImpl.create` constructs `CreateTemplateResponse` from `saved.getId()` only |
| Controller mapping | Controllers do not perform entity-to-DTO mapping |
| Repository mapping | Repositories do not perform DTO mapping |

**Reference in project documentation:**

- `docs/10-development-standard/develop-standards.md` §6 states to use Mapper to convert between DTOs and Domain Models.

---

## 10. Exception Handling (As Implemented)

**Shared exception infrastructure:**

| Component | Location | Role |
|-----------|----------|------|
| `ApplicationException` | `exception` | Base type with `ErrorCode` |
| `NotFoundException`, `ConflictException`, `UnauthorizedException`, etc. | `exception` | Typed exceptions |
| `CommonErrorCode` | `exception` | Cross-cutting codes |
| `GlobalExceptionHandler` | `exception` | `@RestControllerAdvice`; maps exceptions to HTTP status and `ApiResponse` |
| `ErrorResponseWriter` | `exception` | Builds and serializes error envelopes |
| `RestAuthenticationEntryPoint` | `infrastructure.security` | 401 via `ErrorResponseWriter` |
| `RestAccessDeniedHandler` | `infrastructure.security` | 403 via `ErrorResponseWriter` |

**Module error codes in use:**

| Enum | Values used in production code |
|------|-------------------------------|
| `AuthenticationErrorCode` | `INVALID_CREDENTIALS` |
| `TemplateErrorCode` | `TEMPLATE_CODE_ALREADY_EXISTS`, `TEMPLATE_NOT_FOUND` |

**Observed patterns:**

- Services throw `UnauthorizedException`, `NotFoundException`, `ConflictException` with module `ErrorCode` values.
- Controllers do not catch exceptions or build error responses.
- `CommonErrorCode` defines `NOT_FOUND`, `CONFLICT`, `UNAUTHORIZED`, and others; module-specific not-found and conflict codes are used for template operations; `CommonErrorCode.UNAUTHORIZED` is used by security entry point and `GlobalExceptionHandler` for Spring Security exceptions.

---

## 11. SecurityContext Usage (As Implemented)

| Component | Uses SecurityContext? | Detail |
|-----------|----------------------|--------|
| `JwtAuthenticationFilter` | Yes (writes) | Parses Bearer JWT; sets `AuthenticatedUser` as principal on `SecurityContextHolder` |
| `SecurityConfig` | No direct access | Configures stateless JWT; permits login and actuator; requires authentication elsewhere |
| `AuthenticationController` | No | |
| `TemplateController` | No | |
| `AuthenticationServiceImpl` | No | |
| `TemplateServiceImpl.create` | Yes (reads) | Private `getCurrentUser()` casts `authentication.getPrincipal()` to `AuthenticatedUser` |
| `TemplateServiceImpl.findById` | No | |
| Repositories | No | |
| `PersistenceConfig` (`AuditorAware`) | No | Returns `Optional.empty()` |

**Observed behavior in `getCurrentUser()`:**

- No null check on `Authentication`.
- No type check before cast to `AuthenticatedUser`.
- No `UnauthorizedException` thrown from this method in current code.

**JWT / RBAC:**

- `JwtTokenProvider` embeds `admin` claim in JWT.
- No `@PreAuthorize` or role checks in controllers or services.
- `SecurityConfig` requires authentication for protected routes; it does not enforce admin-only routes.

---

## 12. Spring Framework Dependencies by Layer

### Controller

| Dependency | Where |
|------------|-------|
| `@RestController`, `@RequestMapping`, `@PostMapping`, `@GetMapping` | Controllers |
| `@PathVariable`, `@RequestBody` | Controllers |
| `@Valid` | `AuthenticationController` |
| `ApiResponse` | Controllers (project type, not Spring) |

### Service interface

| Dependency | Where |
|------------|-------|
| Spring types | None observed on `AuthenticationService` or `TemplateService` interfaces |

### Service implementation

| Dependency | Where |
|------------|-------|
| `@Service` | Both service impls |
| `@Transactional` | Both service impls |
| `PasswordEncoder` | `AuthenticationServiceImpl` |
| `SecurityContextHolder`, `Authentication` | `TemplateServiceImpl` |
| `JwtTokenProvider` | Uses JJWT library; registered as `@Component` |

### Repository

| Dependency | Where |
|------------|-------|
| `JpaRepository` | Both repositories |
| Spring Data query derivation | Custom `findBy*` / `existsBy*` methods |

### Infrastructure / config

| Dependency | Where |
|------------|-------|
| `SecurityFilterChain`, `HttpSecurity` | `SecurityConfig` |
| `OncePerRequestFilter` | `JwtAuthenticationFilter` |
| `UsernamePasswordAuthenticationToken` | `JwtAuthenticationFilter` |
| `@EnableJpaAuditing`, `AuditorAware` | `PersistenceConfig` |
| `@RestControllerAdvice`, `@ExceptionHandler` | `GlobalExceptionHandler` |
| `@EnableConfigurationProperties` | `SecurityBeansConfig` |

### Not present in production code

- Spring Data `Page`, `Pageable`, `PageRequest`, `Sort`
- `@PreAuthorize` / method security
- `UserDetailsService` custom implementation (Spring Boot auto-configuration may log a generated password at startup)

---

## 13. Existing Strengths

1. Implemented controllers delegate to services and wrap responses in `ApiResponse`.
2. Implemented services own business rules; repositories are persistence-only in practice.
3. Service public interfaces do not expose JPA entity classes or Spring Data types.
4. Transaction annotations are confined to service public methods with read/write distinction on template operations.
5. Centralized error translation via `GlobalExceptionHandler` and `ErrorResponseWriter`.
6. Module-specific `ErrorCode` enums implement shared `ErrorCode` interface.
7. JWT authentication is isolated in infrastructure (`JwtAuthenticationFilter`) with `TokenProvider` abstraction.
8. Constructor injection is used in implemented controllers and services.
9. Feature-based module packaging is established with consistent sub-packages.
10. HTTP pagination envelope types (`ApiResponse`, `PageMeta`) exist before any paginated endpoint is implemented.

---

## 14. Existing Inconsistencies

| # | Area | Observation |
|---|------|-------------|
| 1 | Vertical slice completeness | `TemplateService.create` is implemented; no `POST /api/v1/templates` controller endpoint exists |
| 2 | Mapping documentation vs code | `develop-standards.md` §6 requires Mapper; no Mapper classes exist; inline mapping is used |
| 3 | DTO documentation vs code | `develop-standards.md` §6 assigns DTOs to presentation layer; service interfaces use DTOs |
| 4 | Package documentation vs code | `project-structure.md` lists `domain` under modules; JPA models are in `entity` package |
| 5 | DTO type references | `TemplateResponse` imports `TemplateStatus` from `entity` package |
| 6 | SecurityContext handling | `getCurrentUser()` performs unchecked cast; no explicit unauthorized handling in code |
| 7 | Auditing | `AuditorAware` stub is active; `created_by` is set manually in service |
| 8 | Error code usage | `CommonErrorCode` defines generic `NOT_FOUND` and `CONFLICT`; template flows use module-specific codes |
| 9 | Test layout documentation vs code | `project-structure.md` documents `test/unit/`; service tests are co-located under `{module}/service/` |
| 10 | Pagination | `PageMeta` and `ApiResponse.ok(data, meta)` exist; no service method or endpoint uses paginated results |

---

## 15. Missing Standards (Observed Gaps)

The following topics are not documented in project standards (`develop-standards.md`, `project-structure.md`) in a way that fully matches the implemented codebase:

| Topic | Gap |
|-------|-----|
| Service-layer pagination contract | HTTP types exist; no documented service return pattern; no paginated endpoint implemented |
| Service public contract (allowed types) | No explicit rule for DTOs, domain types, or forbidden Spring/entity types on interfaces |
| Mapping strategy | Standards mention Mapper only; inline mapping is the implemented practice |
| DTO ownership relative to services | Standards describe DTOs as presentation-layer only |
| `entity` vs `domain` package roles | Structure doc does not list `entity`; both packages appear in code with different contents |
| Enum types in response DTOs | No documented rule |
| SecurityContext access policy | No documented rule for which layers may read identity |
| Current-user resolution | No documented pattern; one private helper exists in `TemplateServiceImpl` |
| Module vs `CommonErrorCode` selection | No documented selection criteria |
| `@Transactional` read/write convention | Implemented consistently; not explicitly documented in standards |
| Repository return type rules | Implied by implementation; not explicitly documented |
| Controller boundary rules | Implied by implementation; not fully codified in standards |

---

## 16. Boundary Questions Discovered

Questions observed from the implementation and documentation comparison. These are **questions only**; no answers are provided in this audit.

1. What types may appear on service public interfaces?
2. What is the service-layer contract for paginated queries given `PageMeta` at the HTTP layer?
3. May Spring Data `Page` or `Pageable` cross the service public interface boundary?
4. When is inline mapping appropriate versus a dedicated Mapper class?
5. May service interfaces use request/response DTOs as their public contract?
6. May response DTOs reference enum types associated with persistence models?
7. What are the canonical roles of `entity` versus `domain` packages?
8. Which layers may access `SecurityContext`?
9. What behavior is required when authentication context is missing or invalid inside a service?
10. When should current-user resolution be shared across services?
11. When should `CommonErrorCode` be used versus module-specific `ErrorCode` values?
12. How should `AuditorAware`, `SecurityContext`, and manually assigned audit fields relate?
13. What is the expected unit test directory convention?

---

## 17. Risks Caused by Missing Standards

| Risk | Description |
|------|-------------|
| Blocked paginated features | No service-layer pagination contract is documented or implemented; list endpoints cannot proceed without an implicit per-feature decision |
| Inconsistent pagination implementations | Future modules may each define a different service-to-controller pagination pattern |
| Security handling variance | Unchecked `getCurrentUser()` may surface as `NullPointerException` or `ClassCastException` rather than a controlled unauthorized response |
| Duplicated identity logic | Each service requiring identity may reimplement `SecurityContext` access differently |
| Documentation drift | Contributors following `develop-standards.md` literally may introduce Mappers or avoid DTOs on service interfaces while existing code does the opposite |
| Package placement uncertainty | New types may be placed in `entity` or `domain` without a documented rule |
| DTO coupling spread | Response DTOs may increasingly reference persistence-associated types without a documented boundary |
| Audit field inconsistency | Manual `created_by` assignment alongside inactive `AuditorAware` may diverge across modules |
| Error code fragmentation | Similar failures may receive different error code treatment without documented selection rules |
| Repeated boundary debate | Each vertical slice may reopen the same architectural questions documented in §16 |

---

## 18. Implementation Inventory

| Category | Count / items |
|----------|----------------|
| Controllers | 2 |
| Service interfaces | 3 (`AuthenticationService`, `TemplateService`, `TokenProvider`) |
| Service implementations | 2 (`AuthenticationServiceImpl`, `TemplateServiceImpl`) + `JwtTokenProvider` |
| Repositories | 2 |
| JPA entities | 2 |
| Mapper classes | 0 |
| Unit test classes (service) | 2 (`AuthenticationServiceImplTest`, `TemplateServiceImplTest`) |
| Integration test classes | 1 (`XmlgenApplicationTests`) |
| Live paginated endpoints | 0 |
| Implemented HTTP endpoints | 2 |

---

## 19. Verification

| Check | Result |
|-------|--------|
| Production code modified | **No** |
| Documentation only | **Yes** |
| New architecture decisions introduced | **No** |
| Solutions or abstractions proposed | **No** |

---

## 20. Files Created

| File | Purpose |
|------|---------|
| `docs/adr/task-1-service-layer-audit.md` | This architecture audit document |
