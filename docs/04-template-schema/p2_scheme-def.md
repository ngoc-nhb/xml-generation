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
| masterDataType  | Master Data Type Code           |
| masterDataField | Master Data Field Name          |
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

A GROUP becomes active when at least one child node contains a resolved value that is allowed to trigger activation.

A resolved value may come from:

* INPUT
* MASTER_DATA
* STATIC

However, not all child nodes may trigger group activation.

Each node may define:

```text
triggerActivation
```

Supported values:

```text
true

false
```

Default behavior:

```text
INPUT         → triggerActivation = true

MASTER_DATA   → triggerActivation = false

STATIC        → triggerActivation = false
```

Group activation rule:

```text
At least one child node satisfies:

triggerActivation = true

and

resolved value is meaningful

            ↓

      Group Active

            ↓

Validate all required child fields
```

### Meaningful Value Definition

A value is considered meaningful when:

STRING
    → value != null AND trim(value) != ""

INTEGER
    → value != null

LONG
    → value != null

DECIMAL
    → value != null

BOOLEAN
    → value != null

### Example 1

Schema:

```text
GoalInfo
 ├─ Version

      sourceType = STATIC

      staticValue = 1

      triggerActivation = false

 └─ PlayerName

      sourceType = INPUT

      required = true
```

Input:

```json
{
}
```

Result:

```text
GoalInfo Not Active
```

The static field alone does not activate the group.

---

### Example 2

Schema:

```text
GoalInfo
 ├─ Version

      sourceType = STATIC

      staticValue = 1

 └─ PlayerName

      sourceType = INPUT

      required = true
```

Input:

```json
{
  "PlayerName": "Tanaka"
}
```

Result:

```text
GoalInfo Active
```

because PlayerName contains a value.

---

### Example 3

Schema:

```text
GoalInfo
 └─ GameKindID

      sourceType = MASTER_DATA

      triggerActivation = true
```

Selected Master Data:

```json
{
  "GAME_KIND": {
      "game_kind_id": 2
  }
}
```

Result:

```text
GoalInfo Active
```

because the resolved Master Data value activates the group.


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

Supported validation rules:

```text
required

dataType

format

emptyValueRule
```

Supported source resolution:

```text
INPUT

MASTER_DATA

STATIC
```

---

### Example 1

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

---

### Example 2

XML:

```xml
<Player ID="1651283">
```

Compiled Schema:

```json
{
  "name": "ID",

  "fieldType": "ATTRIBUTE",

  "sourceType": "MASTER_DATA",

  "masterDataType": "PLAYER",

  "masterDataField": "player_id"
}
```

The generated attribute shall be attached to its parent node during XML generation.
