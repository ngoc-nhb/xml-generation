# Part 4. Application Service Layer

---

# 34. Purpose

## Responsibility

Define the Application Service Layer.

Application Services coordinate business workflows between:

* Controllers
* Domain Models
* Repositories
* XML Generation Engine

Application Services are responsible for executing use cases.

They shall not contain persistence implementation details.

---

## Scope

This Part defines:

* Service responsibilities
* Public operations
* Dependencies
* Transaction ownership
* Service interaction rules

---

# 35. Service Overview

The system defines the following Application Services.

```text
AuthService

TemplateService

CompileService

MasterDataService

SavedInputService

PreviewService

ExportService

ExportHistoryService
```

Each Service represents one business capability.

---

# 36. AuthService

## Responsibility

Authenticate users and manage login sessions.

---

## Public Operations

* login()
* logout()
* validateCredentials()

---

## Dependencies

```text
UserRepository

PasswordEncoder

TokenProvider
```

---

## Used By

```text
AuthController
```

---

## Notes

AuthService performs authentication only.

Authorization is handled by the security layer.

---

# 37. TemplateService

## Responsibility

Manage Template lifecycle.

---

## Public Operations

* createTemplate()
* updateTemplate()
* updateSchema()
* deleteTemplate()
* getTemplate()
* searchTemplates()

---

## Dependencies

```text
TemplateRepository

TemplateMapper
```

---

## Used By

```text
TemplateController
```

---

## Notes

TemplateService stores editable Template data only.

Compilation is delegated to CompileService.

---

# 38. CompileService

## Responsibility

Compile editable Template definitions into runtime schemas.

---

## Public Operations

* compileTemplate()

---

## Dependencies

```text
TemplateRepository

TemplateCompiler
```

---

## Used By

```text
TemplateController
```

---

## Transaction Boundary

```text
Load Template

↓

Compile

↓

Persist Compiled Schema

↓

Commit
```

---

## Notes

CompileService is the only component allowed to invoke TemplateCompiler.

---

# 39. MasterDataService

## Responsibility

Manage Master Data Types and Records.

---

## Public Operations

* createType()
* updateType()
* deleteType()
* createRecord()
* updateRecord()
* deleteRecord()
* searchRecords()

---

## Dependencies

```text
MasterDataTypeRepository

MasterDataRecordRepository
```

---

## Used By

```text
MasterDataController
```

---

## Notes

MasterDataService validates business rules before persistence.

Repositories perform persistence only.

---

# 40. SavedInputService

## Responsibility

Manage Draft data for XML Generation.

---

## Public Operations

* saveDraft()
* loadDraft()
* deleteDraft()

---

## Dependencies

```text
SavedInputRepository
```

---

## Used By

```text
SavedInputController

PreviewService
```

---

## Notes

Only one Draft exists for each User + Template combination.

Business validation is intentionally skipped during saveDraft().

---

# 41. PreviewService

## Responsibility

Generate an XML Preview using only the data supplied in the current request.

PreviewService shall not depend on any previously saved Draft data.

---

## Public Operations

* generatePreview()

---

## Dependencies

```text
TemplateRepository

MasterDataRecordRepository

XMLGenerationEngine
```

---

## Used By

```text
XMLController
```

---

## Standard Workflow

```text
Load Template

↓

Load Required Master Data

↓

Build Runtime Model
(using request payload only)

↓

Invoke XML Engine

↓

Return XML String
```

---

## Notes

PreviewService shall never:

* Load Saved Drafts automatically.
* Persist Draft data.
* Create ExportHistory.
* Persist XML files.
* Modify business data.

The Application Service shall construct the Runtime Model exclusively from:

* Request Payload
* Template
* Required Master Data

Draft management belongs exclusively to SavedInputService.

If a user wishes to preview previously saved data, the Frontend shall first retrieve the Draft through SavedInput APIs and include that data in the Preview request payload.

This design preserves a fully stateless Preview API and guarantees deterministic execution.

# 42. ExportService

## Responsibility

Generate and persist XML export results.

---

## Public Operations

* exportXML()

---

## Dependencies

```text
TemplateRepository

MasterDataRecordRepository

ExportHistoryRepository

XMLGenerationEngine

ExportProcessor
```

---

## Used By

```text
XMLController
```

---

## Standard Workflow

```text
Load Template

↓

Load Master Data

↓

Build Runtime Model

↓

Invoke XML Engine

↓

Persist XML File

↓

Create Export History

↓

Return Export Metadata
```

---

## Notes

ExportService owns the complete Export transaction.

XML Engine shall remain persistence-independent.

---

# 43. ExportHistoryService

## Responsibility

Provide read-only access to Export History.

---

## Public Operations

* getHistory()
* getHistoryDetail()
* downloadFile()

---

## Dependencies

```text
ExportHistoryRepository

StorageProvider
```

---

## Used By

```text
ExportHistoryController
```

---

## Notes

ExportHistory records are immutable.

Expired files shall be handled gracefully.

---

# 44. Service Interaction Rules

Application Services may collaborate only through clearly defined responsibilities.

Allowed interactions include:

```text
TemplateService

↓

CompileService
```

```text
PreviewService

↓

SavedInputService
```

Direct circular dependencies between Services are prohibited.

---

## Forbidden

```text
PreviewService

↓

ExportService
```

```text
ExportService

↓

PreviewService
```

```text
TemplateService

↓

ExportHistoryService
```

Business workflows shall remain cohesive and avoid cyclic dependencies.

---

# 45. Transaction Rules

Application Services own transaction boundaries.

Repositories shall participate in transactions initiated by Services.

Long-running operations (such as XML Generation) shall occur within clearly defined transaction scopes.

---

## Transaction Owner

```text
Application Service

↓

Repository

↓

Commit / Rollback
```

---

# 46. Phase 1 Service Decisions

| Topic                 | Decision                            |
| --------------------- | ----------------------------------- |
| Business Logic        | Service Layer                       |
| Transaction Owner     | Application Service                 |
| XML Engine Invocation | PreviewService / ExportService only |
| Compile Entry Point   | CompileService only                 |
| Repository Access     | Service only                        |
| Circular Dependencies | Prohibited                          |
| Dependency Injection  | Required                            |
| Stateless Services    | Required                            |
| Unit Test Support     | Required                            |
