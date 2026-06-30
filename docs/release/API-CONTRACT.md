# XMLGen v1.0.0 — API Contract

Authoritative REST contract for the MVP release. Generated OpenAPI is available in **dev** at `/swagger-ui.html` (disabled in production).

Base path: `/api/v1`

Envelope: `{ "success", "data", "errors", "meta", "message" }`

---

## Authentication

| Method | Path | Auth | Status | Request | Response `data` |
| ------ | ---- | ---- | ------ | ------- | ----------------- |
| POST | `/auth/login` | Public | 200 | `{ username, password }` | `{ userId, username, isAdmin, accessToken }` |

Protected routes: `Authorization: Bearer <accessToken>`

| Condition | HTTP | Error code |
| --------- | ---- | ---------- |
| Missing/invalid token | 401 | `UNAUTHORIZED` |
| Authenticated, denied | 403 | `FORBIDDEN` |

---

## Templates

| Method | Path | Status | Notes |
| ------ | ---- | ------ | ----- |
| GET | `/templates` | 200 | Query: `page`, `pageSize`, `keyword?`, `status?` |
| POST | `/templates` | **201** | Optional `schema` triggers compile in same transaction |
| GET | `/templates/{id}` | 200 | Returns metadata + reconstructed `schema` |
| PUT | `/templates/{id}` | 200 | Metadata only; does not recompile |
| PUT | `/templates/{id}/schema` | 200 | Replaces fields/mappings; recompiles |
| DELETE | `/templates/{id}` | 200 | Cascades fields and mappings |

---

## Master Data — Types

| Method | Path | Status |
| ------ | ---- | ------ |
| GET | `/master-data/types` | 200 |
| POST | `/master-data/types` | **201** |
| GET | `/master-data/types/{id}` | 200 |
| PUT | `/master-data/types/{id}` | 200 |
| DELETE | `/master-data/types/{id}` | 200 |

Query (list): `page`, `pageSize`, `keyword?`

---

## Master Data — Fields

| Method | Path | Status |
| ------ | ---- | ------ |
| GET | `/master-data/fields` | 200 |
| POST | `/master-data/fields` | **201** |
| GET | `/master-data/fields/{id}` | 200 |
| PUT | `/master-data/fields/{id}` | 200 |
| DELETE | `/master-data/fields/{id}` | 200 |

Query (list): `typeId?`, `page`, `pageSize`, `keyword?`

---

## Master Data — Records

| Method | Path | Status |
| ------ | ---- | ------ |
| GET | `/master-data/records` | 200 |
| POST | `/master-data/records` | **201** |
| GET | `/master-data/records/{id}` | 200 |
| PUT | `/master-data/records/{id}` | 200 |
| DELETE | `/master-data/records/{id}` | 200 |

Query (list): **`typeId` (required)**, `page`, `pageSize`, `keyword?`

---

## XML Generation

| Method | Path | Status | Notes |
| ------ | ---- | ------ | ----- |
| POST | `/templates/{id}/preview` | 200 | Body: `{ inputData?, selectedMasterData? }` |
| POST | `/templates/{id}/export` | 200 | Same request shape as preview |

**Success:** `{ "success": true, "data": { "xml": "..." } }`

**Validation failure:** `{ "success": false, "errors": [{ "field", "code" }] }` — HTTP **200** (by design)

---

## Actuator (public)

| Method | Path |
| ------ | ---- |
| GET | `/actuator/health` |
| GET | `/actuator/info` |

---

## Future work (not implemented)

| Method | Path | Module |
| ------ | ---- | ------ |
| GET/PUT/DELETE | `/saved-inputs/{templateId}` | Saved Inputs |
| GET | `/export-histories` | Export History |
| GET | `/export-histories/{id}/download` | Export History |

Legacy design paths (`/master-data-types`, `/master-data-records`) were **not** implemented. See `docs/06-api-design/p4_master-data-api.md` header note.

---

## Verification

Postman: `postman/XMLGen - MVP.postman_collection.json`

OpenAPI (dev): `http://localhost:8080/swagger-ui.html`

Smoke test checklist: `docs/release/DEPLOYMENT.md`
