# Deployment Guide — XMLGen v1.0.0

---

## Prerequisites

| Component | Version |
| --------- | ------- |
| JDK | 21 |
| Node.js | 22+ |
| PostgreSQL | 15+ (16 recommended) |
| Docker | 24+ (optional) |

---

## Option A — Docker Compose (recommended)

One-command startup for local or demo environments:

```bash
docker compose up --build
```

| Service | URL |
| ------- | --- |
| Frontend | http://localhost:8081 |
| Backend API | http://localhost:8080/api/v1 |
| Swagger UI (dev profile) | http://localhost:8080/swagger-ui.html |
| PostgreSQL | localhost:5433 |

Default login: `admin` / `admin123`

Stop:

```bash
docker compose down
```

Production overrides (requires secrets):

```bash
export JWT_SECRET='your-production-secret-minimum-32-chars'
export POSTGRES_PASSWORD='strong-db-password'
export SPRING_DATASOURCE_PASSWORD="$POSTGRES_PASSWORD"
docker compose -f docker-compose.yml -f docker-compose.prod.yml up --build -d
```

Production disables Swagger UI and exposes PostgreSQL only on the internal network.

---

## Option B — Manual development

### Database

Create PostgreSQL database `xmlgen` (default port 5433 in dev profile):

```bash
createdb -p 5433 xmlgen
```

### Backend

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5433/xmlgen
export SPRING_DATASOURCE_USERNAME=xmlgen
export SPRING_DATASOURCE_PASSWORD=xmlgen
export JWT_SECRET=dev-only-jwt-secret-minimum-32-characters-long
./gradlew bootRun
```

### Frontend

```bash
cd frontend
npm install
npm run dev    # http://localhost:5173 — proxies /api to :8080
```

---

## Build verification

```bash
./gradlew build          # backend compile + tests
cd frontend && npm run lint && npm run build
```

---

## Smoke test checklist

After deployment, verify the MVP business flow:

1. **Login** — POST `/api/v1/auth/login` or UI login with `admin` / `admin123`
2. **Template CRUD** — create, list, get, update metadata, update schema, delete
3. **Master Data CRUD** — create type, add fields, create/update/delete records
4. **Preview** — POST `/api/v1/templates/{id}/preview` with `inputData` and optional `selectedMasterData`
5. **Export** — POST `/api/v1/templates/{id}/export` with same request body

Use Postman collection `postman/XMLGen - MVP.postman_collection.json` for API-level verification.

---

## Environment variables

| Variable | Required | Default (dev) | Purpose |
| -------- | -------- | ------------- | ------- |
| `JWT_SECRET` | prod | dev default in `application-dev.yml` | HMAC signing key (min 32 chars) |
| `SPRING_DATASOURCE_URL` | prod | `jdbc:postgresql://localhost:5433/xmlgen` | Database JDBC URL |
| `SPRING_DATASOURCE_USERNAME` | prod | `xmlgen` | Database user |
| `SPRING_DATASOURCE_PASSWORD` | prod | `xmlgen` | Database password |
| `SPRING_PROFILES_ACTIVE` | no | `dev` | Spring profile (`dev` / `prod`) |
| `SPRINGDOC_ENABLED` | no | `true` in dev, `false` in prod | OpenAPI / Swagger UI |
| `VITE_API_BASE_URL` | no | `/api/v1` | Frontend API base (Docker/nginx same-origin) |

---

## CI/CD

GitHub Actions workflow `.github/workflows/ci.yml`:

- Backend: `./gradlew build`
- Frontend: `npm ci`, `npm run lint`, `npm run build`
- Docker: image build on push to main/master

---

## Related documents

- `API-CONTRACT.md` — endpoint inventory
- `KNOWN-LIMITATIONS.md` — MVP scope boundaries
- `RELEASE-NOTES-v1.0.0.md` — release summary
