# 01. Template Module

---

# 1. Purpose

Manage XML Template definitions.

The module is responsible for:

* Template metadata
* Template schema
* Template compilation
* Template lifecycle

---

# 2. Scope

Included

* Create Template
* Update Metadata
* Update Schema
* Compile Template
* Delete Template
* Retrieve Template

Excluded

* XML Generation
* Master Data
* Saved Input

---

# 3. Components

| Component              | Responsibility            |
| ---------------------- | ------------------------- |
| TemplateController     | Expose Template APIs      |
| TemplateService        | Manage Template lifecycle |
| TemplateCompileService | Compile editable schema   |
| TemplateRepository     | Persist Template          |
| TemplateMapper         | DTO conversion            |

---

# 4. Responsibilities

## TemplateService

Responsible for:

* Create Template
* Update metadata
* Save editable schema
* Delete Template
* Retrieve Template

Business rules related to compilation are delegated to TemplateCompileService.

---

## TemplateCompileService

Responsible for:

* Validate editable schema
* Build compiled schema
* Update compiled schema version

Compilation shall not modify editable schema.

---

# 5. Dependencies

```text
TemplateController
        │
        ▼
TemplateService
        │
        ├──────────────► TemplateRepository
        │
        └──────────────► TemplateCompileService
                                │
                                ▼
                     Schema Compiler
```

---

# 6. Public Interfaces

## TemplateService

* create()
* updateMetadata()
* updateSchema()
* delete()
* findById()
* findAll()

---

## TemplateCompileService

* compile()

---

# 7. Domain Objects

* Template
* TemplateField
* CompiledSchema

Editable schema and compiled schema shall always be stored separately.

---

# 8. Repository

TemplateRepository

Responsibilities

* Save
* Update
* Delete
* Query

Repository shall not perform business validation.

---

# 9. Exceptions

* ValidationException
* ConflictException
* NotFoundException
* BusinessException

---

# 10. Validation Rules

TemplateService validates:

* Metadata
* Business rules

TemplateCompileService validates:

* XML structure
* Schema integrity
* Root node
* Circular references
* Duplicate fields

Template deletion shall verify all business constraints before persistence.

Compilation shall verify that the editable schema version is still current before generating the compiled schema.

---

# 11. Implementation Notes

* Metadata update shall not trigger compilation.
* Editable schema shall always be saved before compilation.
* Compilation shall replace the existing compiled schema.
* Optimistic locking shall be applied when updating editable schema.
* Compilation shall fail if the editable schema has been modified by another administrator.
* Deletion behavior shall follow the business rules defined by the API specification and shall never violate referential integrity.
---

# 12. Unit Test Strategy

Required unit tests:

TemplateService

* Create
* Update
* Delete
* Find

TemplateCompileService

* Successful compile
* Invalid schema
* Multiple root nodes
* Circular references
* Duplicate fields
* Optimistic locking

---

# 13. Phase 1 Decisions

| Topic           | Decision          |
| --------------- | ----------------- |
| Editable Schema | Stored separately |
| Compiled Schema | Stored separately |
| Save Draft      | Supported         |
| Compile         | Manual            |
| Auto Compile    | Excluded          |
| Optimistic Lock | Required          |
| Schema Version  | Required          |
