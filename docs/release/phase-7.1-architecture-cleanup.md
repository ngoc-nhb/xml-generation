# Phase 7.1 — Architecture Cleanup Review

Review date: Phase 7.0 stabilization. **No unnecessary refactoring applied** (Rule of Three, MVP-First).

---

## Findings

### Duplicated code

| Item | Decision |
| ---- | -------- |
| Per-feature `ConfirmDialog` (templates, master-data) | **Keep** — Rule of Three not satisfied |
| Preview/export envelope parsing in xml-generation API | **Keep** — TD-F005; extract when third consumer exists |
| Error message maps per feature | **Keep** — domain-specific codes |

### Dead code

| Item | Decision |
| ---- | -------- |
| `deletedAt` on entities | **Keep** — tracked TD-001/TD-002; cleanup is scoped migration |
| `docs/12-ui-design/` duplicate tree | **Deprecated** — canonical source is `docs/13-ui-design/` (TD-F006) |
| `buildFormFromSchema.ts` in folder-structure doc only | **Doc drift only** — file never existed |

### Unnecessary abstractions

None identified for removal. Shared `api/client.ts`, feature public APIs, and engine separation are intentional.

### Naming / package consistency

| Area | Status |
| ---- | ------ |
| Backend feature packages | Consistent (`template`, `masterdata`, `engine`, etc.) |
| Frontend features | Consistent kebab-case folders, camelCase exports |
| REST paths | `/master-data/types` (not legacy `/master-data-types`) — docs updated in Phase 7.2 |

### Documentation drift

Addressed in Phase 7.2 (API contract, implementation guide headers, UI integration doc).

---

## Files changed (Phase 7.1)

No production code refactored. Review captured in this document and release docs.

---

## Verification

Architecture boundaries unchanged. ADR-001 and ADR-002 remain satisfied.

---

## Assumptions

Cleanup excludes deferred TD items requiring migrations or new features.

## Deviations

None.

## Recommendations

- Remove `docs/12-ui-design/` in a future doc-only PR after team confirmation
- Execute TD-001/TD-002 in dedicated hard-delete cleanup phase
