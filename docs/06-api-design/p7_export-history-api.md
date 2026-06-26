# Part 7. Export History APIs

---

## 64. Overview

Export History APIs allow authenticated users to view previously generated XML exports.

An Export History record represents one successful or failed execution of the XML Export process.

These APIs provide:

* Export History List
* Export History Detail
* XML File Download

Export History records are immutable after creation.

---

## 65. GET /api/v1/export-histories

Returns a paginated list of Export History records.

### Request

```http
GET /api/v1/export-histories
```

---

### Query Parameters

| Name       | Type     | Required | Description                   |
| ---------- | -------- | -------- | ----------------------------- |
| page       | integer  | No       | Page number                   |
| pageSize   | integer  | No       | Records per page              |
| templateId | integer  | No       | Filter by Template            |
| status     | string   | No       | PROCESSING / SUCCESS / FAILED |
| fromDate   | datetime | No       | Start date                    |
| toDate     | datetime | No       | End date                      |

---

### Processing

The Backend shall:

1. Verify the authenticated user.
2. Return only Export Histories owned by the authenticated user.
3. Apply optional filters.
4. Return paginated results.

Administrators are subject to the same ownership restriction in Phase 1.

---

### Success Response

```json
{
  "success": true,
  "data": [
    {
      "id": 101,
      "templateId": 1,
      "templateName": "Live Game",
      "status": "SUCCESS",
      "fileName": "live_game.xml",
      "createdAt": "2026-06-25T10:30:00Z"
    }
  ],
  "meta": {
    "page": 1,
    "pageSize": 20,
    "totalRecords": 15,
    "totalPages": 1
  }
}
```

---

### Authorization

Authenticated users only.

---

## 66. GET /api/v1/export-histories/{id}

Returns the detail of a single Export History.

---

### Request

```http
GET /api/v1/export-histories/{id}
```

---

### Processing

The Backend shall:

1. Verify authentication.
2. Verify ownership.
3. Return the Export History.

---

### Success Response

```json
{
  "success": true,
  "data": {
    "id": 101,
    "templateId": 1,
    "templateName": "Live Game",
    "status": "SUCCESS",
    "fileName": "live_game.xml",
    "createdAt": "2026-06-25T10:30:00Z",
    "expiredAt": "2026-07-25T10:30:00Z"
    }
}
```

---

### Authorization

Authenticated users only.

---

## 67. GET /api/v1/export-histories/{id}/download

Downloads the generated XML file.

Only successfully generated XML files may be downloaded.

---

### Request

```http
GET /api/v1/export-histories/{id}/download
```

---

### Processing

The Backend shall:

1. Verify authentication.
2. Verify ownership.
3. Verify the Export History status is:

```text
SUCCESS
```

4. Verify the physical XML file exists.
5. Stream the XML file directly to the client.

---

### Success Response

```http
200 OK

Content-Type: application/xml

Content-Disposition: attachment; filename="live_game.xml"
```

The XML file shall be streamed directly to the client.

The Backend shall not load the entire XML file into memory before transmission.

The filename used in the `Content-Disposition` header shall be the generated filename stored in the corresponding Export History record.

---

### Failure Conditions

The API shall reject download when:

* Export History does not exist
* User is not the owner
* Export status is FAILED
* Export status is PROCESSING
* Physical XML file has expired
* Physical XML file cannot be found

---

### Error Codes

```text
EXPORT_NOT_READY

EXPORT_FILE_NOT_FOUND

FORBIDDEN
```


---

## 68. Export History Lifecycle

Each Export History progresses through the following lifecycle.

```text
Export Requested

↓

PROCESSING

↓

SUCCESS

or

FAILED
```

Export History records are immutable.

The status may transition only:

```text
PROCESSING

↓

SUCCESS

or

FAILED
```

No other transitions are permitted.

---

## 69. Retention Policy

Export Histories may reference XML files that expire.

Expiration is determined by:

```text
expired_at
```

---

### Expired File

When:

```text
current_time > expired_at
```

the physical XML file may no longer be available.

The Export History record itself may remain available for auditing purposes.

---

### Download Behavior

If the XML file has expired:

```http
404 Not Found
```

shall be returned.

Error Code:

```text
EXPORT_FILE_NOT_FOUND
```

---

## 70. Authorization Matrix

| API                   | Admin | User |
| --------------------- | :---: | :--: |
| List Export History   |   ✅   |   ✅  |
| Export History Detail |   ✅   |   ✅  |
| Download XML          |   ✅   |   ✅  |

All operations are limited to Export Histories owned by the authenticated user.

---

## 71. Error Codes

| Error Code               | Description                                             |
| ------------------------ | ------------------------------------------------------- |
| EXPORT_HISTORY_NOT_FOUND | Export History does not exist.                          |
| EXPORT_NOT_READY         | Export is still processing.                             |
| EXPORT_FILE_NOT_FOUND    | XML file does not exist or has expired.                 |
| FORBIDDEN                | User attempted to access another user's Export History. |
| VALIDATION_FAILED        | Request validation failed.                              |
| INTERNAL_SERVER_ERROR    | Unexpected server error.                                |

---

### 72. Design Principles

The Export History APIs follow the architectural decisions below.

---

### Ownership

Every Export History belongs to exactly one user.

Users may access only their own Export Histories.

---

### Immutability

Export History records are immutable after creation.

No Update API is provided.

No Delete API is provided.

---

### Download Security

Downloads shall always verify:

* Authentication
* Ownership
* Export Status
* Physical File Availability

before returning any XML file.

---

### Audit Preservation

The XML file may expire according to the configured retention policy.

The Export History record may remain available after file expiration to preserve auditability.

---

### Streaming Download

The XML file shall be streamed directly to the client.

The Backend shall not load the entire XML file into memory before sending it to the client.

This design is consistent with the Streaming XML architecture defined in:

```text
05-xml-generation-engine.md
```

---

### Future Enhancements

Future versions of the system may introduce automated retention management for Export History records.

Examples include:

* Scheduled cleanup of historical records after a configurable retention period
* Archival of old Export History records
* User-configurable retention policies

These capabilities are intentionally excluded from Phase 1 to preserve a simple and deterministic audit model.

