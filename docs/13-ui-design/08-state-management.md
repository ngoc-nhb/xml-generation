# 08. State Management

---

## 1. Purpose

Define how frontend state is categorized, owned, and updated.

**Principle:** Do not mix server state, auth state, UI state, and form state in one store.

---

## 2. State Categories

```text
┌─────────────────────────────────────────────────────────┐
│ Authentication State     │ Global, persisted (session)   │
├──────────────────────────┼──────────────────────────────┤
│ Server State             │ TanStack Query cache        │
├──────────────────────────┼──────────────────────────────┤
│ Temporary Form State     │ react-hook-form / useState  │
├──────────────────────────┼──────────────────────────────┤
│ Client UI State          │ Local component state       │
└─────────────────────────────────────────────────────────┘
```

---

## 3. Authentication State

**Owner:** `AuthProvider` (React Context) or lightweight store (Zustand).

| Field | Source |
| ----- | ------ |
| `accessToken` | Login response |
| `userId` | Login response |
| `username` | Login response |
| `isAdmin` | Login response |

**Persistence:** `sessionStorage` (session tab) — no "Remember Me" in MVP.

**Lifecycle:**

- Login → set auth
- Logout / 401 → clear auth
- App boot → restore token if present; optional validate via protected ping

**Must not store:** password, runtime models, compiled schema.

---

## 4. Server State

**Owner:** TanStack Query (`useQuery`, `useMutation`).

| Resource | Hook example |
| -------- | ------------ |
| Template list | `useTemplates(params)` |
| Template detail | `useTemplate(id)` |
| Master data types | `useMasterDataTypes()` |
| Records | `useMasterDataRecords(typeId)` |
| Preview | `usePreviewMutation()` |
| Export | `useExportMutation()` |
| Export history | `useExportHistories()` — future |

**Rules:**

- Queries represent GET operations
- Mutations represent POST/PUT/DELETE
- Optimistic updates only where safe (not for Preview/Export)
- Invalidate related queries after mutations

---

## 5. Temporary Form State

**Owner:** React Hook Form or controlled `useState` in feature components.

| Screen | Form state |
| ------ | ---------- |
| Login | username, password |
| Template metadata | code, name, description, status |
| Schema editor | fields[], mappings[] |
| Master data record | dynamic JSON fields |
| XML Generator | `inputData`, `selectedMasterData` object trees |

**Dirty tracking:** `formState.isDirty` drives unsaved route guards.

**Submit:** Serialize to API request DTO shape — not internal engine format.

---

## 6. Client UI State

**Owner:** Local `useState` / `useReducer` in components.

Examples:

- Sidebar collapsed
- Active schema tree node
- XML viewer wrap mode
- Open dialog IDs
- Toast queue
- Table column visibility (future)

**Must not persist** business data the user expects to recover — use Saved Draft API.

---

## 7. XML Generator State Model

```text
selectedTemplateId          (URL param or feature state)
        │
        ▼
templateSchema              (server query — GET template)
        │
        ▼
formValues                  (local — inputData shape)
masterDataSelections        (local — selectedMasterData shape)
        │
        ├── Preview mutation → previewXml (transient result)
        └── Export mutation  → exportXml (transient result)
```

Preview/Export results are **not** global app state. They live in mutation result or panel-local state.

---

## 8. Anti-Patterns (Forbidden)

| Anti-pattern | Why |
| ------------ | --- |
| Global Redux store for all API data | Duplicates Query; hard to invalidate |
| Storing `compiled_schema_json` in client | Internal artifact |
| Caching Preview XML as source of truth | Always re-fetch via Preview |
| Form state in Context app-wide | Leaks across routes |
| Business validation in UI state layer | Belongs on backend |

---

## 9. URL State

Use URL search params for shareable list filters:

- `?page=1&pageSize=20&keyword=live`
- `?status=ACTIVE`

Optional: `/generate/:templateId` for deep link.

---

## 10. Related Documents

- `07-api-integration.md`
- `04-navigation-flow.md` — unsaved guards
- `09-error-handling.md`
