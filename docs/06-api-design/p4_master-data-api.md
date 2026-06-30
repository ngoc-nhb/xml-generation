# Part 4. Master Data Management APIs

---

## v1.0.0 Implementation Note (Phase 7)

**Canonical REST paths** (implemented in backend, frontend, and Postman):

| Resource | Base path |
| -------- | --------- |
| Types | `/api/v1/master-data/types` |
| Fields | `/api/v1/master-data/fields` |
| Records | `/api/v1/master-data/records` |

Authoritative contract: `docs/release/API-CONTRACT.md`.

Sections below retain the **original design draft** using legacy path names
(`/master-data-types`, `/master-data-records`, nested type schema). Those paths were
**not implemented**. Do not build clients against legacy paths.

Field management uses a **separate Fields resource** (`GET/POST/PUT/DELETE /master-data/fields`)
rather than `PUT /master-data-types/{id}/schema`.

---

## 31. Overview

Master Data Management APIs allow administrators to manage reusable reference data used during XML generation.

Master Data is shared across multiple Templates and is resolved by the XML Generation Engine at runtime.

These APIs are restricted to administrators.

---

## 32. GET /api/v1/master-data-types

Returns a paginated list of Master Data Types.

### Request

```http
GET /api/v1/master-data-types
```

---

### Query Parameters

| Name     | Type    | Required | Description      |
| -------- | ------- | -------- | ---------------- |
| page     | integer | No       | Page number      |
| pageSize | integer | No       | Records per page |
| keyword  | string  | No       | Search by name   |

---

### Success Response

```json
{
  "success": true,
  "data": [
    {
      "id": 1,
      "code": "GAME_KIND",
      "name": "Game Kind"
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

## 33. GET /api/v1/master-data-types/{id}

Returns the complete editable Master Data definition.

### Request

```http
GET /api/v1/master-data-types/{id}
```

---

### Success Response

```json
{
  "success": true,
  "data": {
    "id": 1,
    "code": "GAME_KIND",
    "name": "Game Kind",
    "fields": [
      {
        "id": 1,
        "fieldName": "game_kind_id",
        "dataType": "INTEGER",
        "required": true
      },
      {
        "id": 2,
        "fieldName": "game_kind_name",
        "dataType": "STRING",
        "required": true
      }
    ]
  }
}
```

---

### Notes

This API returns the editable Master Data schema.

It does not return Master Data records.

---

### Authorization

Admin only.

---

## 34. POST /api/v1/master-data-types

Creates a new Master Data Type.

### Request

```http
POST /api/v1/master-data-types
```

---

### Request Body

```json
{
  "code": "GAME_KIND",
  "name": "Game Kind"
}
```

---

### Processing

The Backend shall:

1. Validate request
2. Create Master Data Type
3. Initialize an empty field definition
4. Persist data

---

### Validation

The API shall validate:

* code is required
* code is unique
* name is required

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

### Authorization

Admin only.

---

## 35. PUT /api/v1/master-data-types/{id}

Updates Master Data Type metadata.

This API updates only:

* Name
* Description

Field definitions are managed separately.

---

### Request

```http
PUT /api/v1/master-data-types/{id}
```

---

### Success Response

```json
{
  "success": true
}
```

---

### Authorization

Admin only.

---

## 36. PUT /api/v1/master-data-types/{id}/schema

Updates the editable field definitions of a Master Data Type.

The request replaces the complete schema.

---

### Request Body

```json
{
  "version": 5,
  "fields": [
    {
      "fieldName": "game_kind_id",
      "dataType": "INTEGER",
      "required": true
    }
  ]
}
```

---

### Processing

The Backend shall:

1. Validate payload
2. Verify version
3. Validate schema compatibility
4. Replace existing field definitions
5. Persist schema
6. Increment version

---

### Optimistic Locking

The API shall use optimistic locking.

If:

```text
Request Version != Database Version
```

the update shall be rejected.

---

### Existing Record Protection

If the Master Data Type already contains one or more Master Data Records, the Backend shall protect existing data integrity.

The following schema changes shall be rejected:

* Changing the data type of an existing field
* Removing an existing field
* Adding a new required field without a default value

These operations shall return:

```text
MASTER_DATA_SCHEMA_CONFLICT
```

This restriction prevents existing records from becoming incompatible with the updated schema.

---

### Conflict Response

```http
409 Conflict
```

```json
{
  "success": false,
  "errors": [
    {
      "code": "MASTER_DATA_SCHEMA_CONFLICT"
    }
  ]
}
```

---

### Notes

This API updates only the editable schema.

No automatic migration of existing Master Data Records is performed in Phase 1.


---

## 37. DELETE /api/v1/master-data-types/{id}

Deletes a Master Data Type.

---

### Processing

The Backend shall verify that the Master Data Type is not referenced by any Template Mapping.

If references exist:

```text
MASTER_DATA_IN_USE
```

shall be returned.

---

### Success Response

```json
{
  "success": true
}
```

---

### Authorization

Admin only.

---

## 38. GET /api/v1/master-data-types/{id}/records

Returns paginated Master Data Records belonging to the specified Master Data Type.

---

### Request

```http
GET /api/v1/master-data-types/{id}/records
```

---

### Query Parameters

| Name     | Type    | Required | Description      |
| -------- | ------- | -------- | ---------------- |
| page     | integer | No       | Page number      |
| pageSize | integer | No       | Records per page |

---

### Success Response

```json
{
  "success": true,
  "data": [
    {
      "id": 100,
      "data": {
        "game_kind_id": 1,
        "game_kind_name": "J1"
      }
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

## 39. POST /api/v1/master-data-records

Creates a Master Data Record.

---

### Request

```http
POST /api/v1/master-data-records
```

---

### Request Body

```json
{
  "masterDataTypeId": 1,
  "data": {
    "game_kind_id": 1,
    "game_kind_name": "J1"
  }
}
```

---

### Processing

The Backend shall validate the payload against the configured Master Data Field schema.

Validation includes:

* Required fields
* Data types
* Unknown fields
* Missing fields

---

### Success Response

```http
201 Created
```

```json
{
  "success": true,
  "data": {
    "id": 100
  }
}
```

---

### Authorization

Admin only.

---

## 40. PUT /api/v1/master-data-records/{id}

Updates a Master Data Record.

---

### Processing

The Backend shall validate the updated data against the current Master Data schema before persisting.

---

### Success Response

```json
{
  "success": true
}
```

---

### Authorization

Admin only.

---

## 41. DELETE /api/v1/master-data-records/{id}

Deletes a Master Data Record.

---

### Processing

The Backend shall verify that deleting the record will not violate business rules.

If deletion is not allowed:

```text
MASTER_DATA_RECORD_IN_USE
```

shall be returned.

---

### Success Response

```json
{
  "success": true
}
```

---

### Authorization

Admin only.

---

## 42. Validation Rules

Master Data Management APIs shall validate:

### Master Data Type

* Code is required
* Code is unique
* Name is required

---

### Field Definition

* Field name is required
* Field name is unique within a Master Data Type
* Supported data type only
* Display order is unique

---

### Master Data Record

* Required fields
* Data type validation
* Unknown field validation
* Missing field validation

Validation rules shall be based on the configured Master Data schema.

---

## 43. Authorization Matrix

| API                         | Admin | User |
| --------------------------- | :---: | :--: |
| GET Master Data Types       |   ✅   |   ❌  |
| GET Master Data Type Detail |   ✅   |   ❌  |
| Create Master Data Type     |   ✅   |   ❌  |
| Update Master Data Type     |   ✅   |   ❌  |
| Update Master Data Schema   |   ✅   |   ❌  |
| Delete Master Data Type     |   ✅   |   ❌  |
| GET Master Data Records     |   ✅   |   ❌  |
| Create Master Data Record   |   ✅   |   ❌  |
| Update Master Data Record   |   ✅   |   ❌  |
| Delete Master Data Record   |   ✅   |   ❌  |

---

## 44. Error Codes

| Error Code                      | Description                                         |
| ------------------------------- | --------------------------------------------------- |
| MASTER_DATA_TYPE_NOT_FOUND      | Master Data Type does not exist.                    |
| MASTER_DATA_RECORD_NOT_FOUND    | Master Data Record does not exist.                  |
| MASTER_DATA_TYPE_ALREADY_EXISTS | Code already exists.                                |
| MASTER_DATA_IN_USE              | Master Data Type is referenced by Template Mapping. |
| MASTER_DATA_RECORD_IN_USE       | Master Data Record cannot be deleted.               |
| MASTER_DATA_VERSION_CONFLICT    | Optimistic locking conflict.                        |
| VALIDATION_FAILED               | Request validation failed.                          |
| FORBIDDEN                       | User does not have permission.                      |
| INTERNAL_SERVER_ERROR           | Unexpected server error.                            |
