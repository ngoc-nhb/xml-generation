# Part 8. API Standards & Common Models

---

## 73. Standard Request Headers

All API requests shall use UTF-8 encoding.

### Required Headers

```http
Content-Type: application/json
```

---

Authenticated requests shall include the authentication information according to the deployment architecture.

The authentication transport mechanism (for example, Session Cookie or Bearer Token) is outside the scope of this document.

---

### Optional Headers

```http
Accept: application/json
```

---

For XML Download APIs:

```http
Accept: application/xml
```

---

## 74. Standard Response Headers

JSON APIs shall return:

```http
Content-Type: application/json
```

---

XML Download APIs shall return:

```http
Content-Type: application/xml

Content-Disposition: attachment; filename="<generated-file-name>"
```

where:

```text
<generated-file-name>
```

is the filename stored in the Export History record.

---

## 75. Standard Response Model

All APIs shall follow a common response structure.

---

### Success Response

```json
{
    "success": true,
    "data": {}
}
```

---

### Error Response

```json
{
    "success": false,
    "errors": [
        {
            "code": "VALIDATION_FAILED"
        }
    ]
}
```

---

### Paginated Response

```json
{
    "success": true,
    "data": [],
    "meta": {
        "page": 1,
        "pageSize": 20,
        "totalRecords": 125,
        "totalPages": 7
    }
}
```

---

## 76. Error Object

Every error object shall follow the structure below.

```json
{
    "field": "GameDate",
    "code": "INVALID_DATE_FORMAT"
}
```

---

### Properties

| Property | Required | Description                        |
| -------- | -------- | ---------------------------------- |
| code     | Yes      | Stable machine-readable error code |
| field    | No       | Related request field              |

---

The Backend shall not return localized error messages.

Frontend applications are responsible for translating error codes.

---

## 77. Common HTTP Status Codes

| Status                    | Meaning                              |
| ------------------------- | ------------------------------------ |
| 200 OK                    | Successful request                   |
| 201 Created               | Resource created                     |
| 400 Bad Request           | Invalid request                      |
| 401 Unauthorized          | Authentication failed                |
| 403 Forbidden             | Permission denied                    |
| 404 Not Found             | Resource not found                   |
| 409 Conflict              | Version conflict / business conflict |
| 413 Payload Too Large     | Payload exceeds configured limit     |
| 500 Internal Server Error | Unexpected server error              |

---

## 78. Pagination Standard

Collection APIs shall support pagination.

---

### Request

```http
?page=1&pageSize=20
```

---

### Response

```json
{
    "meta": {
        "page":1,
        "pageSize":20,
        "totalRecords":250,
        "totalPages":13
    }
}
```

---

Default page size:

```text
20
```

Maximum page size:

```text
100
```

---

## 79. Optimistic Locking Standard

Editable resources supporting concurrent editing shall use optimistic locking.

Supported resources:

* Template Schema
* Master Data Schema

---

Requests shall include:

```json
{
    "version":12
}
```

The Backend shall compare the submitted version with the current database version.

If the versions differ:

```http
409 Conflict
```

shall be returned.

---

## 80. Naming Conventions

### API Path

Use:

```text
kebab-case
```

Examples:

```http
/export-histories

/master-data-types

/saved-inputs
```

---

### JSON Property

Use:

```text
camelCase
```

Examples:

```text
templateId

fileNamePattern

pageSize
```

---

### Error Code

Use:

```text
UPPER_SNAKE_CASE
```

Examples:

```text
MASTER_DATA_NOT_FOUND

INVALID_DATE_FORMAT

PAYLOAD_TOO_LARGE
```

---

## 81. API Design Principles

All APIs shall comply with the following principles.

### Stateless

Each request shall contain all information required for processing.

---

### Deterministic

The same request shall always produce the same result.

---

### Idempotent

The following operations are idempotent:

* GET
* PUT
* DELETE

where applicable.

---

### Immutable History

Export History records are immutable.

No Update API.

No Delete API.

---

### Separation of Responsibilities

Each API shall have a single responsibility.

Examples:

* Save Draft
* Compile Template
* Preview XML
* Export XML

rather than combining multiple responsibilities into a single endpoint.

---

## 82. API Backward Compatibility

Future API versions shall preserve compatibility whenever possible.

Breaking changes shall be introduced only through a new API version.

Example:

```text
/api/v1

↓

/api/v2
```

Existing API versions may continue to operate until officially deprecated.

---

## 83. MVP Scope

The current API design includes:

* Authentication
* Template Management
* Master Data Management
* Saved Input Management
* XML Preview
* XML Export
* Export History

The following capabilities are intentionally excluded:

* GraphQL
* Batch APIs
* WebSocket APIs
* Async Export APIs
* Public APIs
* Multi-Tenant APIs

These capabilities may be introduced in future phases as business requirements evolve.
