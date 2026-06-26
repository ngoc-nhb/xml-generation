# Part 7. Supporting Components

---

# 73. Purpose

## Responsibility

Define reusable supporting components shared across the application.

Supporting Components provide reusable functionality without owning business workflows.

They improve separation of concerns and reduce duplicated implementation.

---

## Scope

This Part defines:

* Factory components
* Builder components
* Mapper components
* Utility components
* Shared abstractions

Supporting Components shall never implement business workflows.

---

# 74. Component Overview

The following supporting components are defined.

```text
RuntimeModelFactory

XMLWriterFactory

TemplateMapper

MasterDataMapper

SavedInputMapper

ExportHistoryMapper

PasswordEncoder

TokenProvider

StorageProvider

ClockProvider

IdGenerator
```

Each component owns one well-defined responsibility.

---

# 75. RuntimeModelFactory

## Responsibility

Construct a ResolvedRuntimeModel from runtime inputs supplied by the Application Service.

The RuntimeModelFactory operates exclusively on domain-level runtime data.

It shall never depend on Presentation Layer DTOs.

---

## Input

* CompiledTemplate
* User Input Map
* Selected Master Data Map
* Static Runtime Values

---

## Output

```text
ResolvedRuntimeModel
```

---

## Used By

```text
PreviewService

ExportService
```

---

## Notes

Application Services are responsible for extracting data from Request DTOs before invoking the RuntimeModelFactory.

RuntimeModelFactory shall never receive or reference:

* Request DTOs
* Response DTOs
* HTTP Requests
* Controller objects

Its responsibility is limited to assembling domain runtime objects required by the XML Generation Engine.

---

# 76. XMLWriterFactory

## Responsibility

Create XML Writer instances.

The implementation may choose any compliant streaming XML library.

---

## Public Operations

* createWriter()

---

## Used By

```text
XMLBuilder
```

---

## Notes

XMLBuilder shall never instantiate XML writers directly.

Writer creation shall always be delegated to XMLWriterFactory.

---

# 77. Mapper Components

## Responsibility

Convert between Presentation models and Domain models.

---

## Components

* TemplateMapper
* MasterDataMapper
* SavedInputMapper
* ExportHistoryMapper

---

## Mapping Rules

```text
Request DTO

↓

Domain Model
```

```text
Domain Model

↓

Response DTO
```

---

## Notes

Mapper components shall:

* be deterministic
* contain no business logic
* perform field mapping only

---

# 78. PasswordEncoder

## Responsibility

Provide password hashing and verification.

---

## Public Operations

* encode()
* matches()

---

## Used By

```text
AuthService
```

---

## Notes

PasswordEncoder abstracts the hashing implementation.

The Application Layer shall never depend on a specific hashing algorithm.

---

# 79. TokenProvider

## Responsibility

Generate and validate authentication tokens.

---

## Public Operations

* generateToken()
* validateToken()

---

## Used By

```text
AuthService
```

---

## Notes

TokenProvider abstracts token implementation.

The Application Layer shall remain independent of JWT or any specific token technology.

---

# 80. StorageProvider

## Responsibility

Provide access to physical XML file storage.

---

## Public Operations

* save()
* load()
* delete()
* exists()

---

## Used By

```text
ExportProcessor

ExportHistoryService
```

---

## Notes

StorageProvider hides implementation details.

Supported implementations may include:

* Local File System
* Amazon S3
* Google Cloud Storage

Application Services shall never depend on storage implementation details.

---

# 81. ClockProvider

## Responsibility

Provide the current system time.

---

## Public Operations

* now()

---

## Used By

```text
ExportService

ExportHistoryService
```

---

## Notes

ClockProvider enables deterministic unit testing.

Business components shall avoid calling system clocks directly.

---

# 82. IdGenerator

## Responsibility

Generate unique identifiers.

---

## Public Operations

* generate()

---

## Used By

```text
ExportService

MasterDataService

TemplateService
```

---

## Notes

The implementation may use:

* UUID
* Snowflake
* Database-generated identifiers

Business components shall remain independent of identifier generation strategy.

---

# 83. Supporting Component Rules

Supporting Components shall:

* own one responsibility
* be stateless whenever possible
* contain no business workflow
* be reusable
* be independently unit testable

Supporting Components shall never:

* invoke Controllers
* invoke Repositories directly
* coordinate business transactions
* execute XML generation workflows

---

# 84. Dependency Rules

Allowed dependencies:

```text
Application Service

↓

Supporting Component
```

```text
XML Engine

↓

XMLWriterFactory
```

```text
Controller

↓

Mapper
```

Forbidden dependencies:

```text
Mapper

↓

Repository
```

```text
StorageProvider

↓

Controller
```

```text
TokenProvider

↓

Repository
```

Supporting Components shall remain infrastructure-independent whenever possible.

---

# 85. Phase 1 Supporting Component Decisions

| Topic                | Decision   |
| -------------------- | ---------- |
| RuntimeModelFactory  | Required   |
| XMLWriterFactory     | Required   |
| Mapper Components    | Required   |
| PasswordEncoder      | Required   |
| TokenProvider        | Required   |
| StorageProvider      | Required   |
| ClockProvider        | Required   |
| IdGenerator          | Required   |
| Stateless Components | Preferred  |
| Dependency Injection | Required   |
| Business Logic       | Prohibited |
