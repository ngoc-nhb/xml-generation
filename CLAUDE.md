# CLAUDE.md

## Project Overview

This repository implements the XML Generation System.

The software design has been completed and is considered the single source of truth.

Do not invent new business rules.

---

# Working Principles

* Read the relevant design documents before writing code.
* Follow the architecture defined in the documentation.
* Keep changes small and focused.
* Do not modify unrelated modules.
* Explain the implementation plan before generating code.
* Prefer production-ready code over quick prototypes.

---

# Architecture Rules

* Follow Layered Architecture.
* Organize code by feature.
* Keep Controllers thin.
* Business logic belongs only in Services.
* Repositories perform persistence only.
* The XML Engine must remain infrastructure-independent.
* DTOs must never be passed into the XML Engine.

---

# Coding Rules

* Java 21
* Spring Boot 3
* Gradle
* Spring Data JPA
* Flyway
* Bean Validation
* JUnit 5
* Mockito

Use constructor injection.

Avoid circular dependencies.

Do not introduce unnecessary libraries.

---

# Before Writing Code

Always:

1. Explain the implementation plan.
2. List the files to be created or modified.
3. Wait for approval before generating code.

---

# While Implementing

* Implement only the requested scope.
* Do not refactor unrelated code.
* Keep methods focused and readable.
* Follow the Development Standards.
* Follow the Module Specification.

---

# After Implementation

Always:

1. Verify architecture consistency.
2. Check for dependency violations.
3. Check for unnecessary code.
4. Ensure the project can compile.

---

# Documents

Use these documents as the source of truth.

Priority:

1. Module Specifications
2. Development Standards
3. Project Structure
4. API Design
5. Database Design
6. Remaining design documents

If documents conflict, stop and ask for clarification instead of making assumptions.

---

# Project Conventions

These are architectural decisions that guide future implementation. They are not bugs.

## Convention 1 — Optimize only with evidence

Do NOT optimize for performance unless there is measurable evidence of a bottleneck.

Example: Do NOT replace sequential JSONB search with complex indexed search during
MVP. Optimization belongs to a dedicated performance phase.

## Convention 2 — Do not redesign REST URLs without discussion

The following API style is intentional:

```text
GET /master-data/fields?typeId=
```

instead of `/types/{id}/fields`.

Reason: the roadmap requires browsing all fields before filtering by type. Do not
redesign REST URLs without architectural discussion.

## Convention 3 — Metadata may exist before being enforced

Fields such as `searchable` and `defaultValue` are part of the metadata model.
Their existence does NOT imply they must already be enforced. Do not remove them
simply because they are not yet used. See [ADR-002](./docs/adr/ADR-002-metadata-driven-architecture.md).

## Convention 4 — Do not prematurely normalize runtime data

JSONB is the intended storage for `MasterDataRecord`. Do not introduce
entity-per-type implementations.

## Convention 5 — Business rules must be metadata-driven

Never hardcode field names. Validation, Mapping, and XML Generation must always
read metadata. See [ADR-002](./docs/adr/ADR-002-metadata-driven-architecture.md).

## Convention 6 — Extend validation via new rules, not new conditionals

Do not introduce new service-layer conditionals when extending validation. New
validation rules are implemented by adding a new `ValidationRule` with its own
priority. `MasterDataValidationServiceImpl` remains closed for modification.

---

# MVP-First Principle

When reviewing or proposing improvements:

1. Prioritize correctness over optimization.
2. Do not recommend performance optimizations unless a measurable bottleneck exists.
3. Do not propose architectural refactoring solely for theoretical scalability.
4. Distinguish clearly between:
   - Bug
   - Architectural issue
   - Technical debt
   - Future enhancement
5. A feature that exists in the metadata model but is intentionally not implemented
   yet (e.g. `searchable`, `defaultValue`) must NOT be classified as dead code.
6. Recommendations should align with the current roadmap. Avoid proposing work
   scheduled for later phases unless it blocks the current milestone.

---

# Technical Debt

Accepted and intentionally deferred. These do not block milestone completion.
See [docs/technical-debt.md](./docs/technical-debt.md) for the tracked register.
