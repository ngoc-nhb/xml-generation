# XMLGen Postman Collection

Official Postman assets for the **Template Module** (`v0.4.0`).

## Required Postman version

Postman **v10.18+** or **Postman for Web** (Collection Format v2.1).

## Files

| File | Purpose |
| ---- | ------- |
| `XMLGen - Template Module.postman_collection.json` | Template Module API requests |
| `XMLGen - Local.postman_environment.json` | Local development environment |

## Import

1. Open Postman.
2. Click **Import**.
3. Import both JSON files from this directory.
4. Select the **XMLGen - Local** environment in the top-right environment picker.

## Environment variables

| Variable | Description |
| -------- | ----------- |
| `baseUrl` | API base URL. Default: `http://localhost:8080` |
| `username` | Dev admin username. Default: `admin` |
| `password` | Dev admin password. Default: `admin123` |
| `token` | JWT bearer token. Set automatically by **Login** |
| `templateId` | Last created template id. Set automatically by **Create Template** |
| `templateCode` | Last generated unique template code. Set automatically by **Create Template** |

## Authentication

All Template requests require a bearer token.

1. Start the application locally (see root `README.md`).
2. Run **Authentication > Login**.
3. The test script stores `data.accessToken` into the `token` environment variable.
4. Run Template requests in order.

Protected routes use:

```http
Authorization: Bearer {{token}}
Content-Type: application/json
Accept: application/json
```

## Execution order

1. **Authentication > Login**
2. **Template > Create Template**
3. **Template > List Templates**
4. **Template > Get Template Detail**
5. **Template > Update Template**
6. **Template > Update Template Schema**
7. **Template > Delete Template**

Each request includes saved examples for common success and error responses.

## Template compilation behavior

The collection reflects the current implementation:

- **Create Template** with a non-null `schema` persists `TemplateField` and `TemplateMapping`, then compiles `compiled_schema_json` in the same transaction.
- **Update Template Schema** replaces all schema metadata and recompiles `compiled_schema_json` in the same transaction.
- **Update Template** changes metadata only and does **not** trigger compilation.
- **Get Template Detail** reconstructs `schema` from metadata. It does **not** return `compiled_schema_json`.
- **Delete Template** removes the template and cascades deletion of fields and mappings.

If compilation fails, the entire transaction rolls back and metadata remains unchanged.

To clear schema metadata, send empty `fields` and `mappings` arrays to **Update Template Schema**.

## Implemented endpoints covered

| Method | Path |
| ------ | ---- |
| POST | `/api/v1/auth/login` |
| POST | `/api/v1/templates` |
| GET | `/api/v1/templates` |
| GET | `/api/v1/templates/{id}` |
| PUT | `/api/v1/templates/{id}` |
| PUT | `/api/v1/templates/{id}/schema` |
| DELETE | `/api/v1/templates/{id}` |

## Not included

Master Data APIs are out of scope for this collection.

Standalone compile APIs were rejected by the approved architecture. Compilation
runs automatically during **Create Template** (when `schema` is provided) and
**Update Template Schema** through `TemplateCompilationOrchestrator`.

## Local prerequisites

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/xmlgen
export SPRING_DATASOURCE_USERNAME=xmlgen
export SPRING_DATASOURCE_PASSWORD=xmlgen
export JWT_SECRET=dev-only-jwt-secret-minimum-32-characters-long
./gradlew bootRun
```

Seed admin credentials:

| Setting | Value |
| ------- | ----- |
| Username | `admin` |
| Password | `admin123` |

## Manual verification checklist

After import, verify:

1. **Create Template** returns `200` with an `id`, and the database row has non-null `compiled_schema_json` when `schema` is provided.
2. **Get Template Detail** returns `schema.fields` reconstructed from metadata.
3. **Update Template Schema** returns updated schema metadata and regenerates `compiled_schema_json`.
4. **Delete Template** returns `200`, and related field/mapping rows are removed.

Use saved examples on each request to compare expected response shapes.
