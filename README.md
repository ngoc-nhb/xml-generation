# XMLGen

Template-driven XML generation system. Administrators configure templates and master data; authenticated users generate XML through preview and export.

**Release:** MVP v1.0.0 — full business flow available end-to-end.

---

## 1. Project Overview

XMLGen turns structured input and master data into XML using metadata-defined templates.

**Purpose:** Replace hand-written XML with a configurable, metadata-driven pipeline that stays correct as schemas evolve.

**High-level architecture:**

```text
Template & Master Data (Metadata)
              │
              ▼
       Compile Engine
   (schema → compiled artifact)
              │
              ▼
       Runtime Engine
 (validation → resolution → XML)
              │
              ▼
          REST API
        (/api/v1/*)
              │
              ▼
          React UI
   (orchestration only — no runtime logic)
```

The backend owns all business rules. The frontend calls public REST APIs only.

Design documents live under `docs/`. This README is the entry point for **running and manually testing** the MVP.

---

## 2. Requirements

| Tool | Version | Required |
| ---- | ------- | -------- |
| Java (JDK) | 21 | Yes — backend |
| Gradle | Wrapper included (`./gradlew`) | Yes |
| Node.js | 22+ | Yes — frontend |
| npm | 10+ | Yes |
| PostgreSQL | 15+ (16 recommended) | Yes — backend database |
| Docker | 24+ | Optional — one-command stack |

**Optional:** Postman v10.18+ for API-level verification.

---

## 3. Project Structure

```text
xmlgen/
├── src/                    Backend (Spring Boot) — repo root, not a subfolder
│   └── main/java/com/company/xmlgen/
├── frontend/               React 19 + Vite SPA
├── docs/                   Design, workflow, release notes
├── postman/                Postman collections and environment
├── docker-compose.yml      Optional full stack (Postgres + backend + frontend)
├── build.gradle            Backend build
└── README.md               This file — start here
```

| Folder | Contents |
| ------ | -------- |
| `src/` | REST controllers, services, compile/runtime engine, Flyway migrations |
| `frontend/` | Feature modules (auth, templates, master-data, xml-generation) |
| `docs/` | Architecture and module specifications |
| `postman/` | API smoke-test collections |

---

## 4. Launch Backend

### 4.1 Prepare PostgreSQL

Create a database (default dev port **5433**):

```bash
createdb -p 5433 xmlgen
# Or use Docker: docker run -d --name xmlgen-pg -e POSTGRES_DB=xmlgen \
#   -e POSTGRES_USER=xmlgen -e POSTGRES_PASSWORD=xmlgen -p 5433:5432 postgres:16-alpine
```

### 4.2 Start the server

From the **repository root** (not a `backend/` folder):

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/xmlgen
export SPRING_DATASOURCE_USERNAME=xmlgen
export SPRING_DATASOURCE_PASSWORD=xmlgen
export JWT_SECRET=dev-only-jwt-secret-minimum-32-characters-long

./gradlew bootRun
```

**Expected result:**

- Spring Boot starts on **http://localhost:8080**
- Flyway applies migrations and seeds the dev admin user
- Health check: `curl http://localhost:8080/actuator/health` → `{"status":"UP"}`

**Environment variables:**

| Variable | Dev default | Purpose |
| -------- | ----------- | ------- |
| `SPRING_DATASOURCE_URL` | `jdbc:postgresql://localhost:5433/xmlgen` | JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | `xmlgen` | DB user |
| `SPRING_DATASOURCE_PASSWORD` | `xmlgen` | DB password |
| `JWT_SECRET` | see above | HMAC signing key (min 32 chars) |
| `SPRING_PROFILES_ACTIVE` | `dev` | Spring profile |
| `PORT` | `8080` | HTTP port (Render injects this in production) |
| `XMLGEN_CORS_ALLOWED_ORIGINS` | see `application.yml` | Comma-separated CORS origin patterns |

OpenAPI (dev only): http://localhost:8080/swagger-ui.html

### Production (Render + Vercel + Supabase)

Backend on **Render**, frontend on **Vercel**, database on **Supabase PostgreSQL**.

**Render environment:**

| Variable | Example / notes |
| -------- | ----------------- |
| `SPRING_PROFILES_ACTIVE` | `prod` |
| `SPRING_DATASOURCE_URL` | Supabase JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | Supabase user |
| `SPRING_DATASOURCE_PASSWORD` | Supabase password |
| `JWT_SECRET` | Strong secret (min 32 chars) |
| `PORT` | Set automatically by Render |
| `XMLGEN_CORS_ALLOWED_ORIGINS` | `https://your-app.vercel.app,https://*.vercel.app` |

CORS origins are configured via `xmlgen.cors.allowed-origins` in `application.yml` (defaults include `http://localhost:5173` and `https://*.vercel.app`). Override with `XMLGEN_CORS_ALLOWED_ORIGINS` — no Java changes required for a new Vercel URL.

**Vercel:** set `VITE_API_BASE_URL` to the Render backend URL (e.g. `https://your-service.onrender.com/api/v1`).

---

## 5. Launch Frontend

In a **second terminal**:

```bash
cd frontend
npm install
npm run dev
```

**Expected result:**

- Vite dev server at **http://localhost:5173**
- API requests to `/api/*` are proxied to `http://localhost:8080` (see `frontend/vite.config.ts`)

Do not set `VITE_API_BASE_URL` to an absolute backend URL in local dev — the default `/api/v1` works with the proxy.

---

## 6. Login

| Item | Value |
| ---- | ----- |
| URL | http://localhost:5173/login |
| Username | `admin` |
| Password | `admin123` |

**Expected result:**

- Success toast: “Signed in successfully”
- Redirect to **http://localhost:5173/dashboard**
- Sidebar shows Templates, Master Data, XML Generation (admin user)

These credentials are seeded by Flyway in development only. Change them in any shared or production environment.

---

## 7. Complete MVP Walkthrough

Follow this sequence in the UI after backend and frontend are running. Use the **admin** account — Templates and Master Data require admin access.

### Step 1 — Create Template

| | |
| --- | --- |
| **Screen** | Templates → **New template** (`/templates/new`) |
| **Action** | Enter code (e.g. `LIVE_GAME_001`), name, optional description → **Create** |
| **Expected** | Success toast; redirect to schema editor for the new template |

### Step 2 — Design Schema

| | |
| --- | --- |
| **Screen** | Schema editor (`/templates/{id}/schema`) |
| **Action** | Add fields — e.g. root group `Game`, child element `Title` with source type **INPUT** → **Save schema** |
| **Expected** | Schema saved; backend compiles template in the same transaction (`compiled_schema_json` populated) |

Minimal example: one `GROUP` field `Game` and one `ELEMENT` field `Title` (`sourceType: INPUT`, `emptyHandling: REQUIRED`).

### Step 3 — Create Master Data Type

| | |
| --- | --- |
| **Screen** | Master Data → **New type** (`/master-data`) |
| **Action** | Code `GAME_KIND`, name “Game Kind”, status Active → **Create** |
| **Expected** | Type appears in list; open type detail |

### Step 4 — Create Master Data Fields

| | |
| --- | --- |
| **Screen** | Type detail → **Fields** (`/master-data/types/{typeId}/fields`) |
| **Action** | Add field `game_kind_id` (INTEGER, required) and `game_kind_name` (STRING, required) |
| **Expected** | Fields listed with correct data types |

### Step 5 — Create Master Data Records

| | |
| --- | --- |
| **Screen** | Type detail → **Records** (`/master-data/types/{typeId}/records`) |
| **Action** | **New record** → fill `game_kind_id: 1`, `game_kind_name: J1 League` → **Save** |
| **Expected** | Record appears in list |

### Step 6 — Open Template

| | |
| --- | --- |
| **Screen** | Templates → select your template (`/templates/{id}`) |
| **Action** | Confirm metadata and status **Active** |
| **Expected** | Template detail loads; link to schema editor available |

### Step 7 — Configure Mapping (optional)

| | |
| --- | --- |
| **Screen** | Schema editor (`/templates/{id}/schema`) |
| **Action** | On a field with source type **MASTER_DATA**, set mapping via the master data field picker → **Save schema** |
| **Expected** | Mapping saved; template recompiled |

Skip this step if all fields use **INPUT** or **STATIC** only.

### Step 8 — Open XML Generation

| | |
| --- | --- |
| **Screen** | Sidebar → **XML Generation** (`/xml-generation`) |
| **Action** | Page loads execution panel |
| **Expected** | Template selector, master data selector, JSON editor, preview panel visible |

### Step 9 — Select Template

| | |
| --- | --- |
| **Screen** | XML Generation — template dropdown |
| **Action** | Choose the template created in Step 1 |
| **Expected** | Template name shown in toolbar; preview/export enabled when JSON is valid |

### Step 10 — Select Master Data (if mappings use master data)

| | |
| --- | --- |
| **Screen** | Master Data selector |
| **Action** | Add type `GAME_KIND` and select record `J1 League` |
| **Expected** | Selection chip shown; included in preview/export payload |

Skip if template has no master-data mappings.

### Step 11 — Input JSON

| | |
| --- | --- |
| **Screen** | JSON input editor (left panel) |
| **Action** | Enter input matching your schema, e.g. `{ "Title": "Sample Match" }` for the minimal schema above |
| **Expected** | No syntax error; **Preview** button enabled |

Use **Format** to pretty-print. **Reset** clears input to `{}`.

### Step 12 — Preview XML

| | |
| --- | --- |
| **Screen** | XML Generation — **Preview** button |
| **Action** | Click **Preview** |
| **Expected** | Success toast; XML displayed in right panel (read-only). Validation errors appear in the error panel if input fails runtime validation |

### Step 13 — Export XML

| | |
| --- | --- |
| **Screen** | XML Generation — **Export** button |
| **Action** | Click **Export** |
| **Expected** | XML updated in panel (source label “export”); same validation rules as preview. MVP returns XML in JSON — use copy/download from the viewer if available |

Preview and export do **not** reset template, master data, or input JSON (Execution Session principle).

---

## 8. Smoke Test Checklist

| Test | Expected |
| ---- | -------- |
| Backend health | `GET /actuator/health` → UP |
| Login | Success; redirect to `/dashboard` |
| Create template | 201; redirect to schema editor |
| Save schema | Success; template compiles |
| Create master data type | Type in list |
| Create master data fields | Fields on type |
| Create master data record | Record in list |
| Configure mapping | Mapping saved (if used) |
| Select template in XML Generation | Template shown in toolbar |
| Preview XML | XML displayed in panel |
| Export XML | XML returned; no unexpected reset of inputs |

---

## 9. API Verification (Postman)

Use the collections in `postman/` for REST-level verification without the UI.

1. Import into Postman:
   - `postman/XMLGen - Template Module.postman_collection.json`
   - `postman/XMLGen - Master Data.postman_collection.json`
   - `postman/XMLGen - Local.postman_environment.json`
2. Select environment **XMLGen - Local** (`baseUrl` = `http://localhost:8080`).
3. Run in order:

```text
Authentication → Login
        ↓
Template → Create Template (sets templateId; includes sample schema)
        ↓
Master Data → Create Type → Create Field → Create Record
        ↓
XML Generation → Preview Template
        ↓
XML Generation → Export Template
```

Details: [`postman/README.md`](postman/README.md)

OpenAPI (dev backend): http://localhost:8080/swagger-ui.html

---

## 10. Troubleshooting

| Problem | Common cause | Solution |
| ------- | -------------- | -------- |
| Backend won't start | PostgreSQL not running or wrong port | Start Postgres on port **5433**; verify `SPRING_DATASOURCE_*` |
| Flyway / connection error | Database `xmlgen` missing | Run `createdb -p 5433 xmlgen` |
| Port 8080 in use | Another process on 8080 | Stop other service or change `server.port` |
| Port 5173 in use | Another Vite app | Stop other dev server or change Vite port |
| Frontend cannot reach backend | Backend not running or wrong API URL | Keep backend on :8080; use default `VITE_API_BASE_URL=/api/v1` with `npm run dev` |
| 401 Unauthorized | Missing or expired JWT | Log in again at `/login` |
| 403 / Access denied | Non-admin on admin routes | Use `admin` account for Templates and Master Data |
| Template not compiled | Schema never saved or compile failed | Open schema editor → **Save schema**; check backend logs |
| Preview validation failed | Input JSON doesn't match schema | Fix field names/types; read errors in preview panel (`field` + `code`) |
| JSON parse error (UI) | Invalid syntax in JSON editor | Fix syntax or click **Reset**; only `{}` is valid empty input |
| Empty preview with errors | Required INPUT fields missing | Add keys to `inputData` matching schema field names |
| Docker frontend can't login | Wrong API base | Postman/UI against Docker: frontend at :8081, API proxied via nginx; manual dev uses :5173 |
| **Templates / XML Generation show “Coming in a future phase”** | Stale Vite dev server still running after code update | Stop all Vite processes, clear cache, restart (see below) |

### Stale Vite dev server

If **Templates**, **Master Data**, or **XML Generation** show *“Coming in a future phase”* but the repo has full feature modules, an **old `npm run dev` process** is likely still serving a cached router from an earlier phase.

Symptoms:

- Sidebar routes exist, but pages show placeholder text instead of CRUD / execution UI
- Multiple terminals may each have a Vite instance (ports 5173, 5174, …)

Fix:

```bash
# Stop old dev servers (Ctrl+C in each terminal, or:)
pkill -f "xmlgen/frontend/node_modules/.bin/vite"

cd frontend
rm -rf node_modules/.vite
npm run dev
```

Then hard-refresh the browser (`Cmd+Shift+R` / `Ctrl+Shift+R`) at **http://localhost:5173**.

Only **Export History** and **Settings** should show “Coming in a future phase” in MVP.

---

## 11. Build Verification

Run before committing or releasing.

**Backend** (repository root):

```bash
./gradlew build
```

Expected: `BUILD SUCCESSFUL` — compiles, runs unit and integration tests.

**Frontend**:

```bash
cd frontend
npm run lint    # 0 errors (warnings acceptable)
npm run build   # dist/ produced
```

Expected: TypeScript check passes; Vite build completes (bundle size warning is known — see `docs/technical-debt.md` TD-F002).

**Optional — full stack via Docker:**

```bash
docker compose up --build
# Frontend http://localhost:8081 | Backend http://localhost:8080
```

---

## 12. Release Status

### Backend

| Component | Status |
| --------- | ------ |
| Compile Engine | ✅ |
| Runtime Engine | ✅ |
| REST APIs | ✅ |

### Frontend

| Feature | Status |
| ------- | ------ |
| Auth | ✅ |
| Templates | ✅ |
| Master Data | ✅ |
| XML Generation | ✅ |

### Future work (not in MVP)

- Export History
- Saved Inputs
- Batch Export
- Versioning
- Dashboard (placeholder)
- User management / RBAC on endpoints

Release artifacts: [`docs/release/`](docs/release/)  
CI: [`.github/workflows/ci.yml`](.github/workflows/ci.yml)

---

## Quick reference

| Service | Manual dev URL |
| ------- | -------------- |
| Frontend | http://localhost:5173 |
| Backend API | http://localhost:8080/api/v1 |
| Swagger (dev) | http://localhost:8080/swagger-ui.html |
| Login | http://localhost:5173/login |

| Docker compose URL |
| ------------------ |
| Frontend http://localhost:8081 |
| Backend http://localhost:8080 |
