# Part 6. XML Generation APIs

---

## 53. Overview

The XML Generation APIs execute the XML Generation Engine defined in:

```text
05-xml-generation-engine.md
```

The engine supports two execution modes:

* Preview
* Export

Both modes execute the same runtime pipeline.

The only difference is the final output.

---

## 54. Runtime Flow

Both Preview and Export execute the following pipeline:

```text
Validate Request

↓

Load Template

↓

Load Master Data

↓

Resolve Runtime Model

↓

Validation Engine

↓

XML Builder

↓

Preview Response

or

Export Processor
```

---

## 55. POST /api/v1/xml/preview

Generates XML for preview purposes.

No physical XML file shall be created.

No Export History shall be created.

---

### Request

```http
POST /api/v1/xml/preview
```

---

### Request Body

```json
{
  "templateId": 1,
  "templateVersion": 8,
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
2. Verify the submitted templateVersion matches the latest compiled Template.
3. Load compiled template.
4. Load referenced Master Data.
5. Build the Resolved Runtime Model.
6. Execute the Validation Engine.
7. Generate XML.
8. Return the generated XML.

The Export Processor shall not be executed.

---

### Version Validation

If the submitted Template version is older than the current compiled version, the request shall be rejected with:

```text
TEMPLATE_VERSION_OUTDATED
```

The client should reload the latest Template before retrying.


---

## 56. POST /api/v1/xml/export

Generates and exports an XML file.

Unlike Preview, Export shall:

* Generate XML
* Persist XML file
* Create Export History

---

### Request

```http
POST /api/v1/xml/export
```

---

### Request Body

The request body is identical to:

```http
POST /api/v1/xml/preview
```

---

### Processing

The Backend shall:

1. Validate request payload.
2. Load compiled template.
3. Load referenced Master Data.
4. Build the Resolved Runtime Model.
5. Execute the Validation Engine.
6. Generate XML.
7. Execute the Export Processor.
8. Create Export History.
9. Return export metadata.

---

### Success Response

```http
200 OK
```

```json
{
  "success": true,
  "data": {
    "exportHistoryId": 101,
    "fileName": "live_game.xml"
  }
}
```

---

### Validation Failure

Validation behavior is identical to Preview.

---

### Authorization

Authenticated users only.

---

## 57. Shared Request Model

Both Preview and Export use the same request structure.

```json
{
  "templateId": 1,
  "templateVersion": 8,
  "inputData": {},
  "selectedMasterData": {}
}
```

---

### Request Validation

The API shall validate:

* Template exists
* Template is ACTIVE
* Submitted Template version matches the latest compiled Template
* Valid JSON structure
* Payload size
* Duplicate Master Data selection

Business validation shall be performed by the XML Generation Engine.

If the submitted Template version is outdated, the API shall return:

```text
TEMPLATE_VERSION_OUTDATED
```

before XML generation begins.


---

## 58. Runtime Data Loading

Before XML generation, the Backend shall load:

### Template

Using:

```text
compiled_schema_json
```

Only compiled templates are executed.

---

### Master Data

The Backend shall load every referenced Master Data Record.

If any referenced record does not exist:

```text
MASTER_DATA_NOT_FOUND
```

shall be reported.

---

### Input Data

User input shall be passed directly to the Value Resolver.

---

## 59. Validation Strategy

The XML Generation APIs shall follow:

```text
Collect All Errors
```

Validation strategy.

The engine shall return every validation error discovered during execution.

Example:

```json
{
  "success": false,
  "errors": [
    {
      "field": "GameDate",
      "code": "INVALID_DATE_FORMAT"
    },
    {
      "field": "HomeTeam",
      "code": "REQUIRED_FIELD_MISSING"
    }
  ]
}
```

Immediate termination shall occur only for critical runtime failures defined in:

```text
05-xml-generation-engine.md
```

---

## 60. Authorization Matrix

| API         | Admin | User |
| ----------- | :---: | :--: |
| XML Preview |   ✅   |   ✅  |
| XML Export  |   ✅   |   ✅  |

All authenticated users may generate XML using Templates that are available for execution.

---

## 61. Error Codes

| Error Code                | Description                                                                |
| ------------------------- | -------------------------------------------------------------------------- |
| TEMPLATE_NOT_FOUND        | Template does not exist.                                                   |
| TEMPLATE_NOT_ACTIVE       | Template is not available for execution.                                   |
| TEMPLATE_NOT_COMPILED     | Template has not been successfully compiled.                               |
| TEMPLATE_VERSION_OUTDATED | The submitted Template version is older than the current compiled version. |
| MASTER_DATA_NOT_FOUND     | Referenced Master Data Record does not exist.                              |
| INVALID_JSON_STRUCTURE    | Invalid request body.                                                      |
| PAYLOAD_TOO_LARGE         | Payload exceeds configured limits.                                         |
| VALIDATION_FAILED         | XML validation failed.                                                     |
| EXPORT_FAILED             | Export process failed.                                                     |
| FORBIDDEN                 | User does not have permission.                                             |
| INTERNAL_SERVER_ERROR     | Unexpected server error.                                                   |


---

## 62. Performance Requirements

The XML Generation APIs shall satisfy the runtime performance requirements defined by the XML Generation Engine.

Target response time:

| API     | Target      |
| ------- | ----------- |
| Preview | < 3 seconds |
| Export  | < 3 seconds |

These targets assume:

* A compiled Template is available.
* Master Data is available.
* Normal server operating conditions.
* XML document sizes remain within the supported MVP operating range.

The Export API executes synchronously in Phase 1.

Future versions may introduce asynchronous export processing for large XML documents or long-running export operations.


---

## 63. Design Principles

The XML Generation APIs shall comply with the following architectural decisions.

### Compiled Templates

Only:

```text
compiled_schema_json
```

shall be used during runtime.

---

### Streaming XML Generation

The XML Builder shall generate XML using streaming.

The engine shall not construct an in-memory DOM tree.

---

### Preview vs Export

Preview and Export share the same runtime pipeline.

The only behavioral difference is the final processing step:

```text
Preview

↓

Return XML

Export

↓

Persist XML
+
Create Export History
```

---

### Draft Independence

The XML Generation APIs operate solely on the request payload.

Saved Inputs are never loaded automatically.

If a client wishes to generate XML from a Saved Input, it shall first retrieve the draft and then submit it as the request payload.

This separation keeps the XML Generation APIs stateless and deterministic.
