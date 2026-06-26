# Part 2. XML Generation Flow

---

## 9. Overview

The XML Generator Engine shall execute a deterministic processing flow.

The execution flow is identical for:

```text
PREVIEW

EXPORT
```

until XML generation is completed.

Only the final processing step differs based on:

```text
ExecutionMode
```

---

## 10. High-Level Flow

```text
Load Compiled Schema

        ↓

Load Runtime Data

        ↓

Resolve Runtime Values

        ↓

Validate Resolved Values

        ↓

Build XML Structure

        ↓

Generate XML Document

        ↓

Execution Mode Branch

 ┌──────────────┐
 │   PREVIEW    │
 └──────┬───────┘
        ↓

 Return XML Preview

 ┌──────────────┐
 │    EXPORT    │
 └──────┬───────┘
        ↓

 Execute Export Processor

        ↓

 Return Export Result
```

---

## 11. Step 1 – Load Compiled Schema

The engine shall load:

```text
compiled_schema_json
```

from:

```text
templates.compiled_schema_json
```

The engine must not:

* Load TemplateField records
* Rebuild Template Tree
* Resolve Mapping Rules from database tables

The compiled schema is the single source of truth during runtime.

---

### Failure Conditions

The process shall stop if:

```text
compiled_schema_json = null
```

or:

```text
compiled_schema_json is invalid
```

Result:

```text
TEMPLATE_SCHEMA_INVALID
```

---

## 12. Step 2 – Load Runtime Data

The engine shall load:

```text
input_data_json

selected_master_data_json
```

associated with the current request.

---

### Payload Validation

Before processing:

The engine shall validate:

* Payload size
* JSON structure
* Maximum occurrence count
* Maximum field count

---

### Failure Conditions

Examples:

```text
PAYLOAD_TOO_LARGE

INVALID_JSON_STRUCTURE
```

The process shall stop immediately.

---

## 13. Step 3 – Resolve Runtime Values

The Value Resolver shall convert all runtime sources into resolved values.

Supported source types:

```text
INPUT

MASTER_DATA

STATIC
```

---

### INPUT Resolution

Example:

```json
{
  "GameDate": "20260618"
}
```

Resolved Value:

```text
20260618
```

---

### MASTER_DATA Resolution

Example:

Schema:

```text
GAME_KIND.game_kind_id
```

Master Data:

```json
{
  "GAME_KIND": {
    "game_kind_id": 2
  }
}
```

Resolved Value:

```text
2
```

---

### STATIC Resolution

Example:

```text
staticValue = 1
```

Resolved Value:

```text
1
```

---

### Resolution Output

The Value Resolver shall produce a:

```text
Resolved Runtime Model
```

containing:

* Resolved values
* Resolution status
* Resolution errors

Example:

```json
{
  "PlayerID": {
    "value": null,
    "status": "RESOLUTION_ERROR",
    "error": "MASTER_DATA_NOT_FOUND"
  }
}
```

---

### Runtime Model Design

The Resolved Runtime Model is a lightweight runtime structure.

Its purpose is to:

* Store resolved values
* Store validation context
* Store resolution errors
* Support streaming XML generation

The Resolved Runtime Model shall not be treated as a complete in-memory XML document.

The system shall not construct:

```text
DOM Tree

XML Tree

XML Document Object
```

during value resolution.

---

### Memory Strategy

The Resolved Runtime Model should contain only information required for:

```text
Validation

Streaming XML Generation
```

The engine should minimize memory allocation whenever possible.

Large XML structures shall be generated through streaming rather than pre-building XML content in memory.

Payload limits defined by the application shall act as the primary protection against excessive memory consumption.

---

### Resolution Error Handling

Resolution errors shall not immediately terminate processing.

Examples:

```text
MASTER_DATA_NOT_FOUND

MASTER_DATA_FIELD_NOT_FOUND
```

shall be recorded in the Resolved Runtime Model.

The Validation Engine is responsible for collecting and reporting resolution errors together with validation errors.

---

### Fatal Resolution Errors

The process shall terminate immediately only for unrecoverable runtime errors.

Examples:

```text
INVALID_JSON_STRUCTURE

PAYLOAD_TOO_LARGE

INVALID_OCCURRENCE_STRUCTURE
```

Result:

```text
PROCESS_TERMINATED
```


---

## 14. Step 4 – Validate Resolved Values

The Validation Engine shall validate the resolved runtime model.

Validation must occur after value resolution.

---

### Validation Scope

The engine shall validate:

* Required fields
* Data types
* Formats
* Occurrence rules
* Group activation rules
* Master Data rules

---

### Validation Strategy

The Validation Engine shall:

```text
Collect All Errors
```

instead of:

```text
Fail Fast
```

---

### Validation Result

Success:

```text
Validation Passed
```

Failure:

```json
[
  {
    "field": "GameDate",
    "code": "INVALID_DATE_FORMAT"
  },
  {
    "field": "PlayerID",
    "code": "MASTER_DATA_NOT_FOUND"
  }
]
```

---

### Failure Conditions

If validation errors exist:

```text
XML Generation Blocked
```

The process shall stop.

---

## 15. Step 5 – Build XML Structure

The XML Builder shall process the Resolved Runtime Model and generate XML output sequentially.

The XML Builder shall not construct an intermediate XML tree structure in memory.

The XML Builder should process nodes in a streaming manner whenever possible.

---

### Processing Scope

The builder shall process:

* GROUP nodes
* ELEMENT nodes
* ATTRIBUTE nodes

according to:

```text
compiled_schema_json
```

---

### XML Ordering

Sibling nodes shall be generated according to:

```text
displayOrder
```

---

### Group Processing

The builder shall:

* Evaluate group activation rules
* Generate active groups
* Skip inactive groups

---

### Repeatable Group Processing

The builder shall generate one XML node for each occurrence.

Example:

Input:

```json
{
  "GoalInfo": [
    {},
    {},
    {}
  ]
}
```

Generated:

```xml
<GoalInfo/>
<GoalInfo/>
<GoalInfo/>
```

---

### Memory Strategy

The builder shall avoid constructing a full XML Document Object Model (DOM) in memory.

The builder should process and emit XML incrementally from the Resolved Runtime Model.


---

## 16. Step 6 – Generate XML Document

The XML Builder shall serialize XML directly from the Resolved Runtime Model.

No intermediate XML tree is required.

---

### XML Escaping

The builder shall escape:

```text
&
<
>
"
'
```

before generating XML output.

---

### Example

Input:

```text
A & B
```

Output:

```xml
<Name>A&amp;B</Name>
```

---

### Streaming Generation

The XML Builder should generate XML using:

```text
Streaming XML Writer
```

or equivalent streaming technologies.

The builder should emit XML incrementally during processing.

The engine should avoid buffering the complete XML document in memory.

---

### Output

The output of this step is:

```text
XML Text Stream
```

which is passed to:

```text
PREVIEW Response

or

Export Processor
```

depending on the execution mode.


---

## 17. Step 7 – Execution Mode Branch

After XML generation is completed:

The engine shall branch according to:

```text
ExecutionMode
```

---

### PREVIEW

The engine shall:

* Return generated XML
* Skip Export Processor

The engine shall not:

* Create files
* Create ExportHistory records

---

### EXPORT

The engine shall:

* Execute Export Processor
* Generate file
* Create ExportHistory
* Return export result

---

## 18. Process Termination Rules

The XML Generator Engine shall terminate processing immediately only for unrecoverable runtime failures.

These failures prevent the engine from continuing execution safely.

---

### Immediate Termination Conditions

The process shall terminate immediately when:

```text
TEMPLATE_SCHEMA_INVALID

PAYLOAD_TOO_LARGE

INVALID_JSON_STRUCTURE

INVALID_OCCURRENCE_STRUCTURE
```

Result:

```text
PROCESS_TERMINATED
```

The engine shall return the corresponding application error.

---

### Non-Terminating Validation Errors

The following errors shall not terminate processing during the resolution phase:

```text
MASTER_DATA_NOT_FOUND

MASTER_DATA_FIELD_NOT_FOUND

REQUIRED_FIELD_MISSING

INVALID_DATA_TYPE

INVALID_DATE_FORMAT

INVALID_DATETIME_FORMAT

OCCURRENCE_RULE_VIOLATION

GROUP_VALIDATION_FAILED
```

These errors shall be:

```text
Recorded

↓

Collected

↓

Returned Together
```

according to the:

```text
Collect All Errors
```

validation strategy.

---

### Validation Failure

If validation errors exist after Step 4:

```text
Validation Failed
```

the engine shall:

```text
Stop XML Generation

↓

Return Validation Errors
```

XML generation shall not begin when validation errors are present.

---

### Successful Completion

The process is considered successful only when:

```text
XML Generated Successfully
```

and:

```text
PREVIEW Completed

or

EXPORT Completed
```

without fatal runtime failures.