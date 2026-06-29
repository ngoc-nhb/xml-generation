# ADR-002: Metadata-Driven Architecture

| Field | Value |
|-------|-------|
| **Status** | Accepted |
| **Date** | 2026-06-29 |
| **Scope** | Master Data module, Template module, Mapping Engine, XML Generation |
| **Supersedes** | None |
| **Related** | [ADR-001 — Service Layer Boundary](./ADR-001-service-layer-boundary.md) |

---

## Context

The XML Generation System must let business users define **what data exists** and
**how it is structured** without code changes or new database tables per business
concept. A "Game Kind", a "Stadium", or a "League" is not a hand-written entity;
it is a record whose shape is described by metadata that users configure at runtime.

The same principle extends to output: a Template describes the editable schema and
field mappings used to produce XML, and the Mapping Engine binds master data values
into that schema. None of these layers may depend on hardcoded field names.

This ADR records the metadata-driven model as the architectural foundation of the
system and defines the contract every consuming module must honor.

---

## Decision

The system is built on three layers: **Metadata → Schema → Runtime**.

```text
        ┌─────────────────────────────────────────────┐
        │                  METADATA                     │
        │  (definition — configured by users)           │
        │                                               │
        │  MasterDataType      Template                 │
        │  MasterDataField     TemplateField            │
        │                      TemplateMapping          │
        └───────────────────────┬───────────────────────┘
                                 │  describes
                                 ▼
        ┌─────────────────────────────────────────────┐
        │                  SCHEMA                        │
        │  (resolved shape derived from metadata)       │
        │                                               │
        │  field set, data types, required/unique,      │
        │  references, editable template schema,        │
        │  field mappings                               │
        └───────────────────────┬───────────────────────┘
                                 │  governs
                                 ▼
        ┌─────────────────────────────────────────────┐
        │                  RUNTIME                       │
        │  (instance data — created by users)           │
        │                                               │
        │  MasterDataRecord (data_json: JSONB)          │
        │  Mapping Engine → XML Generation              │
        └─────────────────────────────────────────────┘
```

The architectural rule that ties the layers together:

> **Behavior is read from metadata. No business logic may depend on hardcoded
> field names.** Validation, mapping, and XML generation must always resolve
> field definitions from the Metadata layer at runtime.

---

## Layer 1 — Metadata (Definition)

Metadata is user-configured definition data. It is the only place where the
structure of business data lives.

### MasterDataType

The aggregate root for a category of master data (e.g. `GAME_KIND`, `STADIUM`).

- Identity: `code` (unique), `name`, `description`, `status`.
- Owns its fields and its records.

### MasterDataField

The definition of a single attribute of a `MasterDataType`.

- `field_name` — the key used inside `data_json` (unique per type).
- `data_type` — `STRING | INTEGER | LONG | DECIMAL | BOOLEAN | DATE | DATETIME`.
- `required`, `unique`, `searchable` — behavioral metadata flags.
- `default_value` — declared default for the field.
- `display_order` — ordering for form rendering.
- `master_data_reference_type_id` — when set, the field references records of
  another `MasterDataType` (a one-level reference).

> Behavioral flags such as `searchable` and `default_value` are part of the
> metadata model. Their presence does **not** require immediate enforcement; see
> "Metadata May Precede Enforcement" below.

### Template / TemplateField / TemplateMapping

The output-side metadata. A `Template` owns an editable XML schema (`TemplateField`)
and separate field mappings (`TemplateMapping`). `TemplateField` describes the XML
structure only. `TemplateMapping` connects a `TemplateField` to a `MasterDataField`.
`source_type` declares value origin: `INPUT`, `STATIC`, or `MASTER_DATA`. It is
stored on `TemplateField` and is **not** derived from mapping existence.

When `source_type = MASTER_DATA`, exactly one `TemplateMapping` must exist for
that field. Compilation fails if the mapping is missing or if more than one
mapping exists. When `source_type` is `INPUT` or `STATIC`, no `TemplateMapping`
may exist for that field.

`trigger_activation` controls whether a child node's resolved value activates an
optional parent GROUP (see `04-template-schema` Part 2 §13). It is a required
business rule, not a deferred enhancement.

---

## Layer 2 — Schema (Resolved Shape)

The Schema layer is the resolved view derived from metadata. It is not a separate
storage; it is computed from `MasterDataField` definitions (and, for output, from
the stored editable template schema).

The Schema layer answers:

- Which fields exist for a type?
- What is each field's data type and constraints?
- Which fields are required / unique / references?
- For templates: what is the editable XML schema (`TemplateField`) and how fields
  map to master data (`TemplateMapping`)?

Consumers (validation, mapping, form rendering) operate against this resolved
schema, never against literal field names.

---

## Layer 3 — Runtime (Instance Data)

### MasterDataRecord

Runtime instance data is stored as **JSONB** (`data_json`), not as one table per
type.

- The record stores values keyed by `field_name`.
- JSON is persisted exactly as received (no key transformation, no reordering).
- The shape is validated against the Schema layer before persistence.

### Mapping Engine → XML Generation (future)

At generation time the Mapping Engine resolves `TemplateMapping` rows against
master data runtime values, combines them with the compiled schema, and produces
XML. It consumes domain/engine types, not DTOs (per ADR-001 §3), and resolves
fields from metadata.

---

## Schema–Mapping Separation

Schema and Mapping are independent metadata concerns. They are stored, edited, and
validated separately and combined only at compile time or runtime by the Mapping
Engine.

### Responsibilities

| Entity | Responsibility | Must NOT contain |
|--------|----------------|------------------|
| `TemplateField` | XML structure: hierarchy, node type, value type, occurrence, empty handling, source intent (`INPUT` / `STATIC` / `MASTER_DATA`) | Master data field references |
| `MasterDataField` | Master data attribute definition for a `MasterDataType` | Template or XML mapping |
| `TemplateMapping` | Link from one `TemplateField` to one `MasterDataField` | XML structure or master data schema |

### Ownership

```text
Template
 ├─ TemplateField        (1:N, cascade delete)
 └─ TemplateMapping      (1:N, cascade delete)

TemplateMapping
 ├─ template_field_id    → TemplateField
 └─ master_data_field_id → MasterDataField (ON DELETE SET NULL)
```

`TemplateField` does not own `TemplateMapping`. Both are owned by `Template`.
Deleting a `MasterDataField` must not be blocked by a mapping; the mapping FK
uses `ON DELETE SET NULL` and compilation/validation detects invalid mappings.

### Dependency direction

```text
TemplateField          MasterDataField
      │                       │
      └──── TemplateMapping ──┘
                  │
                  ▼
         Mapping Engine (future)
                  │
                  ▼
         compiled_schema_json
                  │
                  ▼
           XML Generator
```

Dependency flows **inward** to `TemplateMapping`. `TemplateField` and
`MasterDataField` do not reference each other directly.

### Future Mapping Engine architecture

1. Load `TemplateField` rows and build the XML tree.
2. Load `TemplateMapping` rows and resolve `MasterDataField` (+ parent
   `MasterDataType`) for each linked template field.
3. Validate mappings:
   - Each `TemplateField` with `source_type = MASTER_DATA` has exactly one
     `TemplateMapping`.
   - No `TemplateMapping` exists for fields where `source_type` is not
     `MASTER_DATA`.
   - Target and source fields exist; types are compatible.
   Invalid mappings (e.g. after master data deletion) produce compile-time or
   generation-time errors — they do not prevent master data deletion.
4. Merge schema + mappings into `compiled_schema_json` (pre-compilation).
5. At XML generation, the engine reads only `compiled_schema_json` + runtime
   input/master-data payloads (no live metadata queries).

`field_name` on `TemplateField` is the internal stable key (form binding,
`input_data_json`). It is never emitted as an XML node name. `xml_name` is the
actual XML tag or attribute name.

---

## Single Save Principle

`TemplateField` and `TemplateMapping` form **one metadata definition**. They are
edited, persisted, and compiled together — never through standalone Mapping CRUD.

### Rules

1. **Atomic persistence** — A schema save persists `Template`, all
   `TemplateField` rows, and all `TemplateMapping` rows inside a single database
   transaction.
2. **Immediate compilation** — Compilation runs immediately after persistence in
   the same transaction. `compiled_schema_json` is a **generated artifact only**;
   it is never the source of truth for edits.
3. **Rollback on failure** — Any compilation failure rolls back the entire
   transaction. Partial updates (fields saved but mappings not, or metadata saved
   but compile failed) are not permitted.
4. **No standalone Mapping API** — There is no separate endpoint to create, update,
   or delete `TemplateMapping` rows. Mapping changes are submitted as part of the
   template schema save payload.

### Save flow

```text
PUT /api/v1/templates/{id}/schema
        ↓
Validate payload (fields + mappings)
        ↓
BEGIN TRANSACTION
        ↓
Replace TemplateField rows
        ↓
Replace TemplateMapping rows
        ↓
Compile → compiled_schema_json
        ↓
COMMIT (or ROLLBACK on any failure)
```

Metadata-only updates (`PUT /api/v1/templates/{id}`) do not trigger compilation.

### Rejected: Lazy migration (legacy templates)

Lazy migration of legacy `compiled_schema_json` into editable metadata tables is
**rejected**. Editable metadata (`TemplateField`, `TemplateMapping`) is the only
source of truth. Legacy templates without metadata rows must be recreated through
normal schema save APIs rather than automatic JSON backfill.

---

## Consequences

### Validation is metadata-driven (implemented)

The Master Data validation framework reads `MasterDataField` definitions and
applies rules generically. Each rule is a `ValidationRule` with a `priority()`:

| Rule | Priority | Reads from metadata |
|------|----------|---------------------|
| `RequiredValidationRule` | 100 | `required` flag |
| `DataTypeValidationRule` | 200 | `data_type` |
| `UniqueValidationRule` | 300 | `unique` flag |
| `ReferenceValidationRule` | 400 | `master_data_reference_type_id` |

No rule references a literal field name. New rules are added as new
`ValidationRule` beans without modifying the orchestrator (Open/Closed; see
ADR-001 and project conventions).

### Storage is intentionally denormalized

`MasterDataRecord.data_json` is JSONB by design. The system must **not** introduce
an entity-per-type or column-per-field implementation. Normalizing runtime data
would defeat the metadata-driven model.

### Metadata may precede enforcement

A field defined in metadata (e.g. `searchable`, `default_value`) may exist before
its runtime behavior is implemented. Such fields are part of the model and must
not be treated as dead code or removed merely because they are not yet enforced.
Their enforcement is scheduled with the features that consume them (Dynamic
Search, Dynamic Form Rendering, Template Mapping).

### Future compatibility

The model supports, without major refactoring:

- **TemplateField** — XML schema metadata (`field_name`, `xml_name`, `display_name`,
  `occurrence_rule`, `empty_handling`).
- **TemplateMapping** — connects `TemplateField` to `MasterDataField`.
- **Mapping Engine** — resolves mappings from metadata against runtime values.
- **XML Generator** — consumes resolved schema + runtime values.
- **CSV / Batch Import** — reuses the same validation framework over the Schema layer.
- **Dynamic Form Rendering** — renders forms from `MasterDataField` metadata
  (`display_order`, `data_type`, `required`, `default_value`).

---

## Compliance Checklist

A change complies with this ADR when:

1. No business logic branches on a hardcoded `field_name`.
2. Validation, mapping, and generation resolve fields from metadata at runtime.
3. Runtime master data stays in JSONB; no per-type tables are introduced.
4. New validation behavior is a new `ValidationRule`, not a new conditional in the
   orchestrator.
5. Metadata flags are honored or explicitly deferred — never silently dropped.
6. `TemplateField` describes XML only; master data bindings live in
   `TemplateMapping`, never inline on `TemplateField` or `MasterDataField`.
7. Schema saves follow the **Single Save Principle**: fields, mappings, and
   compilation are atomic; `compiled_schema_json` is generated only.
8. `source_type = MASTER_DATA` requires exactly one `TemplateMapping`; do not
   derive `source_type` from mapping existence.

---

## References

- [ADR-001 — Service Layer Boundary](./ADR-001-service-layer-boundary.md)
- `docs/03-database-design/03-database-design.md` (§4.5–§4.7)
- `docs/06-api-design/p4_master-data-api.md`
- `CLAUDE.md` — Project Conventions
