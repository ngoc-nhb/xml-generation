# 11. Frontend Folder Structure

---

## 1. Purpose

Define the proposed frontend project structure for Phase 6+ implementation.

Design only — no code in Phase 6.0.

---

## 2. Repository Layout (Monorepo Option)

Recommended: frontend as sibling to backend in same repository.

```text
xmlgen/
├── src/                          # Backend (existing)
├── docs/
├── postman/
└── frontend/                     # New SPA root
    ├── public/
    ├── src/
    ├── index.html
    ├── package.json
    ├── tsconfig.json
    └── vite.config.ts
```

Alternative: separate repository — requires explicit approval.

---

## 3. Feature Module Structure (Feature Isolation)

Each feature is self-contained and independently removable:

```text
features/templates/
├── api/
│   └── templates.api.ts
├── hooks/
│   ├── useTemplates.ts
│   └── useTemplateSchema.ts
├── components/
│   ├── TemplateListTable.tsx
│   └── SchemaTreeEditor.tsx
├── types/
│   └── template.types.ts
├── TemplateListPage.tsx      # or pages/ re-export
├── SchemaEditorPage.tsx
└── index.ts                  # public exports
```

Repeat for: `auth/`, `master-data/`, `xml-generation/`, `export-history/`, `settings/`.

Shared cross-cutting code only:

```text
api/client.ts                   # Envelope parse, auth header — no domain endpoints
components/                     # Generic UI only
layouts/
hooks/                          # useAuth, useUnsavedChangesGuard
types/api/common.ts             # ApiResponse, ApiError, PageMeta
```

**Forbidden:** domain `*.api.ts` files in a global `api/` folder mixed across modules.

---

## 4. Frontend `src/` Structure

```text
frontend/src/
├── main.tsx                      # Entry, providers
├── app/
│   ├── App.tsx                   # Router root
│   ├── routes.tsx                # Route definitions
│   └── providers.tsx             # QueryClient, Auth, Toast
│
├── layouts/
│   ├── AppShell.tsx
│   ├── AuthLayout.tsx
│   └── PageLayout.tsx
│
├── pages/                        # Thin route wrappers
│   ├── LoginPage.tsx
│   ├── DashboardPage.tsx
│   ├── templates/
│   ├── master-data/
│   ├── generate/
│   ├── export-history/
│   └── settings/
│
├── features/                     # Domain UI logic
│   ├── auth/
│   ├── dashboard/
│   ├── templates/
│   ├── master-data/
│   ├── xml-generation/
│   ├── export-history/
│   └── settings/
│
├── components/                   # Shared generic UI library
│   ├── button/
│   ├── form/
│   ├── table/
│   ├── dialog/
│   ├── feedback/
│   └── xml/                      # XmlViewer — generated views only
│
├── api/
│   └── client.ts                 # Shared fetch wrapper only
│
├── hooks/                        # Shared hooks (non-domain)
│   ├── useAuth.ts
│   ├── useUnsavedChangesGuard.ts
│   └── usePaginationParams.ts
│
├── types/                        # TypeScript types
│   ├── api/                      # Mirror REST DTOs
│   │   ├── common.ts             # ApiResponse, ApiError, PageMeta
│   │   ├── template.ts
│   │   └── ...
│   └── ui/                       # UI-only types
│
├── utils/
│   ├── errorMessages.ts          # code → message map
│   ├── formatDate.ts
│   └── buildFormFromSchema.ts
│
├── styles/
│   ├── tokens.css                # Design tokens
│   └── global.css
│
└── test/                         # Vitest + Testing Library
    ├── setup.ts
    └── ...
```

---

## 5. Module Boundaries

| Folder | May import from |
| ------ | --------------- |
| `pages/` | `features/`, `layouts/`, `hooks/` |
| `features/` | `components/`, `api/client`, `hooks/`, `types/api/common`, `utils/` |
| `components/` | `styles/`, `types/ui/` only |
| `api/client.ts` | `types/api/common` only |
| `layouts/` | `components/`, `hooks/useAuth` |

**Forbidden:**

- `components/` → `features/`
- `components/` → `fetch()` / HTTP libraries directly
- Any layer → backend Java packages

---

## 6. Feature Folder Convention

```text
features/xml-generation/
├── api/
│   └── xml-generation.api.ts
├── components/
│   ├── DynamicForm.tsx
│   ├── PreviewPanel.tsx      # generated view — read-only
│   └── TemplateSelector.tsx
├── hooks/
│   ├── useGeneratorForm.ts
│   ├── usePreview.ts
│   └── useExport.ts
├── types/
│   └── generator.types.ts
├── utils/
│   └── mapValidationErrors.ts
├── GeneratorWorkspace.tsx
└── index.ts
```

---

## 7. Naming Conventions

| Item | Convention |
| ---- | ------------ |
| Pages | `*Page.tsx` |
| API modules | `*.api.ts` |
| Query hooks | `useTemplates`, `usePreviewMutation` |
| Types | PascalCase interfaces matching API names |
| Routes | kebab-case paths |

---

## 8. Environment Configuration

```text
frontend/.env.development
VITE_API_BASE_URL=http://localhost:8080/api/v1
```

Never embed secrets in frontend. JWT obtained via login only.

---

## 9. Testing Structure

| Layer | Tool |
| ----- | ---- |
| Unit | Vitest |
| Components | Testing Library |
| API client | Mock Service Worker (MSW) |
| E2E (future) | Playwright |

Colocate tests: `Button.test.tsx` next to `Button.tsx`.

---

## 10. Alignment with Backend Modules

| Frontend feature | Backend module | Coupling |
| ---------------- | -------------- | -------- |
| templates | `template` | REST only |
| master-data | `masterdata` | REST only |
| xml-generation | `xmlgeneration` controllers | Preview/Export REST |
| export-history | `exporthistory` | Future REST |
| auth | `authentication` | Login/logout REST |

No shared code between frontend and backend repositories except OpenAPI/types generation (future optional).

---

## 11. Related Documents

- `01-ui-architecture.md`
- `06-component-architecture.md`
- `12-frontend-stable-architecture.md`
- `docs/12-project-structure/project-structure.md`
