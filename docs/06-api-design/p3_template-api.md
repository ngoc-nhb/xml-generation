# Part 3. Template Management APIs

---

## 21. Overview

Template Management APIs allow administrators to create, modify, manage, and delete XML templates.

A Template defines:

* XML structure
* XML node hierarchy
* Field mapping
* Validation rules
* XML output configuration
* File naming pattern

Template Management APIs are restricted to administrators.

---

## 22. GET /api/v1/templates

Returns a paginated list of templates.

### Request

```http
GET /api/v1/templates
```

---

### Query Parameters

| Name     | Type    | Required | Description                    |
| -------- | ------- | -------- | ------------------------------ |
| page     | integer | No       | Page number (default: 1)       |
| pageSize | integer | No       | Records per page (default: 20) |
| keyword  | string  | No       | Search by template name        |

---

### Success Response

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "templateCode": "LIVE_GAME",
      "templateName": "Live Game",
      "status": "ACTIVE"
    }
  ],
  "meta": {
    "page": 1,
    "pageSize": 20,
    "totalRecords": 1,
    "totalPages": 1
  }
}
```

---

### Authorization

Admin only.

---

## 23. GET /api/v1/templates/{id}

Returns the complete editable template definition.

### Request

```http
GET /api/v1/templates/{id}
```

---

### Success Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "templateCode": "LIVE_GAME",
    "templateName": "Live Game",
    "description": "J League Live Match XML",
    "fileNamePattern": "live_game.xml",
    "fields": [
      {
        "id": 1,
        "parentId": null,
        "name": "LiveGame",
        "fieldType": "GROUP",
        "displayOrder": 1
      }
    ],
    "mappings": [],
    "compiledSchema": {}
  }
}
```

---

### Notes

This API returns both:

* Editable template definition (`fields`, `mappings`)
* Runtime representation (`compiledSchema`)

The editable definition is used by the Template Editor.

The compiled schema is provided for inspection only and shall not be modified directly.

---

### Authorization

Admin only.


---

## 24. POST /api/v1/templates

Creates a new template.

### Request

```http
POST /api/v1/templates
```

---

### Request Body

```json
{
  "templateCode": "LIVE_GAME",
  "templateName": "Live Game",
  "description": "J League Live Match XML",
  "fileNamePattern": "live_game.xml"
}
```

---

### Processing

The Backend shall:

1. Validate request
2. Create an empty Template
3. Initialize an empty editable schema
4. Persist the Template

Compilation is not performed during template creation.

---

### Success Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": 1
  }
}
```

---

### Validation

The API shall validate:

* templateCode is unique
* templateName is required
* fileNamePattern is required

---

### Authorization

Admin only.

---

## 25. PUT /api/v1/templates/{id}

Updates Template metadata.

This API updates only Template information.

Examples:

* Template Name
* Description
* File Name Pattern

This API does not update Template Fields.

This API does not perform template compilation.

---

### Request

```http
PUT /api/v1/templates/{id}
```

---

### Success Response

```json
{
    "success": true
}
```

---

### Transaction

Template metadata shall be updated within a single database transaction.

Compilation is intentionally excluded from this API.

---

### Authorization

Admin only.

---

## 25A. PUT /api/v1/templates/{id}/schema

Updates the editable Template Schema.

The request contains the complete editable XML structure.

---

### Request Body

```json
{
    "version": 12,
    "fields": [],
    "mappings": []
}
```

---

### Processing

The Backend shall:

1. Validate payload structure
2. Verify the submitted version against the current Template version
3. Replace the existing editable schema
4. Persist Template Fields
5. Persist Mapping configuration
6. Increment the Template version

The editable schema may contain validation errors.

Compilation is not performed.

---

### Optimistic Locking

The API shall use optimistic locking to prevent concurrent update conflicts.

The submitted:

```text
version
```

must match the current version stored in the database.

If the submitted version is outdated, the update shall be rejected.

Example:

```text
Database Version = 13

Request Version = 12

↓

409 Conflict
```

---

### Conflict Response

```json
{
    "success": false,
    "errors": [
        {
            "code": "TEMPLATE_VERSION_CONFLICT"
        }
    ]
}
```

The client should reload the latest Template before attempting another update.

---

### Notes

This API supports Save Draft.

The schema is validated only for structural integrity.

Business validation and runtime compilation occur only when:

```http
POST /api/v1/templates/{id}/compile
```

is executed.


---

## 26. DELETE /api/v1/templates/{id}

Deletes a template.

### Request

```http
DELETE /api/v1/templates/{id}
```

---

### Processing

The Backend shall validate whether the template can be deleted.

Templates currently referenced by:

* Saved Inputs
* Export Histories

shall not be deleted.

---

### Success Response

```json
{
  "success": true
}
```

---

### Failure Response

```json
{
  "success": false,
  "errors": [
    {
      "code": "TEMPLATE_IN_USE"
    }
  ]
}
```

---

### Authorization

Admin only.

---

## 27. POST /api/v1/templates/{id}/compile

Compiles the current editable template into its runtime representation.

---

### Purpose

Generate:

```text
compiled_schema_json
```

used by the XML Generation Engine.

---

### Processing

The Backend shall:

1. Load Template
2. Load Template Fields
3. Load Mapping Rules
4. Validate Template
5. Build Template Tree
6. Generate compiled_schema_json
7. Update Template

All operations shall execute within a single database transaction.

If compilation fails:

* Editable Template
* Template Fields
* Mapping Configuration

shall remain unchanged.

Only:

```text
compiled_schema_json
```

shall fail to update.

---

### Success Response

```json
{
    "success": true
}
```

---

### Failure Response

```json
{
    "success": false,
    "errors": [
        {
            "code": "TEMPLATE_COMPILATION_FAILED"
        }
    ]
}
```

---

### Notes

Compilation is an explicit administrator action.

Template editing and template compilation are intentionally separated to support draft editing and iterative development.

---

### Authorization

Admin only.


---

## 28. Validation Rules

Template Management APIs shall validate:

### Template Code

* Required
* Unique
* Maximum length
* Uppercase format

---

### Template Name

* Required
* Maximum length

---

### File Name Pattern

* Required
* Valid placeholder syntax

Examples:

```text
live_game.xml

schedule-{game_kind_id}.xml

team_stats-{team_id}.xml
```

---

### Compilation Validation

Compilation shall fail when:

* Invalid XML hierarchy
* Circular references
* Invalid mapping
* Duplicate displayOrder
* Invalid placeholder syntax
* Invalid activation rules

---

## 29. Authorization Matrix

| API                 | Admin | User |
| ------------------- | :---: | :--: |
| GET Templates       |   ✅   |   ❌  |
| GET Template Detail |   ✅   |   ❌  |
| Create Template     |   ✅   |   ❌  |
| Update Template     |   ✅   |   ❌  |
| Delete Template     |   ✅   |   ❌  |
| Compile Template    |   ✅   |   ❌  |

---

## 30. Error Codes

| Error Code                   | Description                                                        |
| ---------------------------- | ------------------------------------------------------------------ |
| TEMPLATE_NOT_FOUND           | Template does not exist.                                           |
| TEMPLATE_CODE_ALREADY_EXISTS | Template code already exists.                                      |
| TEMPLATE_IN_USE              | Template cannot be deleted because it is referenced by other data. |
| INVALID_FILE_NAME_PATTERN    | File name pattern is invalid.                                      |
| TEMPLATE_COMPILATION_FAILED  | Template compilation failed.                                       |
| VALIDATION_FAILED            | Request validation failed.                                         |
| FORBIDDEN                    | User does not have permission.                                     |
| INTERNAL_SERVER_ERROR        | Unexpected server error.                                           |
