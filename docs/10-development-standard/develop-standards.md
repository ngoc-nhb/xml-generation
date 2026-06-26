# 10. Development Standards

## 1. Purpose

This document defines the common development standards for the XML Generation System.

These standards apply to all modules unless explicitly overridden by a module specification.

---

# 2. General Principles

* Follow Layered Architecture.
* Follow Single Responsibility Principle (SRP).
* Keep components loosely coupled.
* Prefer composition over inheritance.
* Avoid duplicated code.

---

# 3. Naming Convention

| Item      | Convention       | Example           |
| --------- | ---------------- | ----------------- |
| Package   | lowercase        | `service`         |
| Class     | PascalCase       | `TemplateService` |
| Interface | PascalCase       | `StorageProvider` |
| Method    | camelCase        | `generateXml()`   |
| Variable  | camelCase        | `templateId`      |
| Constant  | UPPER_SNAKE_CASE | `MAX_PAGE_SIZE`   |

---

# 4. Layer Responsibilities

| Layer      | Responsibility                            |
| ---------- | ----------------------------------------- |
| Controller | HTTP request and response handling        |
| Service    | Business logic and transaction management |
| Repository | Database access only                      |
| Mapper     | Object conversion only                    |
| Engine     | XML generation logic only                 |

---

# 5. Dependency Rules

Allowed:

* Controller → Service
* Service → Repository
* Service → Engine
* Repository → Database

Forbidden:

* Controller → Repository
* Repository → Service
* Engine → Repository
* Engine → DTO
* Repository → Controller

---

# 6. DTO Rules

* DTOs belong to the Presentation layer.
* DTOs shall not be passed into the Domain or Engine.
* Use Mapper to convert between DTOs and Domain Models.

---

# 7. Transaction Rules

* Transactions shall be managed only by the Service layer.
* Repository shall never start transactions.
* Engine shall never manage transactions.

---

# 8. Exception Handling

Use the appropriate exception type.

| Exception           | Usage                   |
| ------------------- | ----------------------- |
| ValidationException | Invalid input           |
| BusinessException   | Business rule violation |
| NotFoundException   | Resource not found      |
| ConflictException   | Version conflict        |

Unexpected exceptions shall be handled by the Global Exception Handler.

---

# 9. Logging Rules

| Level | Usage                      |
| ----- | -------------------------- |
| INFO  | Business milestones        |
| WARN  | Expected business failures |
| ERROR | Unexpected failures        |

Do not log:

* Passwords
* Tokens
* Secrets
* Personal sensitive information

---

# 10. Unit Test Rules

* Follow Arrange – Act – Assert (AAA).
* Mock external dependencies only.
* Every public Service method should have unit tests.
* Unit tests should be deterministic and independent.

---

# 11. Code Review Checklist

* Business logic is not implemented in Controllers.
* Repository is used only for data access.
* DTOs are not passed into the Engine.
* Transactions exist only in Services.
* Naming conventions are followed.
* Unit tests are provided.
* Logging follows the standard.

---

# 12. Phase 1 Decisions

| Topic           | Decision             |
| --------------- | -------------------- |
| Architecture    | Layered Architecture |
| Authentication  | Stateless            |
| XML Engine      | Pure Logic           |
| Streaming       | Required             |
| Auto Save       | Excluded             |
| Mobile Support  | Excluded             |
| Dark Mode       | Excluded             |
| Background Jobs | Excluded             |
| Event Bus       | Excluded             |
| Microservices   | Excluded             |
