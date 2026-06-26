# Part 1. API Overview

---

## 1. Overview

This document defines the API contract for the XML Generator System.

The API layer provides access to:

* Authentication
* Template Management
* Master Data Management
* Saved Input Management
* XML Preview
* XML Export
* Export History

The API shall act as the boundary between:

```text
Frontend Application

↓

Backend Services

↓

Database
```

---

## 2. API Design Principles

All APIs shall follow the principles below.

### Stateless

Each request shall contain all information required for processing.

The server shall not maintain session state.

---

### JSON-Based Communication

Request and response payloads shall use:

```text
application/json
```

unless otherwise specified.

---

### RESTful Design

APIs shall follow REST conventions whenever practical.

Examples:

```http
GET    /templates

POST   /templates

PUT    /templates/{id}

DELETE /templates/{id}
```

---

### Consistent Response Structure

All APIs shall return a standardized response structure.

Success:

```json
{
  "success": true,
  "data": {}
}
```

Failure:

```json
{
  "success": false,
  "errors": []
}
```

---

## 3. API Categories

The system APIs are grouped into the following categories.

### Authentication APIs

Purpose:

```text
User Login

User Logout
```

---

### Template APIs

Purpose:

```text
Create Template

Update Template

Delete Template

View Template
```

---

### Master Data APIs

Purpose:

```text
Create Master Data Type

Manage Master Data Records

Manage Master Data Fields
```

---

### Saved Input APIs

Purpose:

```text
Load Saved Draft

Save Draft
```

---

### XML Generation APIs

Purpose:

```text
Preview XML

Export XML
```

---

### Export History APIs

Purpose:

```text
View Export History

Download Generated XML
```

---

## 4. Authentication Model

The system uses simple username/password authentication.

Supported credentials:

```text
username

password
```

---

The system intentionally excludes:

```text
OAuth

Refresh Token

Session Tracking

Remember Me

Social Login
```

for MVP.

---

### Authorization

The system supports:

```text
Admin

User
```

permissions.

Authorization rules are enforced at the API layer.

---

## 5. API Versioning

The API shall support versioning.

Current version:

```text
v1
```

Example:

```http
/api/v1/templates

/api/v1/xml/preview

/api/v1/xml/export
```

---

### Future Versions

Future versions may introduce:

```text
v2

v3
```

without breaking existing clients.

---

## 6. Request Standards

All requests shall use:

```text
UTF-8
```

encoding.

---

### Content Type

JSON requests:

```http
Content-Type: application/json
```

---

### Path Parameters

Example:

```http
GET /templates/123
```

where:

```text
123
```

is a path parameter.

---

### Query Parameters

Example:

```http
GET /export-histories?page=1&pageSize=20
```

---

### Request Body

Example:

```json
{
  "templateId": 1
}
```

---

## 7. Response Standards

Successful responses shall follow a consistent structure.

### Standard Success Response

```json
{
  "success": true,
  "data": {}
}
```

---

### Standard Error Response

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

APIs returning collections should include pagination metadata.

Example:

```json
{
  "success": true,
  "data": [
    {
      "id": 1
    },
    {
      "id": 2
    }
  ],
  "meta": {
    "page": 1,
    "pageSize": 20,
    "totalRecords": 156,
    "totalPages": 8
  }
}
```

---

### Response Requirements

Responses shall:

* Be deterministic
* Use UTF-8 encoding
* Follow a consistent structure
* Avoid exposing internal implementation details

Collection APIs should include pagination metadata when paging is supported.

---

### Internationalization

The Backend shall return:

```text
Error Codes
```

instead of localized messages.

Examples:

```text
INVALID_DATE_FORMAT

MASTER_DATA_NOT_FOUND

VALIDATION_FAILED
```

The Frontend is responsible for translating error codes into user-facing messages.


---

## 8. HTTP Status Codes

The API shall use standard HTTP status codes.

### Success

```http
200 OK
```

Request completed successfully.

---

```http
201 Created
```

Resource created successfully.

---

### Client Errors

```http
400 Bad Request
```

Invalid request payload.

---

```http
401 Unauthorized
```

Invalid credentials.

---

```http
403 Forbidden
```

User lacks required permissions.

---

```http
404 Not Found
```

Requested resource does not exist.

---

```http
409 Conflict
```

Resource conflict detected.

---

```http
413 Payload Too Large
```

Request exceeds configured limits.

---

### Server Errors

```http
500 Internal Server Error
```

Unexpected server error.

---

## 9. Error Handling Strategy

The API shall use:

```text
Collect All Errors
```

whenever possible.

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
      "field": "GameKindID",
      "code": "MASTER_DATA_NOT_FOUND"
    }
  ]
}
```

---

### Security

The API shall not expose:

* Stack traces
* Database errors
* Internal exception messages
* Sensitive configuration data

---

## 10. API Security Principles

The API shall require authentication for all business operations.

Exceptions:

```text
Login
```

---

### Access Control

Admin-only APIs:

```text
Template Management

Master Data Management
```

---

User APIs:

```text
Saved Input

XML Preview

XML Export

Export History
```

---

### Input Validation

All incoming requests shall be validated before processing.

Validation includes:

* Required fields
* Data types
* Maximum payload size
* Allowed value constraints

---

## 11. Performance Requirements

The API shall support the runtime targets defined in:

```text
05-xml-generation-engine.md
```

---

Target response times:

```text
Preview XML

< 3 seconds
```

---

```text
Export XML

< 3 seconds
```

under normal operating conditions.

---

## 12. Out of Scope

The following capabilities are intentionally excluded from MVP.

```text
GraphQL

WebSocket APIs

Bulk Export APIs

Batch Import APIs

API Rate Limiting

Multi-Tenant APIs

Public APIs
```

These capabilities may be introduced in future phases.
