# 04-template-schema.md

# Part 1. Overview & Architecture

---

## 1. Overview

This document defines the Template Schema structure used by the XML Generator System.

The Template Schema is the core configuration used to generate XML files.

The system supports:

* Nested XML structures
* XML Elements
* XML Attributes
* Required fields
* Optional fields
* Master Data mapping
* User Input fields
* Conditional child fields
* XML Preview
* XML Generation

The Template Schema is designed to support:

* Dynamic XML generation
* Template reusability
* Future XML import capabilities
* Pre-compilation architecture
* High performance XML generation

---

## 2. Architecture

### Template Definition Flow

```text
Template
    +
TemplateField
    +
TemplateMasterDataMapping

        ↓

Compiled Template Schema

        ↓

templates.compiled_schema_json
```

---

### XML Generation Flow

```text
compiled_schema_json
        +
input_data_json
        +
selected_master_data_json

        ↓

XML Generator Engine

        ↓

XML File
```

---

## 3. Design Principles

The Template Schema is designed to:

* Minimize runtime processing
* Support Pre-compilation
* Support future XML parsing
* Support future Template generation
* Keep XML generation deterministic
* Keep Template definitions independent from implementation code

---

## 4. Source Types

Every XML field must define where its value comes from.

Supported source types:

```text
INPUT

MASTER_DATA

STATIC
```

---

### INPUT

Value entered by the user.

Example:

```json
{
  "GameDate": "2026-06-18"
}
```

---

### MASTER_DATA

Value loaded automatically from selected Master Data.

Example:

```json
{
  "game_kind_id": 2,
  "game_kind_name": "J1"
}
```

---

### STATIC

Fixed value defined by the Template.

Example:

```text
Version = 1
```

---

## 5. Field Types

Supported XML node types:

```text
GROUP

ELEMENT

ATTRIBUTE
```

---

### GROUP

Container node.

Example:

```xml
<GoalInfo>
</GoalInfo>
```

---

### ELEMENT

XML Element.

Example:

```xml
<GameID>123</GameID>
```

---

### ATTRIBUTE

XML Attribute.

Example:

```xml
<GoalInfo No="1">
```

---

## 6. Occurrence Rules

Defines how many times a node may appear.

Supported values:

```text
ONE_OR_MORE

ZERO_OR_MORE

ZERO_OR_ONE
```

Specification Mapping:

```text
１回以上出現
    ↓
ONE_OR_MORE

*
    ↓
ZERO_OR_MORE

?
    ↓
ZERO_OR_ONE
```

---

## 7. Empty Value Rules

Defines behavior when data is missing.

Supported values:

```text
REQUIRED

OMIT_IF_EMPTY

EMPTY_TAG_IF_EMPTY

ZERO_IF_EMPTY
```

---

### REQUIRED

XML generation fails when the value is missing.

---

### OMIT_IF_EMPTY

The XML node is not generated.

---

### EMPTY_TAG_IF_EMPTY

Generate an empty XML tag.

Example:

```xml
<AssistKind></AssistKind>
```

---

### ZERO_IF_EMPTY

Generate value 0.

Example:

```xml
<PKGoalF>0</PKGoalF>
```
# Part 2. Compiled Schema Definition

---

## 8. Compiled Schema Structure

Each Template is converted into a pre-compiled schema and stored in:

```text
templates.compiled_schema_json
```

The XML Generator Engine must read this schema directly during XML generation.

The system shall not rebuild:

```text
Template Tree

TemplateField Hierarchy

Mapping Rules
```

during runtime.

---

### Example

```json
{
  "templateCode": "LIVE_GAME",

  "root": {
    "name": "LiveGame",

    "fieldType": "GROUP",

    "displayOrder": 1,

    "children": [
      {
        "name": "GameID",

        "fieldType": "ELEMENT",

        "sourceType": "INPUT",

        "required": true,

        "dataType": "INTEGER",

        "displayOrder": 1
      },

      {
        "name": "GameDate",

        "fieldType": "ELEMENT",

        "sourceType": "INPUT",

        "required": true,

        "dataType": "DATE",

        "format": "yyyyMMdd",

        "displayOrder": 2
      },

      {
        "name": "GameKindID",

        "fieldType": "ELEMENT",

        "sourceType": "MASTER_DATA",

        "masterDataType": "GAME_KIND",

        "masterDataField": "game_kind_id",

        "required": true,

        "dataType": "INTEGER",

        "displayOrder": 3
      }
    ]
  }
}
```

---

## 9. Schema Node Definition

Every node inside compiled_schema_json shall follow the structure below.

| Property        | Description                     |
| --------------- | ------------------------------- |
| name            | XML node name                   |
| fieldType       | GROUP / ELEMENT / ATTRIBUTE     |
| sourceType      | INPUT / MASTER_DATA / STATIC    |
| dataType        | Data type                       |
| format          | Data format for DATE / DATETIME |
| required        | Required flag                   |
| displayOrder    | XML output order                |
| occurrenceRule  | Node occurrence rule            |
| emptyValueRule  | Empty value handling            |
| staticValue     | Static value                    |
| masterDataType  | Master Data Type                |
| masterDataField | Master Data Field               |
| children        | Child nodes                     |

---

## 10. Data Types

Supported data types:

```text
STRING

INTEGER

LONG

DECIMAL

BOOLEAN

DATE

DATETIME
```

---

### DATE

Examples:

```text
yyyyMMdd

yyyy-MM-dd
```

---

### DATETIME

Examples:

```text
yyyyMMddHHmm

yyyy-MM-dd HH:mm:ss
```

---

### Format Requirement

If:

```text
dataType = DATE

or

dataType = DATETIME
```

the node must define:

```text
format
```

Example:

```json
{
  "dataType": "DATE",

  "format": "yyyyMMdd"
}
```

---

## 11. XML Output Order

XML nodes shall be generated according to:

```text
displayOrder
```

The XML Generator Engine must sort all sibling nodes by:

```text
displayOrder
```

before generating XML.

The system must not rely on:

```text
JSON array order
```

to determine XML output order.

---

### Example

```text
GameID      displayOrder = 1

GameDate    displayOrder = 2

Weather     displayOrder = 3
```

Generated XML:

```xml
<GameID></GameID>

<GameDate></GameDate>

<Weather></Weather>
```

---

## 12. Parent-Child Relationship

A node may contain child nodes.

Example:

```xml
<GoalInfo>

    <StateID></StateID>

    <StateName></StateName>

    <Time></Time>

</GoalInfo>
```

Compiled Schema:

```text
GoalInfo
 ├─ StateID
 ├─ StateName
 └─ Time
```

The XML Generator Engine shall process child nodes recursively.

---

## 13. Group Activation Rule

Some GROUP nodes are optional.

The group becomes active when at least one child field contains a value.

Rule:

```text
At least one child field contains data
                ↓
          Group Active
                ↓
Validate all required child fields
```

---

### Example 1

User input:

```json
{
  "GoalInfo": {}
}
```

Result:

```text
GoalInfo Not Active
```

No child validation is required.

---

### Example 2

User input:

```json
{
  "GoalInfo": {
      "StateID": 1
  }
}
```

Result:

```text
GoalInfo Active
```

The system must validate all required fields inside GoalInfo.

---

### Example 3

User input:

```json
{
  "GoalInfo": {
      "StateID": 1
  }
}
```

Template:

```text
StateID      Required

StateName    Required
```

Validation result:

```text
FAIL
```

because StateName is missing.

---

## 14. Conditional Child Validation

When a parent GROUP becomes active:

```text
Group Active
        ↓
Validate Required Child Fields
```

Required child validation must occur before XML generation.

If validation fails:

```text
XML Generation = Failed
```

and the system shall return validation errors to the user.

---

## 15. ATTRIBUTE Handling

ATTRIBUTE nodes follow the same source resolution logic as ELEMENT nodes.

Supported source types:

```text
INPUT

MASTER_DATA

STATIC
```

---

### Example

XML:

```xml
<GoalInfo No="1">
```

Compiled Schema:

```json
{
  "name": "No",

  "fieldType": "ATTRIBUTE",

  "sourceType": "STATIC",

  "staticValue": "1"
}
```

The generated attribute shall be attached to its parent node during XML generation.

# Part 3. Master Data Resolution & Validation

---

## 16. Master Data Resolution

MASTER_DATA fields are resolved using:

```text id="1ehlgi"
masterDataType
        +
masterDataField
```

Both properties are required.

The system must not resolve Master Data using:

```text id="xlxwaf"
masterDataField
```

alone.

---

### Reason

Different Master Data Types may contain fields with identical names.

Example:

```text id="0vf32n"
TEAM.id

PLAYER.id

STADIUM.id

COMPETITION.id
```

Resolving by field name alone may produce incorrect XML values.

---

### Example

Compiled Schema:

```json id="w3izc9"
{
  "name": "GameKindID",

  "sourceType": "MASTER_DATA",

  "masterDataType": "GAME_KIND",

  "masterDataField": "game_kind_id"
}
```

Selected Master Data:

```json id="r8ys3n"
{
  "GAME_KIND": {
    "game_kind_id": 2,
    "game_kind_name": "J1"
  }
}
```

Generated XML:

```xml id="4h9hbx"
<GameKindID>2</GameKindID>
```

---

## 17. Master Data Mapping Validation

Before XML generation, the system shall validate:

```text id="c3uvzu"
masterDataType exists

masterDataField exists

mapping exists
```

---

### Validation Failure Example

Compiled Schema:

```json id="5qnyzg"
{
  "masterDataType": "GAME_KIND",

  "masterDataField": "game_kind_id"
}
```

Selected Master Data:

```json id="thfbxu"
{
  "TEAM": {
      "team_id": 1
  }
}
```

Result:

```text id="cgq3pp"
Validation Failed

MASTER_DATA_NOT_FOUND
```

---

## 18. Input Data Validation

INPUT fields shall be validated before XML generation.

Validation includes:

* Required validation
* Data type validation
* Format validation
* Group validation
* Occurrence validation

---

### Required Validation

Example:

```json id="cxlcc8"
{
}
```

Schema:

```text id="4wdn8u"
GameID = Required
```

Result:

```text id="40jkj7"
VALIDATION_ERROR
```

---

### Data Type Validation

Example:

Schema:

```text id="g6zl0e"
GameID = INTEGER
```

Input:

```json id="l3xgr8"
{
  "GameID": "ABC"
}
```

Result:

```text id="sdz86s"
VALIDATION_ERROR
```

---

### Format Validation

Example:

Schema:

```text id="mbw7f0"
GameDate

DATE

yyyyMMdd
```

Input:

```json id="zrk0a5"
{
  "GameDate": "2026-06-18"
}
```

Result:

```text id="q3vk3f"
VALIDATION_ERROR
```

Expected:

```text id="jy9yrx"
20260618
```

---

## 19. Occurrence Validation

The XML Generator Engine shall validate node occurrence rules before generating XML.

Supported Rules:

```text id="b63psw"
ONE_OR_MORE

ZERO_OR_MORE

ZERO_OR_ONE
```

---

### ONE_OR_MORE

Example:

```text id="97b7hf"
GoalInfo
```

Rule:

```text id="k0l1e4"
ONE_OR_MORE
```

Input:

```json id="qjl4we"
{
  "GoalInfo": []
}
```

Result:

```text id="v1o27o"
VALIDATION_ERROR
```

---

### ZERO_OR_ONE

Example:

```json id="p6olx6"
{
  "Weather": [
      "Sunny",
      "Rainy"
  ]
}
```

Result:

```text id="9cwyzi"
VALIDATION_ERROR
```

---

## 20. Unknown Field Handling

SavedInput may contain fields that no longer exist in the latest template.

Example:

SavedInput:

```json id="h4lx0o"
{
  "Weather": "Sunny"
}
```

Current Template:

```text id="8ejlyc"
Weather field removed
```

Behavior:

```text id="ndqrd2"
Unknown Field
        ↓
Ignored
```

The XML Generator Engine must ignore obsolete fields.

The system must not fail XML generation because of unknown fields.

---

## 21. Template Compatibility Rules

The system always validates data against:

```text id="cfy12x"
Latest Active Template
```

Only one active version of a template exists in Phase 1.

---

### Example

Template V1:

```text id="89w6pt"
GameID = Optional
```

User saves draft:

```json id="1jlwmx"
{
  "GameID": null
}
```

---

Admin updates template:

```text id="yv1it4"
GameID = Required
```

---

User generates XML:

```text id="0lsj9z"
Validation Failed
```

because the latest template definition is used.

---

## 22. Validation Execution Order

The XML Generator Engine shall perform validation in the following order:

```text id="yquv2q"
1. Template Validation

2. Master Data Validation

3. Required Validation

4. Data Type Validation

5. Format Validation

6. Group Validation

7. Occurrence Validation

8. XML Generation
```

The generation process must stop immediately when any validation step fails.

---

## 23. Validation Error Codes

The system should return standardized validation errors.

Examples:

| Code                        | Description                  |
| --------------------------- | ---------------------------- |
| REQUIRED_FIELD_MISSING      | Required field is missing    |
| INVALID_DATA_TYPE           | Invalid data type            |
| INVALID_DATE_FORMAT         | Invalid date format          |
| INVALID_DATETIME_FORMAT     | Invalid datetime format      |
| MASTER_DATA_NOT_FOUND       | Master data not found        |
| MASTER_DATA_FIELD_NOT_FOUND | Master data field not found  |
| OCCURRENCE_RULE_VIOLATION   | Occurrence rule violation    |
| GROUP_VALIDATION_FAILED     | Required child field missing |
| TEMPLATE_SCHEMA_INVALID     | Invalid compiled schema      |

These error codes are intended for both API responses and UI validation messages.

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

TemplateMasterDataMapping Updated
```

---

### Compilation Process

```text id="f6thlj"
Template

    +

TemplateField

    +

TemplateMasterDataMapping

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

TemplateMasterDataMapping

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

Update TemplateMasterDataMapping

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
TemplateMasterDataMapping
=
compiled_schema_json
```

at all times.

Partial updates are not allowed.

---

## 29. Error Handling During Compilation

Compilation failures shall prevent Template updates from being committed.

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

master_data_type_id

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
