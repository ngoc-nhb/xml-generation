# Part 8. Class Relationships and UML Rules

---

# 86. Purpose

## Responsibility

Define the relationships between all major classes in the system.

This Part serves as the blueprint for generating UML Class Diagrams.

It consolidates the architectural rules defined throughout this document.

---

## Scope

This Part defines:

* Class relationships
* Dependency rules
* Ownership
* Aggregation
* Composition
* Layer interaction rules

Implementation details are intentionally excluded.

---

# 87. Layer Dependency Matrix

Application dependencies shall follow the hierarchy below.

```text
Presentation Layer
        │
        ▼
Application Layer
        │
        ▼
Domain Layer
        │
        ▼
Infrastructure Layer
```

Reverse dependencies are prohibited.

---

## Allowed Dependencies

| From       | To                    |
| ---------- | --------------------- |
| Controller | Service               |
| Controller | Mapper                |
| Service    | Repository            |
| Service    | Supporting Components |
| Service    | XML Engine            |
| XML Engine | Supporting Components |
| Repository | Database              |
| Mapper     | Domain Model          |

---

## Forbidden Dependencies

| From                  | To            |
| --------------------- | ------------- |
| Controller            | Repository    |
| Controller            | XML Engine    |
| Controller            | Domain Entity |
| XML Engine            | Repository    |
| XML Engine            | DTO           |
| Domain Model          | Repository    |
| Repository            | Controller    |
| Repository            | XML Engine    |
| Mapper                | Repository    |
| Supporting Components | Controller    |

---

# 88. Major Class Relationships

```text
Controller
        │
        ▼
Service
        │
        ├──────────────► Repository
        │
        ├──────────────► Supporting Components
        │
        └──────────────► XML Engine
                                │
                                ▼
                     RuntimeModelFactory
                                │
                                ▼
                    ResolvedRuntimeModel
                                │
                                ▼
                         ValueResolver
                                │
                                ▼
                       ValidationEngine
                                │
                                ▼
                          XMLBuilder
                                │
                                ▼
                        ExportProcessor
```

All dependencies are unidirectional.

Circular references are prohibited.

---

# 89. Ownership Rules

## Aggregate Ownership

| Aggregate Root | Owns             |
| -------------- | ---------------- |
| Template       | TemplateField    |
| MasterDataType | MasterDataRecord |
| User           | SavedInput       |
| User           | ExportHistory    |

Child objects shall never exist without their owning Aggregate Root.

---

## Lifecycle Ownership

Application Services own workflow execution.

Repositories own persistence.

XML Engine owns XML generation.

Supporting Components own reusable technical functionality.

---

# 90. Component Communication Rules

Communication between components shall always follow these principles.

### Controller

* invokes Services
* invokes Mappers

### Service

* invokes Repositories
* invokes Supporting Components
* invokes XML Engine

### Repository

* accesses persistent storage only

### XML Engine

* processes runtime models only

### Supporting Components

* provide reusable technical capabilities

No component shall invoke a higher architectural layer.

---

# 91. Object Creation Rules

Object creation responsibilities are standardized.

| Object               | Created By          |
| -------------------- | ------------------- |
| Domain Entity        | Service             |
| Request DTO          | Framework           |
| Response DTO         | Mapper              |
| CompiledTemplate     | TemplateCompiler    |
| ResolvedRuntimeModel | RuntimeModelFactory |
| ValidationResult     | ValidationEngine    |
| XML Writer           | XMLWriterFactory    |

Business components shall avoid direct object construction whenever a Factory exists.

---

# 92. Dependency Injection Rules

The following components shall be injected.

* Services
* Repositories
* Supporting Components
* XML Engine
* Factories
* Providers

Components shall depend on abstractions whenever practical.

Direct instantiation using `new` should be avoided except for immutable value objects or simple internal helper objects.

---

# 93. UML Modeling Rules

The UML Class Diagram shall represent:

* Composition
* Aggregation
* Association
* Dependency

The following implementation details shall be omitted:

* SQL
* HTTP
* JSON
* Framework annotations
* ORM configuration

The Class Diagram focuses on software architecture rather than implementation.

---

# 94. Architectural Constraints

The following architectural constraints are mandatory.

### Layer Isolation

Each layer shall depend only on lower layers.

---

### Stateless Components

Services and XML Engine components shall not retain request-specific state after execution.

---

### Pure XML Engine

The XML Generation Engine shall never access:

* Controllers
* DTOs
* Repositories
* Database
* Storage

---

### DTO Isolation

DTOs shall exist only within the Presentation Layer.

DTOs shall never cross into the Domain Layer or XML Engine.

---

### Runtime Model Isolation

The RuntimeModelFactory shall receive only domain runtime data prepared by the Application Service.

Request DTOs shall never be passed directly into the XML Engine.

---

### Transaction Ownership

Application Services own transaction boundaries.

Repositories participate in, but never create, transactions.

---

# 95. Phase 1 UML Decisions

| Topic                | Decision             |
| -------------------- | -------------------- |
| UML Style            | Layered Architecture |
| Dependency Direction | One-way only         |
| Circular Dependency  | Prohibited           |
| Aggregate Ownership  | Required             |
| Composition Modeling | Required             |
| DTO Isolation        | Required             |
| Repository Pattern   | Required             |
| Factory Pattern      | Required             |
| Builder Pattern      | Required             |
| Dependency Injection | Required             |
| XML Engine Isolation | Required             |
| Stateless Components | Required             |

---

# 96. Implementation Readiness

The architecture defined across Parts 1–8 provides a complete implementation blueprint.

The following artifacts are now fully specified:

* Layered Architecture
* Domain Model
* Repository Layer
* Application Service Layer
* XML Generation Engine
* Controller & DTO Layer
* Supporting Components
* UML Relationships

This document is intended to be the primary reference for implementation and UML Class Diagram generation.
