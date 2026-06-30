# 06. Component Architecture

---

## 1. Purpose

Define reusable UI components, composition hierarchy, and ownership rules.

---

## 2. Component Layers

```text
layouts/          App shell, auth layout, page layout
    ↓
features/         Domain-specific compositions (non-exported internals)
    ↓
components/       Shared UI library
    ↓
primitives/       Lowest-level styled elements (optional)
```

---

## 3. Layout Components

| Component | Responsibility | Owns state? |
| --------- | -------------- | ----------- |
| `AppShell` | Header + Sidebar + outlet | Nav collapse (UI only) |
| `AuthLayout` | Centered login card | No |
| `PageLayout` | Title, breadcrumbs, actions slot, content | No |
| `SplitPanelLayout` | Generator: form left, XML right | Panel width (UI) |
| `Sidebar` | Primary nav, role-filtered items | No |
| `Header` | App name, user menu, logout | No |

**Rule:** Layouts never call domain APIs.

---

## 4. Shared Components

### 4.1 Data display

| Component | Usage |
| --------- | ----- |
| `DataTable` | Sortable columns, row actions, loading/empty |
| `Pagination` | Wired to API `PageMeta` |
| `StatusBadge` | ACTIVE / INACTIVE |
| `DescriptionList` | Detail panels |
| `EmptyState` | Lists with no data |
| `DateTimeText` | Formatted timestamps |

### 4.2 Forms

| Component | Usage |
| --------- | ----- |
| `FormField` | Label + control + error wrapper |
| `TextInput`, `NumberInput`, `DatePicker` | Typed inputs |
| `Select`, `Combobox` | Enums, master data picks |
| `Checkbox`, `Switch` | Booleans |
| `FormActions` | Cancel / Save footer |

### 4.3 Dynamic / domain

| Component | Usage |
| --------- | ----- |
| `DynamicForm` | Renders template schema fields |
| `RepeatableGroupField` | ZERO_OR_MORE / ONE_OR_MORE groups |
| `SchemaTreeEditor` | Admin field hierarchy editor (template feature — see §12) |
| `MappingEditor` | Template field ↔ master data mapping (template feature) |
| `MasterDataSelector` | Record picker per mapping |
| `TemplateSelector` | Active template dropdown |

### 4.4 Feedback

| Component | Usage |
| --------- | ----- |
| `Button` | Variants per design system |
| `Spinner` | Inline / overlay |
| `Toast` | Global notifications |
| `Alert` | Inline banners |
| `ConfirmDialog` | Destructive / unsaved confirm |
| `ValidationSummary` | List of API validation errors |

### 4.5 XML

| Component | Usage |
| --------- | ----- |
| `XmlViewer` | Read-only XML from `data.xml` |
| `CopyButton` | Clipboard copy |

**Rule:** `XmlViewer` accepts string XML only. No runtime tree props.

**Generated view rule:** `XmlViewer` and preview/export panels are **read-only generated
views**. They must never become editable. See `12-frontend-stable-architecture.md` §6.

---

## 5. Feature Module Components

Feature folders own screen-specific compositions:

```text
features/
├── auth/
│   └── LoginForm.tsx
├── dashboard/
│   └── QuickLinks.tsx
├── templates/
│   ├── TemplateListPage.tsx
│   ├── TemplateForm.tsx
│   └── SchemaEditorPage.tsx
├── master-data/
│   ├── TypeListPage.tsx
│   └── RecordForm.tsx
├── xml-generation/
│   ├── GeneratorWorkspace.tsx
│   ├── PreviewPanel.tsx
│   └── useGeneratorForm.ts
├── export-history/
│   ├── HistoryListPage.tsx
│   └── HistoryDetailPage.tsx
└── settings/
    └── SettingsPage.tsx
```

---

## 6. Ownership Rules

| Rule | Description |
| ---- | ----------- |
| **R1** | Shared `components/` must be domain-agnostic |
| **R2** | Features may import shared components and hooks; not vice versa |
| **R3** | API calls only in feature `api/` modules + hooks — never in components or pages |
| **R4** | Pages compose one feature root component per route |
| **R5** | Props down, events up; no cross-feature imports |
| **R6** | Types from `types/api/` mirror REST DTOs — not backend entities |
| **R7** | Each feature owns its API module — no HTTP in shared components |
| **R8** | Generated views (`XmlViewer`) are read-only — never editable |
| **R9** | Cross-feature imports use `features/<name>/index.ts` only — not internal paths |
| **R10** | Cross-feature integration uses public hooks/types from target feature `index.ts` only |
| **R11** | Integration UI components belong in the consumer feature, not the provider |

---

## 7. Editable vs Generated Components

| Type | Components | Behavior |
| ---- | ---------- | -------- |
| **Editable** | Template `SchemaEditor` family; master-data `DynamicRecordForm`; static `FormField` wrappers | User input; dirty tracking; save to REST |
| **Generated** | `XmlViewer`, `PreviewPanel`, export result panel | Display `data.xml` only; copy/download OK; no edit |

Mirrors backend: Metadata → editable; compile/execution artifacts → invisible; XML output → generated view.

---

## 8. Component Hierarchy Example — XML Generator

```text
GeneratorPage (page)
└── GeneratorWorkspace (feature)
    ├── PageLayout
    ├── TemplateSelector
    ├── SplitPanelLayout
    │   ├── DynamicForm
    │   │   └── FormField × N
    │   ├── MasterDataSelector × M
    │   └── ActionBar
    │       ├── Button Save Draft
    │       ├── Button Preview
    │       └── Button Export
    └── PreviewPanel
        ├── ValidationSummary
        └── XmlViewer
```

---

## 9. Dialog Patterns

| Dialog | Trigger | Components |
| ------ | ------- | ------------ |
| Delete template | List row | `ConfirmDialog` |
| Discard changes | Route leave | `ConfirmDialog` |
| Switch template | Selector change | `ConfirmDialog` |

---

## 10. Editor Patterns (Admin)

| Editor | Pattern |
| ------ | ------- |
| Schema tree | Left tree + right detail panel |
| Mapping | Table of fields with master data pickers |
| Records | Dynamic form from type schema |

Drag-and-drop reorder for field `displayOrder` is supported per requirements; order sent in schema save payload.

---

## 11. Feature Public API

Each feature owns its internal implementation. As a feature grows, protect its boundary
with a public export module:

```text
features/templates/
├── index.ts              # public surface — only entry for other features
├── api/                  # internal
├── hooks/                # internal (+ public hooks re-exported from index.ts)
├── components/           # internal
├── pages/                # route screens (re-exported from index.ts)
├── types/                # internal (+ public types re-exported from index.ts)
└── utils/                # internal
```

### Allowed

```typescript
import { TemplateListPage, useTemplateList, type TemplateDetail } from '@/features/templates';
```

### Forbidden (cross-feature)

```typescript
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import * as templatesApi from '@/features/templates/api/templates.api';
```

Routes and other features import **pages**, **public hooks**, and **public types** from
the feature index. Feature-internal files may import sibling paths within the same feature.

---

## 12. Schema Editor Boundary (Template Feature)

The Template Schema Editor (`SchemaEditor`, `SchemaFieldTree`, `SchemaFieldEditor`,
`SchemaMappingEditor`) is a **metadata editor only**. It remains inside
`features/templates/` and must not be extracted to shared `components/` until Rule of
Three applies across features.

### Responsibilities

- Edit field metadata
- Edit parent-child hierarchy
- Edit mappings
- Save via `PUT /templates/{id}/schema`

### Forbidden

- Preview or export execution
- Runtime value validation
- Access to `RuntimeExecutionTree`, `compiled_schema_json`, or engine internals

Preview and export belong exclusively to `features/xml-generation/`.

---

## 13. Stable Feature Boundary — Template

```text
Template Feature
  Metadata
  Schema
  Mappings
  CRUD
        ↓
      REST
```

Nothing below REST is visible to the UI.

---

## 14. Dynamic Form Boundary (Master Data Feature)

Master Data is the first feature that renders forms **dynamically from metadata** (field
definitions loaded via REST).

| Component | Owner | Responsibility |
| --------- | ----- | -------------- |
| `DynamicRecordForm` | `features/master-data/` | Edit master data record JSON from type field schema |
| `RecordFormDialog` | `features/master-data/` | Dialog wrapper for record create/edit |

### Rules

- Metadata-driven forms belong to the feature that owns the metadata.
- Do **not** move `DynamicRecordForm` to shared `components/` until Rule of Three is
  satisfied by a second business feature.
- Future XML Generation may introduce its own input form (`DynamicForm` in
  `features/xml-generation/`) driven by **template** schema — that is a separate concern
  from master data record editing.

### Forbidden (DynamicRecordForm)

- Preview or export execution
- XML generation or runtime validation
- Template schema editing

Preserves separation: **Master Data → Template → Runtime → XML**.

---

## 15. Cross-Feature Integration (Template ↔ Master Data)

When Template needs master data field metadata (e.g. mapping picker), integration follows:

```text
features/templates/  (SchemaMappingEditor → MasterDataFieldPicker)
        │
        ▼
@/features/master-data  (public API only)
        │
        ▼
useMasterDataFieldPickerOptions()  →  REST
```

**Status:** Implemented Phase 6.3.5. Numeric `masterDataFieldId` input replaced with
searchable picker; display format `TYPE_CODE / field_code — Display Name`.

### Consumer-owned integration component

`MasterDataFieldPicker` belongs in `features/templates/`. It renders Master Data
information but its responsibility is *select mapping for Template* — not *manage Master
Data*. Do not move it to `features/master-data/`.

Future features (XML Generation, Saved Inputs) implement their own integration components
via the same public API. Do not extract `SharedEntityPicker` / `LookupPicker` until Rule
of Three applies.

### Allowed

```typescript
import {
    useMasterDataFieldDetail,
    useMasterDataFieldPickerOptions,
    useMasterDataTypeDetail,
    type MasterDataFieldOption,
} from '@/features/master-data';
```

### Forbidden

```typescript
import { toMasterDataFieldOption } from '@/features/master-data/utils/fieldPicker';
import * as fieldsApi from '@/features/master-data/api/fields.api';
import { DynamicRecordForm } from '@/features/master-data/components/DynamicRecordForm';
```

---

## 16. Stable Feature Boundary — Master Data

```text
Master Data Feature
  Types
  Fields
  Records
        ↓
      REST
```

Nothing below REST is visible to the UI.

---

## 17. Related Documents

- `03-design-system.md`
- `05-screen-specification.md`
- `11-frontend-folder-structure.md`
- `12-frontend-stable-architecture.md`
