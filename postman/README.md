# XMLGen Postman Collections

REST-level verification for **XMLGen MVP v1.0.0**. Use alongside the [root README](../README.md) UI walkthrough (§7) and API section (§9).

---

## Prerequisites

Backend running at **http://localhost:8080**:

```bash
# From repository root — see README §4
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/xmlgen
export SPRING_DATASOURCE_USERNAME=xmlgen
export SPRING_DATASOURCE_PASSWORD=xmlgen
export JWT_SECRET=dev-only-jwt-secret-minimum-32-characters-long
./gradlew bootRun
```

**Docker alternative:** `docker compose up --build` — keep Postman `baseUrl` as `http://localhost:8080` (direct backend).

---

## Import

Postman **v10.18+** or Postman for Web.

1. **Import** these files from this directory:
   - `XMLGen - Template Module.postman_collection.json`
   - `XMLGen - Master Data.postman_collection.json`
   - `XMLGen - Workspace.postman_collection.json`
   - `XMLGen - Local.postman_environment.json`
2. Select environment **XMLGen - Local** (top-right).

---

## Environment variables

| Variable | Default | Set by |
| -------- | ------- | ------ |
| `baseUrl` | `http://localhost:8080` | Environment |
| `username` | `admin` | Environment |
| `password` | `admin123` | Environment |
| `token` | (empty) | **Authentication → Login** test script |
| `templateId` | (empty) | **Template → Create Template** test script |
| `templateCode` | (empty) | **Create Template** pre-request script |
| `masterDataTypeId` | (empty) | **Master Data → Create Type** |
| `masterDataFieldId` | (empty) | **Master Data → Create Field** |
| `masterDataRecordId` | (empty) | **Master Data → Create Record** |
| `workspaceId` | (empty) | **Workspace → Create Workspace** |
| `workspaceCode` | (empty) | **Workspace → Create Workspace** |

All collections use bearer auth: `Authorization: Bearer {{token}}`.

Protected API requests also require workspace context (Phase 7.1.4): `X-Workspace-Id: {{workspaceId}}` (defaults to `1` via collection pre-request script).

---

## Verification order

Run requests **top to bottom** within each folder. Variables chain automatically via test scripts.

```text
1. Authentication
      └── Login
              ↓ sets token

2. Template
      └── Create Template
              ↓ sets templateId (includes sample schema → compiles)
      └── List / Get / Update / Update Schema / Delete (optional CRUD checks)

3. Master Data Types
      └── Create Type
              ↓ sets masterDataTypeId

4. Master Data Fields
      └── Create Field
              ↓ sets masterDataFieldId

5. Master Data Records
      └── Create Record
              ↓ sets masterDataRecordId

6. XML Generation (in Template collection)
      └── Preview Template
      └── Export Template
```

### Minimum smoke path (5 requests)

| # | Request | Expected |
| - | ------- | -------- |
| 1 | **Authentication → Login** | 200; `data.accessToken` stored |
| 2 | **Template → Create Template** | 201; `data.id` stored |
| 3 | **Master Data Types → Create Type** | 201; type id stored |
| 4 | **Master Data Fields → Create Field** | 201; field id stored |
| 5 | **XML Generation → Preview Template** | 200; `success: true`, `data.xml` non-empty |

Then run **Export Template** with the same body shape as Preview.

---

## Request bodies (reference)

**Preview / Export** (Template collection):

```json
{
  "inputData": {
    "Title": "Sample Match"
  },
  "selectedMasterData": {}
}
```

Matches the sample schema created by **Create Template** (`Title` field under `Game` group).

**Create Record** (after field setup):

```json
{
  "typeId": {{masterDataTypeId}},
  "data": {
    "game_kind_id": 1,
    "game_kind_name": "J1 League"
  }
}
```

---

## Implemented endpoints

| Domain | Collection | Paths |
| ------ | ------------ | ----- |
| Auth | Template Module | `POST /api/v1/auth/login` |
| Templates | Template Module | `/api/v1/templates`, `/templates/{id}`, `/templates/{id}/schema` |
| Preview / Export | Template Module | `POST /api/v1/templates/{id}/preview`, `.../export` |
| Master Data | Master Data | `/api/v1/master-data/types`, `/fields`, `/records` |
| Workspace | Workspace | `/api/v1/workspaces`, `/workspaces/{id}` |

Full inventory: [`docs/release/API-CONTRACT.md`](../docs/release/API-CONTRACT.md)

OpenAPI (dev backend): http://localhost:8080/swagger-ui.html

---

## Expected responses

| Case | HTTP | Body |
| ---- | ---- | ---- |
| Login success | 200 | `{ "success": true, "data": { "accessToken", ... } }` |
| Create template | **201** | `{ "success": true, "data": { "id" } }` |
| Preview success | 200 | `{ "success": true, "data": { "xml": "..." } }` |
| Preview validation failure | 200 | `{ "success": false, "errors": [{ "field", "code" }] }` |
| Missing token | 401 | `{ "success": false, "errors": [...] }` |

Validation failures on preview/export return **HTTP 200** with `success: false` by design.

---

## Troubleshooting

| Problem | Solution |
| ------- | -------- |
| 401 on all requests | Run **Login** first; check `token` in environment |
| Preview returns validation errors | Ensure **Create Template** ran (compiled schema); adjust `inputData` field names |
| `templateId` empty | Re-run **Create Template** or set manually |
| Connection refused | Start backend; confirm `baseUrl` is `http://localhost:8080` |
| Master Data Create Record fails | Run Create Type and Create Field first; match `data` keys to field codes |

---

## Related docs

- [Root README — MVP walkthrough](../README.md#7-complete-mvp-walkthrough)
- [Root README — Troubleshooting](../README.md#10-troubleshooting)
- [API contract](../docs/release/API-CONTRACT.md)
