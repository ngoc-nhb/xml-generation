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
* Save editable schema (fields + mappings, atomic with compile)
* Lazy migration of legacy `compiled_schema_json` when fields are empty
* Delete Template
* Retrieve Template

Schema save delegates compilation to `TemplateCompileService` within the same
transaction.

---

## TemplateCompileService

Responsible for:

* Validate editable schema (including `sourceType = MASTER_DATA` ↔ mapping rules)
* Build compiled schema
* Update `compiled_schema_json`

Compilation during schema save runs in the caller's transaction. A standalone
`compile()` remains for repair or post-migration recompile only.

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
* TemplateMapping
* CompiledSchema

`TemplateField` describes XML structure. `TemplateMapping` connects fields to
`MasterDataField`. Editable metadata and `compiled_schema_json` are stored
separately.

When `sourceType = MASTER_DATA`, exactly one `TemplateMapping` must exist;
compilation fails if missing. `sourceType` is explicit and is not derived from
mapping existence.

`triggerActivation` on `TemplateField` controls group activation (nullable;
defaults by `sourceType`).

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

* XML structure (TemplateField)
* Mapping integrity (TemplateMapping)
* `sourceType = MASTER_DATA` requires exactly one mapping per field
* `INPUT` / `STATIC` fields must not have mappings
* Schema integrity
* Root node
* Circular references
* Duplicate fields
* Empty handling combinations (`INVALID_EMPTY_HANDLING`)

Template deletion shall verify all business constraints before persistence.

Compilation shall verify that the editable schema version is still current before generating the compiled schema.

---

# 11. Implementation Notes

* Metadata update shall not trigger compilation.
* Schema save (`updateSchema`) shall persist `Template`, `TemplateField`, and
  `TemplateMapping` in one transaction, then compile immediately (Single Save
  Principle, ADR-002).
* Any compilation failure rolls back the entire transaction.
* `compiled_schema_json` is generated only; it is never accepted as editable input.
* There is no standalone Mapping CRUD API.
* Lazy migration: when `TemplateField` count is zero and `compiled_schema_json`
  exists, parse JSON → persist fields and mappings → recompile → overwrite JSON.
  No Flyway data migration.
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
* `MAPPING_REQUIRED` when `sourceType = MASTER_DATA` without mapping
* `UNEXPECTED_MAPPING` when mapping exists for non-MASTER_DATA field
* Multiple root nodes
* Circular references
* Duplicate fields
* `INVALID_EMPTY_HANDLING`
* Optimistic locking

---

# 13. Phase 1 Decisions

| Topic           | Decision                                      |
| --------------- | --------------------------------------------- |
| Editable Schema | `TemplateField` + `TemplateMapping`           |
| Compiled Schema | `compiled_schema_json` (generated only)       |
| Schema Save     | Atomic persist + compile (Single Save)        |
| Mapping API     | No standalone CRUD; edited with schema        |
| Lazy Migration  | Application-layer; no Flyway data migration   |
| Compile Endpoint| Superseded by schema save; retained for repair  |
| Optimistic Lock | Required                                      |
| Schema Version  | Required                                      |
