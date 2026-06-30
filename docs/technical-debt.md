# Technical Debt Register

Accepted and intentionally deferred items. None of these block Master Module
completion (`v0.3.0`). See [CLAUDE.md](../CLAUDE.md) for the MVP-First Principle
governing prioritization.

| ID | Item | Deferred to | Notes |
|----|------|-------------|-------|
| TD-001 | Remove `deletedAt` fields from entities | Hard Delete Cleanup Phase | Entities still carry mapped `deletedAt` fields after the hard-delete migration. |
| TD-002 | Drop `deleted_at` columns | Later Flyway migration | Schema cleanup, separate from business logic changes. |
| TD-003 | Implement `searchable` metadata | Dynamic Search / Template Mapping / Dynamic Form | Flag exists in the metadata model but is not yet enforced. |
| TD-004 | Implement `defaultValue` behavior | Dynamic Form Rendering | Default value is stored but not yet applied at runtime. |
| TD-005 | Add controller slice tests | After Template Module | `@WebMvcTest` coverage for request validation, status codes, error envelopes. |
| TD-006 | Investigate JSONB search optimization | After performance measurement | Only act on a demonstrated, measured bottleneck. |
| TD-007 | Database-backed uniqueness for high concurrency | CSV Import / Batch Import / Multi-user editing | App-level uniqueness is sufficient for MVP; re-evaluate under concurrency. |
| TD-008 | Template compile-time mapping validation | Compile-validation phase | `source_type = MASTER_DATA` mapping rules and unexpected-mapping checks are not yet enforced during compilation. |

---

## TD-001 — Remove `deletedAt` fields from entities

The project migrated to hard delete. `MasterDataTypeEntity` and
`MasterDataRecordEntity` still expose mapped `deletedAt` fields that nothing reads
or writes. Remove the mapped fields during the Hard Delete Cleanup Phase.

## TD-002 — Drop `deleted_at` columns

The `deleted_at` columns remain in the schema. Drop them in a dedicated Flyway
migration, separately from the entity field removal (TD-001).

## TD-003 — Implement `searchable` metadata

Record keyword search currently serializes the whole `data_json` to text. Honor
the `searchable` field flag when implementing Dynamic Search / Template Mapping /
Dynamic Form.

## TD-004 — Implement `defaultValue` behavior

`default_value` is stored on `MasterDataField` but never applied when a record omits
the field. Implement together with Dynamic Form Rendering.

## TD-005 — Add controller slice tests

Service and integration tests exist. Add `@WebMvcTest` slice tests for the Master
Data controllers after the Template Module.

## TD-006 — Investigate JSONB search optimization

The keyword search path does not use the GIN index. Investigate only after
performance measurements demonstrate a real bottleneck (per MVP-First Principle).

## TD-007 — Database-backed uniqueness constraints

Uniqueness for record values and `display_order` is enforced at the application
layer (read-then-write), which is race-prone under concurrency. Re-evaluate
database-backed constraints when implementing CSV Import, Batch Import, or
multi-user editing.

## TD-008 — Template compile-time mapping validation

The orchestrator resolves mappings and compiles schema but does not yet enforce
compile-time rules such as `source_type = MASTER_DATA` requiring a valid mapping,
or rejecting mappings on non-`MASTER_DATA` fields. Implement in the dedicated
compile-validation phase.
