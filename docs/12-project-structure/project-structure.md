# 12. Project Structure

## 1. Purpose

This document defines the physical project structure for the XML Generation System.

Its purpose is to establish a consistent package organization, dependency boundary, and directory layout for all developers.

All source code shall follow this structure unless explicitly approved otherwise.

---

# 2. Technology Stack

| Layer              | Technology            |
| ------------------ | --------------------- |
| Language           | Java 21               |
| Framework          | Spring Boot           |
| Build Tool         | Gradle                |
| Database           | PostgreSQL            |
| ORM                | Spring Data JPA       |
| Database Migration | Flyway                |
| Storage            | Local / S3 Compatible |
| Testing            | JUnit 5 + Mockito     |

---

# 3. Project Structure

```text
src
├── main
│   ├── java
│   │   └── com.company.xmlgen
│   │       ├── common
│   │       ├── config
│   │       ├── infrastructure
│   │       ├── engine
│   │       │
│   │       ├── authentication
│   │       ├── template
│   │       ├── masterdata
│   │       ├── xmlgeneration
│   │       ├── savedinput
│   │       └── exporthistory
│   │
│   └── resources
│       ├── application.yml
│       ├── application-dev.yml
│       ├── application-test.yml
│       ├── application-prod.yml
│       └── db
│           └── migration
│
└── test
    ├── java
    └── resources
```

---

# 4. Package Organization

Each business module shall own its related components.

Example:

```text
template
├── controller
├── service
├── repository
├── dto
├── mapper
├── domain
└── exception
```

Each module shall be self-contained and expose only its public interfaces.

---

# 5. Common Packages

| Package        | Responsibility                                |
| -------------- | --------------------------------------------- |
| common         | Shared utilities, constants and common models |
| config         | Framework configuration                       |
| infrastructure | Storage providers, external integrations      |
| engine         | XML Generation Engine                         |
| exception      | Global exception handling                     |

Business logic shall not be implemented inside these packages.

---

# 6. Dependency Rules

Allowed

```text
Controller
      │
      ▼
Service
      │
 ┌────┴──────────┐
 ▼               ▼
Repository    Engine
      │
      ▼
Infrastructure
```

Forbidden

* Controller → Repository
* Repository → Service
* Engine → Repository
* Engine → Controller
* Engine → DTO
* Repository → Controller

Business modules shall communicate only through Services.

---

# 7. Resource Structure

```text
resources
├── application.yml
├── application-dev.yml
├── application-test.yml
├── application-prod.yml
└── db
    └── migration
```

Application configuration shall be environment-specific.

Sensitive values shall never be committed to source control.

---

# 8. Test Structure

```text
test
├── unit
├── integration
├── fixtures
└── resources
```

Unit tests and integration tests shall be maintained separately.

Test fixtures shall be reusable across test suites.

---

# 9. Build Output

```text
build
├── libs
├── reports
├── test-results
└── generated
```

Generated XML files shall not be committed to source control.

Temporary files shall be cleaned automatically after execution.

---

# 10. Development Principles

* Organize code by feature.
* Keep business logic inside Services.
* Keep Controllers thin.
* Keep Repositories persistence-only.
* Keep the XML Engine independent from infrastructure.
* Prefer constructor injection.
* Avoid circular dependencies.
* Follow the Development Standards defined in **10-development-standards.md**.

---

# 11. Phase 1 Decisions

| Topic                | Decision             |
| -------------------- | -------------------- |
| Package Structure    | Feature-based        |
| Architecture         | Layered              |
| XML Engine           | Separate Package     |
| Database Migration   | Flyway               |
| Configuration        | YAML                 |
| Dependency Injection | Spring               |
| Storage              | Provider abstraction |
| Logging              | SLF4J                |
| Build Tool           | Gradle               |
| Background Jobs      | Excluded             |
| Microservices        | Excluded             |
