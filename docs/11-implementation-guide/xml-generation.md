# 03. XML Generation Module

---

# 1. Purpose

Generate XML documents from compiled Templates and user input.

This module is responsible for:

* XML Preview
* XML Export
* Runtime Model construction
* XML generation workflow

---

# 2. Scope

Included

* Preview XML
* Export XML
* Runtime Model preparation
* Engine invocation
* XML file generation

Excluded

* Template management
* Master Data management
* Saved Input management

---

# 3. Components

| Component               | Responsibility                 |
| ----------------------- | ------------------------------ |
| PreviewController       | Expose Preview REST API        |
| ExportController        | Expose Export REST API         |
| PreviewService          | Execute Preview workflow       |
| ExportService           | Execute Export workflow        |
| RuntimeModelFactory     | Build Runtime Model            |
| RuntimeLoader           | Load `compiled_schema_json` into `RuntimeTemplate` |
| RuntimeValidationService | Validate runtime model integrity after loading |
| ValueResolutionService | Produce `RuntimeExecutionTree` execution artifact |
| XMLGenerationService  | Serialize `RuntimeExecutionTree` to XML (public boundary) |
| RuntimeExecutionOrchestrator | Coordinate the runtime pipeline (application orchestration) |
| StorageProvider         | Persist exported XML           |
| ExportHistoryRepository | Persist export history         |

---

# 4. Responsibilities

## PreviewService

Responsible for:

* Load Template and assemble `RuntimeExecutionRequest`
* Invoke `RuntimeExecutionOrchestrator`
* Map `RuntimeExecutionResult` to Preview response

Preview shall never persist any data.

Preview must not invoke `RuntimeLoader`, `RuntimeValidationService`, `ValueResolutionService`, or `XMLGenerationService` directly.

```text
TemplateRepository
        ↓
build RuntimeExecutionRequest
        ↓
RuntimeExecutionOrchestrator
        ↓
RuntimeExecutionResult
        ↓
PreviewResponse
```

---

## PreviewController

Responsible for:

* Receive `POST /api/v1/templates/{id}/preview`
* Map HTTP request body to service `PreviewRequest`
* Invoke `PreviewService`
* Map service result to `ApiResponse`

The controller must not load repositories, build `RuntimeExecutionRequest`, or invoke
`RuntimeExecutionOrchestrator` directly.

Runtime validation failures return `success: false` with field-level errors in the
standard envelope (`200 OK`). Infrastructure errors (not found, not compiled, invalid
JSON) propagate through `GlobalExceptionHandler`.

---

## ExportController

Responsible for:

* Receive `POST /api/v1/templates/{id}/export`
* Map HTTP request body to service `ExportRequest`
* Invoke `ExportService`
* Map service result to `ApiResponse`

The controller must not load repositories, build `RuntimeExecutionRequest`, or invoke
`RuntimeExecutionOrchestrator` directly.

Phase 5.5 returns generated XML in the JSON response only. File download, export
history, and storage are deferred.

Runtime validation failures and infrastructure errors follow the same transport rules
as `PreviewController`.

---

## ExportService

Responsible for:

* Load Template and assemble `RuntimeExecutionRequest`
* Resolve mappings via `TemplateCompileMappingResolver`
* Invoke `RuntimeExecutionOrchestrator`
* Map `RuntimeExecutionResult` to `ExportResponse`

Export must not invoke runtime pipeline components directly.

Phase 5.4 implements orchestration and response mapping only. Storage, export history,
and REST endpoints are deferred.

```text
TemplateRepository
        ↓
TemplateCompileMappingResolver
        ↓
build RuntimeExecutionRequest
        ↓
RuntimeExecutionOrchestrator
        ↓
RuntimeExecutionResult
        ↓
ExportResponse
```

Future phases add file persistence and export metadata on top of this service.

---

## RuntimeModelFactory

Responsible for:

* Build Runtime Model
* Resolve required runtime resources
* Prepare Engine input

The factory shall not access HTTP objects.

---

# 5. Dependencies

```text
                XMLController
                     │
          ┌──────────┴──────────┐
          ▼                     ▼
 PreviewService          ExportService
          │                     │
          └──────────┬──────────┘
                     ▼
          RuntimeModelFactory
                     │
                     ▼
                XMLEngine
                     │
             XML Stream Result
                     │
                     ▼
              ExportService
             ├────────────► StorageProvider
             └────────────► ExportHistoryRepository
```

---

# 6. Public Interfaces

## PreviewService

preview()

↓

returns XML Stream
---

## ExportService

export()

↓

returns Export Result

---

## RuntimeModelFactory

* build()

---

## XMLEngine

* generate()

---

# 7. Runtime Model

`RuntimeTemplate` is the canonical runtime definition loaded from `compiled_schema_json`.

Execution inputs are supplied separately:

* User Input (`input_data_json`)
* Selected Master Data (`selected_master_data_json`)
* Mapping metadata (via `ValueResolutionContext`)

## Runtime Pipeline

After `compiled_schema_json` is loaded, the XML Generation module applies pure runtime transformations in order:

```text
compiled_schema_json
        ↓
RuntimeLoader
        ↓
RuntimeTemplate            (Canonical Runtime Model)
        ↓
RuntimeValidationService
        ↓
ValueResolutionService
        ↓
RuntimeExecutionTree         (Execution Artifact)
        ↓
XMLGenerationService
        ↓
XML
```

This is the project's **canonical runtime architecture**. Preview, Export, and future APIs shall invoke `RuntimeExecutionOrchestrator` rather than introducing new runtime models or bypassing existing stages.

### RuntimeExecutionOrchestrator

Application orchestration boundary for the runtime engine. Coordinates:

```text
RuntimeLoader → RuntimeValidationService → ValueResolutionService → XMLGenerationService
```

Must not parse metadata, compile schemas, resolve values, validate runtime, or serialize XML directly.

Validation failure returns `RuntimeExecutionResult.validationFailed(...)` without invoking later stages. Loader, resolution, and generation failures propagate from the owning component.

#### RuntimeExecutionRequest

Transport object only. Contains execution inputs (`compiledSchemaJson`, `inputData`, `selectedMasterData`, mappings). Extend with new immutable fields when needed. Must not become a service object.

#### RuntimeExecutionResult

Runtime Engine output only (`xml`, `executionTree`, `validationResult`). Application layers map this to Preview/Export responses. Do not add HTTP, persistence, or timing metadata here.

This mirrors the compile-time pattern:

```text
Metadata → Compiler → compiled_schema_json
RuntimeTemplate → Value Resolution → RuntimeExecutionTree
```

Both outputs are generated artifacts, not editable source-of-truth models.

### XML Generation Serialization Pipeline

The public XML generation boundary is `XMLGenerationService` only.

Internal implementation (not a pipeline component):

```text
RuntimeExecutionTree
        ↓
XMLGenerationService
        ↓
ExecutionTreeXmlWriter       (package-private internal helper)
        ↓
XMLStreamWriter              (StAX — project standard)
        ↓
String                       (MVP)
        ↓
Writer / OutputStream        (future extension — output sink only)
```

Rules:

* Use StAX (`XMLStreamWriter`). Do not use DOM.
* `ExecutionTreeXmlWriter` must remain package-private and must not appear in architecture diagrams.
* No interface is required for `ExecutionTreeXmlWriter` unless a second serializer implementation is justified.

### RuntimeValidationService

Operates on `RuntimeTemplate` immediately after loading and **before** value resolution.

Responsibilities:

* Validate executable runtime integrity (corrupted artifacts, impossible states, illegal structures)
* Accumulate validation errors and return `RuntimeValidationResult`

Must not:

* Access repositories
* Resolve values
* Generate XML
* Validate mapping metadata (`RuntimeTemplate` excludes mappings by design)

Runtime Validation re-checks some constraints also enforced at compile time. That overlap is intentional when runtime artifacts may originate from external or untrusted sources. Mapping validation remains in compile/orchestration phases.

### ValueResolutionService

Produces `RuntimeExecutionTree` — an immutable execution artifact that materializes:

* runtime occurrence expansion (for example, repeatable GROUP instances)
* resolved values from INPUT, STATIC, DEFAULT_VALUE, and MASTER_DATA sources

Must not:

* Access repositories
* Generate XML
* Modify `RuntimeTemplate`

### XMLGenerationService

Public serializer boundary. Walks `RuntimeExecutionTree` and produces XML.

MVP API:

```text
generate(RuntimeExecutionTree) → String
```

Reserved future extension (traversal algorithm unchanged; output sink only):

```text
generate(RuntimeExecutionTree)
        ↓
generate(RuntimeExecutionTree, Writer)
        ↓
generate(RuntimeExecutionTree, OutputStream)
```

Must not perform:

* occurrence expansion
* path lookup
* value resolution
* source selection
* repository access
* business validation

Serialization rules owned here:

* XML declaration (UTF-8)
* element start/end
* attributes
* text values
* sibling ordering (`displayOrder`)
* empty-handling serialization
* XML escaping (via StAX)

---

The execution artifact shall be immutable once value resolution completes.

`XMLGenerationService` shall operate only on `RuntimeExecutionTree` and shall focus on XML serialization.

---

# 8. Supporting Components

## StorageProvider

Responsible for:

* Store XML files
* Retrieve XML files
* Delete expired files

---

## ExportHistoryRepository

Responsible for:

* Create Export History
* Update Export Status
* Query Export History

---

# 9. Exceptions

* ValidationException
* BusinessException
* NotFoundException
* StorageException

---

# 10. Validation Rules

PreviewService and ExportService shall verify:

* Template availability
* Compiled schema availability
* Runtime Model completeness

RuntimeValidationService validates `RuntimeTemplate` integrity after loading.

Input data and resolved-value validation occur after value resolution (separate from runtime model validation).

The XML Generator walks `RuntimeExecutionTree` via `XMLGenerationService` and is responsible for XML serialization only, not business value resolution.

---

# 11. Implementation Notes

* Services shall load all required runtime data before invoking `XMLGenerationService`.
* `XMLGenerationService` shall never access repositories directly.
* Preview shall not persist any business data.
* Export shall persist only the generated XML file and Export History.
* XML generation shall operate solely on `RuntimeExecutionTree` after value resolution.
* `XMLGenerationService` shall never access repositories, storage providers, or HTTP components directly.
* Value resolution (INPUT, STATIC, DEFAULT_VALUE, MASTER_DATA) must complete before XML generation runs.
* `RuntimeTemplate` is the canonical runtime definition; `RuntimeExecutionTree` is a generated execution artifact.
* XML serialization standard is StAX (`XMLStreamWriter`). Do not use DOM.
* `ExecutionTreeXmlWriter` is an internal helper; only `XMLGenerationService` is the public pipeline component.
* Reserve `Writer` and `OutputStream` overloads on `XMLGenerationService` for Preview/Export streaming; do not implement until required.
* Shared tree traversal across runtime components may be extracted into a reusable abstraction later if duplication emerges; do not refactor prematurely.
* Preview and Export must invoke `RuntimeExecutionOrchestrator` only — never runtime pipeline components directly.
* `RuntimeExecutionRequest` is transport-only; `RuntimeExecutionResult` is runtime output-only.

---

# 12. Unit Test Strategy

Minimum coverage:

PreviewService

* Preview success
* Validation failure
* Engine failure

ExportService

* Export success
* Storage failure
* Export History creation
* Export History update on failure

RuntimeModelFactory

* Runtime Model construction
* Runtime data resolution

XMLGenerationService

* XML generation from `RuntimeExecutionTree`
* Empty-handling serialization
* XML escaping
* Deterministic output
* Future: `Writer` / `OutputStream` overloads (extension point)

---

# 13. Implementation Checklist

* Create PreviewController
* Create ExportController
* Create PreviewService
* Create ExportService
* Create RuntimeModelFactory
* Integrate XMLEngine
* Integrate StorageProvider
* Implement Export History persistence
* Implement Preview workflow
* Implement Preview REST API
* Implement Export REST API
* Implement Export workflow
* Write unit tests

---

# 14. Stable Architecture Status

The following architecture is stable. Future phases should build on top of it rather than modifying the runtime pipeline.

## Compile Engine

```text
Template Metadata
        ↓
Parser
        ↓
RuntimeTemplate
        ↓
Compiler
        ↓
compiled_schema_json
```

## Runtime Engine

```text
compiled_schema_json
        ↓
RuntimeLoader
        ↓
RuntimeTemplate
        ↓
RuntimeValidation
        ↓
ValueResolution
        ↓
RuntimeExecutionTree
        ↓
XMLGenerationService
        ↓
XML
```

## Application Layer

```text
                TemplateCompileMappingResolver
                           ▲
                           │
     ┌─────────────────────┼─────────────────────┐
     │                     │                     │
     ▼                     ▼                     ▼
TemplateCompilation   PreviewService     ExportService
   Orchestrator              │                     │
     │                       └──────────┬──────────┘
     │                                  ▼
     │                    RuntimeExecutionOrchestrator
     │                                  │
     └──────────────────────────────────┼──────────► Runtime Engine
                                        ▼
                                       XML
```

The Application Layer and REST transport layer are complete. Avoid introducing additional
orchestration layers unless a future use case genuinely requires them.

Core compile, runtime, application orchestration, and Runtime REST API architecture is
frozen unless a future ADR explicitly changes it. See §15–§17.

---

## Generated Artifact Analogy

Treat `RuntimeExecutionTree` exactly like `compiled_schema_json`.

| Artifact | Stage | Visibility |
| -------- | ----- | ---------- |
| `compiled_schema_json` | Compile artifact | Internal |
| `RuntimeExecutionTree` | Execution artifact | Internal |

Both are generated objects. Both may evolve independently. Clients must never depend on
either.

```text
Metadata Layer
        ↓
Compile Engine (TemplateCompilationOrchestrator)
        ↓
compiled_schema_json          (Compile Artifact — Internal)
        ↓
Runtime Engine (RuntimeExecutionOrchestrator)
        ↓
RuntimeExecutionTree          (Execution Artifact — Internal)
        ↓
Application Layer (TemplateCompileMappingResolver → PreviewService / ExportService)
        ↓
Public REST API               (Business contract only — xml, errors, export metadata)
        ↓
Clients
```

This separation reinforces three stable boundaries:

- **Metadata** — editable template definition
- **Runtime** — generated artifacts and pure transformations
- **API** — stable business outputs only

The Preview and Export APIs (`xml` on success, `errors` on validation failure) are the
stable Runtime API contract. See §16.

---

# 15. XML Engine Complete (v1.0)

Phase 5.5 completes the XML Engine. The following stack is stable:

```text
Metadata Layer
        ↓
Compile Engine
        ↓
compiled_schema_json
        ↓
Runtime Engine
        ↓
RuntimeExecutionTree
        ↓
Application Layer
        ↓
Preview API
        ↓
Export API
        ↓
Clients
```

Suggested git tag: `v1.0.0` — **XML Engine Complete**.

Everything below this milestone is **business capability and infrastructure** work, not
core engine architecture. Subsequent versions should extend higher application layers
rather than modifying the Runtime Engine pipeline or Runtime REST contracts.

---

# 16. Runtime API Contract Freeze

The Runtime REST API contract is frozen as of Phase 5.5.

## Runtime APIs

| API | Endpoint |
| --- | -------- |
| Preview | `POST /api/v1/templates/{id}/preview` |
| Export | `POST /api/v1/templates/{id}/export` |

## Stable contract

**Success**

```json
{
  "success": true,
  "data": {
    "xml": "<Game>...</Game>"
  }
}
```

**Validation failure**

```json
{
  "success": false,
  "errors": [
    { "field": "GameId", "code": "SOURCE_TYPE_REQUIRED" }
  ]
}
```

## Rules

- Expose `xml` and validation `errors` only.
- Do not expose `RuntimeExecutionTree`, `RuntimeExecutionResult`, `RuntimeTemplate`, or
  `RuntimeExecutionRequest`.
- Do not change this contract unless a future ADR explicitly approves it.

## Future extensions

Storage, Export History, file download, batch export, and import must extend **higher
application layers** (new response fields on export metadata endpoints, separate
download routes, persistence services) without modifying the core Preview/Export runtime
contract or the Runtime Engine pipeline.

---

# 17. Engine vs Business Features

The project now divides cleanly into two categories.

## Core Engine (complete — v1.0)

| Layer | Components |
| ----- | ---------- |
| Metadata | Template fields, mappings, schema APIs |
| Compile Engine | Parser, Compiler, `TemplateCompilationOrchestrator` |
| Runtime Engine | Loader, Validation, Value Resolution, XML Generation, `RuntimeExecutionOrchestrator` |
| Application Layer | `TemplateCompileMappingResolver`, `PreviewService`, `ExportService` |
| REST Layer | `PreviewController`, `ExportController` |

Engine work modifies compile/runtime pipelines, canonical models, or Runtime REST
contracts. Requires architecture review and ADR approval for contract changes.

## Business Features (remaining)

| Feature | Description |
| ------- | ----------- |
| Saved Inputs | Persist and reload user input drafts |
| Export History | Record export events |
| Storage | Persist exported XML files |
| File download | Serve stored XML to clients |
| Import XML | Reverse or validate imported documents |
| Template versioning | Optimistic locking and version validation |
| Batch Export | Multi-template or scheduled export |

Business features compose **on top of** the frozen engine. They may add new endpoints,
persistence, and response metadata but must not bypass `RuntimeExecutionOrchestrator` or
 leak runtime artifacts into public APIs.

---

# 18. Phase 1 Decisions

| Topic                 | Decision             |
| --------------------- | -------------------- |
| XML Generation        | Streaming            |
| Runtime Model         | Required             |
| XML Engine            | Pure Logic           |
| Preview               | No persistence       |
| Export                | Synchronous          |
| Storage               | Provider abstraction |
| Background Processing | Excluded             |
| Auto Retry            | Excluded             |
