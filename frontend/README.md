# XMLGen Frontend

React 19 + Vite SPA for the XMLGen MVP. Orchestrates public REST APIs only — no runtime engine logic on the client.

**Full launch and manual testing guide:** see the [root README](../README.md) (sections 4–8).

---

## Quick start

**Prerequisites:** Backend running on http://localhost:8080 (see root README §4).

```bash
cd frontend
npm install
npm run dev
```

Open **http://localhost:5173** → login at `/login` with `admin` / `admin123`.

---

## Commands

| Command | Purpose |
| ------- | ------- |
| `npm run dev` | Dev server on port **5173**; proxies `/api` → backend `:8080` |
| `npm run build` | Production build to `dist/` |
| `npm run lint` | ESLint |
| `npm run preview` | Serve production build locally |

---

## Environment

| Variable | Default | Purpose |
| -------- | ------- | ------- |
| `VITE_API_BASE_URL` | `/api/v1` | API base path (`.env.development`) |

For local dev, keep the default `/api/v1` so Vite proxies requests to the backend. Do not point at `http://localhost:8080/api/v1` directly unless you configure CORS on the backend.

**Docker:** The frontend image builds with `VITE_API_BASE_URL=/api/v1` and nginx proxies `/api/` to the backend container. Access at http://localhost:8081.

---

## Routes (MVP)

| Feature | Route | Access |
| ------- | ----- | ------ |
| Login | `/login` | Public |
| Dashboard | `/dashboard` | Authenticated |
| Templates | `/templates`, `/templates/new`, `/templates/:id/schema` | Admin |
| Master Data | `/master-data`, `/master-data/types/:typeId/fields`, `.../records` | Admin |
| XML Generation | `/xml-generation` | Authenticated |

After login, default redirect is `/dashboard`.

---

## Manual test flow (summary)

1. **Templates** — create template → design schema → save (compiles on backend)
2. **Master Data** — create type → fields → records
3. **Templates** — optional master-data mappings in schema editor
4. **XML Generation** — select template → optional master data → JSON input → Preview → Export

Step-by-step instructions with expected results: [root README §7](../README.md#7-complete-mvp-walkthrough).

---

## Stack

- React 19, TypeScript, Vite
- React Router, TanStack Query, Axios
- Tailwind CSS, shadcn/ui-style components
- React Hook Form, Zod, Lucide React, Sonner

---

## Architecture

See `docs/13-ui-design/12-frontend-stable-architecture.md`.

- Features under `src/features/` own `api/`, `hooks/`, `components/`, `types/`
- Cross-feature imports via `features/<name>/index.ts` only
- Shared HTTP client: `src/api/client.ts`
- XML Generation execution state stays local in `ExecutionPanel` (not global context)

---

## Build verification

```bash
npm run lint
npm run build
```

Expected: 0 lint errors; `dist/` created. Bundle ~654 KB (gzip ~198 KB) — see TD-F002 in `docs/technical-debt.md`.

---

## Scope

| Feature | Status |
| ------- | ------ |
| Auth | ✅ |
| Templates | ✅ |
| Master Data | ✅ |
| XML Generation | ✅ |
| Export History | Placeholder |
| Dashboard / Settings | Placeholder |

Pending: Saved Inputs, Export History, Dashboard expansion, Vitest suite (TD-F001).
