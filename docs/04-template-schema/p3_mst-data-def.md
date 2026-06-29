# Part 3. Master Data Resolution & Validation

---

## 16. Master Data Resolution

At metadata level, master data bindings are defined by `TemplateMapping` (linking
`TemplateField` to `MasterDataField`). At runtime, the compiled schema embeds
resolved `masterDataType` and `masterDataField` per node.

MASTER_DATA fields are resolved using:

```text id="v5hf2w"
masterDataType
        +
masterDataField
```

Both properties are required.

The system must not resolve Master Data using:

```text id="ibnyji"
masterDataField
```

alone.

---

### Reason

Different Master Data Types may contain fields with identical names.

Example:

```text id="sww3j2"
TEAM.id

PLAYER.id

STADIUM.id

COMPETITION.id
```

Resolving by field name alone may produce incorrect XML values.

---

### Example

Compiled Schema:

```json id="oftrkk"
{
  "name": "GameKindID",

  "sourceType": "MASTER_DATA",

  "masterDataType": "GAME_KIND",

  "masterDataField": "game_kind_id"
}
```

Selected Master Data:

```json id="m8l9rf"
{
  "GAME_KIND": {
    "game_kind_id": 2,
    "game_kind_name": "J1"
  }
}
```

Generated XML:

```xml id="9bxk8v"
<GameKindID>2</GameKindID>
```

---

## 17. Master Data Mapping Validation

Before XML generation, the system shall validate:

```text id="u2ct9u"
masterDataType exists

masterDataField exists

mapping exists
```

---

### Required Master Data

If:

```text id="pfw5hm"
emptyHandling = REQUIRED
```

and the specified Master Data cannot be resolved:

```text id="xtl0au"
MASTER_DATA_NOT_FOUND
```

validation shall fail.

---

### Optional Master Data

If:

```text id="i0e1r6"
emptyHandling ≠ REQUIRED
```

and the specified Master Data cannot be resolved:

```text id="j07tdn"
MASTER_DATA_NOT_FOUND
```

validation shall not fail.

The resolved value shall be treated as:

```text id="5v8mpu"
null
```

and processed according to:

```text id="7zztvc"
emptyHandling
```

---

### Validation Failure Example

Compiled Schema:

```json id="ggkjaj"
{
  "masterDataType": "GAME_KIND",

  "masterDataField": "game_kind_id",

  "emptyHandling": "REQUIRED"
}
```

Selected Master Data:

```json id="u7rthx"
{
  "TEAM": {
      "team_id": 1
  }
}
```

Result:

```text id="0nnmxk"
Validation Failed

MASTER_DATA_NOT_FOUND
```

---

## 18. Empty Value Resolution

After source resolution, the system may receive:

```text id="8p8r3f"
null
```

from:

```text id="qf8vvn"
INPUT

MASTER_DATA

STATIC
```

The XML Generator Engine shall apply:

```text id="f0t62r"
emptyHandling
```

regardless of the source type.

---
## 18A. Repeatable Master Data Payload Structure

The system stores user input data and selected Master Data separately.

```text
input_data_json

selected_master_data_json
```

Both structures must follow the same occurrence order for repeatable GROUP nodes.

---

### Example

Template:

```text
GoalInfo *
```

Each GoalInfo occurrence may reference a different Master Data record.

---

### input_data_json

```json
{
  "GoalInfo": [
    {
      "Time": 17,
      "Body": "Right"
    },
    {
      "Time": 35,
      "Body": "Left"
    },
    {
      "Time": 88,
      "Body": "Header"
    }
  ]
}
```

---

### selected_master_data_json

```json
{
  "GoalInfo": [
    {
      "PLAYER": {
        "player_id": 1001
      }
    },
    {
      "PLAYER": {
        "player_id": 1002
      }
    },
    {
      "PLAYER": {
        "player_id": 1003
      }
    }
  ]
}
```

---

### Runtime Resolution

```text
GoalInfo[0]
    ↓
Input Data
    Time = 17
    Body = Right

Master Data
    PLAYER = 1001

--------------------------------

GoalInfo[1]
    ↓
Input Data
    Time = 35
    Body = Left

Master Data
    PLAYER = 1002

--------------------------------

GoalInfo[2]
    ↓
Input Data
    Time = 88
    Body = Header

Master Data
    PLAYER = 1003
```

---

### Occurrence Matching Rule

For repeatable GROUP nodes:

```text
input_data_json[index]

        ↕

selected_master_data_json[index]
```

The XML Generator Engine shall resolve Master Data using the same occurrence index.

Example:

```text
GoalInfo[0]
    ↔
selected_master_data_json.GoalInfo[0]

GoalInfo[1]
    ↔
selected_master_data_json.GoalInfo[1]

GoalInfo[2]
    ↔
selected_master_data_json.GoalInfo[2]
```

---

### Validation Rules

The XML Generator Engine shall validate occurrence alignment only when the repeatable GROUP contains at least one field with:

```text id="a5gm0t"
sourceType = MASTER_DATA
```

---

Example:

```text id="2w9v7z"
GoalInfo *
```

Contains:

```text id="bdj4r4"
PlayerID

PlayerName
```

resolved from:

```text id="nuk0jc"
MASTER_DATA
```

Validation shall verify:

```text id="o3n1kz"
input_data_json occurrence count

=

selected_master_data_json occurrence count
```

---

Example:

```text id="3v1bho"
GoalInfo[0]
    ↔
selected_master_data_json.GoalInfo[0]

GoalInfo[1]
    ↔
selected_master_data_json.GoalInfo[1]
```

---

Validation fails if:

```text id="tb8jwl"
input_data_json occurrence count
        ≠
selected_master_data_json occurrence count
```

for the same repeatable GROUP.

---

### Groups Without Master Data

If a repeatable GROUP contains only:

```text id="g2u0r9"
INPUT

STATIC
```

fields and does not contain any:

```text id="g5gk4t"
MASTER_DATA
```

fields, the system shall not require:

```text id="76ih7k"
selected_master_data_json
```

to contain matching occurrences.

---

Example

Template:

```text id="6bgd8y"
GoalInfo *

 ├─ Time

 ├─ Body

 └─ Comment
```

All fields use:

```text id="8i6m1w"
INPUT
```

Input:

```json id="8w1jgm"
{
  "GoalInfo": [
    {
      "Time": 17
    },
    {
      "Time": 35
    }
  ]
}
```

selected_master_data_json:

```json id="i79z2r"
{}
```

Result:

```text id="2o6t9z"
Validation Passed
```

because the group does not contain any MASTER_DATA fields.


---

### Design Principle

The system must treat each occurrence of a repeatable GROUP as an independent context.

Master Data selected for one occurrence shall not be shared automatically with other occurrences unless explicitly configured by the template.


---

### OMIT_IF_EMPTY

Resolved Value:

```text id="5n6m3u"
null
```

Result:

```xml id="cgh3xq"
Node is not generated
```

---

### EMPTY_TAG_IF_EMPTY

Resolved Value:

```text id="lkk6x5"
null
```

Result:

```xml id="wgpprj"
<Node></Node>
```

---

### ZERO_IF_EMPTY

Resolved Value:

```text id="rf3vgf"
null
```

Result:

```xml id="4c7r1s"
<Node>0</Node>
```

---

### REQUIRED

Resolved Value:

```text id="r6nphd"
null
```

Result:

```text id="jlwmv1"
VALIDATION_ERROR
```

---

## 19. Input Data Validation

INPUT fields shall be validated before XML generation.

Validation includes:

* Required validation
* Data type validation
* Format validation
* Group validation
* Occurrence validation

---

### Required Validation

Schema:

```text id="jlwmv2"
GameID = Required
```

Input:

```json id="jlwmv3"
{
}
```

Result:

```text id="jlwmv4"
REQUIRED_FIELD_MISSING
```

---

### Data Type Validation

Schema:

```text id="jlwmv5"
GameID = INTEGER
```

Input:

```json id="jlwmv6"
{
  "GameID": "ABC"
}
```

Result:

```text id="jlwmv7"
INVALID_DATA_TYPE
```

---

### Format Validation

Schema:

```text id="jlwmv8"
GameDate

DATE

yyyyMMdd
```

Input:

```json id="jlwmv9"
{
  "GameDate": "2026-06-18"
}
```

Result:

```text id="jlwmva"
INVALID_DATE_FORMAT
```

Expected:

```text id="jlwmvb"
20260618
```

---

## 20. Occurrence Validation

The XML Generator Engine shall validate node occurrence rules before generating XML.

Supported Rules:

```text id="jlwmvc"
ONE_OR_MORE

ZERO_OR_MORE

ZERO_OR_ONE
```

---

### ONE_OR_MORE

Rule:

```text id="jlwmvd"
GoalInfo
```

Occurrence:

```text id="jlwmve"
ONE_OR_MORE
```

Input:

```json id="jlwmvf"
{
  "GoalInfo": []
}
```

Result:

```text id="jlwmvg"
OCCURRENCE_RULE_VIOLATION
```

---

### ZERO_OR_ONE

Input:

```json id="jlwmvh"
{
  "Weather": [
      "Sunny",
      "Rainy"
  ]
}
```

Result:

```text id="jlwmvi"
OCCURRENCE_RULE_VIOLATION
```

---

## 21. Unknown Field Handling

SavedInput may contain fields that no longer exist in the latest template.

Example:

SavedInput:

```json id="jlwmvj"
{
  "Weather": "Sunny"
}
```

Current Template:

```text id="jlwmvk"
Weather field removed
```

Behavior:

```text id="jlwmvl"
Unknown Field
        ↓
Ignored
```

The XML Generator Engine must ignore obsolete fields.

The system must not fail XML generation because of unknown fields.

---

## 22. Template Compatibility Rules

The system always validates data against:

```text id="jlwmvm"
Latest Active Template
```

Only one active version of a template exists in Phase 1.

---

### Example

Template:

```text id="jlwmvn"
GameID = Optional
```

User saves draft:

```json id="jlwmvo"
{
  "GameID": null
}
```

---

Admin updates template:

```text id="jlwmvp"
GameID = Required
```

---

User generates XML:

```text id="jlwmvq"
Validation Failed
```

because the latest template definition is used.

---

## 23. Validation Execution Order

The XML Generator Engine shall perform validation in the following order:

```text
1. Template Validation

2. Master Data Validation

3. Required Validation

4. Data Type Validation

5. Format Validation

6. Group Validation

7. Occurrence Validation
```

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

Validation must continue even when validation errors are found.

The system shall return all validation errors in a single response.

---

### Field-Level Short Circuit

To avoid duplicate or cascading validation errors, validation shall stop for the current field once a critical validation error is detected.

Example:

```text
PlayerID

↓

MASTER_DATA_NOT_FOUND
```

Result:

```text
PlayerID marked as INVALID
```

The Validation Engine shall skip:

```text
Required Validation

Data Type Validation

Format Validation
```

for that field.

Validation shall continue for all other fields.

---

### Example

Input:

```json
{
  "GameID": "ABC",

  "GameDate": "2026/99/99",

  "PlayerID": 999999
}
```

Validation Result:

```json
[
  {
    "field": "GameID",
    "code": "INVALID_DATA_TYPE"
  },
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

The Validation Engine shall not return:

```text
MASTER_DATA_NOT_FOUND

REQUIRED_FIELD_MISSING
```

for the same field simultaneously.

Only the root validation error shall be returned for that field.

---

### XML Generation Condition

XML generation may begin only when:

```text
Validation Error Count = 0
```

If any validation errors exist:

```text
XML Generation Blocked
```

and the collected validation errors shall be returned to the caller.


---

## 24. Validation Error Codes

The system should return standardized validation errors.

| Code                        | Description                  |
| --------------------------- | ---------------------------- |
| REQUIRED_FIELD_MISSING      | Required field is missing    |
| INVALID_DATA_TYPE           | Invalid data type            |
| INVALID_DATE_FORMAT         | Invalid date format          |
| INVALID_DATETIME_FORMAT     | Invalid datetime format      |
| MASTER_DATA_NOT_FOUND       | Master Data not found        |
| MASTER_DATA_FIELD_NOT_FOUND | Master Data field not found  |
| OCCURRENCE_RULE_VIOLATION   | Occurrence rule violation    |
| GROUP_VALIDATION_FAILED     | Required child field missing |
| TEMPLATE_SCHEMA_INVALID     | Invalid compiled schema      |

These error codes are intended for:

* API responses
* UI validation messages
* XML Preview validation
* XML Generation validation

---

## 25. XML Generation Prerequisite

The XML Generator Engine may begin XML generation only when:

```text id="jlwmvw"
Template Validation Passed

Master Data Validation Passed

Input Validation Passed

Occurrence Validation Passed

Validation Error Count = 0
```

If any validation errors exist:

```text id="jlwmvx"
XML Generation Blocked
```

and validation errors shall be returned to the caller.

---
## 26. Repeatable Master Data Resolution

Some repeatable GROUP nodes may reference different Master Data records for each occurrence.

Example:

```text
GoalInfo *
```

Each GoalInfo may reference a different player.

Example:

```text
Goal 1
    → Player A

Goal 2
    → Player B

Goal 3
    → Player C
```

The system shall support Master Data selection at the occurrence level.

Example:

```json
{
  "GoalInfo": [
    {
      "player_id": 1001
    },
    {
      "player_id": 1002
    },
    {
      "player_id": 1003
    }
  ]
}
```

During XML generation:

```text
GoalInfo[0]
    ↓
Player 1001

GoalInfo[1]
    ↓
Player 1002

GoalInfo[2]
    ↓
Player 1003
```

The XML Generator Engine shall resolve Master Data independently for each occurrence.

The system must not assume a single Master Data record is shared by all occurrences of a repeatable GROUP.
