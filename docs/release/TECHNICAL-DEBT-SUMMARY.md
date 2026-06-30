# Technical Debt Summary — v1.0.0

Consolidated view for release. Full register: `docs/technical-debt.md`.

---

## Backend (blocking: none for MVP)

| ID | Item | Priority |
| -- | ---- | -------- |
| TD-001 | Remove `deletedAt` entity fields | Low — Hard Delete Cleanup |
| TD-002 | Drop `deleted_at` columns | Low — Flyway migration |
| TD-003 | Enforce `searchable` metadata | Future — Dynamic Search |
| TD-004 | Apply `defaultValue` at runtime | Future — Dynamic Form |
| TD-005 | Controller `@WebMvcTest` slice tests | Medium — quality |
| TD-006 | JSONB search optimization | Future — after measurement |
| TD-007 | DB uniqueness constraints | Future — concurrency phase |
| TD-008 | Compile-time mapping validation | Medium — compile phase |
| TD-011 | Runtime DATE/DATETIME format validation | Future — runtime model |

---

## Frontend (v1.0.0)

| ID | Item | Priority |
| -- | ---- | -------- |
| TD-F001 | No Vitest / component test suite | Medium — Phase 8+ |
| TD-F002 | Single JS bundle ~654 KB | Low — code split when measured need |
| TD-F003 | Native `<select>` in some pickers | Low — UX polish |
| TD-F004 | `/generate` vs `/xml-generation` doc drift | Low — docs only |
| TD-F005 | Preview/export envelope helper duplication | Low — extract `postEnvelope()` when Rule of Three |
| TD-F006 | Duplicate `docs/12-ui-design/` tree | Low — mark deprecated |

---

## Release recommendation

None of the above block v1.0.0. Address TD-005 and TD-F001 in a dedicated quality phase after release.
