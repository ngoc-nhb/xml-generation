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
| XMLController           | Expose Preview and Export APIs |
| PreviewService          | Execute Preview workflow       |
| ExportService           | Execute Export workflow        |
| RuntimeModelFactory     | Build Runtime Model            |
| RuntimeLoader           | Load `compiled_schema_json` into `RuntimeTemplate` |
| RuntimeValidationService | Validate runtime model integrity after loading |
| ValueResolutionService | Produce `RuntimeExecutionTree` execution artifact |
| XMLGenerationService  | Serialize `RuntimeExecutionTree` to XML (public boundary) |
| StorageProvider         | Persist exported XML           |
| ExportHistoryRepository | Persist export history         |

---

# 4. Responsibilities

## PreviewService

Responsible for:

* Load required runtime data
* Build Runtime Model
* Invoke XML Engine
* Return generated XML

Preview shall never persist any data.

---

## ExportService

Responsible for:

* Load required runtime data
* Build Runtime Model
* Invoke XML Engine
* Store XML file
* Create Export History

Export owns the complete export workflow.

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

This is the project's **canonical runtime architecture**. Preview, Export, and future APIs shall orchestrate these stages rather than introducing new runtime models or bypassing existing stages.

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
* If the runtime module grows significantly (Preview, Export, Streaming XML), consider separating definition models (`RuntimeTemplate`) and execution artifacts (`RuntimeExecutionTree`) into dedicated packages; not required at current project size.

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

* Create XMLController
* Create PreviewService
* Create ExportService
* Create RuntimeModelFactory
* Integrate XMLEngine
* Integrate StorageProvider
* Implement Export History persistence
* Implement Preview workflow
* Implement Export workflow
* Write unit tests

---

# 14. Phase 1 Decisions

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
