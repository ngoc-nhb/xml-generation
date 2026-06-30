# Phase 7.5 — Security Review

Architecture review only. No penetration testing.

---

## Authentication & JWT

| Check | Status | Notes |
| ----- | ------ | ----- |
| Stateless JWT | ✅ | No server sessions |
| Secret length | ✅ | HMAC-SHA256; min 32 chars documented |
| Token transport | ✅ | `Authorization: Bearer` |
| Expiration | ✅ | Configurable `xmlgen.jwt.expiration-ms` |
| Invalid token | ✅ | 401 via `RestAuthenticationEntryPoint` |
| Logout endpoint | ⚠️ | Client-side token clear only; no server revocation (acceptable MVP) |

---

## Authorization

| Check | Status | Notes |
| ----- | ------ | ----- |
| Protected routes | ✅ | All except login + actuator health/info |
| Role-based access | ⚠️ | **Not enforced at endpoint level** — admin flag in JWT for audit only |
| Method security | N/A | No `@PreAuthorize` |

**Recommendation:** Document as known limitation; add RBAC in user-management phase.

---

## Input validation

| Check | Status | Notes |
| ----- | ------ | ----- |
| Bean Validation on DTOs | ✅ | Controllers use `@Valid` |
| JSON parsing | ✅ | Jackson; malformed JSON → 400 envelope |
| Runtime validation | ✅ | Backend engine; not duplicated on frontend |
| Multipart limits | ✅ | 10 MB in `application.yml` |

---

## XSS & output

| Check | Status | Notes |
| ----- | ------ | ----- |
| XML in API responses | ✅ | JSON-encoded string; UI displays as text (`XmlViewer`) |
| React default escaping | ✅ | No `dangerouslySetInnerHTML` in XML Generation path |
| Error messages | ✅ | No stack traces in API responses (`server.error.*` disabled) |

---

## XML generation

| Check | Status | Notes |
| ----- | ------ | ----- |
| Escaping | ✅ | Engine responsibility; not client-side |
| External entities | ✅ | Standard DOM/serializer usage in engine (review engine module separately) |
| User-controlled tag names | ⚠️ | From admin-configured template metadata only |

---

## Secrets & configuration

| Check | Status | Notes |
| ----- | ------ | ----- |
| JWT secret in prod | ✅ | Required via env; no default in prod profile |
| Dev default secret | ⚠️ | Fixed dev default — acceptable for local/docker demo only |
| DB credentials | ✅ | Env-driven in prod |

---

## CORS & headers

| Check | Status | Notes |
| ----- | ------ | ----- |
| CORS | N/A (by design) | Same-origin via Vite proxy / nginx in Docker |
| CSRF | Disabled | Stateless JWT API — acceptable |
| Security headers (nginx) | ⚠️ | Basic nginx; add HSTS/CSP at reverse proxy in hardening phase |

---

## Session handling

Stateless — no session cookies. Frontend stores JWT in auth context (memory/localStorage per implementation).

---

## Swagger / OpenAPI

| Profile | Access |
| ------- | ------ |
| dev | `/swagger-ui.html` public (API structure visible) |
| prod | Disabled via `springdoc.enabled=false` |

---

## Critical issues

**None** blocking v1.0.0 MVP release.

## Recommendations (post-MVP)

1. Endpoint-level admin authorization when user management ships
2. Security headers at production reverse proxy
3. JWT refresh / revocation strategy for multi-user deployments
