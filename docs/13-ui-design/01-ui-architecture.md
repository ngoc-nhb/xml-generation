# 01. UI Architecture

---

## 1. Purpose

This document defines the frontend architecture for the XML Generation System.

It aligns the UI with:

- Metadata-driven backend design (ADR-002)
- Frozen XML Engine architecture (v1.0)
- Application Layer REST boundaries
- Approved API contracts in `docs/06-api-design/`

This phase produces design documentation only. No implementation.

---

## 2. Architectural Position

```text
Browser (SPA)
        │
        ▼
REST API Layer          ← only boundary the frontend knows
        │
        ▼
Application Services
        │
        ▼
Runtime Engine          ← invisible to frontend
```

The frontend treats the backend as a **business API**. It must never depend on:

- `RuntimeTemplate`
- `RuntimeExecutionTree`
- `compiled_schema_json`
- `RuntimeExecutionRequest` / `RuntimeExecutionResult`
- Database schema or JPA entities

---

## 3. Core Principles

### 3.1 Backend-Driven Behavior

All business rules, validation, and XML generation logic live on the backend.

The UI is responsible for:

- Rendering metadata returned by APIs
- Collecting user input
- Displaying API responses and errors
- Navigation and layout
- Client-side UX validation only (format, required-before-submit)

The UI must not re-implement backend validation rules or hardcode field names for generation logic.

### 3.2 Metadata-Driven UI

Dynamic forms, field labels, occurrence rules, and master-data selectors are rendered from **Template schema** and **Master Data Type schema** returned by REST APIs.

The UI renders metadata; it does not encode template structure.

### 3.3 API Boundary Principle (Frontend View)

Runtime-facing endpoints expose stable outputs only:

| Success | `data.xml` |
| Failure | `errors[]` with `field` + `code` |

The UI displays XML and translates error codes to user-facing messages. It never expects execution trees or internal runtime models.

### 3.4 Feature Module Alignment

Frontend features mirror **business modules**, not backend Java packages literally:

| Frontend feature | Backend domain |
| ---------------- | -------------- |
| Auth | Authentication |
| Templates | Template module |
| Master Data | Master Data module |
| XML Generation | Preview / Export REST APIs |
| Export History | Export History module (future persistence) |
| Settings | Session / account display only |

### 3.5 Engine vs Business Features

**Core Engine (complete — backend v1.0):** Template schema, Preview, Export runtime APIs.

**Business Features (UI may design now, backend partially pending):** Saved Inputs, Export History file storage, download, versioning UI.

UI designs for business features must compose on top of public REST APIs and degrade gracefully when endpoints are not yet implemented.

---

## 4. Technology Direction (Design Only)

Recommended stack for Phase 6+ implementation (not implemented in Phase 6.0):

| Concern | Recommendation |
| ------- | -------------- |
| Framework | React 18+ |
| Language | TypeScript |
| Build | Vite |
| Routing | React Router |
| Server state | TanStack Query |
| Forms | React Hook Form |
| HTTP | Fetch or Axios with shared API client |
| Styling | Design tokens + component library (see `03-design-system.md`) |

Final toolchain selection requires implementation-phase approval if it diverges.

---

## 5. Application Shell

All authenticated routes share one **App Shell**:

```text
┌─────────────────────────────────────────────────────────┐
│ Header (app name, user, logout)                        │
├──────────┬──────────────────────────────────────────────┤
│ Sidebar  │ Main Content                                 │
│ (nav)    │ (page outlet)                                │
│          │                                              │
└──────────┴──────────────────────────────────────────────┘
```

Shell responsibilities:

- Role-based navigation visibility
- Active route highlighting
- Global loading / toast region
- Auth guard for protected routes

---

## 6. Layered Frontend Architecture

```text
Pages           Route-level screens, compose features
    ↓
Features        Domain UI flows (templates, xml-generation, …)
    ↓
Components      Reusable UI (tables, forms, dialogs, xml-viewer)
    ↓
API Client      Typed REST wrappers, error normalization
    ↓
Shared          Types, hooks, utils, layouts, design tokens
```

**Ownership rules:**

- Pages orchestrate layout and navigation; minimal logic.
- Features own domain-specific forms and workflows.
- Components are presentational and reusable; no direct API calls except through hooks.
- API client is the only layer that performs HTTP requests.

---

## 7. Conflicts Resolved (Legacy UI Docs)

The following legacy references in `docs/07-ui-screen-design/` are **superseded** by approved backend architecture:

| Legacy UI concept | Resolution |
| ----------------- | ---------- |
| Standalone **Compile Template** screen / button | **Rejected.** Compilation runs inline on schema save via `PUT /templates/{id}/schema` or create-with-schema. No `POST /templates/{id}/compile`. |
| `POST /api/v1/xml/preview` body with `templateId` | **Updated.** Use `POST /api/v1/templates/{id}/preview`. |
| `POST /api/v1/xml/export` | **Updated.** Use `POST /api/v1/templates/{id}/export`. |
| Export always creates file + history | **Phased.** MVP Export returns `data.xml` in JSON. File download and history UI activate when storage APIs ship. |

Phase 6.0 design follows backend v1.0 contracts.

---

## 8. Non-Goals (Phase 6.0)

- React / HTML / CSS implementation
- Pixel-perfect mockups
- i18n implementation (design hooks only)
- Dark mode (Light Mode only per requirements)
- Mobile-first layout (desktop-first per requirements)

---

## 9. Related Documents

| Document | Purpose |
| -------- | ------- |
| `02-information-architecture.md` | Sitemap and user journeys |
| `07-api-integration.md` | Screen-to-API mapping |
| `docs/07-ui-screen-design/` | Legacy screen-level wireframes (reference only where aligned) |
| `docs/11-implementation-guide/xml-generation.md` | Frozen Runtime API contract |
| `docs/project-development-workflow.md` | Mandatory development workflow |
