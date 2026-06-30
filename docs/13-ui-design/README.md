# 13. UI Design

Frontend architecture and UX design for the XML Generation System.

**Phase 6.0 — design documentation only.** No React, HTML, or CSS implementation.

**Status:** Frontend architecture frozen. Implementation ready to begin.

---

## Documents

| # | Document | Purpose |
| - | -------- | ------- |
| 01 | [ui-architecture.md](./01-ui-architecture.md) | Frontend layers, principles, tech direction |
| 02 | [information-architecture.md](./02-information-architecture.md) | Sitemap, journeys, terminology |
| 03 | [design-system.md](./03-design-system.md) | Typography, color, component tokens |
| 04 | [navigation-flow.md](./04-navigation-flow.md) | Routes, guards, transitions |
| 05 | [screen-specification.md](./05-screen-specification.md) | Per-screen responsibilities and states |
| 06 | [component-architecture.md](./06-component-architecture.md) | Reusable components and ownership |
| 07 | [api-integration.md](./07-api-integration.md) | Screen → REST API mapping |
| 08 | [state-management.md](./08-state-management.md) | Server, form, auth, UI state |
| 09 | [error-handling.md](./09-error-handling.md) | Validation and HTTP error UX |
| 10 | [responsive-strategy.md](./10-responsive-strategy.md) | Breakpoints and layout rules |
| 11 | [frontend-folder-structure.md](./11-frontend-folder-structure.md) | Proposed project layout |
| 12 | [frontend-stable-architecture.md](./12-frontend-stable-architecture.md) | **Frozen principles** — start here for implementation |

---

## Frontend Stable Architecture

```text
Pages → Features → API Layer → REST → Backend (opaque)
```

Key frozen principles (detail in doc 12):

- REST client only — no Runtime Engine internals
- Feature isolation — each feature owns pages, hooks, api, components, types
- API ownership — HTTP only in feature API modules
- Editable vs generated views — XML output is read-only

---

## Relationship to Other Docs

| Document | Relationship |
| -------- | ------------ |
| `docs/07-ui-screen-design/` | Legacy wireframes — reference where aligned |
| `docs/06-api-design/` | Authoritative REST contracts |
| `docs/11-implementation-guide/xml-generation.md` | Frozen backend Runtime API |
| `docs/project-development-workflow.md` | Mandatory workflow + frontend principles |

---

## Backend Alignment

- XML Engine v1.0 (complete)
- API Boundary Principle — `xml` + validation `errors` only
- Metadata-driven architecture (ADR-002)
- Template-scoped Preview/Export endpoints

Business features (Saved Inputs, Export History storage) are designed but gated on backend availability.
