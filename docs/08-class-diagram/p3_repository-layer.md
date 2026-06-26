# Part 3. Repository Layer

---

# 21. Purpose

## Responsibility

Define the Repository Layer responsible for persistence operations.

Repositories provide an abstraction between the Application Layer and the underlying database.

Repositories are responsible only for data access.

Business logic shall never be implemented inside a Repository.

---

## Scope

This Part defines:

* Repository interfaces
* Repository responsibilities
* Query responsibilities
* Persistence boundaries

Implementation details (SQL, ORM, NoSQL, etc.) are outside the scope of this document.

---

# 22. Repository Overview

The system defines the following repositories.

```text
UserRepository

TemplateRepository

MasterDataTypeRepository

MasterDataRecordRepository

SavedInputRepository

ExportHistoryRepository
```

Repositories expose persistence operations only.

Repositories shall not coordinate business workflows.

---

# 23. Repository Responsibilities

| Repository                 | Responsibility                                 |
| -------------------------- | ---------------------------------------------- |
| UserRepository             | Persist User entities                          |
| TemplateRepository         | Persist Template aggregate                     |
| MasterDataTypeRepository   | Persist MasterDataType aggregate               |
| MasterDataRecordRepository | Query and persist MasterDataRecord efficiently |
| SavedInputRepository       | Persist SavedInput                             |
| ExportHistoryRepository    | Persist ExportHistory                          |

---

# 24. UserRepository

## Responsibility

Persist User entities.

---

## Primary Operations

* Find by ID
* Find by Username
* Exists
* Save
* Update

---

## Used By

```text
AuthService
```

---

## Notes

Authentication rules are implemented by AuthService.

UserRepository only retrieves and persists data.

---

# 25. TemplateRepository

## Responsibility

Persist Template aggregates.

TemplateRepository manages Template together with its TemplateFields.

---

## Primary Operations

* Find by ID
* Find by Template Code
* Search
* Save
* Update
* Delete
* Exists

---

## Aggregate Scope

```text
Template

↓

TemplateFields
```

---

## Used By

```text
TemplateService

CompileService
```

---

## Notes

TemplateFields shall always be persisted through TemplateRepository.

Separate TemplateFieldRepository is intentionally not provided.

---

# 26. MasterDataTypeRepository

## Responsibility

Persist MasterDataType aggregates.

---

## Primary Operations

* Find by ID
* Search
* Save
* Update
* Delete

---

## Aggregate Scope

```text
MasterDataType
```

---

## Used By

```text
MasterDataService
```

---

## Notes

Schema changes are coordinated by MasterDataService.

Repository performs persistence only.

---

# 27. MasterDataRecordRepository

## Responsibility

Provide efficient persistence and querying of MasterDataRecords.

This repository exists for performance optimization.

---

## Primary Operations

* Find by ID
* Search
* Pagination
* Insert
* Update
* Delete
* Bulk Insert
* Bulk Update

---

## Used By

```text
MasterDataService

PreviewService

ExportService
```

---

## Notes

MasterDataRecordRepository shall only be accessed from the Application Service layer.

Before invoking the XML Generation Engine, the Application Service shall preload all required Master Data Records and construct the Runtime Model.

The XML Generation Engine, including the ValueResolver, shall never access repositories or databases directly.

This separation ensures that the Engine remains:

* Stateless
* Database-independent
* Reusable
* Fully unit testable


---

# 28. SavedInputRepository

## Responsibility

Persist user Drafts.

---

## Primary Operations

* Find by User + Template
* Upsert
* Delete

---

## Used By

```text
SavedInputService
```

---

## Notes

Only one SavedInput may exist for a User and Template combination.

Repository enforces persistence constraints only.

---

# 29. ExportHistoryRepository

## Responsibility

Persist ExportHistory records.

---

## Primary Operations

* Insert
* Find by ID
* Search
* Pagination
* Update Export Status

---

## Used By

```text
ExportService

DownloadService
```

---

## Notes

ExportHistory records are immutable after successful export.

Only system-managed status fields may be updated during export execution.

---

# 30. Query Rules

Repositories may expose optimized query operations.

Examples include:

* Pagination
* Filtering
* Sorting
* Search
* Existence checks

Repositories shall not implement:

* Business validation
* XML Generation
* Workflow orchestration

---

# 31. Transaction Boundary

Repositories do not manage business transactions.

Transaction boundaries belong to the Application Service layer.

Example:

```text
TemplateService

↓

Begin Transaction

↓

TemplateRepository.Save()

↓

CompileService.Compile()

↓

Commit Transaction
```

Repositories shall participate in transactions initiated by Services.

---

# 32. Repository Design Principles

## Persistence Ignorance

Repositories hide persistence implementation details from higher layers.

---

## No Business Logic

Repositories shall never contain:

* Validation rules
* XML Generation
* Workflow decisions
* Authorization logic

---

## Aggregate Consistency

Aggregate Repositories persist complete Aggregate Roots.

Child entities shall not be modified independently unless explicitly allowed for performance optimization.

---

## Performance Optimization

Large collections may expose dedicated repositories.

Such repositories shall exist only to improve:

* Query performance
* Pagination
* Bulk operations

They shall not bypass Aggregate business rules.

---

## Testability

All repositories should be defined as interfaces.

Concrete implementations shall be injected using Dependency Injection.

Application Services shall depend on repository abstractions rather than implementation classes.

---

# 33. Phase 1 Repository Decisions

| Topic                        | Decision                       |
| ---------------------------- | ------------------------------ |
| Repository Pattern           | Adopted                        |
| ORM                          | Implementation-defined         |
| Aggregate Repository         | Yes                            |
| Dedicated Record Repository  | Allowed for large collections  |
| Pagination                   | Supported                      |
| Bulk Operations              | Supported for MasterDataRecord |
| Transaction Owner            | Service Layer                  |
| Business Logic in Repository | Prohibited                     |
| Interface-based Design       | Recommended                    |
| Dependency Injection         | Required                       |

```
```
