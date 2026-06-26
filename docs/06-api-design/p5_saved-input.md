# Part 5. Saved Input APIs

---

## 45. Overview

Saved Input APIs allow authenticated users to save and restore working drafts for a specific Template.

A Saved Input contains:

* User input values
* Selected Master Data
* Draft state

Each user may have only one Saved Input per Template.

Saved Inputs are private and are accessible only by their owner.

---

## 46. GET /api/v1/saved-inputs/{templateId}

Returns the latest Saved Input for the specified Template.

### Request

```http
GET /api/v1/saved-inputs/{templateId}
```

---

### Processing

The Backend shall:

1. Verify the authenticated user.
2. Locate the Saved Input using:

```text
userId
+
templateId
```

3. Return the stored draft without modifying its content.

The Backend shall not automatically remove or transform fields that are no longer defined in the current Template.

---

### Success Response

```json
{
  "success": true,
  "data": {
    "templateId": 1,
    "templateVersion": 8,
    "inputData": {
      "GameDate": "2026-06-18",
      "Weather": "Sunny",
      "OldField": "ABC"
    },
    "selectedMasterData": {
      "GAME_KIND": {
        "id": 2
      }
    },
    "updatedAt": "2026-06-25T10:15:00Z"
  }
}
```

---

### Schema Drift

A Saved Input may have been created using an older Template version.

The API shall return the original draft exactly as stored.

The client application is responsible for reconciling the draft with the current Template definition.

Typical client-side handling includes:

* Ignoring unknown fields
* Initializing newly added fields
* Displaying draft compatibility warnings when appropriate

---

### Empty Result

If no Saved Input exists:

```http
200 OK
```

```json
{
  "success": true,
  "data": null
}
```

This is not considered an error.

---

### Authorization

Authenticated users only.

Users may access only their own Saved Inputs.


---

## 47. PUT /api/v1/saved-inputs/{templateId}

Creates or updates the Saved Input for the specified Template.

The operation follows an **upsert** strategy.

---

### Request

```http
PUT /api/v1/saved-inputs/{templateId}
```

---

### Request Body

```json
{
  "inputData": {
    "GameDate": "2026-06-18",
    "Weather": "Sunny"
  },
  "selectedMasterData": {
    "GAME_KIND": {
      "id": 2
    }
  }
}
```

---

### Processing

The Backend shall:

1. Validate request payload.
2. Verify the Template exists.
3. Search for an existing Saved Input.

If a Saved Input exists:

* Update the existing record.

Otherwise:

* Create a new record.

Only one Saved Input shall exist for each:

```text
User

+

Template
```

---

### Validation

The API shall validate:

* Template exists.
* JSON payload is valid.
* Payload size does not exceed configured limits.

Business validation (required fields, XML rules, etc.) is intentionally not performed.

Saved drafts may contain incomplete or invalid data.

---

### Success Response

```json
{
  "success": true
}
```

---

### Authorization

Authenticated users only.

Users may update only their own Saved Inputs.

---

## 48. DELETE /api/v1/saved-inputs/{templateId}

Deletes the Saved Input for the specified Template.

---

### Request

```http
DELETE /api/v1/saved-inputs/{templateId}
```

---

### Processing

The Backend shall:

1. Verify ownership.
2. Delete the Saved Input if it exists.

Deleting a non-existing Saved Input shall be treated as a successful operation.

---

### Success Response

```json
{
  "success": true
}
```

---

### Authorization

Authenticated users only.

Users may delete only their own Saved Inputs.

---

## 49. Saved Input Lifecycle

Each Saved Input is uniquely identified by:

```text
User

+

Template
```

Lifecycle:

```text
First Save
    ↓
Create

Subsequent Save
    ↓
Update

Delete
    ↓
Removed
```

No version history is maintained.

No draft history is maintained.

This behavior is consistent with the MVP design.

---

## 50. Validation Rules

Saved Input APIs shall validate:

### Template

* Template exists
* Template is active

---

### Payload

* Valid JSON
* Maximum payload size
* Supported JSON structure

---

### Draft Compatibility

Saved Inputs are treated as raw user drafts.

The Backend shall not modify stored draft content to match newer Template versions.

Template compatibility shall be resolved by the client application when loading the draft.

---

The API intentionally shall not validate:

* Required fields
* XML business rules
* Master Data existence
* XML generation rules

Those validations occur only during:

```text
XML Preview

or

XML Export
```

---

## 51. Authorization Matrix

| API             | Admin | User |
| --------------- | :---: | :--: |
| GET Saved Input |   ✅   |   ✅  |
| Save Draft      |   ✅   |   ✅  |
| Delete Draft    |   ✅   |   ✅  |

All operations are restricted to the authenticated user's own drafts.

Administrators are not permitted to access or modify another user's Saved Inputs through these APIs.

---

## 52. Error Codes

| Error Code             | Description                                          |
| ---------------------- | ---------------------------------------------------- |
| TEMPLATE_NOT_FOUND     | Template does not exist.                             |
| TEMPLATE_INACTIVE      | Template is not available.                           |
| INVALID_JSON_STRUCTURE | Request body is not valid JSON.                      |
| PAYLOAD_TOO_LARGE      | Request exceeds configured limits.                   |
| FORBIDDEN              | User attempted to access another user's Saved Input. |
| VALIDATION_FAILED      | Request validation failed.                           |
| INTERNAL_SERVER_ERROR  | Unexpected server error.                             |
