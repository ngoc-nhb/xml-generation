# 05. Screen Specification

---

## 1. Purpose

Define purpose, responsibilities, components, APIs, state, and UX states for every screen.

Template for each screen:

- Purpose
- Responsibilities
- Components
- APIs used
- State owned
- Navigation
- Loading / Empty / Error / Success

---

## SCR-01 Login

**Purpose:** Authenticate user and establish session.

**Responsibilities:** Collect credentials; call login API; store auth; redirect.

**Components:** `AuthLayout`, `LoginForm`, `Button`, `Input`, `Alert`

**APIs:** `POST /api/v1/auth/login`

**State owned:**

| State | Owner |
| ----- | ----- |
| Username/password fields | Local form state |
| Submit loading | Local UI state |
| Auth token + user | Auth context (global) |

**Navigation:** Success → `/` or `returnUrl`; already logged in → redirect away.

| State | UX |
| ----- | -- |
| Loading | Disable form, button spinner |
| Error | Show translated error from `errors[].code` (e.g. invalid credentials) |
| Success | Redirect |
| Empty | N/A |

---

## SCR-02 Dashboard

**Purpose:** Landing hub after login with role-appropriate shortcuts.

**Responsibilities:** Show welcome; quick links; optional summary counts (future).

**Components:** `AppShell`, `PageHeader`, `QuickLinkCard`, `StatCard` (future)

**APIs:** None required for MVP dashboard; optional `GET /templates` count for admin.

**State owned:** None persistent; read-only view.

**Navigation:** Cards link to Templates, Master Data, Generate, Export History.

| State | UX |
| ----- | -- |
| Loading | Skeleton cards |
| Empty | Welcome message + guided CTAs by role |
| Error | Toast if optional stats fail |
| Success | Render links |

---

## SCR-03 Template List

**Purpose:** Browse, search, create, open, delete templates.

**Responsibilities:** Paginated list; search; navigate to create/edit/schema; delete with confirm.

**Components:** `AppShell`, `DataTable`, `SearchInput`, `Pagination`, `Button`, `ConfirmDialog`, `StatusBadge`

**APIs:**

- `GET /api/v1/templates?page&pageSize&keyword&status`
- `DELETE /api/v1/templates/{id}`

**State owned:**

| State | Owner |
| ----- | ----- |
| Search keyword, page | URL search params or local list state |
| Table data | Server state (TanStack Query) |

**Navigation:** Create → `/templates/new`; Edit → `/templates/:id`; Schema → `/templates/:id/schema`

**Note:** No standalone Compile action. Compilation status inferred from successful schema save (backend); optional "last compiled" indicator when API exposes it.

| State | UX |
| ----- | -- |
| Loading | Table skeleton |
| Empty | "No templates" + Create CTA |
| Error | Toast + retry |
| Success | Populated table |

---

## SCR-04 Create / Edit Template Metadata

**Purpose:** Create or update template code, name, description, status.

**Responsibilities:** Metadata form; optional initial schema on create; save.

**Components:** `AppShell`, `Form`, `Input`, `Select`, `Breadcrumb`, `UnsavedChangesGuard`

**APIs:**

- `POST /api/v1/templates` (create, optional schema)
- `GET /api/v1/templates/{id}` (edit)
- `PUT /api/v1/templates/{id}` (metadata only — does not recompile)

**State owned:** Form state (react-hook-form); dirty flag for route guard.

**Navigation:** Save success → `/templates/:id/schema` or list; Cancel → back with guard.

| State | UX |
| ----- | -- |
| Loading | Form skeleton on edit |
| Error | Field errors from API |
| Success | Toast + navigate |

---

## SCR-05 Template Schema Editor

**Purpose:** Define fields, hierarchy, mappings; save triggers backend compilation.

**Responsibilities:** Edit field tree; mapping configuration; validate structure client-side for UX; save schema.

**Components:** `AppShell`, `SchemaTreeEditor`, `FieldFormPanel`, `MappingEditor`, `Toolbar`, `ConfirmDialog`

**APIs:**

- `GET /api/v1/templates/{id}` (load schema)
- `PUT /api/v1/templates/{id}/schema` (save + compile)

**State owned:**

| State | Owner |
| ----- | ----- |
| Schema draft | Local form / editor state |
| Selected field | Local UI state |
| Save loading | Local UI state |

**Navigation:** Back to template detail or list with unsaved guard.

| State | UX |
| ----- | -- |
| Loading | Editor skeleton |
| Empty | Starter field or guided empty schema |
| Error | Inline + toast on save failure |
| Success | Toast "Schema saved"; schema persisted + compiled server-side |

**Boundary (frozen):** Metadata editor only. No preview, export, runtime validation, or
engine internals. See `06-component-architecture.md` §12 and
`12-frontend-stable-architecture.md` §6.1.

---

## SCR-06 Master Data Type List

**Purpose:** Browse and manage master data types.

**Components:** `DataTable`, `SearchInput`, `Pagination`, `Button`

**APIs:** `GET /api/v1/master-data-types`

**State owned:** Pagination, search — server state + URL params.

**Navigation:** Row → type detail; Create → new type flow.

| State | UX |
| ----- | -- |
| Empty | CTA to create first type |
| Loading / Error | Standard patterns |

---

## SCR-07 Master Data Type Detail / Schema

**Purpose:** Edit type metadata and field schema.

**APIs:**

- `GET /api/v1/master-data-types/{id}`
- `POST /api/v1/master-data-types`
- `PUT /api/v1/master-data-types/{id}`
- `PUT /api/v1/master-data-types/{id}/schema`
- `DELETE /api/v1/master-data-types/{id}`

**State owned:** Form + schema editor state.

**Navigation:** Link to records list for type.

---

## SCR-08 Master Data Record List / Editor

**Purpose:** CRUD records for a master data type.

**APIs:**

- `GET /api/v1/master-data-types/{id}/records`
- `POST /api/v1/master-data-records`
- `PUT /api/v1/master-data-records/{id}`
- `DELETE /api/v1/master-data-records/{id}`

**State owned:** Record form from type schema (dynamic fields).

**Navigation:** Breadcrumb from type list.

---

## SCR-09 XML Generator Workspace

**Purpose:** Primary end-user screen — select template, enter data, preview, export.

**Responsibilities:**

- Template selection
- Dynamic input form from template schema
- Master data selectors from mappings + record APIs
- Preview XML in viewer panel
- Export XML (MVP: display/download client-side from `data.xml`)
- Save / load draft when Saved Input API available

**Components:**

- `TemplateSelector`
- `DynamicForm` / `RepeatableGroupField`
- `MasterDataSelector`
- `ActionBar` (Save Draft, Preview, Export)
- `XmlViewer`
- `ValidationSummary`

**APIs (implemented):**

- `GET /api/v1/templates` (selector list — active templates)
- `GET /api/v1/templates/{id}` (schema for form)
- `POST /api/v1/templates/{id}/preview`
- `POST /api/v1/templates/{id}/export`

**APIs (future):**

- `GET /api/v1/saved-inputs/{templateId}`
- `PUT /api/v1/saved-inputs/{templateId}`
- `DELETE /api/v1/saved-inputs/{templateId}`
- Master data record lookups per mapping

**State owned:**

| State | Owner |
| ----- | ----- |
| Selected template ID | URL or feature state |
| Input JSON | Local form state |
| Selected master data | Local form state |
| Preview XML result | Server state (preview mutation) |
| Export result | Server state (export mutation) |
| Dirty flag | Local — route guard |

**Navigation:** Template switch in-page; sidebar to other modules with guard.

| State | UX |
| ----- | -- |
| Loading | Form skeleton while schema loads |
| Empty | Prompt to select template |
| Preview success | XML Viewer shows `data.xml` |
| Validation failure | Inline field errors from `errors[]`; no XML panel update |
| Export success (MVP) | Show XML + offer Copy / client download |
| Export success (future) | Toast + link to Export History |
| Error | Toast for infrastructure errors |

---

## SCR-10 Export History List

**Purpose:** Browse past exports (when backend persistence available).

**APIs:** `GET /api/v1/export-histories?page&pageSize`

**Components:** `DataTable`, `Pagination`, `DateDisplay`

**State owned:** Server state (query).

**Navigation:** Row → detail.

| State | UX |
| ----- | -- |
| Empty | "No exports yet" + link to Generate |
| MVP backend missing | Feature flag / "Coming soon" or hide nav until API ready |

---

## SCR-11 Export History Detail

**Purpose:** View export metadata and download file.

**APIs:**

- `GET /api/v1/export-histories/{id}`
- `GET /api/v1/export-histories/{id}/download`

**Components:** `DetailPanel`, `XmlViewer` (optional preview), `DownloadButton`

**Navigation:** Back to list.

---

## SCR-12 Settings

**Purpose:** Display session info and logout.

**Responsibilities:** Show username, role; logout; app version (static).

**Components:** `DescriptionList`, `Button`

**APIs:** `POST /api/v1/auth/logout`

**State owned:** Read auth context only.

**Navigation:** Logout → `/login`

| State | UX |
| ----- | -- |
| Success logout | Clear auth, redirect |

---

## SCR-13 Access Denied / Not Found

**Purpose:** Explain authorization or routing failures.

**Components:** `EmptyState`, `Button` (Go to Dashboard)

**APIs:** None

---

## 2. Screen Index

| ID | Screen | Doc section |
| -- | ------ | ----------- |
| SCR-01 | Login | §SCR-01 |
| SCR-02 | Dashboard | §SCR-02 |
| SCR-03 | Template List | §SCR-03 |
| SCR-04 | Template Metadata | §SCR-04 |
| SCR-05 | Schema Editor | §SCR-05 |
| SCR-06–08 | Master Data | §SCR-06–08 |
| SCR-09 | XML Generator | §SCR-09 |
| SCR-10–11 | Export History | §SCR-10–11 |
| SCR-12 | Settings | §SCR-12 |
| SCR-13 | Error pages | §SCR-13 |

---

## 3. Related Documents

- `06-component-architecture.md`
- `07-api-integration.md`
- `08-state-management.md`
