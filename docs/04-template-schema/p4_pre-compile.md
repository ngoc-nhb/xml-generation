# Part 4. Pre-compilation & Runtime Architecture

---

## 24. Pre-compilation Strategy

The system adopts a Pre-compilation architecture.

The XML Generator Engine must not reconstruct:

```text id="ij5md5"
Template Tree

TemplateField Hierarchy

Master Data Mapping Rules
```

during every XML generation request.

Instead, the system shall generate a compiled schema and store it inside:

```text id="3r4n4l"
templates.compiled_schema_json
```

---

### Objective

Reduce runtime processing by shifting expensive operations from:

```text id="j7t7d4"
Generate XML Time
```

to:

```text id="bjlkkq"
Template Save Time
```

---

### Benefits

* Faster XML generation
* Lower database load
* Reduced recursive processing
* Simpler XML generation logic
* Better scalability

---

## 25. Compilation Flow

The compiled schema shall be generated whenever:

```text id="q0rl8n"
Template Created

Template Updated

TemplateField Updated

TemplateMapping Updated
```

---

### Compilation Process

```text id="f6thlj"
Template

    +

TemplateField

    +

TemplateMapping

        ↓

Build Template Tree

        ↓

Resolve Mapping Rules

        ↓

Generate Compiled Schema

        ↓

Store compiled_schema_json
```

---

## 26. Runtime Generation Flow

The XML Generator Engine shall use:

```text id="6eb8s2"
compiled_schema_json

input_data_json

selected_master_data_json
```

to generate XML.

---

### Runtime Flow

```text id="5myb0s"
Load compiled_schema_json

        ↓

Validate Input Data

        ↓

Resolve Master Data

        ↓

Apply Validation Rules

        ↓

Generate XML

        ↓

Return XML
```

---

### Runtime Restriction

The XML Generator Engine shall not:

```text id="e0d8wy"
Query TemplateField

Build Template Tree

Resolve Mapping Configuration
```

during XML generation.

These operations must already be included inside:

```text id="n9a6a7"
compiled_schema_json
```

---

## 27. Compiled Schema Consistency

The following artifacts must always remain synchronized:

```text id="krw0i8"
Template

TemplateField

TemplateMapping

compiled_schema_json
```

---

The system must prevent situations where:

```text id="9vf3i8"
TemplateField
        ≠
compiled_schema_json
```

because this may result in:

* Invalid XML generation
* Missing XML nodes
* Incorrect validation behavior
* Incorrect Master Data resolution

---

## 28. Transaction Requirement

Template updates and schema compilation must execute within a single database transaction.

---

### Required Flow

```text id="f50k18"
Update Template

        ↓

Update TemplateField

        ↓

Update TemplateMapping

        ↓

Build Compiled Schema

        ↓

Update compiled_schema_json

        ↓

COMMIT
```

---

### Failure Flow

```text id="00qqqz"
Update Template

        ↓

Update TemplateField

        ↓

Build Compiled Schema

        ↓

FAILED
```

Result:

```text id="mjlwm1"
ROLLBACK
```

---

The system must guarantee:

```text id="6qvv1s"
Template
=
TemplateField
=
TemplateMapping
=
compiled_schema_json
```

at all times.

Partial updates are not allowed.

---

## 29. Error Handling During Compilation

Compilation failures shall prevent Template updates from being committed.

### a. Compilation Validation Rules

Before a template can be compiled, the system shall validate template configuration rules.

Invalid template configurations shall prevent compilation.

---



### Empty Handling Validation

Some combinations of:

```text
dataType
+
emptyHandling
```

are not allowed.

The Template Compiler must validate these combinations before generating:

```text
compiled_schema_json
```

---

Supported combinations:

| Data Type | REQUIRED | OMIT_IF_EMPTY | EMPTY_TAG_IF_EMPTY | ZERO_IF_EMPTY |
| --------- | -------- | ------------- | ------------------ | ------------- |
| STRING    | ✓        | ✓             | ✓                  | ✗             |
| INTEGER   | ✓        | ✓             | ✗                  | ✓             |
| LONG      | ✓        | ✓             | ✗                  | ✓             |
| DECIMAL   | ✓        | ✓             | ✗                  | ✓             |
| BOOLEAN   | ✓        | ✓             | ✗                  | ✗             |
| DATE      | ✓        | ✓             | ✗                  | ✗             |
| DATETIME  | ✓        | ✓             | ✗                  | ✗             |

---

### Invalid Example 1

Configuration:

```text
GameDate

dataType = DATE

emptyHandling = ZERO_IF_EMPTY
```

Result:

```text
Compilation Failed

INVALID_EMPTY_HANDLING
```

---

### Invalid Example 2

Configuration:

```text
CreatedAt

dataType = DATETIME

emptyHandling = EMPTY_TAG_IF_EMPTY
```

Result:

```text
Compilation Failed

INVALID_EMPTY_HANDLING
```

---

### Invalid Example 3

Configuration:

```text
GameID

dataType = INTEGER

emptyHandling = EMPTY_TAG_IF_EMPTY
```

Result:

```text
Compilation Failed

INVALID_EMPTY_HANDLING
```

---

### Design Principle

EMPTY_TAG_IF_EMPTY is intended only for text-based values where an empty XML node is semantically meaningful.

Examples:

```xml
<AssistKind></AssistKind>

<Comment></Comment>
```

Numeric, Boolean, Date, and DateTime fields shall not use EMPTY_TAG_IF_EMPTY because empty values may produce invalid or misleading XML content.

---

### Example

Invalid Template Tree:

```text id="8pgy80"
Circular Parent Reference

A → B → C → A
```

Result:

```text id="o1c7r3"
Compilation Failed

Transaction Rollback
```

---

### Example

Invalid Mapping:

```text id="0kq4c8"
GAME_KIND.id

↓

Target Field Not Found
```

Result:

```text id="np7kj6"
Compilation Failed

Transaction Rollback
```

---
### The supported type:
```
INTEGER
LONG
DECIMAL
```

### The un-supported type:
```
STRING
DATE
DATETIME
BOOLEAN
```

---
### Template Depth Validation

The Template Compiler shall validate the maximum nesting depth of a Template.

The purpose of this validation is to prevent:

* Excessive recursion
* Stack overflow errors
* Excessive memory consumption
* Performance degradation

---

### Maximum Depth

The system shall define:

```text id="jlwm8"
MAX_TEMPLATE_DEPTH = 20
```

The value may be configurable in future versions.

---

### Example

Valid:

```text id="jlwm9"
Game

 └─ GoalInfo

      └─ PlayerInfo

           └─ AssistInfo
```

Depth:

```text id="jlwm10"
4
```

Result:

```text id="jlwm11"
Compilation Passed
```

---

Invalid:

```text id="jlwm12"
Level1

 └─ Level2

      └─ Level3

           ...

                └─ Level21
```

Depth:

```text id="jlwm13"
21
```

Result:

```text id="jlwm14"
Compilation Failed

TEMPLATE_DEPTH_EXCEEDED
```

---

### Validation Timing

Depth validation shall occur during:

```text id="jlwm15"
Template Compilation
```

before generating:

```text id="jlwm16"
compiled_schema_json
```

---

### Error Code

```text id="jlwm17"
TEMPLATE_DEPTH_EXCEEDED
```

Description:

```text id="jlwm18"
Template nesting depth exceeds the allowed maximum depth.
```

---
### Conditional Required Validation

The Template Compiler validates `required_when_parent_exists` on child fields when
a parent GROUP becomes active (see Part 2 §13–§14). `empty_handling` on
`TemplateField` is the sole expression of field-level requiredness; there is no
separate `required` boolean on template metadata.

---

### MASTER_DATA Mapping Validation

`source_type` is stored on `TemplateField` and is **not** derived from mapping
existence.

| Condition | Rule |
|-----------|------|
| `source_type = MASTER_DATA` | Exactly one `TemplateMapping` must reference the field |
| `source_type = INPUT` or `STATIC` | No `TemplateMapping` may reference the field |
| More than one mapping per field | Compilation fails |

If `source_type = MASTER_DATA` and no mapping exists:

```text
Compilation Failed

MAPPING_REQUIRED
```

If a mapping exists for a field where `source_type` is not `MASTER_DATA`:

```text
Compilation Failed

UNEXPECTED_MAPPING
```

If `master_data_field_id` is NULL (source field deleted):

```text
Compilation Failed

MASTER_DATA_FIELD_NOT_FOUND
```

---
### Display Order Validation

The Template Compiler shall validate displayOrder uniqueness among sibling nodes.

The XML Generator Engine relies on displayOrder to determine XML output sequence.

---

### Rule

Sibling nodes under the same parent must have unique:

```text
displayOrder
```

values.

---

### Valid Example

```text
Game

 ├─ GameID
      displayOrder = 1

 ├─ GameDate
      displayOrder = 2

 └─ Weather
      displayOrder = 3
```

Result:

```text
Compilation Passed
```

---

### Invalid Example

```text
Game

 ├─ GameID
      displayOrder = 1

 ├─ GameDate
      displayOrder = 1

 └─ Weather
      displayOrder = 2
```

Result:

```text
Compilation Failed

DUPLICATE_DISPLAY_ORDER
```

---

### Scope

Uniqueness is validated only among sibling nodes.

Example:

```text
Game

 ├─ GameID
      displayOrder = 1

 └─ GoalInfo

       └─ PlayerID
            displayOrder = 1
```

Result:

```text
Compilation Passed
```

because the nodes belong to different parent groups.

---

### Error Code

```text
DUPLICATE_DISPLAY_ORDER
```

Description:

```text
Multiple sibling nodes use the same displayOrder value.
```

---

## 30. Performance Requirements

The architecture should support:

```text id="zjlwm1"
Large XML Templates

Large Input Payloads

Large Master Data Sets
```

while maintaining acceptable response times.

---

### XML Generation Target

```text id="6jlwm2"
Preview XML

< 3 seconds
```

---

```text id="7jlwm3"
Generate XML

< 3 seconds
```

---

## 31. Database Optimization

The following optimizations are required.

---

### Foreign Key Indexes

All Foreign Key columns shall be indexed.

Examples:

```text id="8jlwm4"
template_id

parent_id

template_field_id

master_data_field_id

user_id
```

---

### JSONB Indexes

JSONB columns should support GIN indexes.

Examples:

```text id="9jlwm5"
master_data_records.data_json

saved_inputs.input_data_json

export_histories.input_data_json
```

---

Example:

```sql id="0jlwm6"
CREATE INDEX idx_master_data_records_json
ON master_data_records
USING GIN (data_json);
```

---

## 32. Caching Strategy

The system does not use Redis Cache in Phase 1.

The primary optimization strategy is:

```text id="1jlwm7"
Pre-compilation
```

---

Reason:

```text id="2jlwm8"
Template Updates
    = Rare

XML Generation
    = Frequent
```

---

The architecture prioritizes:

```text id="3jlwm9"
Reduce Processing Complexity
```

instead of:

```text id="4jlwm0"
Cache Expensive Processing
```

---

## 33. Future Optimization

The following optimizations may be introduced in future phases if required.

### In-Memory Cache

Examples:

```text id="5jlwm1"
Spring Cache

Caffeine Cache
```

---

### Distributed Cache

Examples:

```text id="6jlwm2"
Redis
```

---

### Advanced Database Optimization

Examples:

```text id="7jlwm3"
Materialized Path

ltree

Partitioning
```

---

These optimizations are intentionally excluded from Phase 1.

The current architecture relies on:

```text id="8jlwm4"
Pre-compilation

Database Indexing

Transaction Consistency
```

to satisfy performance and scalability requirements.

---

## 34. Design Decisions

The following architectural decisions are officially adopted for Phase 1:

### Included

```text id="9jlwm5"
Pre-compilation

compiled_schema_json

Database Transaction

Compiled Schema Consistency

Foreign Key Indexes

GIN Indexes
```

---

### Excluded

```text id="0jlwm6"
Redis Cache

Distributed Cache

Template Versioning

Asynchronous Compilation

Advanced Tree Storage
```

These features may be introduced in future phases if business requirements justify the additional complexity.

---

## 35. Lazy Migration (Legacy Templates)

Templates created before `TemplateField` normalization may have
`compiled_schema_json` populated but no `TemplateField` or `TemplateMapping`
rows.

### Trigger

Lazy migration runs when **all** of the following are true:

```text
TemplateField count = 0

compiled_schema_json IS NOT NULL
```

Typical triggers: first `GET /api/v1/templates/{id}` that needs editable schema,
or first `PUT /api/v1/templates/{id}/schema` after upgrade.

### Flow

```text
Load compiled_schema_json
        ↓
Parse JSON tree
        ↓
Create TemplateField rows
        ↓
Create TemplateMapping rows (from MASTER_DATA nodes)
        ↓
BEGIN TRANSACTION
        ↓
Save TemplateField + TemplateMapping
        ↓
Recompile
        ↓
Overwrite compiled_schema_json
        ↓
COMMIT
```

### Constraints

* No Flyway data migration.
* No SQL conversion script.
* Parsing is application-layer only.
* If parse or recompile fails, the transaction rolls back; the legacy JSON remains
  unchanged until the next attempt.
* After successful migration, `TemplateField` becomes the source of truth;
  subsequent saves follow the Single Save Principle (ADR-002).
