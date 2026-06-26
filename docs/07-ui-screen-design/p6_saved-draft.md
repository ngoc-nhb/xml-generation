# Part 6. Saved Draft

---

# 41. Purpose

The Saved Draft feature allows users to preserve unfinished XML input and continue editing later.

A Saved Draft belongs to:

* One User
* One Template

Each user may have at most one Saved Draft for each Template.

Saved Drafts are independent of XML Preview and XML Export.

---

# 42. Draft Lifecycle

The lifecycle of a Saved Draft is shown below.

```text
Select Template

↓

Load Draft (if exists)

↓

Edit Form

↓

Save Draft

↓

Continue Editing

↓

Preview

or

Export

↓

(Optional)

Delete Draft
```

Saving a Draft does not generate XML.

Saving a Draft does not create an Export History.

---

# 43. Automatic Draft Loading

## Purpose

Restore previously saved work when a user selects a Template.

---

## User Flow

```text
Select Template

↓

GET Saved Draft

↓

Draft Exists?

├── Yes

│     ↓

│  Populate Form

│

└── No

      ↓

 Empty Form
```

---

## Processing

After the user selects a Template, the UI shall:

1. Load the current Template.
2. Generate the Dynamic Input Form.
3. Request the Saved Draft.
4. Populate the form if a Draft exists.

The user is not required to manually load the Draft.

---

## API Integration

```http
GET /api/v1/saved-inputs/{templateId}
```

Reference:

```
06-api-design.md
```

---

## Schema Compatibility

The Saved Draft may have been created using an older Template version.

The UI shall reconcile the Draft with the current Template Schema.

The UI should:

* Ignore unknown fields.
* Display newly added fields.
* Initialize new fields using Default Values when available.
* Apply current validation rules.

---

# 44. Save Draft

## Purpose

Persist the current input without generating XML.

---

## User Flow

```text
Edit Form

↓

Save Draft

↓

Validation

↓

PUT Saved Draft

↓

Success
```

---

## API Integration

```http
PUT /api/v1/saved-inputs/{templateId}
```

---

## Processing

The UI shall submit:

* Current Input Data
* Selected Master Data

The Saved Draft shall replace any existing Draft for the same:

* User
* Template

---

## Validation

The UI performs only:

* Valid JSON generation
* Basic input validation

Business validation is intentionally not performed.

Incomplete or partially filled forms may still be saved successfully.

---

## Success Feedback

Upon successful save, the UI should:

* Display a success notification.
* Keep the user on the current screen.
* Preserve all current form values.

---

# 45. Delete Draft

## Purpose

Delete the current Saved Draft.

---

## User Flow

```text
Delete Draft

↓

Confirmation

↓

DELETE Saved Draft

↓

Success

↓

Clear Draft
```

---

## Confirmation

Deleting a Draft requires explicit user confirmation.

Deletion shall never occur immediately after clicking the Delete button.

---

## API Integration

```http
DELETE /api/v1/saved-inputs/{templateId}
```

---

## Processing

After successful deletion, the UI shall:

* Clear the Saved Draft status.
* Continue displaying the current form.

Deleting the Draft shall not automatically clear the user's current working data.

The current form remains editable until the user explicitly resets or leaves the screen.

---

## 46. Draft Status

The UI should clearly indicate whether the current form has been saved.

Example indicators:

```text
Draft Saved

Unsaved Changes

Saving...
```

The exact visual presentation is implementation-defined.

---

### Unsaved Changes

If the current form differs from the latest Saved Draft:

```text
Unsaved Changes
```

should be displayed.

---

### Save Timestamp

The UI may display:

```text
Last saved

2026-06-25 14:30
```

to help users understand the freshness of the Draft.

---

### Navigation Guard

If the current form contains **Unsaved Changes**, the UI shall warn the user before leaving the current screen.

Examples include:

* Navigating to another menu.
* Returning to the Dashboard.
* Refreshing the browser.
* Closing the browser tab or window.
* Navigating away using the browser's Back or Forward buttons.

The warning shall require explicit user confirmation before the current page is abandoned.

Example:

```text
You have unsaved changes.

If you leave this page, your unsaved work will be lost.

Leave this page?
```

If the user chooses to remain on the page, the current editing session shall continue without modification.

This navigation guard applies only to temporary unsaved changes.

Once a Draft has been successfully saved, leaving the page shall not require additional confirmation unless new unsaved changes are introduced.

---

# 47. Error Handling

If a Save Draft request fails:

* Preserve all current form data.
* Display an error notification.
* Allow the user to retry.

The UI shall never discard user input because of a failed Save Draft operation.

---

## Network Failure

Temporary network failures shall not clear:

* Current form values
* Selected Template
* Selected Master Data

The user may retry saving once connectivity is restored.

---

# 48. Design Principles

The Saved Draft feature follows the principles below.

---

## Explicit Save

Drafts are saved only when the user explicitly selects **Save Draft**.

The UI shall not automatically save Drafts in the background during Phase 1.

---

## Separation of Responsibilities

Saved Drafts are independent of:

* Preview
* Export
* Export History

Saving a Draft never triggers XML generation.

---

## User Ownership

Each Saved Draft belongs exclusively to the authenticated user.

Users shall never be able to view or modify another user's Draft.

---

## Schema Evolution

The UI shall adapt older Drafts to the latest Template Schema during loading.

Draft data shall never be modified automatically on the server.

Schema reconciliation is performed only within the UI.

---

## Data Preservation

The UI shall prioritize preserving user input.

User-entered data shall never be discarded without explicit confirmation or user action.

---

## Backend-Driven Persistence

The UI is responsible for editing Drafts.

The Backend is responsible for persisting Drafts.

The UI shall always synchronize Draft changes through the Saved Input APIs defined in:

```
06-api-design.md
```
