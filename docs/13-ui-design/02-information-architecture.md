# 02. Information Architecture

---

## 1. Purpose

Define the sitemap, page hierarchy, and primary user journeys for the XML Generation System frontend.

---

## 2. Top-Level Sitemap

```text
Login (public)
    │
    └── App Shell (authenticated)
            │
            ├── Workspace Selector (header — Phase 7.1.1+)
            │       └── scopes all features below
            │
            ├── Dashboard
            │
            ├── Templates (Admin)
            │       ├── Template List
            │       ├── Create Template
            │       ├── Edit Template Metadata
            │       └── Template Schema Editor
            │
            ├── Master Data (Admin)
            │       ├── Type List
            │       ├── Type Detail / Schema Editor
            │       └── Record List / Record Editor
            │
            ├── XML Generation (Admin + User)
            │       └── Generator Workspace
            │
            ├── Export History (Admin + User)
            │       ├── History List
            │       └── History Detail (+ Download when available)
            │
            └── Settings (Admin + User)
                    └── Account & Session
```

---

## 3. Page Hierarchy

| Level | Page | Route (proposed) | Role |
| ----- | ---- | ---------------- | ---- |
| L0 | Login | `/login` | Public |
| L1 | Dashboard | `/` | All |
| L2 | Template List | `/templates` | Admin |
| L2 | Create Template | `/templates/new` | Admin |
| L2 | Edit Template | `/templates/:id` | Admin |
| L2 | Schema Editor | `/templates/:id/schema` | Admin |
| L2 | Master Data Types | `/master-data/types` | Admin |
| L2 | Master Data Type | `/master-data/types/:typeId` | Admin |
| L2 | Master Data Records | `/master-data/types/:typeId/records` | Admin |
| L2 | XML Generator | `/generate` | All |
| L2 | Export History List | `/export-history` | All |
| L2 | Export History Detail | `/export-history/:id` | All |
| L2 | Settings | `/settings` | All |
| L3 | Access Denied | `/access-denied` | All |
| L3 | Not Found | `*` | All |

---

## 4. Navigation Model

### Primary sidebar (role-filtered)

| Item | Admin | User | Destination |
| ---- | :---: | :--: | ----------- |
| Dashboard | ✅ | ✅ | `/` |
| Templates | ✅ | hidden | `/templates` |
| Master Data | ✅ | hidden | `/master-data/types` |
| XML Generation | ✅ | ✅ | `/generate` |
| Export History | ✅ | ✅ | `/export-history` |
| Settings | ✅ | ✅ | `/settings` |

Backend authorization remains authoritative. Hidden nav items must not be the only access control.

### Workspace context (Phase 7.1.1+)

| Rule | Behavior |
| ---- | -------- |
| Single workspace | Auto-select; selector hidden or read-only |
| Multiple workspaces | Header dropdown; switching invalidates TanStack Query caches |
| API calls | All collection requests include `workspaceId` from context |
| Deep links | Optional future: `/w/:workspaceId/...` — not required for 7.1.1 |

See [Phase 7.1.0 Workspace Architecture](../release/phase-7.1.0-workspace-architecture.md) §7.

### Breadcrumbs (management screens)

Examples:

- `Templates > LIVE_GAME > Schema Editor`
- `Master Data > Game Kind > Records`
- `Export History > #101`

---

## 5. User Journeys

### 5.1 Administrator — Configure Template and Generate XML

```text
Login
  → Dashboard
  → Templates → Create Template (metadata + optional initial schema)
  → Schema Editor (define fields, mappings)
  → Save Schema (triggers backend compilation)
  → XML Generation
  → Select template → Enter input → Preview → Export
  → Export History (when persistence available)
```

### 5.2 Administrator — Manage Master Data

```text
Login
  → Master Data → Type List
  → Create / Edit Type Schema
  → Manage Records for Type
  → Return to XML Generation with updated reference data
```

### 5.3 User — Generate XML (no admin screens)

```text
Login
  → Dashboard or XML Generation
  → Select active template
  → Enter input + master data selections
  → (Optional) Save Draft — when Saved Input API available
  → Preview XML
  → Export XML
  → View Export History
```

### 5.4 User — Resume Saved Draft (future)

```text
XML Generation
  → Select template
  → Load Draft (GET saved-inputs/{templateId})
  → Edit → Preview → Export
```

Draft load uses Saved Input APIs only. Client-side form cache is not a substitute.

### 5.5 Either Role — Session End

```text
Settings or Header
  → Logout (POST /auth/logout)
  → Login
```

---

## 6. Content Grouping Rationale

| Area | Why separate |
| ---- | ------------ |
| **Dashboard** | Entry hub; role-specific shortcuts without mixing config and generation |
| **Templates** | Admin metadata + schema editing; infrequent, high complexity |
| **Master Data** | Admin reference data; distinct lifecycle from templates |
| **XML Generation** | Primary user workflow; single focused workspace |
| **Export History** | Read-only audit trail; separate from live generation |
| **Settings** | Session/account utilities; no business configuration |

---

## 7. Terminology (UI ↔ API)

| UI label | API / domain term |
| -------- | ----------------- |
| Template | `Template` |
| Schema Editor | `TemplateField` + `TemplateMapping` via schema API |
| Master Data Type | `MasterDataType` |
| Master Data Record | `MasterDataRecord` |
| Save Draft | Saved Input (`saved-inputs`) |
| Preview | Preview REST API |
| Export | Export REST API |
| Export History | `ExportHistory` |

Avoid exposing internal terms: *compile artifact*, *runtime model*, *execution tree*.

---

## 8. Phase Alignment

| IA area | Backend status |
| ------- | -------------- |
| Templates CRUD + schema | ✅ Implemented |
| Preview / Export runtime | ✅ Implemented |
| Saved Inputs | Designed; UI designed; API integration when ready |
| Export History + download | Designed; UI designed; API integration when ready |
| Settings | Client-only + logout API |

---

## 9. Related Documents

- `04-navigation-flow.md` — routing and guards
- `05-screen-specification.md` — per-screen detail
- `07-api-integration.md` — endpoint mapping
