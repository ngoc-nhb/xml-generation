# XMLGen Postman Collection

Postman assets for manual API verification of the Template Module (`v0.4.0`).

## Files

| File | Purpose |
| ---- | ------- |
| `xmlgen-template-module.postman_collection.json` | Template Module API requests |
| `xmlgen-local.postman_environment.json` | Local development variables |

## Import

1. Open Postman
2. Import both JSON files from this directory
3. Select the **XMLGen Local** environment

## Prerequisites

Start the application locally:

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/xmlgen
export SPRING_DATASOURCE_USERNAME=xmlgen
export SPRING_DATASOURCE_PASSWORD=xmlgen
export JWT_SECRET=dev-only-jwt-secret-minimum-32-characters-long
./gradlew bootRun
```

Default seed admin credentials:

| Setting | Value |
| ------- | ----- |
| Username | `admin` |
| Password | `admin123` |

## Recommended run order

1. **Authentication > Login** — stores `accessToken`
2. **Setup > Create Master Data Type** — stores `masterDataTypeId`
3. **Setup > Create Master Data Field** — stores `masterDataFieldId`
4. **Templates > Create Template (with schema)** — stores `templateId`
5. **Templates > Get Template Detail**
6. **Templates > Update Template Metadata**
7. **Templates > Update Template Schema**
8. **Templates > Clear Template Schema**
9. **Templates > Delete Template**

Use **Create Template (metadata only)** when testing templates without schema metadata.

## Covered endpoints

| Method | Path |
| ------ | ---- |
| POST | `/api/v1/auth/login` |
| POST | `/api/v1/master-data/types` |
| POST | `/api/v1/master-data/fields` |
| GET | `/api/v1/templates` |
| POST | `/api/v1/templates` |
| GET | `/api/v1/templates/{id}` |
| PUT | `/api/v1/templates/{id}` |
| PUT | `/api/v1/templates/{id}/schema` |
| DELETE | `/api/v1/templates/{id}` |
