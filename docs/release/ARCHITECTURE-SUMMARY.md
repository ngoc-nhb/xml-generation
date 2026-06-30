# Architecture Summary — XMLGen v1.0.0

---

## System overview

XMLGen is a template-driven XML generation system. Administrators configure templates and master data; authenticated users generate XML through preview and export.

```text
Frontend (REST client)
        │
        ▼
Application Layer (Controllers → Services)
        │
        ├── Compile Engine (template schema → compiled artifact)
        └── Runtime Engine (input + master data → XML)
        │
        ▼
PostgreSQL (metadata + master data)
```

---

## Layer boundaries (frozen)

| Layer | Responsibility | UI visibility |
| ----- | -------------- | ------------- |
| Metadata | Template fields, mappings, master data definitions | Editable |
| Compile artifact | `compiled_schema_json` | Never exposed |
| Runtime | Validation, value resolution, XML serialization | Never exposed |
| Generated XML | Preview/export output | Read-only display |

---

## Frontend architecture

- Feature modules under `frontend/src/features/`
- Public API via `features/<name>/index.ts` only
- Execution state local to XML Generation (`ExecutionPanel`)
- No runtime engine types or compile artifacts in UI

Principles documented in `docs/13-ui-design/12-frontend-stable-architecture.md` and `docs/project-development-workflow.md`.

---

## Backend architecture

- Layered, feature-organized packages under `com.company.xmlgen`
- Controllers thin; business logic in services
- XML Engine infrastructure-independent
- DTOs never passed into the engine

See `docs/adr/ADR-001-service-layer-boundary.md` and `docs/adr/ADR-002-metadata-driven-architecture.md`.

---

## API symmetry

Preview and Export share request shape, validation pipeline, and error envelope. Only downstream business output may diverge in future phases.

---

## Deployment topology (Docker)

```text
Browser → nginx (frontend:80)
              ├── /        → static SPA
              └── /api/*   → backend:8080
                        └── PostgreSQL:5432
```

Same-origin `/api/v1` avoids CORS in containerized deployments.

---

## Out of scope (v1.0.0)

Saved Inputs, Export History, Batch Export, Versioning, Dashboard, User management, role-based endpoint authorization.

---

## Related documents

- `docs/project-development-workflow.md`
- `docs/13-ui-design/12-frontend-stable-architecture.md`
- `docs/release/API-CONTRACT.md`
