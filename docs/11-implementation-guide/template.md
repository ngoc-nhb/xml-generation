# 01. Template Module

---

# 1. Purpose

Manage XML Template definitions.

The module is responsible for:

* Template metadata
* Template schema metadata
* Template schema compilation
* Template lifecycle

---

# 2. Scope

Included

* Create Template
* Update Metadata
* Update Schema
* Delete Template
* Retrieve Template

Excluded

* XML Generation
* Master Data management
* Saved Input
* Standalone compile endpoint (rejected; compilation runs on schema save)

---

# 3. Components

| Component                         | Responsibility                                      |
| --------------------------------- | --------------------------------------------------- |
| TemplateController                | Expose Template APIs                                |
| TemplateService                   | Template CRUD and metadata validation               |
| TemplateCompilationOrchestrator   | Coordinate parse → compile → persist pipeline       |
| TemplateSchemaParser              | Build runtime hierarchy from loaded metadata        |
| TemplateSchemaCompiler            | Transform runtime model into `compiled_schema_json` |
| TemplateRepository                | Persist Template                                    |
| TemplateFieldRepository           | Persist TemplateField                               |
| TemplateMappingRepository         | Persist TemplateMapping                             |

---

# 4. Responsibilities

## TemplateService

Responsible for:

* Create Template
* Update metadata
* Replace editable schema metadata (`TemplateField` + `TemplateMapping`)
* Delete Template
* Retrieve Template and reconstruct API schema from metadata

Schema save delegates compilation to `TemplateCompilationOrchestrator` within
the same transaction.

Must not:

* parse runtime hierarchy for compilation
* compile schema JSON
* resolve Master Data mapping metadata for compilation
* write `compiled_schema_json` directly

---

## TemplateCompilationOrchestrator

Responsible for:

* Load Template, TemplateField, and TemplateMapping metadata
* Resolve `TemplateMappingEntity` → Master Data business metadata
* Build `TemplateCompileContext`
* Invoke `TemplateSchemaParser`
* Invoke `TemplateSchemaCompiler`
* Persist `compiled_schema_json`
* Clear `compiled_schema_json` when metadata is empty

Must not contain parser logic, compiler logic, or XML generation logic.

---

## TemplateSchemaParser

Responsible for:

* Convert loaded `Template` and `TemplateField` metadata into `RuntimeTemplate`
* Build hierarchy from `parent_id`
* Preserve sibling `displayOrder`
* Validate parser-level consistency: duplicate field names, missing parents,
  cyclic hierarchy

The parser receives already-loaded entities. It must not query repositories,
depend on DTOs, resolve mappings, depend on Master Data metadata, write
persistence state, serialize JSON, resolve runtime values, or generate XML.

---

## TemplateSchemaCompiler

Responsible for:

* Consume `RuntimeTemplate` and `TemplateCompileContext`
* Produce deterministic `compiled_schema_json`

The compiler is a pure transformation component. It must not access repositories,
rebuild hierarchy, persist data, resolve database identifiers, or generate XML.

`TemplateCompileContext` is intentionally part of the compiler API even when it
contains only mappings. It is the stable extension point for future compile-time
inputs such as namespaces, compiler options, version information, generation
settings, and feature flags.

`TemplateCompileMapping` is immutable business metadata. It must not contain
persistence identifiers. The orchestrator resolves database identifiers into
business metadata before constructing this object.

---

# 5. Dependencies

```text
TemplateController
        │
        ▼
TemplateService
        │
        ├──────────────► TemplateRepository
        ├──────────────► TemplateFieldRepository
        ├──────────────► TemplateMappingRepository
        │
        └──────────────► TemplateCompilationOrchestrator
                                │
                                ├────────► TemplateSchemaParser
                                ├────────► TemplateSchemaCompiler
                                ├────────► MasterDataFieldRepository
                                └────────► MasterDataTypeRepository
```

---

# 6. Public Interfaces

## TemplateService

* create()
* update()
* updateSchema()
* delete()
* findById()
* findAll()

## TemplateCompilationOrchestrator

* compileAndPersist()

## TemplateSchemaParser

* parse()

## TemplateSchemaCompiler

* compile()

---

# 7. Domain Objects

* Template
* TemplateField
* TemplateMapping
* RuntimeTemplate
* RuntimeField
* TemplateCompileContext
* TemplateCompileMapping

`TemplateField` describes XML structure. `TemplateMapping` connects fields to
`MasterDataField`. Editable metadata and `compiled_schema_json` are stored
separately.

`RuntimeTemplate` and `RuntimeField` are non-persistence runtime schema types.
They expose business structure only and do not expose database identifiers,
repository concerns, or parser lookup indexes.

`RuntimeTemplate` and `RuntimeField` represent hierarchy only. Mapping metadata is
provided separately through `TemplateCompileContext`.

When `sourceType = MASTER_DATA`, exactly one `TemplateMapping` must exist;
compile-time validation for this rule is deferred to the dedicated
compile-validation phase. `sourceType` is explicit and is not derived from
mapping existence.

`triggerActivation` on `TemplateField` controls group activation (nullable;
defaults by `sourceType`).

---

# 8. Repository

TemplateRepository, TemplateFieldRepository, and TemplateMappingRepository

Responsibilities

* Save
* Update
* Delete
* Query

Repositories shall not perform business validation, parsing, compilation, or
orchestration.

---

# 9. Exceptions

* ValidationException
* ConflictException
* NotFoundException
* TemplateSchemaParserException

---

# 10. Validation Rules

TemplateService validates metadata on create/update schema:

* Duplicate `fieldName`
* Invalid `parentFieldName`
* Cyclic parent relationships
* Orphan mappings
* Duplicate mappings

Deferred to compile-validation phase:

* `sourceType = MASTER_DATA` requires a valid mapping
* `INPUT` / `STATIC` fields must not have mappings
* XML structure validation
* Empty handling combinations beyond metadata persistence rules

Template deletion shall verify all business constraints before persistence.

---

# 11. Implementation Notes

* Metadata update shall not trigger compilation.
* Schema save (`updateSchema`) and create-with-schema shall replace all
  `TemplateField` and `TemplateMapping` rows atomically, compile through the
  orchestrator, and return schema reconstructed from metadata.
* There is no standalone Mapping CRUD API.
* Lazy migration of legacy `compiled_schema_json` is a **rejected** architectural
  approach. Editable metadata is the only source of truth.
* Optimistic locking and schema versioning are deferred to later phases.
* Deletion behavior shall follow the business rules defined by the API
  specification and shall never violate referential integrity.

---

# 12. Unit Test Strategy

Required unit tests:

TemplateService

* Create
* Update
* Delete
* Find
* Schema replace validation

TemplateCompilationOrchestrator

* Successful compile and persist
* Clear compiled JSON when metadata is empty
* Mapping resolution into compile context

TemplateSchemaParser

* Simple and nested hierarchy
* Invalid hierarchy detection

TemplateSchemaCompiler

* Deterministic JSON output
* Mapping metadata in compiled JSON

Deferred compile-validation tests:

* `MAPPING_REQUIRED` when `sourceType = MASTER_DATA` without mapping
* `UNEXPECTED_MAPPING` when mapping exists for non-MASTER_DATA field

---

# 13. Phase 1 Decisions

| Topic           | Decision                                      |
| --------------- | --------------------------------------------- |
| Editable Schema | `TemplateField` + `TemplateMapping`           |
| Compiled Schema | `compiled_schema_json` (generated only)       |
| Schema Save     | Atomic persist + compile (Single Save)        |
| Mapping API     | No standalone CRUD; edited with schema        |
| Lazy Migration  | Rejected                                      |
| Compile Endpoint| Rejected (inline via schema save)              |
| Optimistic Lock | Deferred                                      |
| Schema Version  | Deferred                                      |
