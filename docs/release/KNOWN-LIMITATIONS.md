# Known Limitations — v1.0.0

Accepted MVP boundaries. These are not bugs unless marked as technical debt.

---

## Business features not implemented

| Feature | Status |
| ------- | ------ |
| Saved Inputs | Not started |
| Export History | Not started |
| Batch Export | Not started |
| Template/record versioning | Not started |
| Dashboard | Placeholder route only |
| User management | Seed admin only |
| File download export | Export returns JSON `{ xml }`; client-side download only |

---

## Metadata not yet enforced at runtime

| Metadata | Limitation |
| -------- | ---------- |
| `searchable` | Stored but not used for field-level search (TD-003) |
| `defaultValue` | Stored but not applied when input omits field (TD-004) |
| DATE/DATETIME `format` | Not on runtime model yet (TD-011) |

---

## Security

| Topic | MVP behavior |
| ----- | ------------ |
| Authorization | JWT required; **no role-based endpoint restrictions** (admin flag in token for audit only) |
| CORS | Not configured on backend; use same-origin proxy (Vite dev / nginx Docker) |
| Swagger UI | Enabled in dev only; disabled in production |
| Penetration testing | Not performed; architecture review only |

---

## Frontend

| Topic | Limitation |
| ----- | ---------- |
| XML Generation input | Raw JSON editor; metadata-driven dynamic form deferred |
| Test suite | Vitest planned; no automated frontend tests in v1.0.0 (TD-F001) |
| Bundle size | ~654 KB JS (gzip ~198 KB); no code splitting (TD-F002) |
| Route naming | App uses `/xml-generation`; some design docs reference `/generate` |

---

## Backend

| Topic | Limitation |
| ----- | ---------- |
| Soft-delete columns | `deleted_at` columns remain in schema (TD-001, TD-002) |
| Compile-time mapping validation | Partial (TD-008) |
| Controller slice tests | Deferred (TD-005) |
| OpenAPI | Generated from code; design docs may lag minor DTO field names |

---

## Documentation

| Topic | Note |
| ----- | ---- |
| `docs/12-ui-design/` | Superseded by `docs/13-ui-design/` — do not update duplicate tree |
| `docs/06-api-design/p4_master-data-api.md` | Legacy path names in body; see header + `API-CONTRACT.md` for canonical paths |

---

## Performance

No measured production bottlenecks. Bundle size and sequential JSONB keyword search documented in Phase 7.4 review — optimization deferred per MVP-First Principle.
