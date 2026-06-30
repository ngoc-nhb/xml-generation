# 03. Design System

---

## 1. Purpose

Define visual and interaction tokens shared across all screens.

Phase 1 supports **Light Mode only**. Dark mode is out of scope.

---

## 2. Design Philosophy

- **Clarity over decoration** — enterprise admin + generation tool
- **Consistency** — same patterns for forms, tables, dialogs across modules
- **Accessibility baseline** — keyboard navigation, visible focus, sufficient contrast
- **Backend error codes drive messages** — UI maps codes to copy; backend never returns localized strings

---

## 3. Typography

| Token | Size | Weight | Usage |
| ----- | ---- | ------ | ----- |
| `display-lg` | 28px | 600 | Page titles |
| `heading-md` | 20px | 600 | Section headings |
| `heading-sm` | 16px | 600 | Card titles, dialog titles |
| `body-md` | 14px | 400 | Default body text |
| `body-sm` | 12px | 400 | Helper text, captions |
| `mono-md` | 13px | 400 | XML viewer, code blocks |

**Font stack (recommended):**

- UI: system sans-serif stack (`Inter`, `-apple-system`, `Segoe UI`, sans-serif)
- Monospace: `ui-monospace`, `SFMono-Regular`, `Consolas`, monospace

---

## 4. Spacing Scale

Base unit: **4px**

| Token | Value | Usage |
| ----- | ----- | ----- |
| `space-1` | 4px | Tight inline gaps |
| `space-2` | 8px | Form field gaps |
| `space-3` | 12px | Component internal padding |
| `space-4` | 16px | Section spacing |
| `space-6` | 24px | Card padding |
| `space-8` | 32px | Page section gaps |
| `space-12` | 48px | Major layout separation |

---

## 5. Color Palette (Light Mode)

### Neutral

| Token | Hex | Usage |
| ----- | --- | ----- |
| `bg-page` | `#F8FAFC` | Page background |
| `bg-surface` | `#FFFFFF` | Cards, panels |
| `border-default` | `#E2E8F0` | Dividers, inputs |
| `text-primary` | `#0F172A` | Primary text |
| `text-secondary` | `#64748B` | Labels, hints |
| `text-disabled` | `#94A3B8` | Disabled controls |

### Brand / Action

| Token | Hex | Usage |
| ----- | --- | ----- |
| `primary-600` | `#2563EB` | Primary buttons, links |
| `primary-700` | `#1D4ED8` | Primary hover |
| `primary-50` | `#EFF6FF` | Selected nav, info backgrounds |

### Semantic

| Token | Hex | Usage |
| ----- | --- | ----- |
| `success-600` | `#16A34A` | Success toast, badges |
| `warning-600` | `#D97706` | Warnings |
| `error-600` | `#DC2626` | Errors, validation |
| `info-600` | `#0284C7` | Informational |

---

## 6. Buttons

| Variant | Usage |
| ------- | ----- |
| **Primary** | Main action (Save, Preview, Export, Create) |
| **Secondary** | Cancel, Back |
| **Ghost** | Tertiary actions (Copy XML) |
| **Danger** | Delete, irreversible confirm |

**Rules:**

- One primary button per dialog / form footer
- Destructive actions require confirmation dialog
- Disable + loading spinner during async operations
- Primary actions align **right** in footers

---

## 7. Forms

| Element | Rule |
| ------- | ---- |
| Label | Always visible; required fields marked with `*` |
| Input | Full width in single-column forms; max-width on wide screens |
| Helper text | Below field, `text-secondary` |
| Validation error | Below field, `error-600`; map from API `errors[].field` + `code` |
| Read-only | Muted background, no edit cursor |

**Dynamic forms** (XML Generation): field widgets chosen from metadata `valueType`, `nodeType`, `occurrenceRule`.

---

## 8. Tables

Used for: Template List, Master Data lists, Export History.

| Feature | Rule |
| ------- | ---- |
| Header | Sticky on long lists (desktop) |
| Row actions | Icon or text buttons in trailing column |
| Empty state | Centered message + primary CTA |
| Pagination | Matches API `meta.page`, `meta.pageSize`, `meta.totalPages` |
| Loading | Skeleton rows or table overlay |

---

## 9. Dialogs

| Type | Usage |
| ---- | ----- |
| **Confirm** | Delete, discard unsaved changes |
| **Form** | Quick create when full page not needed (rare) |
| **Alert** | Blocking errors |

**Behavior:**

- Modal overlay blocks background
- Escape closes non-destructive dialogs only when safe
- Focus trap while open
- Explicit Cancel / Confirm buttons

---

## 10. Icons & Badges

**Icons:** outline style, 20px default, 16px inline.

**Badges:**

| Badge | Color | Usage |
| ----- | ----- | ----- |
| ACTIVE | success | Template / type status |
| INACTIVE | neutral | Inactive resources |
| ADMIN | info | Role indicator (Settings) |
| ERROR | error | Failed export (future) |

---

## 11. Code Blocks & XML Viewer

Dedicated component for Preview / Export output and read-only XML display.

| Feature | Phase 1 | Future |
| ------- | ------- | ------ |
| Monospace font | ✅ | |
| Scroll (horizontal + vertical) | ✅ | |
| Line wrap toggle | ✅ | |
| Copy to clipboard | ✅ | |
| Syntax highlighting | | Optional |
| Collapse nodes | | Optional |

XML content comes from `data.xml` only. Never render internal JSON debug trees.

**Generated view:** XML Viewer is a **generated view** — read-only, never editable.
See `12-frontend-stable-architecture.md` §6.

---

## 12. Notifications (Toasts)

| Type | Trigger |
| ---- | ------- |
| Success | Save, delete, export complete |
| Error | HTTP failure, unexpected error |
| Warning | Unsaved changes, stale data |
| Info | Background tips |

Toasts auto-dismiss except errors (user dismisses).

---

## 13. Loading States

| Pattern | Usage |
| ------- | ----- |
| Page spinner | Initial route load |
| Section skeleton | Table / form loading |
| Button spinner | Preview, Export, Save in progress |
| Progress text | Long operations ("Generating XML…") |

Disable duplicate submissions while loading.

---

## 14. Related Documents

- `docs/07-ui-screen-design/p8_common-ui-rules.md` — legacy common rules (aligned)
- `06-component-architecture.md` — component mapping
- `10-responsive-strategy.md` — breakpoints
