# Part 9. Workspace API Strategy

**Phase:** 7.1.0 (architecture only)

---

## 1. Overview

This document defines how REST APIs express **workspace ownership** after Phase 7.1.

It complements [ADR-003](../adr/ADR-003-workspace-ownership.md) and does not introduce new endpoints in Phase 7.1.0.

---

## 2. Design Question

Which style should be canonical?

```http
GET /api/v1/workspaces/{workspaceId}/templates
```

vs

```http
GET /api/v1/templates?workspaceId={workspaceId}
```

---

## 3. Recommendation

**Canonical style: query parameter (`?workspaceId=`) for collection endpoints.**

Nested workspace paths are **not** the primary pattern for XMLGen.

### Rationale

| Factor | Query param | Nested path |
| ------ | ----------- | ----------- |
| Convention 2 (browse-first) | ✅ Matches `/master-data/fields?typeId=` | ❌ Forces hierarchy before browse |
| Migration cost | ✅ Add param to existing list APIs | ❌ New URL tree for every resource |
| Client context | ✅ Workspace id from app context → query | ⚠️ Duplicated in every path segment |
| Authorization | ✅ Same server-side membership check | ✅ Same check |
| OpenAPI / Postman | ✅ Extend existing operations | ❌ Double operation count |

Workspace CRUD itself uses `/workspaces` paths (resource is the workspace).

---

## 4. API Ownership Matrix

| Resource | List | Get by id | Create | Update | Delete |
| -------- | ---- | --------- | ------ | ------ | ------ |
| Workspace | `GET /workspaces` | `GET /workspaces/{id}` | `POST /workspaces` | `PUT /workspaces/{id}` | `DELETE /workspaces/{id}` |
| Template | `GET /templates?workspaceId=` | `GET /templates/{id}` | `POST /templates` + body | `PUT /templates/{id}` | `DELETE /templates/{id}` |
| Master Data Type | `GET /master-data/types?workspaceId=` | `GET /master-data/types/{id}` | `POST /master-data/types` + body | `PUT ...` | `DELETE ...` |
| Master Data Field | `GET /master-data/fields?workspaceId=` | `GET /master-data/fields/{id}` | `POST ...` + body | `PUT ...` | `DELETE ...` |
| Master Data Record | `GET /master-data/records?workspaceId=&typeId=` | `GET .../{id}` | `POST ...` + body | `PUT ...` | `DELETE ...` |
| Saved Input | `GET /saved-inputs/{templateId}?workspaceId=` | — | `PUT /saved-inputs/{templateId}` + body | — | `DELETE ...` |
| Export History | `GET /export-histories?workspaceId=` | `GET /export-histories/{id}` | (via export) | — | — |
| Preview | — | `POST /templates/{id}/preview` + body | — | — | — |
| Export | — | `POST /templates/{id}/export` + body | — | — | — |

---

## 5. Request Rules

### 5.1 Collection GET

`workspaceId` is **required** (after transition period).

```http
GET /api/v1/templates?workspaceId=1&page=1&pageSize=20
```

Server:

1. Authenticate user
2. Verify membership in workspace `1`
3. Return templates WHERE `workspace_id = 1`

### 5.2 Create POST

`workspaceId` in JSON body:

```json
{
  "workspaceId": 1,
  "code": "LIVE_GAME",
  "name": "Live Game"
}
```

Server sets `workspace_id` on insert after membership check.

### 5.3 Single resource GET/PUT/DELETE

Path uses entity id only:

```http
GET /api/v1/templates/12
```

Server loads template, reads `workspace_id`, verifies membership. **Do not trust client-supplied workspaceId alone** for authorization.

Optional query param `workspaceId` on GET may be used for early rejection (404 vs 403) — implementation detail.

### 5.4 Preview / Export

```json
{
  "workspaceId": 1,
  "inputData": { ... },
  "selectedMasterData": { ... }
}
```

If `workspaceId` omitted during transition: derive from template row. Reject if template workspace not accessible.

---

## 6. Error Codes (proposed)

| Code | HTTP | When |
| ---- | ---- | ---- |
| WORKSPACE_NOT_FOUND | 404 | Unknown workspace id (CRUD) |
| WORKSPACE_ACCESS_DENIED | 403 | User not a member |
| WORKSPACE_REQUIRED | 400 | Missing workspace on API request (Phase 7.1.4+) |
| WORKSPACE_INACTIVE | 409 | Workspace status INACTIVE |
| INVALID_WORKSPACE | 400 | Unknown or malformed workspace id in context resolution |
| WORKSPACE_ENTITY_MISMATCH | 409 | Entity belongs to different workspace |

Add to [p8_error-model.md](./p8_error-model.md) in Phase 7.1.1.

---

## 6.1 Workspace Context resolution (Phase 7.1.4)

Every authenticated `/api/v1/**` request resolves workspace context before controllers:

| Priority | Source |
| -------- | ------ |
| 1 | `X-Workspace-Id` request header |
| 2 | `workspaceId` query parameter (backward compatibility) |

No silent fallback to Default Workspace. See [p5_workspace-ownership.md](../02-domain-model/p5_workspace-ownership.md) §8.

---

## 7. Transition Period (7.1.1)

When `workspaceId` is omitted:

1. Resolve user's **primary workspace** (first membership or default id=1)
2. Process request as if `workspaceId` were supplied
3. Log deprecation warning (optional)

Remove fallback in a later phase once UI always sends `workspaceId`.

---

## 8. Frontend Integration

```text
WorkspaceProvider
    │
    ├── workspaceId: number
    ├── workspaces: WorkspaceSummary[]
    └── setWorkspaceId(id)

api client interceptors OR explicit param:
    GET /templates?workspaceId=${context.workspaceId}
```

TanStack Query keys must include workspace:

```text
['templates', workspaceId, filters]
```

Switching workspace invalidates feature queries.

---

## 9. What Does Not Change

- Response envelope (`success`, `data`, `errors`, `meta`)
- Template schema / compile API shapes
- Preview / Export request bodies (except optional `workspaceId`)
- Runtime Engine internal APIs (none exposed)
- Master data record JSONB structure

---

## 10. References

- [ADR-003](../adr/ADR-003-workspace-ownership.md)
- [Phase 7.1.0](../release/phase-7.1.0-workspace-architecture.md)
- [API Overview](./p1_overview.md)
