# ADR-003: Workspace Ownership Boundary

**Status:** Accepted (Phase 7.1.0 — architecture only)  
**Date:** 2026-06-30  
**Deciders:** Architecture review (Phase 7.1.0)

---

## Context

XMLGen currently scopes business data implicitly at **system level**:

- `templates.code` is globally unique
- `master_data_types.code` is globally unique
- `SavedInput` and `ExportHistory` reference `user_id` + `template_id` only

This model works for a single-tenant MVP but does not support:

- Multiple isolated configuration sets (templates + master data)
- Per-team or per-project ownership
- Clear boundaries for Saved Inputs and Export History

Phase 7.1.0 introduces **Workspace** as the business ownership root without implementing CRUD, migrations, or REST endpoints yet.

---

## Decision

### 1. Workspace is the ownership root (not a transactional aggregate)

**Workspace** defines the **tenant boundary** for all configurable and generated business data.

| Owned entity | Relationship |
| ------------ | ------------ |
| Template | `workspace_id` FK (required after migration) |
| MasterDataType | `workspace_id` FK (required after migration) |
| SavedInput | `workspace_id` FK (required after migration) |
| ExportHistory | `workspace_id` FK (required after migration) |

**Workspace is not** the DDD aggregate root for Template or Master Data lifecycle. Those remain independent aggregates. Workspace provides **scope and authorization context**.

```text
User ──membership──► Workspace ──owns──► Templates
                              ──owns──► Master Data Types
                              ──owns──► Saved Inputs
                              ──owns──► Export Histories
```

### 2. Aggregate boundaries (unchanged internally)

| Aggregate | Root | Invariants |
| --------- | ---- | ---------- |
| Template | Template | Schema compile, field tree, mappings within one template |
| MasterDataType | MasterDataType | Fields and records for one type |
| SavedInput | SavedInput | One draft per user per template **within workspace** |
| ExportHistory | ExportHistory | Immutable export snapshot |

Cross-aggregate rule: `TemplateMapping` may reference `MasterDataField` **only when both belong to the same workspace**.

### 3. API style — query parameter for collections (Convention 2 aligned)

**Rejected for canonical style:** nested collection paths such as `/workspaces/{id}/templates` for all operations.

**Accepted canonical style:**

| Operation | Pattern |
| --------- | ------- |
| Workspace CRUD | `/api/v1/workspaces`, `/api/v1/workspaces/{id}` |
| List owned resources | `/api/v1/templates?workspaceId=`, `/api/v1/master-data/types?workspaceId=`, etc. |
| Create owned resources | `workspaceId` in request body (required) |
| Single resource by id | `/api/v1/templates/{id}` — server validates `workspace_id` via stored FK |
| Preview / Export | Existing paths; `workspaceId` in body or derived from template |

**Rationale:** Matches [Convention 2](../CLAUDE.md) (browse-first, filter-by-query). Avoids deep URL churn during Phase 7.1.1 migration. Workspace membership is always verified server-side regardless of URL shape.

Optional future: workspace-scoped paths for BFF or external integrators — not MVP.

### 4. Client workspace context

After login, the frontend holds **current workspace id** in app context (not server session). All collection requests include `workspaceId`. Single-resource requests rely on server-side ownership checks.

### 5. Migration — single default workspace

Existing databases receive:

1. One row in `workspaces` (e.g. id=`1`, code=`DEFAULT`, name=`Default Workspace`)
2. `workspace_members` linking all active users with appropriate role
3. `workspace_id = 1` on all existing templates, master data types, saved inputs, export histories
4. Unique constraints scoped: `(workspace_id, code)` replaces global `code` uniqueness where applicable

No data loss. Existing API consumers can omit `workspaceId` temporarily if server defaults to user's primary workspace (transition period in 7.1.1).

### 6. Backward compatibility (phased)

| Phase | Behavior |
| ----- | -------- |
| 7.1.0 | Architecture docs only — no runtime change |
| 7.1.1 | DB migration + optional `workspaceId` (default workspace) |
| 7.1.2+ | UI workspace selector; `workspaceId` required on collections |
| Future | Remove default fallback for omitting `workspaceId` |

---

## Consequences

### Positive

- Clear isolation boundary for templates, master data, drafts, and exports
- Enables multi-workspace users without changing Template or Engine internals
- Minimal REST surface change (query param + body field)

### Negative

- Every list endpoint gains a required filter parameter
- Unique code constraints move to composite keys — migration required
- Services must verify workspace membership on every mutating operation

### Out of scope (Phase 7.1.0)

- Workspace CRUD implementation
- Flyway migration scripts
- REST controllers
- JWT / security model changes
- UI workspace selector implementation

---

## References

- [Phase 7.1.0 Workspace Architecture](../release/phase-7.1.0-workspace-architecture.md)
- [Domain Model — Workspace](../02-domain-model/p5_workspace-ownership.md)
- [API Strategy — Workspace](../06-api-design/p9_workspace-api-strategy.md)
- [ADR-002 Metadata-Driven Architecture](./ADR-002-metadata-driven-architecture.md)
