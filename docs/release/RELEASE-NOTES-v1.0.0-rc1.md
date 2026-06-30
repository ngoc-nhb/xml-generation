# Release Notes — v1.0.0-rc1

Release candidate for the XMLGen MVP. No new business features — stabilization and release preparation only.

---

## Highlights

- End-to-end MVP: Template administration → Master Data → XML Preview/Export
- Docker Compose one-command startup (PostgreSQL + backend + frontend)
- GitHub Actions CI (backend tests, frontend build/lint, Docker build)
- OpenAPI / Swagger UI in dev profile (`/swagger-ui.html`)
- Synchronized API contract documentation and Postman collection
- Phase 7 architecture, performance, and security reviews documented

---

## Included modules

| Module | Backend | Frontend |
| ------ | ------- | -------- |
| Authentication | ✅ | ✅ |
| Templates (CRUD + schema) | ✅ | ✅ |
| Master Data (types, fields, records) | ✅ | ✅ |
| XML Generation (preview + export) | ✅ | ✅ |

---

## Not included (deferred)

- Saved Inputs
- Export History
- Batch Export
- Versioning
- Dashboard
- User management

---

## Verification status

| Check | Status |
| ----- | ------ |
| `./gradlew build` | Required before tag |
| `npm run build && npm run lint` | ✅ Verified |
| Docker Compose | Requires local Docker verification |
| Postman MVP collection | Updated |
| Documentation sync | Phase 7.2 complete |

---

## Upgrade from rc1 to v1.0.0

Final release requires:

- Full backend test suite green in CI
- Docker smoke test passed
- No unresolved critical architecture issues

See `RELEASE-NOTES-v1.0.0.md` for the final release summary.
