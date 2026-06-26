# 02. Master Data Module

---

# 1. Purpose

Manage reusable Master Data used during XML generation.

The module is responsible for:

* Master Data Type management
* Master Data Record management
* Runtime data retrieval

---

# 2. Scope

Included

* Create Master Data Type
* Update Master Data Type
* Delete Master Data Type
* Create Master Data Record
* Update Master Data Record
* Delete Master Data Record
* Query Master Data

Excluded

* XML Generation
* Template Compilation
* Saved Input

---

# 3. Components

| Component                  | Responsibility                 |
| -------------------------- | ------------------------------ |
| MasterDataTypeController   | Expose Master Data Type APIs   |
| MasterDataRecordController | Expose Master Data Record APIs |
| MasterDataTypeService      | Manage Master Data Types       |
| MasterDataRecordService    | Manage Master Data Records     |
| MasterDataTypeRepository   | Persist Master Data Types      |
| MasterDataRecordRepository | Persist Master Data Records    |
| MasterDataMapper           | DTO conversion                 |

---

# 4. Responsibilities

## MasterDataTypeService

Responsible for:

* Create Type
* Update Type
* Delete Type
* Retrieve Type

---

## MasterDataRecordService

Responsible for:

* Create Record
* Update Record
* Delete Record
* Query Records
* Validate Record against Type schema

---

# 5. Dependencies

```text
MasterDataTypeController
            │
            ▼
MasterDataTypeService
            │
            ▼
MasterDataTypeRepository


MasterDataRecordController
            │
            ▼
MasterDataRecordService
            │
            ├────────► MasterDataRecordRepository
            │
            └────────► MasterDataTypeRepository
```

---

# 6. Public Interfaces

## MasterDataTypeService

* create()
* update()
* delete()
* findById()
* findAll()

---

## MasterDataRecordService

* create()
* update()
* delete()
* findById()
* findByType()
* validate()

---

# 7. Domain Objects

* MasterDataType
* MasterDataField
* MasterDataRecord

MasterDataType defines the schema.

MasterDataRecord stores business data.

---

# 8. Repositories

## MasterDataTypeRepository

Responsibilities

* CRUD Master Data Types

---

## MasterDataRecordRepository

Responsibilities

* CRUD Records
* Pagination
* Filtering by Type

Repository shall not validate business rules.

---

# 9. Exceptions

* ValidationException
* BusinessException
* ConflictException
* NotFoundException

---

# 10. Validation Rules

MasterDataTypeService validates:

* Duplicate Type Code
* Duplicate Field Names
* Schema consistency

MasterDataRecordService validates:

* Required fields
* Data types
* Field formats
* Field length
* Unknown fields

MasterDataTypeService validates:

- Type consistency
- Schema consistency
- Deletion constraints defined by the business rules

---

# 11. Implementation Notes

* Record validation shall always use the latest Master Data Type schema.
* Schema updates shall not automatically modify existing Records.
* Pagination shall be performed by the Repository.
* Services shall never load all Records into memory.
* MasterDataType shall not be deleted if doing so would violate referential integrity.
* The service shall enforce all deletion constraints before invoking the Repository.

---

# 12. Unit Test Strategy

Required unit tests:

MasterDataTypeService

* Create
* Update
* Delete
* Duplicate Type Code
* Invalid Schema

MasterDataRecordService

* Create
* Update
* Delete
* Validation
* Pagination
* Schema validation

---

# 13. Phase 1 Decisions

| Topic             | Decision       |
| ----------------- | -------------- |
| Record Validation | Dynamic Schema |
| Pagination        | Repository     |
| Schema Migration  | Manual         |
| Bulk Import       | Excluded       |
| Bulk Export       | Excluded       |
| Optimistic Lock | Required for MasterDataType only |


---

# 14. Implementation Checklist

* Create MasterDataTypeController
* Create MasterDataRecordController
* Create MasterDataTypeService
* Create MasterDataRecordService
* Create MasterDataTypeRepository
* Create MasterDataRecordRepository
* Create DTOs
* Create Mapper
* Implement CRUD operations
* Implement dynamic validation
* Implement pagination
* Write unit tests
* Verify integration
