# 09. Error Handling

---

## 1. Purpose

Define how the frontend handles API errors, validation failures, and unexpected failures.

Aligns with `docs/06-api-design/p8_error-model.md`.

---

## 2. Error Sources

| Source | Example |
| ------ | ------- |
| HTTP transport | Network offline, timeout |
| API envelope | `{ success: false, errors: [...] }` |
| HTTP status | 401, 403, 404, 500 |
| Client validation | Required field empty before submit |
| Unexpected | Unhandled exception in UI |

---

## 3. API Error Model (Frontend View)

```typescript
interface ApiError {
  field?: string | null;
  code: string;
}
```

UI maintains a **code → message** map (English for Phase 1; i18n-ready structure).

Backend never returns localized messages.

---

## 4. Error Categories & UX

| Category | HTTP | UI behavior |
| -------- | ---- | ----------- |
| **Validation (runtime)** | 200 + `success: false` | Map `errors[]` to fields; show `ValidationSummary` |
| **Request validation** | 400 | Toast + field errors if present |
| **Unauthorized** | 401 | Clear auth → redirect login |
| **Forbidden** | 403 | Access denied page or toast |
| **Not found** | 404 | Inline message or Not Found page |
| **Conflict** | 409 | Toast (e.g. duplicate code) |
| **Server error** | 500 | Generic toast; log internally |
| **Network** | — | "Unable to connect" + retry |

---

## 5. XML Generation Validation Flow

Preview and Export return validation errors without throwing HTTP errors:

```text
POST preview/export
        │
        ▼
success === false ?
   Yes ──► Map errors[].field → DynamicForm paths
           Show summary banner
           Do NOT update XmlViewer
   No  ──► Display data.xml in XmlViewer
```

**Field mapping:** API returns logical `fieldName` from template metadata. Dynamic form registers fields by same names.

Unknown field codes → show in summary list under field name or code.

---

## 6. Form Validation Layers

| Layer | Responsibility |
| ----- | -------------- |
| Client UX | Required, format, numeric range before submit |
| Server | Authoritative business validation |

Client validation reduces round trips; server always wins on conflict.

---

## 7. Global Error Boundary

React Error Boundary at app root:

- Catches render errors
- Shows Unexpected Error page
- No stack trace to user
- Optional error reporting hook (future)

---

## 8. Toast vs Inline

| Situation | Pattern |
| --------- | ------- |
| Save success | Toast |
| Delete success | Toast |
| Preview validation | Inline + summary |
| List load failure | Inline alert in table area |
| Login failure | Inline on form |
| 403 on action | Toast + stay on page |

---

## 9. Retry Strategy

| Operation | Retry |
| --------- | ----- |
| GET list | Query automatic retry ×1 |
| Preview / Export | Manual retry via button |
| Save | User clicks Save again |
| Login | User resubmits |

No auto-retry on mutations except explicit user action.

---

## 10. Loading + Error Combined

While mutation pending:

- Disable Preview / Export buttons
- Show spinner on active button
- Ignore duplicate clicks

On failure:

- Re-enable buttons
- Preserve form input (never clear user data on validation failure)

---

## 11. Sensitive Data

Never display in UI:

- JWT payload details
- Stack traces
- SQL or internal error messages
- Runtime engine artifacts

---

## 12. Related Documents

- `docs/06-api-design/p8_error-model.md`
- `05-screen-specification.md` — SCR-09 error states
- `07-api-integration.md`
