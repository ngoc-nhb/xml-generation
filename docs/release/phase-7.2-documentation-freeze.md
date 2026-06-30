# Phase 7.2 — Documentation Freeze

Synchronized implementation, API docs, UI docs, workflow, and ADRs for v1.0.0 MVP.

---

## Changes applied

| Document | Update |
| -------- | ------ |
| `docs/release/API-CONTRACT.md` | **New** — authoritative endpoint inventory |
| `docs/06-api-design/p4_master-data-api.md` | v1.0 header; canonical paths; legacy paths marked |
| `docs/13-ui-design/07-api-integration.md` | Master Data paths corrected |
| `docs/11-implementation-guide/master-data.md` | Field controller; checklist marked complete |
| `docs/technical-debt.md` | Frontend TD-F001–F006 added |
| `docs/project-development-workflow.md` | Phase 7 status table |
| `postman/README.md` | MVP collections documented |
| Root `README.md` | Docker, release, status updated |

## Future work explicitly marked

| Module | Document |
| ------ | -------- |
| Saved Inputs | `docs/11-implementation-guide/saved-input.md` |
| Export History | `docs/11-implementation-guide/export-history.md` |
| Legacy master-data paths | Body of `p4_master-data-api.md` (historical) |
| `docs/12-ui-design/` | Superseded by `docs/13-ui-design/` |

## Endpoint rule

Every **implemented** endpoint is listed in `API-CONTRACT.md`.

Every **documented but unimplemented** endpoint is marked future work in API contract or implementation guide headers.

---

## Verification

Cross-checked 24 application routes against frontend API modules and Postman collections.

## Assumptions

Legacy API design sections retained for historical reference with clear banners.

## Deviations

OpenAPI generated from code (springdoc) supersedes stale DTO field names in older design drafts for minor differences.

## Recommendations

Regenerate or annotate OpenAPI after major API changes; keep `API-CONTRACT.md` updated at release boundaries.
