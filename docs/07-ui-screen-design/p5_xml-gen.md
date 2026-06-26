# Part 5. XML Generator

---

# 31. Purpose

The XML Generator is the primary working screen for end users.

It allows users to:

* Select a Template
* Enter Input Data
* Select Master Data
* Save Draft
* Preview XML
* Export XML

The screen dynamically adapts to the selected Template.

---

# 32. Screen Layout

```text
+-----------------------------------------------------------------------+

Template

[ LIVE_GAME ▼ ]

------------------------------------------------------------

Input Form

Game Date

[ Date Picker ]

Weather

[____________]

Attendance

[____________]

...

------------------------------------------------------------

Master Data

Game Kind

[ ▼ ]

Home Club

[ ▼ ]

Away Club

[ ▼ ]

------------------------------------------------------------

[ Save Draft ]

[ Preview ]

[ Export ]

------------------------------------------------------------

XML Preview

------------------------------------------------------------

<?xml version="1.0"?>

<LiveGame>

...

</LiveGame>

+-----------------------------------------------------------------------+
```

---

## UI Components

| Component            | Description                    |
| -------------------- | ------------------------------ |
| Template Selector    | Select XML Template            |
| Dynamic Input Form   | Generated from Template Schema |
| Master Data Selector | Select Master Data Records     |
| Save Draft           | Save current input             |
| Preview              | Generate preview XML           |
| Export               | Export XML                     |
| XML Preview Panel    | Display generated XML          |

---

## 33. Template Selection

### Purpose

The selected Template determines:

* Dynamic Input Form
* Master Data Requirements
* XML Structure
* Validation Rules

---

### User Flow

```text
Select Template

↓

Unsaved Changes?

├── Yes

│     ↓

│  Confirm Change

│

└── No

      ↓

Load Template Schema

↓

Generate Dynamic Form

↓

Load Saved Draft (if available)

↓

Ready for Input
```

---

### Unsaved Changes Protection

If the current form contains unsaved changes, the UI shall display a confirmation dialog before switching to another Template.

Example:

```text
You have unsaved changes.

Switching Templates will discard the current form.

Continue?
```

The Template shall not be changed until the user explicitly confirms.

---

### Processing

After the user confirms the Template change, the UI shall:

1. Load the selected Template.
2. Generate the Dynamic Input Form.
3. Request the Saved Draft.
4. Populate the form if a Draft exists.

If the user cancels the confirmation dialog, the current Template and form data shall remain unchanged.

---

### API Integration

```http
GET /api/v1/templates

GET /api/v1/saved-inputs/{templateId}
```

Reference:

```text
06-api-design.md
```


---

## 34. Dynamic Input Form

### Purpose

Generate the input form dynamically using the compiled Template Schema.

The UI shall not hard-code any input field.

---

### Dynamic Controls

| Data Type | UI Control       |
| --------- | ---------------- |
| STRING    | Text Box         |
| INTEGER   | Number Input     |
| DECIMAL   | Decimal Input    |
| BOOLEAN   | Checkbox         |
| DATE      | Date Picker      |
| DATETIME  | Date Time Picker |

---

### Repeatable Groups

If the Template Schema defines a repeatable node or group, the UI shall generate a repeatable form section.

The UI should support:

* Add Item
* Remove Item
* Reorder Items (optional)
* Collapse / Expand individual items (optional)

Each repeated item shall be generated using the same child field definitions from the Template Schema.

Example:

```text
Goals

Goal #1

Minute

Player

Assist

[ Remove ]

-------------------------

Goal #2

Minute

Player

Assist

[ Remove ]

-------------------------

[ + Add Goal ]
```

The UI shall dynamically maintain the occurrence order of repeated items.

---

### Validation

The UI performs:

* Required validation
* Basic type validation

Business validation is performed by the XML Generation Engine.

---

### Input State

The current form remains editable until:

* Preview
* Export
* Save Draft


---

# 35. Master Data Selection

## Purpose

Allow users to select Master Data Records referenced by the Template.

The UI shall generate selectors dynamically according to the Template Mapping.

---

## User Flow

```text
Load Template

↓

Read Mapping

↓

Required Master Data Types

↓

Load Records

↓

Display Selectors
```

---

## API Integration

```http
GET /api/v1/master-data-types/{id}/records
```

---

## Validation

The UI shall verify:

* Required selections
* Existing records

Business validation remains the responsibility of the Backend.

---

# 36. Preview XML

## Purpose

Preview generates XML without creating an Export History.

No XML file is stored.

---

## User Flow

```text
Preview

↓

Validate Form

↓

POST /xml/preview

↓

Validation Success

↓

Display XML

or

Validation Errors
```

---

## API Integration

```http
POST /api/v1/xml/preview
```

---

## XML Preview Panel

The generated XML shall be displayed in a read-only panel.

The UI should support:

* Scroll
* Copy
* Syntax Highlighting (Future)

---

# 37. Export XML

## Purpose

Generate and export the XML file.

Unlike Preview:

* XML file is persisted.
* Export History is created.

---

## User Flow

```text
Export

↓

Validate Form

↓

POST /xml/export

↓

Success

↓

Display Success

↓

Open Export History (Optional)
```

---

## API Integration

```http
POST /api/v1/xml/export
```

---

## Success

Display:

* Export completed.
* File Name.
* Export History ID.

The UI may provide a shortcut to Export History.

---

# 38. Validation Errors

Validation errors returned by the Backend shall be displayed inline whenever possible.

Example:

```text
Game Date

[____________]

Invalid date format.
```

---

## Multiple Errors

The UI shall support displaying multiple validation errors simultaneously.

The user shall not be required to fix one error before viewing the others.

This behavior follows the **Collect All Errors** strategy defined in the XML Generation Engine.

---

# 39. Loading States

The UI shall display loading indicators while:

* Loading Template
* Loading Saved Draft
* Loading Master Data
* Previewing XML
* Exporting XML

During processing:

* Preview button shall be disabled.
* Export button shall be disabled.

Duplicate requests shall not be allowed.

---

# 40. Design Principles

The XML Generator follows the principles below.

---

## Dynamic UI

The entire form shall be generated dynamically from the compiled Template Schema.

No XML-specific field shall be hard-coded.

---

## Stateless Requests

Every Preview and Export request shall contain all information required for execution.

The Backend shall not automatically load Saved Drafts.

---

## Save Draft Integration

Draft management is independent of Preview and Export.

Users explicitly decide when to save their work.

Preview and Export always operate on the current form values.

---

## Backend Validation

The UI performs only basic validation.

Business rules are enforced exclusively by the XML Generation Engine.

---

## Deterministic Execution

Preview and Export use the same runtime pipeline.

The only difference is the final processing step.

---

## User Experience

The UI should minimize unnecessary navigation.

Users should be able to:

* Edit
* Save Draft
* Preview
* Export

from a single working screen.
