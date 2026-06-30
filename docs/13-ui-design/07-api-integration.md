# 07. API Integration

---

## 1. Purpose

Map every screen to backend REST APIs.

**Rule:** The frontend calls REST endpoints only. Never Runtime Engine internals.

All paths prefixed with `/api/v1`.

---

## 2. API Client Architecture

```text
Screen / Hook
    ↓
Feature API module (features/*/api/*.api.ts)
    ↓
Shared apiClient (api/client.ts)
    ↓
REST
    ↓
Backend (opaque)
```

**API Ownership Rule:** All HTTP must live in feature API modules. Never call
`fetch()` or axios from components, dialogs, or pages. See
`12-frontend-stable-architecture.md` §5.

### Envelope handling

Every response follows:

```json
{ "success": true, "data": {}, "meta": {}, "errors": [] }
```

Client responsibilities:

- Parse `success`
- Throw typed `ApiError[]` on failure
- Extract `data` and `meta` for callers
- Attach JWT from auth context to `Authorization: Bearer`

### Error codes

UI translates `errors[].code` to user messages. Never display raw stack traces.

---

## 3. Authentication

| Screen | Method | Endpoint | Notes |
| ------ | ------ | -------- | ----- |
| Login | POST | `/auth/login` | Store `accessToken`, user claims |
| Logout | POST | `/auth/logout` | Clear auth |
| All protected | — | — | Bearer token on every request |

---

## 4. Templates (Admin)

| Screen | Method | Endpoint |
| ------ | ------ | -------- |
| Template List | GET | `/templates?page&pageSize&keyword&status` |
| Create Template | POST | `/templates` |
| Template Detail | GET | `/templates/{id}` |
| Update Metadata | PUT | `/templates/{id}` |
| Save Schema | PUT | `/templates/{id}/schema` |
| Delete | DELETE | `/templates/{id}` |

**Not used:** `POST /templates/{id}/compile` — rejected by architecture.

Schema save triggers compilation server-side.

---

## 5. Master Data (Admin)

| Screen | Method | Endpoint |
| ------ | ------ | -------- |
| Type List | GET | `/master-data-types` |
| Type Detail | GET | `/master-data-types/{id}` |
| Create Type | POST | `/master-data-types` |
| Update Type | PUT | `/master-data-types/{id}` |
| Type Schema | PUT | `/master-data-types/{id}/schema` |
| Delete Type | DELETE | `/master-data-types/{id}` |
| Record List | GET | `/master-data-types/{id}/records` |
| Create Record | POST | `/master-data-records` |
| Update Record | PUT | `/master-data-records/{id}` |
| Delete Record | DELETE | `/master-data-records/{id}` |

---

## 6. XML Generation (All authenticated users)

| Action | Method | Endpoint | Request body |
| ------ | ------ | -------- | ------------ |
| Preview | POST | `/templates/{templateId}/preview` | `{ inputData, selectedMasterData }` |
| Export | POST | `/templates/{templateId}/export` | `{ inputData, selectedMasterData }` |

### Response contract (frozen v1.0)

**Success:**

```json
{ "success": true, "data": { "xml": "..." } }
```

**Validation failure:**

```json
{
  "success": false,
  "errors": [{ "field": "GameId", "code": "SOURCE_TYPE_REQUIRED" }]
}
```

UI maps `field` to dynamic form paths. UI never expects `executionTree`.

### Supporting reads

| Need | Method | Endpoint |
| ---- | ------ | -------- |
| Template picker | GET | `/templates?status=ACTIVE` |
| Form schema | GET | `/templates/{id}` → `schema` |
| Master data options | GET | `/master-data-types/{id}/records` |

---

## 7. Saved Inputs (Future)

| Action | Method | Endpoint |
| ------ | ------ | -------- |
| Load draft | GET | `/saved-inputs/{templateId}` |
| Save draft | PUT | `/saved-inputs/{templateId}` |
| Delete draft | DELETE | `/saved-inputs/{templateId}` |

Feature flag until backend implemented.

---

## 8. Export History (Future)

| Screen | Method | Endpoint |
| ------ | ------ | -------- |
| List | GET | `/export-histories?page&pageSize` |
| Detail | GET | `/export-histories/{id}` |
| Download | GET | `/export-histories/{id}/download` |

Download returns `Content-Type: application/xml` with `Content-Disposition: attachment`.

MVP Export (v1.0) returns XML in JSON — client may offer browser download from string until history API ships.

---

## 9. Screen → API Matrix

| Screen | APIs |
| ------ | ---- |
| Login | login |
| Dashboard | optional templates count |
| Template List | list, delete |
| Template Create/Edit | create, get, update |
| Schema Editor | get, update schema |
| Master Data | full master-data CRUD |
| XML Generator | list templates, get schema, preview, export, (+ saved inputs, records) |
| Export History | list, detail, download |
| Settings | logout |

---

## 10. Caching Strategy (Server State)

| Data | Stale time | Invalidate on |
| ---- | ---------- | ------------- |
| Template list | 30s | create, update, delete |
| Template detail | 0 on editor | schema save |
| Master data types | 60s | CRUD |
| Preview result | No cache | each preview |
| Export result | No cache | each export |

Use TanStack Query keys per resource: `['templates', id]`, `['master-data-types', typeId, 'records']`.

---

## 11. What the Frontend Must Not Call

- Any internal engine endpoint
- Assumed future endpoints without feature flags
- Direct database or file paths

---

## 12. Related Documents

- `docs/06-api-design/` — authoritative API specs
- `08-state-management.md`
- `09-error-handling.md`
