# Release Notes — v1.0.0

First production-ready MVP of XMLGen — template-driven XML generation with admin metadata management and user-facing preview/export.

---

## What's new

### Backend

- Compile Engine and Runtime Engine (frozen v1.0)
- REST APIs: Auth, Templates, Master Data, Preview, Export
- JWT authentication, Flyway migrations, standard API envelope
- OpenAPI documentation (dev profile)

### Frontend

- React 19 + Vite application shell
- Template module (CRUD, schema editor)
- Master Data module (types, fields, records)
- XML Generation (orchestration over preview/export APIs)
- Cross-feature integration via public feature APIs only

### Infrastructure

- Docker Compose (dev + production profile)
- GitHub Actions CI/CD
- Postman MVP collection
- Deployment and API contract documentation

---

## MVP business flow

```text
Configure Template schema → Configure Master Data → Generate XML (Preview/Export)
```

All runtime logic remains on the backend. The frontend orchestrates public REST APIs only.

---

## Known limitations

See `KNOWN-LIMITATIONS.md`.

---

## Deployment

See `DEPLOYMENT.md`.

Quick start:

```bash
docker compose up --build
# Frontend: http://localhost:8081
# Login: admin / admin123
```

---

## Technical debt

See `docs/technical-debt.md` and `TECHNICAL-DEBT-SUMMARY.md`.

---

## Architecture

See `ARCHITECTURE-SUMMARY.md`.
