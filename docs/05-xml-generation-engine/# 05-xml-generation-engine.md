# 05-xml-generation-engine.md

# Part 1. Overview & Architecture

---

## 1. Overview

This document defines the runtime behavior of the XML Generator Engine.

The XML Generator Engine is responsible for:

* Loading Template Schema
* Loading User Input Data
* Loading Master Data
* Resolving Runtime Values
* Validating Data
* Building XML Structure
* Generating XML Output
* Recording Export History

The engine uses the pre-compiled template schema stored in:

```text
templates.compiled_schema_json
```

to generate XML files.

---

## 2. Design Principles

The XML Generator Engine shall:

* Use pre-compiled schema only
* Avoid runtime template reconstruction
* Produce deterministic XML output
* Support large XML structures
* Support nested XML nodes
* Support repeatable groups
* Support XML preview
* Support XML export
* Validate resolved values
* Treat all runtime data as untrusted input
* Minimize memory consumption during XML generation

---

## 3. Runtime Inputs

The engine receives the following inputs.

### Compiled Schema

```text
compiled_schema_json
```

Source:

```text
templates.compiled_schema_json
```

---

### User Input Data

```text
input_data_json
```

Example:

```json
{
  "GameDate": "20260618",
  "Weather": "Sunny"
}
```

---

### Selected Master Data

```text
selected_master_data_json
```

Example:

```json
{
  "GAME_KIND": {
    "game_kind_id": 2,
    "game_kind_name": "J1"
  }
}
```

---

### Execution Mode

The engine shall receive an execution mode parameter.

Supported values:

```text
PREVIEW

EXPORT
```

---

#### PREVIEW

Used when the user previews XML before export.

Behavior:

* Validate data
* Generate XML
* Return XML preview

The system shall not:

* Create ExportHistory records
* Generate downloadable files

---

#### EXPORT

Used when the user exports XML.

Behavior:

* Validate data
* Generate XML
* Generate downloadable file
* Create ExportHistory record
* Return export result

---

### Execution Mode Source

Execution Mode is determined by the application layer.

The XML Generator Engine does not determine execution mode by itself.

Recommended implementation:

```text
POST /xml/preview
    ↓
ExecutionMode = PREVIEW

POST /xml/export
    ↓
ExecutionMode = EXPORT
```

The engine shall receive Execution Mode from the application layer.

The mechanism used to determine Execution Mode is defined in the API Design document.

---

## 4. Runtime Output

The engine may produce the following outputs.

### XML Preview

Displayed in the UI before export.

Available only when:

```text
ExecutionMode = PREVIEW
```

---

### XML File

Generated XML document.

Example:

```xml
<LiveGame>
    <GameDate>20260618</GameDate>
</LiveGame>
```

Available only when:

```text
ExecutionMode = EXPORT
```

---

### Export History Record

Stored in:

```text
export_histories
```

for audit and download purposes.

Available only when:

```text
ExecutionMode = EXPORT
```

---

## 5. Runtime Architecture

```text
compiled_schema_json
        +
input_data_json
        +
selected_master_data_json

                ↓

      XML Generator Engine

                ↓

         Value Resolver

                ↓

       Validation Engine

                ↓

          XML Builder

                ↓

       Export Processor
```

The engine shall resolve all runtime values before validation begins.

Validation must operate on resolved values rather than raw source data.

This ensures that:

* MASTER_DATA values can be validated
* STATIC values can be validated
* Empty value handling can be validated consistently
* Validation results reflect actual XML output

---

## 6. Engine Responsibilities

### Value Resolver

Responsible for:

* INPUT resolution
* MASTER_DATA resolution
* STATIC resolution
* Empty value handling
* Runtime value normalization

The output of the Value Resolver is the source of truth for all subsequent processing.

---

#### Resolver Error Handling

The Value Resolver shall handle malformed runtime data safely.

Examples:

* Invalid JSON structure
* Invalid occurrence structure
* Invalid Master Data payload
* Unsupported runtime value format

Resolver failures shall return structured application errors.

The engine shall not expose internal exception details to users.

Resolver failures shall not result in unhandled runtime exceptions.

---

### Validation Engine

Responsible for:

* Required validation
* Data type validation
* Format validation
* Group validation
* Occurrence validation
* Master Data validation

Validation shall operate only on resolved values.

---

### XML Builder

Responsible for:

* Node generation
* Attribute generation
* Group generation
* XML ordering
* XML escaping

---

#### Trust Boundary

The XML Builder shall treat all runtime values as untrusted data.

Sources include:

* INPUT
* MASTER_DATA
* STATIC

No value shall be inserted directly into XML output without escaping.

---

#### XML Escaping

The XML Builder shall escape reserved XML characters before generating output.

Characters requiring escaping include:

```text
&
<
>
"
'
```

Example:

Input:

```text
A & B
```

Output:

```xml
<Name>A&amp;B</Name>
```

---

#### Security Requirement

The XML Builder must prevent:

* Invalid XML generation
* XML Injection
* XML structure manipulation
* CDATA breakouts
* Malformed XML output

All runtime values must be escaped before insertion into XML elements or attributes.

---

#### Memory Management

The XML Builder should generate XML using a streaming approach whenever possible.

The engine should avoid loading the entire XML document into memory before writing output.

Recommended implementations:

* Streaming XML Writer
* SAX-based Writer
* StAX-based Writer

The engine should minimize memory consumption when processing large XML documents.

Building large XML documents entirely in memory is discouraged.

---

### Export Processor

Responsible for:

* XML file creation
* ExportHistory creation
* ExportHistory update
* File retention support

The Export Processor shall execute only when:

```text
ExecutionMode = EXPORT
```

The Export Processor shall not execute during:

```text
ExecutionMode = PREVIEW
```

to prevent unnecessary file generation and ExportHistory records.

---

## 7. Runtime Processing Boundaries

The XML Generator Engine shall not:

* Load TemplateField records directly
* Rebuild Template Trees
* Resolve Template Mapping Rules from database tables
* Modify Template Definitions

All template-related information must already exist in:

```text
compiled_schema_json
```

before runtime execution begins.

---

## 8. Runtime Performance Objective

The XML Generator Engine is designed around the pre-compilation architecture.

Runtime execution should focus only on:

* Value Resolution
* Validation
* XML Generation

The engine should avoid expensive database joins and template reconstruction during runtime.

Target response time:

```text
Preview XML

< 3 seconds
```

```text
Export XML

< 3 seconds
```

for normal operating conditions.

## 6. Engine Responsibilities

### Value Resolver

Responsible for:

* INPUT resolution
* MASTER_DATA resolution
* STATIC resolution
* Empty value handling
* Runtime value normalization

The output of the Value Resolver is the source of truth for all subsequent processing.

---

#### Resolver Error Handling

The Value Resolver shall handle malformed runtime data safely.

Examples:

* Invalid JSON structure
* Invalid occurrence structure
* Invalid Master Data payload
* Unsupported runtime value format

Resolver failures shall return structured application errors.

The engine shall not expose internal exception details to users.

Resolver failures shall not result in unhandled runtime exceptions.

---

#### Payload Size Protection

The Value Resolver shall validate payload size before parsing runtime data.

The purpose of this validation is to prevent:

* Excessive memory consumption
* JSON parsing attacks
* Out of Memory (OOM) conditions
* Resource exhaustion

The system should define configurable limits for:

```text
input_data_json size

selected_master_data_json size

maximum occurrence count

maximum field count
```

Payload validation shall occur before JSON parsing begins.

If a payload exceeds configured limits:

```text
PAYLOAD_TOO_LARGE
```

shall be returned.

The Resolver shall reject oversized payloads before allocating significant memory resources.

---

### Validation Engine

Responsible for:

* Required validation
* Data type validation
* Format validation
* Group validation
* Occurrence validation
* Master Data validation

Validation shall operate only on resolved values.

---

### XML Builder

Responsible for:

* Node generation
* Attribute generation
* Group generation
* XML ordering
* XML escaping

---

#### Trust Boundary

The XML Builder shall treat all runtime values as untrusted data.

Sources include:

* INPUT
* MASTER_DATA
* STATIC

No value shall be inserted directly into XML output without escaping.

---

#### XML Escaping

The XML Builder shall escape reserved XML characters before generating output.

Characters requiring escaping include:

```text
&
<
>
"
'
```

Example:

Input:

```text
A & B
```

Output:

```xml
<Name>A&amp;B</Name>
```

---

#### Security Requirement

The XML Builder must prevent:

* Invalid XML generation
* XML Injection
* XML structure manipulation
* CDATA breakouts
* Malformed XML output

All runtime values must be escaped before insertion into XML elements or attributes.

---

#### Memory Management

The XML Builder should generate XML using a streaming approach whenever possible.

The engine should avoid loading the entire XML document into memory before writing output.

Recommended implementations:

* Streaming XML Writer
* SAX-based Writer
* StAX-based Writer

The engine should minimize memory consumption when processing large XML documents.

Building large XML documents entirely in memory is discouraged.

---

### Export Processor

Responsible for:

* XML file creation
* ExportHistory creation
* ExportHistory update
* File retention support

The Export Processor shall execute only when:

```text
ExecutionMode = EXPORT
```

The Export Processor shall not execute during:

```text
ExecutionMode = PREVIEW
```

to prevent unnecessary file generation and ExportHistory records.

---

#### File Writing Strategy

The Export Processor should use a streaming file writing approach whenever possible.

The system should avoid buffering the entire XML document in memory before persisting the file.

Recommended implementations:

* Streaming upload to Cloud Storage
* Streaming upload to Object Storage
* Buffered Output Stream
* Asynchronous File I/O

---

#### Performance Requirement

File persistence operations should not significantly increase API response time.

The Export Processor should minimize:

* Network latency impact
* Disk I/O blocking
* Memory overhead

The implementation should be designed to support the target response time defined in:

```text
Section 8 - Runtime Performance Objective
```

---

#### Future Enhancement

For large XML files or high-volume export workloads, the Export Processor may be implemented using:

* Background Jobs
* Queue-based Processing
* Event-driven Export Pipelines

Such optimizations are optional for MVP and may be introduced in future phases.
