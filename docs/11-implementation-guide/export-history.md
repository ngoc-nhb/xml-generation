# 05. Export History Module

---

# 1. Purpose

Manage Export History records generated after successful XML Export.

This module is responsible for:

* Retrieve Export History
* Retrieve Export Details
* Download exported XML
* Export file lifecycle

---

# 2. Scope

Included

* Export History
* Download exported XML
* Export metadata

Excluded

* XML Generation
* File generation
* Template management

---

# 3. Components

| Component               | Responsibility                      |
| ----------------------- | ----------------------------------- |
| ExportHistoryController | Expose Export History APIs          |
| ExportHistoryService    | Manage Export History               |
| ExportHistoryRepository | Persist and retrieve Export History |
| StorageProvider         | Retrieve exported XML files         |
| ExportHistoryMapper     | DTO conversion                      |

---

# 4. Responsibilities

## ExportHistoryService

Responsible for:

* Retrieve Export History
* Retrieve Export Details
* Download exported XML
* Verify file availability

The service owns all business rules related to Export History.

---

# 5. Dependencies

```text
ExportHistoryController
            │
            ▼
ExportHistoryService
      ├────────────► ExportHistoryRepository
      └────────────► StorageProvider
```

---

# 6. Public Interfaces

## ExportHistoryService

- findAll()
- findById()
- download()

The download operation shall return a streamable result and shall never require the entire XML file to be loaded into memory.

---

# 7. Domain Objects

* ExportHistory

An ExportHistory record represents one completed Export operation.

The record is immutable once created.

---

# 8. Repository Responsibilities

## ExportHistoryRepository

Responsible for:

* Retrieve Export History
* Retrieve Export Details
* Update Export Status

+ Repositories are responsible only for persistence.
+ Update Export Status is intended for internal use by the XML Generation module during the Export workflow.
+ ExportHistoryService shall never update Export History records after the Export process has completed.

---

# 9. Exceptions

* NotFoundException
* BusinessException
* StorageException

---

# 10. Validation Rules

ExportHistoryService validates:

* User ownership
* Export History existence
* File availability

Repositories shall not perform business validation.

---

# 11. Implementation Notes

* Export History records shall be immutable.
* The service shall enforce ownership before returning any Export History.
* Download requests shall retrieve XML files through the StorageProvider.
* Expired files shall be handled according to the retention policy.
* Missing files shall not invalidate the corresponding Export History record.
* The authenticated user shall always be resolved from the Security Context.
* Request DTOs shall never contain or override the authenticated User identifier.

---

# 12. Unit Test Strategy

Minimum coverage:

ExportHistoryService

* Retrieve Export History
* Retrieve Export Details
* Download exported XML
* Expired file handling
* Ownership validation

StorageProvider

* Retrieve file
* File not found
* Expired file

---

# 13. Implementation Checklist

* Create ExportHistoryController
* Create ExportHistoryService
* Create ExportHistoryRepository
* Create DTOs
* Create Mapper
* Integrate StorageProvider
* Implement findAll()
* Implement findById()
* Implement download()
* Write unit tests

---

# 14. Phase 1 Decisions

| Topic           | Decision             |
| --------------- | -------------------- |
| Export History  | Immutable            |
| Download        | Streaming            |
| File Storage    | Provider abstraction |
| File Expiration | Retention Policy     |
| Update API      | Excluded             |
| Delete API      | Excluded             |
