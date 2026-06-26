# 03. XML Generation Module

---

# 1. Purpose

Generate XML documents from compiled Templates and user input.

This module is responsible for:

* XML Preview
* XML Export
* Runtime Model construction
* XML generation workflow

---

# 2. Scope

Included

* Preview XML
* Export XML
* Runtime Model preparation
* Engine invocation
* XML file generation

Excluded

* Template management
* Master Data management
* Saved Input management

---

# 3. Components

| Component               | Responsibility                 |
| ----------------------- | ------------------------------ |
| XMLController           | Expose Preview and Export APIs |
| PreviewService          | Execute Preview workflow       |
| ExportService           | Execute Export workflow        |
| RuntimeModelFactory     | Build Runtime Model            |
| XMLEngine               | Generate XML                   |
| StorageProvider         | Persist exported XML           |
| ExportHistoryRepository | Persist export history         |

---

# 4. Responsibilities

## PreviewService

Responsible for:

* Load required runtime data
* Build Runtime Model
* Invoke XML Engine
* Return generated XML

Preview shall never persist any data.

---

## ExportService

Responsible for:

* Load required runtime data
* Build Runtime Model
* Invoke XML Engine
* Store XML file
* Create Export History

Export owns the complete export workflow.

---

## RuntimeModelFactory

Responsible for:

* Build Runtime Model
* Resolve required runtime resources
* Prepare Engine input

The factory shall not access HTTP objects.

---

# 5. Dependencies

```text
                XMLController
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
 PreviewService          ExportService
          │                     │
          └──────────┬──────────┘
                     ▼
          RuntimeModelFactory
                     │
                     ▼
                XMLEngine
                     │
             XML Stream Result
                     │
                     ▼
              ExportService
             ├────────────► StorageProvider
             └────────────► ExportHistoryRepository
```

---

# 6. Public Interfaces

## PreviewService

preview()

↓

returns XML Stream
---

## ExportService

export()

↓

returns Export Result

---

## RuntimeModelFactory

* build()

---

## XMLEngine

* generate()

---

# 7. Runtime Model

The Runtime Model shall contain:

* Compiled Template
* User Input
* Resolved Master Data
* Runtime Context

The XML Engine shall operate only on the Runtime Model.
The Runtime Model shall be immutable once constructed.

---

# 8. Supporting Components

## StorageProvider

Responsible for:

* Store XML files
* Retrieve XML files
* Delete expired files

---

## ExportHistoryRepository

Responsible for:

* Create Export History
* Update Export Status
* Query Export History

---

# 9. Exceptions

* ValidationException
* BusinessException
* NotFoundException
* StorageException

---

# 10. Validation Rules

PreviewService and ExportService shall verify:

* Template availability
* Compiled schema availability
* Runtime Model completeness

The XML Engine is responsible for XML validation.

---

# 11. Implementation Notes

* Services shall load all required runtime data before invoking the XML Engine.
* The XML Engine shall never access repositories directly.
* Preview shall not persist any business data.
* Export shall persist only the generated XML file and Export History.
* XML generation shall operate solely on the current Runtime Model.
* The XML Engine shall never access repositories, storage providers, or HTTP components directly.

---

# 12. Unit Test Strategy

Minimum coverage:

PreviewService

* Preview success
* Validation failure
* Engine failure

ExportService

* Export success
* Storage failure
* Export History creation
* Export History update on failure

RuntimeModelFactory

* Runtime Model construction
* Runtime data resolution

XMLEngine

* XML generation
* Validation failure
* Streaming generation

---

# 13. Implementation Checklist

* Create XMLController
* Create PreviewService
* Create ExportService
* Create RuntimeModelFactory
* Integrate XMLEngine
* Integrate StorageProvider
* Implement Export History persistence
* Implement Preview workflow
* Implement Export workflow
* Write unit tests

---

# 14. Phase 1 Decisions

| Topic                 | Decision             |
| --------------------- | -------------------- |
| XML Generation        | Streaming            |
| Runtime Model         | Required             |
| XML Engine            | Pure Logic           |
| Preview               | No persistence       |
| Export                | Synchronous          |
| Storage               | Provider abstraction |
| Background Processing | Excluded             |
| Auto Retry            | Excluded             |
