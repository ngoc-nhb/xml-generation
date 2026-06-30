# 12. Frontend Stable Architecture

---

## 1. Status

Phase 6.0 completes frontend architecture design. The following stack is **frozen**
as of Phase 6.0 approval.

Frontend implementation may begin. Changes to these principles require architecture
review and explicit approval.

---

## 2. Stable Stack

```text
Pages
        │
        ▼
Features
        │
        ▼
API Layer
        │
        ▼
REST
        │
        ▼
Backend (opaque)
```

Everything below REST is **opaque** to the frontend. No component, hook, or page may
understand backend implementation details (Runtime Engine, JPA, compile artifacts,
execution artifacts).

---

## 3. Frontend Architecture Principle

The frontend is a **REST client only**.

It must not depend on:

- `RuntimeTemplate`
- `RuntimeExecutionTree`
- `compiled_schema_json`
- `RuntimeExecutionRequest` / `RuntimeExecutionResult`

UI behavior is metadata-driven from REST responses. Business rules remain on the backend.

---

## 4. Feature Isolation Principle

Each business feature is a self-contained module under `features/`.

```text
features/
├── auth/
├── templates/
├── master-data/
├── xml-generation/
├── export-history/
└── settings/
```

### Each feature owns

| Concern | Location (within feature) |
| ------- | ------------------------- |
| Pages | Route-level screen compositions |
| Hooks | Data fetching, mutations, form logic |
| API | HTTP calls for that domain |
| Components | Feature-private UI |
| Types | Feature-specific TypeScript types |

### Rules

- Shared `components/` must remain **generic** and domain-agnostic.
- Avoid large global folders that mix unrelated business modules.
- Each feature should be **independently removable** without breaking unrelated features.
- Features must not import from sibling features directly; share via `components/`, `hooks/`, `types/api/`, or the sibling feature's **`index.ts` public API** only.

### Feature Public API

Each mature feature exposes a public surface through `features/<name>/index.ts`:

| Export | Purpose |
| ------ | ------- |
| Pages | Route-level screen components |
| Public hooks | Server-state access for cross-feature needs (e.g. template picker) |
| Public types | REST-aligned types needed outside the feature |

Internal modules (`api/`, `utils/`, private components, internal hooks) must not be
imported across feature boundaries.

Example: `features/templates/index.ts` — validated in Phase 6.2.
Example: `features/master-data/index.ts` — validated in Phase 6.3.
Example: `features/xml-generation/index.ts` — validated in Phase 6.4.

### 4.1 Cross-Feature Integration Principle

Features communicate only through each other's **`index.ts` public API** — never through
internal `api/`, `utils/`, or private components.

| Allowed | Forbidden |
| ------- | --------- |
| `import { useMasterDataFieldPickerOptions } from '@/features/master-data'` | `import … from '@/features/master-data/api/…'` |
| Public hooks → feature API → REST | Internal utils, components, or internal hooks |

Validated Phase 6.3.5: `features/templates/components/MasterDataFieldPicker` uses Master
Data public hooks only.

---

## 5. API Ownership Principle

HTTP access follows a strict chain:

```text
Screen / Hook
        │
        ▼
Feature API module
        │
        ▼
Shared apiClient
        │
        ▼
REST
```

### Allowed

```text
usePreview() → xml-generation.api.ts → apiClient → POST /templates/{id}/preview
```

### Forbidden

```text
Component → axios
Dialog → fetch()
Page → inline HTTP
```

All HTTP must remain inside **feature API modules** (or the shared `api/client.ts` wrapper).

This keeps OpenAPI generation, SDK replacement, and auth header injection in one place.

---

## 6. Editable vs Generated UI Principle

Not every screen behaves the same. The UI distinguishes two view categories:

| Category | Purpose | Examples | Editable? |
| -------- | ------- | -------- | --------- |
| **Editable views** | User configures or enters data | Template metadata, schema editor, dynamic input form, master data records | Yes |
| **Generated views** | System output for inspection | XML Preview panel, Export result viewer | **Never** |

### Backend analogy

| Backend | Frontend view type |
| ------- | ------------------ |
| Metadata (editable template definition) | Editable views |
| Compile artifact (`compiled_schema_json`) | Invisible — never rendered |
| Execution artifact (`RuntimeExecutionTree`) | Invisible — never rendered |
| Generated XML (`data.xml`) | Generated view — read-only |

Generated views use `XmlViewer` (or equivalent). They must not become editable text
areas, form fields, or schema editors. Copy and download-from-string are allowed; mutation
is not.

Preview and Export both produce **generated** output even though Export may later
trigger persistence on the backend.

### 6.1 Schema Editor Boundary (frozen)

The Template Schema Editor is a **metadata editor only**. It must never:

- execute preview or export
- validate runtime input values
- depend on `RuntimeExecutionTree` or `compiled_schema_json`

It edits fields, hierarchy, and mappings; persistence is
`PUT /templates/{id}/schema`. Execution belongs exclusively to
`features/xml-generation/`.

Schema tree components (`SchemaEditor`, `SchemaFieldTree`, etc.) remain inside
`features/templates/`. Do not extract to shared components until Rule of Three applies
across features.

See `06-component-architecture.md` §12.

### 6.2 Dynamic Record Model (frozen)

`DynamicRecordForm` in `features/master-data/` edits **Master Data Records** from field
metadata. It must never:

- execute preview or export
- participate in XML generation or runtime execution
- edit template schema or mappings

Its only responsibility is editing master data record payloads via Master Data REST APIs.

Do not move `DynamicRecordForm` outside `features/master-data/` until Rule of Three
applies across features.

See `06-component-architecture.md` §15.

### 6.3 Execution Session (frozen)

During one execution session on the XML Generation screen, preserve:

- Selected template, master data selections, input JSON
- Latest XML output and validation errors

Do not auto-reset after Preview or Export. Reset on template change or explicit user Reset.

Implemented in `features/xml-generation/components/ExecutionPanel`.

### 6.4 Execution Screen Ownership (frozen)

Execution state lives in the execution feature (`ExecutionPanel` local state + mutations).
It must not move to global context. Future execution UIs follow the same pattern within
their owning feature.

### 6.5 Backend Single Source of Truth (frozen)

Frontend orchestrates REST only. Runtime validation, value resolution, mapping resolution,
and XML serialization remain on the backend. Frontend may validate JSON syntax and present
errors from `errors[]` — nothing more.

---

## 7. State Ownership (Summary)

| State type | Owner |
| ---------- | ----- |
| Server state | TanStack Query via feature hooks |
| Auth state | Auth provider |
| Form state | Feature hooks / React Hook Form |
| Execution session state | Owning execution feature (e.g. `ExecutionPanel` local state) |
| UI chrome | Local component state |

Do not mix categories. See `08-state-management.md`.

---

## 8. Runtime API Contract (Frontend View)

Preview and Export share the same request shape and validation envelope. Only downstream
business output differs (MVP: both return `{ xml }` on success).

| | Preview | Export |
| --- | --- | --- |
| Endpoint | `POST /templates/{id}/preview` | `POST /templates/{id}/export` |
| Request | `{ inputData, selectedMasterData }` | Same |
| Failure | `success: false`, `errors[]` | Same |

Preview and Export REST responses expose only:

- Success: `data.xml`
- Failure: `errors[]`

The frontend never expects execution trees or runtime debug payloads in the default
contract. See `docs/11-implementation-guide/xml-generation.md` §16.

---

## 9. Engine vs Business Features (Frontend View)

| Category | Frontend modules |
| -------- | ---------------- |
| **Core (frozen)** | auth, templates (admin), xml-generation (preview/export) |
| **Business (phased)** | export-history, saved-inputs integration in xml-generation, settings extensions |

Business modules may be feature-flagged until backend endpoints exist.

---

## 10. Stable Feature Matrix

| Feature | Public API | MVP scope | Status |
| ------- | ---------- | --------- | ------ |
| Auth | Session via providers | Login, logout | ✅ Approved |
| Templates | `features/templates/index.ts` | CRUD, schema editor | ✅ Approved |
| Master Data | `features/master-data/index.ts` | Types, fields, records | ✅ Approved |
| XML Generation | `features/xml-generation/index.ts` | Preview, export orchestration | ✅ Approved |
| Export History | — | Not started | Pending |
| Settings | — | Placeholder | Pending |
| Dashboard | — | Placeholder | Pending |

MVP business flow (admin + user): configure templates and master data → generate XML via
`/xml-generation`.

---

## 11. Transition to Implementation

| Phase | Status |
| ----- | ------ |
| Backend XML Engine | ✅ Frozen (v1.0) |
| Frontend architecture | ✅ Frozen (Phase 6.0) |
| Frontend foundation (6.1) | ✅ Approved |
| Template module (6.2) | ✅ Approved |
| Master Data module (6.3) | ✅ Approved |
| Template ↔ Master Data (6.3.5) | ✅ Approved |
| XML Generation (6.4) | ✅ Approved |

Implementation follows `docs/project-development-workflow.md` — architecture review
before code, build + test, implementation review.

---

## 12. Related Documents

- `01-ui-architecture.md`
- `06-component-architecture.md`
- `07-api-integration.md`
- `11-frontend-folder-structure.md`
- `docs/project-development-workflow.md`
