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

Returns the complete editable template definition reconstructed from metadata.

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
    "code": "LIVE_GAME",
    "name": "Live Game",
    "description": "J League Live Match XML",
    "status": "ACTIVE",
    "createdAt": "2026-01-01T00:00:00Z",
    "updatedAt": "2026-01-02T00:00:00Z",
    "schema": {
      "version": null,
      "fields": [
        {
          "fieldName": "Game",
          "parentFieldName": null,
          "xmlName": "Game",
          "displayName": "Game",
          "nodeType": "GROUP",
          "valueType": null,
          "sourceType": null,
          "occurrenceRule": null,
          "emptyHandling": "REQUIRED",
          "requiredWhenParentExists": false,
          "triggerActivation": null,
          "defaultValue": null,
          "staticValue": null,
          "xmlPath": null,
          "namespace": null,
          "displayOrder": 1,
          "description": null
        },
        {
          "fieldName": "GameKindId",
          "parentFieldName": "Game",
          "xmlName": "GameKindID",
          "displayName": "Game Kind ID",
          "nodeType": "ELEMENT",
          "valueType": null,
          "sourceType": "MASTER_DATA",
          "occurrenceRule": null,
          "emptyHandling": "REQUIRED",
          "requiredWhenParentExists": false,
          "triggerActivation": null,
          "defaultValue": null,
          "staticValue": null,
          "xmlPath": null,
          "namespace": null,
          "displayOrder": 1,
          "description": null
        }
      ],
      "mappings": [
        {
          "fieldName": "GameKindId",
          "masterDataFieldId": 99
        }
      ]
    }
  }
}
```

When the template has no `TemplateField` or `TemplateMapping` rows:

```json
{
  "schema": null
}
```

---

### Processing

The Backend shall:

1. Load `Template`
2. Load `TemplateField` rows ordered by `display_order`
3. Load `TemplateMapping` rows
4. Reconstruct `schema.fields` and `schema.mappings` from metadata
5. Resolve `parentFieldName` from `parent_id`

The Backend shall **not** read `compiled_schema_json` for this API.

---

### Notes

* `parentFieldName` is part of the official schema contract (request and response).
* `schema.version` is reserved for optimistic locking on schema save; it is `null`
  until schema versioning is persisted on metadata.
* `compiled_schema_json` is not returned by this API.

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
  "code": "LIVE_GAME",
  "name": "Live Game",
  "description": "J League Live Match XML",
  "schema": null
}
```

Optional `schema` uses the same `fields` and `mappings` shape as
`PUT /api/v1/templates/{id}/schema`.

---

### Processing

The Backend shall execute **one atomic transaction**:

1. Validate request
2. Create Template
3. If `schema` is omitted: persist Template only
4. If `schema` is provided:
   * Validate metadata (structure and hierarchy only)
   * Persist `TemplateField` and `TemplateMapping` rows
   * Resolve `parentFieldName` → `parent_id`
   * Invoke `TemplateCompilationOrchestrator` to generate `compiled_schema_json`
5. Roll back the entire transaction if persistence or compilation fails

When no schema is provided, `compiled_schema_json` remains `NULL`.

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

* `code` is unique
* `name` is required
* Metadata validation when `schema` is provided

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

The request contains the **complete** metadata definition. The Backend replaces
all existing `TemplateField` and `TemplateMapping` rows for the template.

---

### Request Body

```json
{
  "version": null,
  "fields": [
    {
      "fieldName": "Game",
      "parentFieldName": null,
      "xmlName": "Game",
      "displayName": "Game",
      "nodeType": "GROUP",
      "emptyHandling": "REQUIRED",
      "displayOrder": 1
    }
  ],
  "mappings": []
}
```

`version` is reserved for future optimistic locking and is ignored in the current
phase.

---

### Processing

The Backend shall execute **one atomic transaction**:

1. Validate metadata (structure and hierarchy only)
2. Delete all existing `TemplateMapping` rows for the template
3. Delete all existing `TemplateField` rows for the template
4. Persist the submitted `fields` and `mappings`
5. Resolve `parentFieldName` → `parent_id`
6. Invoke `TemplateCompilationOrchestrator` to generate or clear
   `compiled_schema_json`
7. Return the schema reconstructed from persisted metadata

If validation, persistence, or compilation fails, the entire transaction rolls
back.

---

### Success Response

Returns the updated schema reconstructed from metadata (same shape as
`GET /api/v1/templates/{id}` → `schema`).

When the submitted schema is empty:

```json
{
  "success": true,
  "data": null
}
```

---

### Metadata Validation

The API validates metadata only:

| Rule | Error code |
|------|------------|
| Duplicate `fieldName` | `TEMPLATE_FIELD_NAME_DUPLICATE` |
| Unknown `parentFieldName` | `TEMPLATE_PARENT_FIELD_NOT_FOUND` |
| Field is its own parent | `TEMPLATE_INVALID_HIERARCHY` |
| Cyclic parent chain | `TEMPLATE_PARENT_CYCLE` |
| Mapping references unknown field | `TEMPLATE_FIELD_NOT_FOUND` |
| Duplicate mapping per field | `TEMPLATE_MAPPING_DUPLICATE` |
| Mappings without fields | `TEMPLATE_FIELD_NOT_FOUND` |

XML validation, Mapping Engine validation, and compile-time mapping rules
(`source_type = MASTER_DATA`, unexpected mappings) are deferred to the dedicated
compile-validation phase.

---

### Notes

* `fields` and `mappings` form one metadata definition (Single Save Principle).
* `parentFieldName` is part of the official schema contract.
* Optimistic locking via `version` is deferred to a later phase.

---

## 26A. POST /api/v1/templates/{id}/preview

Generates XML for preview purposes without creating an export file or export history.

### Request

```http
POST /api/v1/templates/{id}/preview
```

### Request Body

```json
{
  "inputData": {},
  "selectedMasterData": {}
}
```

Both properties are optional. An empty body is treated as empty input and empty master
data selection.

### Success Response

```http
200 OK
```

```json
{
  "success": true,
  "data": {
    "xml": "<Game>...</Game>"
  }
}
```

### Validation Failure

Runtime validation errors are returned in the standard error envelope. The HTTP status
remains `200 OK` because the request was valid and the engine completed validation.

```json
{
  "success": false,
  "errors": [
    {
      "field": "GameId",
      "code": "SOURCE_TYPE_REQUIRED"
    }
  ]
}
```

### Processing Errors

| Condition | HTTP Status | Error Code |
| --------- | ----------- | ---------- |
| Template not found | 404 | `TEMPLATE_NOT_FOUND` |
| Template not compiled | 400 | `TEMPLATE_NOT_COMPILED` |
| Invalid JSON body | 400 | `VALIDATION_FAILED` |

The controller delegates orchestration to `PreviewService`, which invokes
`RuntimeExecutionOrchestrator`. The response does not expose runtime engine internal
models such as `RuntimeExecutionTree`.

---

## 26B. POST /api/v1/templates/{id}/export

Generates XML for export purposes. Phase 5.5 returns generated XML in the response
body only. File download, export history, and storage are deferred.

### Request

```http
POST /api/v1/templates/{id}/export
```

### Request Body

```json
{
  "inputData": {},
  "selectedMasterData": {}
}
```

Both properties are optional. An empty body is treated as empty input and empty master
data selection.

### Success Response

```http
200 OK
```

```json
{
  "success": true,
  "data": {
    "xml": "<Game>...</Game>"
  }
}
```

### Validation Failure

Runtime validation errors use the same envelope as Preview. HTTP status remains
`200 OK`.

```json
{
  "success": false,
  "errors": [
    {
      "field": "GameId",
      "code": "SOURCE_TYPE_REQUIRED"
    }
  ]
}
```

### Processing Errors

| Condition | HTTP Status | Error Code |
| --------- | ----------- | ---------- |
| Template not found | 404 | `TEMPLATE_NOT_FOUND` |
| Template not compiled | 400 | `TEMPLATE_NOT_COMPILED` |
| Invalid JSON body | 400 | `VALIDATION_FAILED` |

The controller delegates orchestration to `ExportService`, which invokes
`RuntimeExecutionOrchestrator`. The response exposes `xml` only and does not expose
runtime engine internal models.

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

## 27. Validation Rules

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

## 28. Authorization Matrix

| API                   | Admin | User |
| --------------------- | :---: | :--: |
| GET Templates         |   ✅   |   ❌  |
| GET Template Detail   |   ✅   |   ❌  |
| Create Template       |   ✅   |   ❌  |
| Update Template       |   ✅   |   ❌  |
| Update Template Schema|   ✅   |   ❌  |
| Delete Template       |   ✅   |   ❌  |

Compilation runs automatically during **Create Template** (when `schema` is
provided) and **Update Template Schema**. There is no standalone compile API.

---

## 29. Error Codes

| Error Code                   | Description                                                        |
| ---------------------------- | ------------------------------------------------------------------ |
| TEMPLATE_NOT_FOUND           | Template does not exist.                                           |
| TEMPLATE_CODE_ALREADY_EXISTS | Template code already exists.                                      |
| TEMPLATE_IN_USE              | Template cannot be deleted because it is referenced by other data. |
| INVALID_FILE_NAME_PATTERN    | File name pattern is invalid.                                      |
| VALIDATION_FAILED            | Request validation failed.                                         |
| FORBIDDEN                    | User does not have permission.                                     |
| INTERNAL_SERVER_ERROR        | Unexpected server error.                                           |
