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

### Template / TemplateField

The output-side metadata. A `Template` owns an editable schema describing the
fields and mappings used to generate XML. `TemplateField` (future) describes a
single mapped output field. Template metadata follows the same rule: the engine
reads the schema, it does not hardcode template field names.

---

## Layer 2 — Schema (Resolved Shape)

The Schema layer is the resolved view derived from metadata. It is not a separate
storage; it is computed from `MasterDataField` definitions (and, for output, from
the stored editable template schema).

The Schema layer answers:

- Which fields exist for a type?
- What is each field's data type and constraints?
- Which fields are required / unique / references?
- For templates: what is the editable schema and field mapping?

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

At generation time the Mapping Engine resolves template field mappings against
master data runtime values and produces XML. It consumes domain/engine types, not
DTOs (per ADR-001 §3), and it resolves fields from metadata.

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

- **TemplateField** — output metadata using the same Metadata → Schema pattern.
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

---

## References

- [ADR-001 — Service Layer Boundary](./ADR-001-service-layer-boundary.md)
- `docs/03-database-design/03-database-design.md` (§4.5–§4.7)
- `docs/06-api-design/p4_master-data-api.md`
- `CLAUDE.md` — Project Conventions
