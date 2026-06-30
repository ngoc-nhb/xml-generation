# 04. Navigation Flow

---

## 1. Purpose

Define routing, navigation guards, and transition rules between screens.

---

## 2. Route Map

| Route | Screen | Auth | Role |
| ----- | ------ | ---- | ---- |
| `/login` | Login | Public | — |
| `/` | Dashboard | Required | All |
| `/templates` | Template List | Required | Admin |
| `/templates/new` | Create Template | Required | Admin |
| `/templates/:id` | Edit Template Metadata | Required | Admin |
| `/templates/:id/schema` | Schema Editor | Required | Admin |
| `/master-data/types` | Type List | Required | Admin |
| `/master-data/types/:typeId` | Type Detail | Required | Admin |
| `/master-data/types/:typeId/records` | Record List | Required | Admin |
| `/master-data/types/:typeId/records/new` | Create Record | Required | Admin |
| `/master-data/types/:typeId/records/:recordId` | Edit Record | Required | Admin |
| `/generate` | XML Generator | Required | All |
| `/generate/:templateId` | XML Generator (pre-selected) | Required | All |
| `/export-history` | History List | Required | All |
| `/export-history/:id` | History Detail | Required | All |
| `/settings` | Settings | Required | All |
| `/access-denied` | Access Denied | Required | All |
| `*` | Not Found | Optional | All |

---

## 3. Post-Login Redirect

```text
POST /auth/login success
        │
        ▼
Store auth context (token + user claims)
        │
        ▼
Redirect to intended URL or `/`
```

| Role | Default landing |
| ---- | --------------- |
| Admin | `/` (Dashboard) |
| User | `/generate` or `/` (configurable; default `/`) |

---

## 4. Auth Guards

### 4.1 Public routes

Only `/login`. Authenticated users visiting `/login` redirect to `/`.

### 4.2 Protected routes

All app shell routes require valid auth token.

Missing / expired token → redirect to `/login?returnUrl=…`

### 4.3 Role guards

Admin-only routes (`/templates`, `/master-data/*`):

- Non-admin → redirect to `/access-denied` or `/` with toast
- Do not render admin pages even if URL typed manually

Role source: login response `isAdmin` (or equivalent claim). Re-validated on 403 from API.

---

## 5. Unsaved Changes Guard

Apply to screens with editable forms:

- Template metadata edit
- Schema editor
- Master data editors
- XML Generator workspace

```text
User navigates away
        │
        ▼
Form dirty?
   No ──► allow navigation
   Yes ──► Confirm dialog
              ├── Stay
              └── Leave (discard)
```

Saved Draft is the only guaranteed persistence across refresh. Route guard protects accidental navigation loss only.

---

## 6. XML Generator — Template Switch Flow

```text
User selects different template
        │
        ▼
Current form dirty?
   No ──► load new template schema + optional draft
   Yes ──► Confirm discard
              └── load new template
```

After template load:

1. `GET /templates/{id}` for schema metadata
2. Build dynamic form client-side from schema
3. (Future) `GET /saved-inputs/{templateId}` for draft

---

## 7. Preview / Export Flow (in-page)

Preview and Export do **not** navigate away. They update panels within `/generate`:

```text
User clicks Preview
        │
        ▼
POST /templates/{id}/preview
        │
        ├── success → update XML Viewer panel
        └── validation errors → inline field errors + summary
```

Same pattern for Export with `POST /templates/{id}/export`.

---

## 8. Deep Linking

| Deep link | Behavior |
| --------- | -------- |
| `/templates/10/schema` | Open schema editor for template 10 |
| `/generate/10` | Open generator with template 10 selected |
| `/export-history/101` | Open history detail |

Invalid resource ID → Not Found screen after API 404.

---

## 9. Error Navigation

| Condition | Navigation |
| --------- | ---------- |
| 401 Unauthorized | Clear auth → `/login` |
| 403 Forbidden | `/access-denied` |
| 404 Not Found | Inline error or `/404` |
| 500 / network | Toast + retry; no sensitive details |

---

## 10. Sidebar Active State

Highlight nav item matching longest matching route prefix:

- `/templates/5/schema` → **Templates** active
- `/master-data/types/3/records` → **Master Data** active

---

## 11. Related Documents

- `02-information-architecture.md` — sitemap
- `05-screen-specification.md` — screen details
- `09-error-handling.md` — error UX
