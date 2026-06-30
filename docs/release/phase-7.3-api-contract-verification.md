# Phase 7.3 — API Contract Verification

---

## Coverage matrix

| Domain | Endpoints | Postman | OpenAPI | Frontend |
| ------ | --------- | ------- | ------- | -------- |
| Auth | 1 | ✅ Template collection | ✅ | ✅ |
| Templates | 6 | ✅ | ✅ | ✅ |
| Master Data Types | 5 | ✅ Master Data collection | ✅ | ✅ |
| Master Data Fields | 5 | ✅ | ✅ | ✅ |
| Master Data Records | 5 | ✅ | ✅ | ✅ |
| Preview | 1 | ✅ | ✅ | ✅ |
| Export | 1 | ✅ | ✅ | ✅ |
| Actuator | 2 | Manual | N/A | N/A |

**Total application routes:** 24

---

## Status codes verified (by design)

| Case | HTTP |
| ---- | ---- |
| Login success | 200 |
| Create resource | 201 (types, fields, records, templates) |
| Read/update/delete success | 200 |
| Validation (Bean Validation) | 400 |
| Not found | 404 |
| Conflict | 409 |
| Unauthorized | 401 |
| Preview/export validation failure | **200** with `success: false` |

---

## Error envelope

All errors use `ApiResponse` with `errors[].code` and optional `field`.

Frontend maps codes via feature `errorMessages.ts` files.

---

## OpenAPI

- Library: `springdoc-openapi-starter-webmvc-ui` 2.6.0
- Dev: `http://localhost:8080/swagger-ui.html`
- Prod: disabled (`SPRINGDOC_ENABLED=false`)

---

## Postman

| Collection | File |
| ---------- | ---- |
| Auth + Template + XML Gen | `postman/XMLGen - Template Module.postman_collection.json` |
| Master Data | `postman/XMLGen - Master Data.postman_collection.json` |
| Environment | `postman/XMLGen - Local.postman_environment.json` |

---

## Undocumented behavior

None identified for implemented routes.

---

## Assumptions

Postman scripts set environment variables for chained requests; manual IDs work without scripts.

## Deviations

None.

## Recommendations

Add Postman Login to Master Data collection or document dependency on Template collection Login (current README covers this).
