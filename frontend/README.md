# XMLGen Frontend

Phase 6.1 foundation — application shell and development infrastructure only.

## Stack

- React 19, Vite, TypeScript
- React Router, TanStack Query, Axios
- Tailwind CSS, shadcn/ui-style components
- React Hook Form, Zod, Lucide React, Sonner

## Commands

```bash
npm install
npm run dev      # http://localhost:5173 — proxies /api to backend :8080
npm run build
npm run lint
```

## Environment

| Variable | Default | Purpose |
|----------|---------|---------|
| `VITE_API_BASE_URL` | `/api/v1` | REST base path (see `.env.development`) |

## Architecture

See `docs/13-ui-design/12-frontend-stable-architecture.md`.

- **Pages** compose feature modules and shared layout
- **Features** own `api/`, `hooks/`, `components/`, `types/` — expose public API via `index.ts`
- **Shared** `api/client.ts` — Axios instance, JWT injection, envelope parsing
- Components never call Axios directly
- Cross-feature imports use `@/features/<name>` only, not internal paths

## Dev login

Default credentials (when backend is running): `admin` / `admin123`

## Scope (Phase 6.1)

Implemented: routing skeleton, AppShell, auth skeleton, providers, design system primitives, error/loading/empty UI.

Not implemented: Template, Master Data, XML Generation, or other business screens.
