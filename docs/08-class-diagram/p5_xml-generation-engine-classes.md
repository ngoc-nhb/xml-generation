# Part 5. XML Generation Engine Classes

---

# 47. Purpose

## Responsibility

Define the internal components of the XML Generation Engine.

The Engine transforms a validated runtime model into a well-formed XML document.

The Engine is completely independent from:

* HTTP
* Database
* UI
* Repository
* DTO

It operates solely on in-memory runtime objects.

---

## Scope

This Part defines:

* Engine components
* Runtime models
* Component responsibilities
* Component interactions
* XML generation pipeline

---

# 48. XML Engine Overview

The XML Generation Engine consists of the following components.

```text
TemplateCompiler

↓

CompiledTemplate

↓

RuntimeModelFactory

↓

ResolvedRuntimeModel

↓

ValueResolver

↓

ValidationEngine

↓

XMLBuilder

↓

ExportProcessor
```

Each component has one clearly defined responsibility.

---

# 49. TemplateCompiler

## Responsibility

Compile editable Template definitions into an optimized runtime schema.

Compilation occurs only when a Template is explicitly compiled.

---

## Input

* Template
* TemplateFields

---

## Output

```text
CompiledTemplate
```

---

## Public Operations

* compile()

---

## Used By

```text
CompileService
```

---

## Dependencies

None.

---

## Notes

TemplateCompiler shall never execute during Preview or Export.

CompiledTemplate shall be reused until the next successful compilation.

---

# 50. CompiledTemplate

## Responsibility

Represent the immutable runtime schema consumed by the XML Engine.

CompiledTemplate contains all structural information required for XML generation.

---

## Contains

* Runtime Nodes
* XML hierarchy
* Mapping definitions
* Validation metadata
* Display order
* Repeatable group definitions

---

## Created By

```text
TemplateCompiler
```

---

## Used By

```text
RuntimeModelFactory
```

---

## Lifecycle

```text
Compile

↓

Persist

↓

Load

↓

Reuse
```

---

# 51. RuntimeModelFactory

## Responsibility

Construct the runtime model required for XML generation.

The Runtime Model combines:

* Request Payload
* Compiled Template
* Master Data

---

## Input

* CompiledTemplate
* Request Payload
* Master Data

---

## Output

```text
ResolvedRuntimeModel
```

---

## Public Operations

* build()

---

## Used By

```text
PreviewService

ExportService
```

---

## Notes

The RuntimeModelFactory shall not perform XML generation.

Its responsibility is limited to assembling runtime data.

---

# 52. ResolvedRuntimeModel

## Responsibility

Represent all runtime information required by the XML Generation Engine.

The Runtime Model is constructed by the RuntimeModelFactory and completed by the ValueResolver.

After value resolution has completed, the Runtime Model becomes immutable for the remainder of the XML Generation pipeline.

---

## Notes

Repositories shall never be accessed after the Runtime Model has been created.

The Runtime Model may be populated during the Value Resolution phase.

After ValueResolver completes successfully, the Runtime Model shall be treated as immutable.

Subsequent Engine components shall consume the Runtime Model in read-only mode.

---

# 53. ValueResolver

## Responsibility

Resolve every Runtime Node into its final value.

Supported source types include:

* INPUT
* MASTER_DATA
* STATIC
* EMPTY

---

## Input

```text
ResolvedRuntimeModel
```

---

## Output

```
ResolvedRuntimeModel
(populated and finalized)
```

ValueResolver is the final component permitted to populate the Runtime Model.
After resolve() completes successfully, the Runtime Model becomes immutable.

---

## Public Operations

* resolve()

---

## Dependencies

None.

---

## Used By

```text
ValidationEngine
```

---

## Notes

ValueResolver shall never:

* Query databases
* Access repositories
* Modify XML
* Perform validation

Its sole responsibility is value resolution.

---

# 54. ValidationEngine

## Responsibility

Validate the resolved runtime values.

Validation includes:

* Required
* Data Type
* Length
* Format
* Business Rules

---

## Input

```text
ResolvedRuntimeModel
```

---

## Output

```text
ValidationResult
```

---

## Public Operations

* validate()

---

## Used By

```text
XMLBuilder
```

---

## Notes

ValidationEngine shall collect all validation errors before returning.

If ValidationResult contains one or more validation errors, the Engine shall terminate execution immediately.

A ValidationException shall be returned to the invoking Application Service.

XMLBuilder shall not be invoked when validation errors exist.

Immediate termination shall occur only after validation has completed or when a fatal engine error occurs.

---

# 55. XMLBuilder

## Responsibility

Generate XML output from the validated runtime model.

---

## Input

* CompiledTemplate
* ResolvedRuntimeModel

---

## Output

```text
XML Stream
```

---

## Public Operations

* writeDocument()
* writeNode()
* writeAttribute()
* writeText()

---

## Dependencies

```text
XMLWriterFactory
```

---

## Used By

```text
ExportProcessor
```

---

## Notes

XMLBuilder shall:

* Preserve XML order
* Apply XML escaping
* Write XML Declaration
* Stream XML directly

XMLBuilder shall never construct an in-memory DOM tree.

---

# 56. ExportProcessor

## Responsibility

Finalize XML generation.

Responsibilities include:

* Stream XML
* Persist XML file (Export only)
* Return XML String (Preview only)

---

## Input

```text
XML Stream
```

---

## Output

Preview:

```text
XML String
```

Export:

```text
Physical XML File
```

---

## Public Operations

* processPreview()
* processExport()

---

## Used By

```text
PreviewService

ExportService
```

---

## Notes

ExportProcessor shall support both Preview and Export modes.

Execution mode is determined by the invoking Application Service.

---

# 57. Engine Interaction

```text
CompiledTemplate

↓

RuntimeModelFactory

↓

ResolvedRuntimeModel

↓

ValueResolver

↓

ValidationEngine

↓

ValidationResult

↓

Validation Errors?

├── Yes

│      ↓

│  ValidationException

│      ↓

│  Application Service

│

└── No

       ↓

   XMLBuilder

       ↓

ExportProcessor
```

The Engine executes sequentially.

Each component consumes the output of the previous component.

Backward dependencies are prohibited.

---

# 58. Engine Design Principles

## Pure Logic

The Engine shall contain no infrastructure dependencies.

---

## Stateless

The Engine shall maintain no internal execution state between requests.

---

## Deterministic

Identical inputs shall always produce identical XML output.

---

## Streaming

XML shall be generated using streaming techniques.

Intermediate DOM trees are prohibited.

---

## Testability

Each component shall be independently unit testable.

Dependencies shall be injected where appropriate.

---

## Separation of Responsibility

Each Engine component shall own exactly one responsibility.

No component shall perform another component's work.

---

# 59. Phase 1 Engine Decisions

| Topic                 | Decision                       |
| --------------------- | ------------------------------ |
| Engine Entry Points   | PreviewService / ExportService |
| Runtime Model         | Immutable                      |
| XML Generation        | Streaming                      |
| XML Declaration       | Required                       |
| XML Escaping          | Required                       |
| DOM Construction      | Prohibited                     |
| Database Access       | Prohibited                     |
| Repository Access     | Prohibited                     |
| DTO Usage             | Prohibited                     |
| HTTP Awareness        | Prohibited                     |
| Stateless Execution   | Required                       |
| Collect All Errors    | Supported                      |
| Immediate Termination | Fatal Errors Only              |
