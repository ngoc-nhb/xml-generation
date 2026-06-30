# XMLGen Frontend

MVP frontend — foundation plus Template, Master Data, and XML Generation modules.

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
- Cross-feature integration uses public hooks/types from the target feature index (Cross-Feature Integration Principle)
- Integration pickers (e.g. `MasterDataFieldPicker`) stay in the consumer feature — not shared until Rule of Three

## Dev login

Default credentials (when backend is running): `admin` / `admin123`

## Scope (MVP)

| Feature | Route | Status |
| ------- | ----- | ------ |
| Auth | `/login` | ✅ |
| Templates | `/templates`, schema editor | ✅ |
| Master Data | `/master-data` | ✅ |
| XML Generation | `/xml-generation` | ✅ |

Pending: Export History, Saved Inputs, Dashboard, Settings expansion.

Execution state stays local to `features/xml-generation/` (Execution Session + Execution Screen principles). Preview and export call backend only — no runtime logic on the client.
