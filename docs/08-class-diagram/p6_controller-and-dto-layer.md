# Part 6. Controller and DTO Layer

---

# 60. Purpose

## Responsibility

Define the Presentation Layer of the application.

The Presentation Layer is responsible for:

* Receiving HTTP requests
* Validating request structure
* Mapping DTOs
* Invoking Application Services
* Returning HTTP responses

Controllers shall never implement business logic.

---

## Scope

This Part defines:

* Controllers
* Request DTOs
* Response DTOs
* Mapping responsibilities
* Controller interaction rules

---

# 61. Controller Overview

The system defines the following Controllers.

```text
AuthController

TemplateController

MasterDataController

SavedInputController

XMLController

ExportHistoryController
```

Each Controller represents one API resource.

---

# 62. AuthController

## Responsibility

Handle authentication requests.

---

## Endpoints

* POST /login
* POST /logout

---

## Dependencies

```text
AuthService
```

---

## Request DTOs

* LoginRequest

---

## Response DTOs

* LoginResponse

---

## Notes

AuthController shall not validate passwords.

Authentication logic belongs to AuthService.

---

# 63. TemplateController

## Responsibility

Manage Template APIs.

---

## Endpoints

* GET /templates
* GET /templates/{id}
* POST /templates
* PUT /templates/{id}
* PUT /templates/{id}/schema
* DELETE /templates/{id}

Compilation is triggered by `TemplateCompilationOrchestrator` during create-with-schema
and update-schema flows. There is no standalone compile endpoint.

---

## Dependencies

```text
TemplateService

TemplateCompilationOrchestrator
```

---

## Request DTOs

* CreateTemplateRequest
* UpdateTemplateRequest
* UpdateTemplateSchemaRequest

---

## Response DTOs

* TemplateResponse
* TemplateListResponse

---

## Notes

Schema save and create-with-schema trigger compilation through
`TemplateCompilationOrchestrator`. There is no standalone compile controller
endpoint.

---

# 64. MasterDataController

## Responsibility

Manage Master Data APIs.

---

## Endpoints

* GET /master-data-types

* POST /master-data-types

* PUT /master-data-types/{id}

* DELETE /master-data-types/{id}

* GET /master-data-types/{id}/records

* POST /master-data-types/{id}/records

* PUT /master-data-records/{id}

* DELETE /master-data-records/{id}

---

## Dependencies

```text
MasterDataService
```

---

## Request DTOs

* CreateMasterDataTypeRequest
* UpdateMasterDataTypeRequest
* CreateMasterDataRecordRequest
* UpdateMasterDataRecordRequest

---

## Response DTOs

* MasterDataTypeResponse
* MasterDataRecordResponse

---

## Notes

The Controller shall not interpret dynamic schemas.

Schema validation belongs to MasterDataService.

---

# 65. SavedInputController

## Responsibility

Manage Draft APIs.

---

## Endpoints

* GET /saved-inputs/{templateId}
* PUT /saved-inputs/{templateId}
* DELETE /saved-inputs/{templateId}

---

## Dependencies

```text
SavedInputService
```

---

## Request DTOs

* SaveDraftRequest

---

## Response DTOs

* SavedInputResponse

---

## Notes

SavedInputController manages persistence only.

Draft data shall never be loaded automatically by XML generation APIs.

---

# 66. XMLController

## Responsibility

Handle XML Preview and Export APIs.

---

## Endpoints

* POST /preview
* POST /export

---

## Dependencies

```text
PreviewService

ExportService
```

---

## Request DTOs

* XMLGenerationRequest

The same Request DTO shall be used for both Preview and Export APIs.

This ensures a single shared request contract across XML Generation operations.

---

## Response DTOs

* PreviewResponse
* ExportResponse
* ValidationErrorResponse

---

## Notes

Preview and Export requests shall be processed exclusively using the request payload.

Controllers shall not retrieve Saved Drafts automatically.

Preview and Export differ only in execution behavior.

They shall share the same request model.


---

# 67. ExportHistoryController

## Responsibility

Provide access to Export History resources.

---

## Endpoints

* GET /export-histories
* GET /export-histories/{id}
* GET /export-histories/{id}/download

---

## Dependencies

```text
ExportHistoryService
```

---

## Response DTOs

* ExportHistoryResponse
* ExportHistoryDetailResponse

---

## Notes

Download operations shall stream files directly to the client.

Controllers shall never load the entire XML file into memory.

---

# 68. DTO Design

## Request DTO

Request DTOs represent external API contracts.

They shall contain only transport data.

Request DTOs shall never contain business logic.

---

## Response DTO

Response DTOs represent API responses.

They shall never expose:

* Domain Entities
* Repository models
* Internal Engine objects

---

## Mapping Rule

```text
HTTP Request

↓

Request DTO

↓

Mapper

↓

Application Service

↓

Domain Model

↓

Mapper

↓

Response DTO

↓

HTTP Response
```

DTOs shall never be passed directly into the XML Engine.

---
## Shared Request Models

The Presentation Layer shall define reusable DTOs for common API patterns.

Examples include:

* PaginationRequest
* PaginatedResponse<T>

---

### PaginationRequest

Represents common pagination query parameters.

Typical fields include:

* page
* pageSize
* sortBy
* sortDirection

Controllers shall reuse this DTO instead of parsing pagination parameters individually.

---

### PaginatedResponse<T>

Represents a standardized paginated API response.

Typical fields include:

* data
* totalRecords
* totalPages
* currentPage
* pageSize

This structure shall be reused consistently across all list APIs.

---

## Mapping Rule

```text
HTTP Request

↓

Request DTO

↓

Mapper

↓

Application Service

↓

Domain Model

↓

Mapper

↓

Response DTO

↓

HTTP Response
```

DTOs shall never be passed directly into the XML Engine.


---

# 69. Mapper Responsibilities

Dedicated Mapper components are responsible for converting between:

```text
Request DTO

↓

Domain Model
```

and

```text
Domain Model

↓

Response DTO
```

Mapping shall be deterministic and free of business logic.

Typical Mapper components include:

* TemplateMapper
* MasterDataMapper
* SavedInputMapper
* ExportHistoryMapper

---

# 70. Controller Design Rules

Controllers shall:

* Receive HTTP requests
* Validate request format
* Invoke Application Services
* Return HTTP responses

Controllers shall never:

* Execute business rules
* Access repositories
* Invoke the XML Engine directly
* Perform persistence
* Build XML

---

# 71. Exception Handling

All unhandled exceptions shall be delegated to the Global Exception Handler.

Controllers shall not convert exceptions manually.

The Global Exception Handler is responsible for:

* HTTP Status mapping
* Error Code mapping
* Standardized error responses

The behavior shall conform to:

```text
06-api-design.md
```

---

# 72. Phase 1 Controller Decisions

| Topic                    | Decision                |
| ------------------------ | ----------------------- |
| Business Logic           | Prohibited              |
| Repository Access        | Prohibited              |
| XML Engine Access        | Service Layer Only      |
| DTO Usage                | Presentation Layer Only |
| Domain Entity Exposure   | Prohibited              |
| Mapper Components        | Required                |
| Global Exception Handler | Required                |
| Stateless Controllers    | Required                |
| Streaming Download       | Required                |
| Dependency Injection     | Required                |
| XML Generation Request | Shared DTO         |
| Pagination Request     | Shared DTO         |
| Pagination Response    | Shared Generic DTO |
