# 10. Responsive Strategy

---

## 1. Purpose

Define breakpoint strategy and layout adaptation rules.

**Direction:** Desktop-first. Tablet supported. Mobile only where reasonable.

Phase 1 explicitly excludes mobile phone optimization per requirements.

---

## 2. Breakpoints

| Token | Min width | Target devices |
| ----- | --------- | -------------- |
| `sm` | 640px | Large phones (limited support) |
| `md` | 768px | Tablet portrait |
| `lg` | 1024px | Tablet landscape, small laptop |
| `xl` | 1280px | Desktop (primary design target) |
| `2xl` | 1536px | Wide desktop |

**Primary design target:** `xl` (1280px+).

---

## 3. Layout Adaptation

### App Shell

| Breakpoint | Sidebar | Header |
| ---------- | ------- | ------ |
| ≥ `lg` | Fixed visible sidebar (~240px) | Full header |
| `< lg` | Collapsible drawer (hamburger) | Compact header |

### Content max-width

Management forms: max **720px** content column centered or left-aligned.

Tables: full width with horizontal scroll if needed.

---

## 4. Screen-Specific Rules

### Dashboard

| ≥ `md` | Quick link grid 2–3 columns |
| `< md` | Single column stack |

### Template / Master Data tables

| ≥ `lg` | Full columns |
| `< lg` | Hide non-essential columns (e.g. description); horizontal scroll fallback |

### Schema Editor

| ≥ `xl` | Tree + detail side-by-side |
| `< xl` | Stacked: tree above detail |

Requires **`lg` minimum** for comfortable admin editing. Below `md`, show banner: "Use desktop for schema editing."

### XML Generator (critical)

| ≥ `xl` | Split panel: form left (~55%), XML viewer right (~45%) |
| `lg`–`xl` | Split panel with narrower viewer |
| `< lg` | Stacked: form full width, XML viewer below actions |

Preview/Export remain usable on tablet stacked layout.

### XML Viewer

Minimum height **320px** on stacked layout; flexible grow on desktop.

---

## 5. Touch & Tablet

- Minimum tap target **44×44px**
- Adequate spacing between row actions
- Date pickers use native/touch-friendly controls

---

## 6. Mobile (`< md`)

**Not a Phase 1 target.**

Behavior:

- App usable for login and read-only lists if needed
- Complex editors (schema, generator with many fields) show advisory banner
- No dedicated mobile navigation redesign

---

## 7. Print

Not required for Phase 1. XML viewer Copy is preferred over print stylesheet.

---

## 8. Testing Matrix

| Viewport | Priority |
| -------- | -------- |
| 1440×900 | P0 — primary QA |
| 1280×800 | P0 |
| 1024×768 | P1 — tablet landscape |
| 768×1024 | P1 — tablet portrait |
| 375×667 | P2 — smoke only |

---

## 9. Related Documents

- `03-design-system.md`
- `06-component-architecture.md` — SplitPanelLayout
- `docs/07-ui-screen-design/p8_common-ui-rules.md` §68
