# Part 3. Runtime Resolution & XML Generation Rules

---

## 19. Overview

This section defines how runtime values are resolved and converted into XML output.

The rules in this section are applied after:

```text
Step 3 - Resolve Runtime Values

and

Step 4 - Validation
```

have completed successfully.

---

## 20. Value Resolution Rules

Each XML field shall define a:

```text
sourceType
```

Supported values:

```text
INPUT

MASTER_DATA

STATIC
```

---

### INPUT

Value source:

```text
input_data_json
```

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

### MASTER_DATA

Value source:

```text
selected_master_data_json
```

Example:

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

### STATIC

Value source:

```text
compiled_schema_json
```

Example:

```text
staticValue = 1
```

Resolved Value:

```text
1
```

---
## 21. Group Activation Runtime

A GROUP node may be active or inactive.

Only active groups shall be generated.

---

### Root Node Exception

The root node of the XML document shall always be considered active.

The XML Builder shall always generate the root node regardless of child node activation status.

This ensures that every generated XML document contains exactly one root element.

Example:

```xml
<LiveGame>
</LiveGame>
```

is valid.

The engine shall never generate an empty XML document.

---

### Activation Rule

For non-root GROUP nodes:

A GROUP becomes active when:

```text
At least one child field

triggerActivation = true

AND

contains a meaningful value
```

---

### Meaningful Value

STRING:

```text
value != null

AND

trim(value) != ""
```

---

INTEGER / LONG / DECIMAL:

```text
value != null
```

---

BOOLEAN:

```text
value != null
```

---

### Inactive Group

Inactive non-root groups shall not be generated.

Example:

```xml
<GoalInfo>
</GoalInfo>
```

Result:

```text
Node Omitted
```


---

## 22. Repeatable Group Runtime

A repeatable GROUP may contain multiple occurrences.

Example:

```text
GoalInfo *
```

---

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

---

Generated XML:

```xml
<GoalInfo/>
<GoalInfo/>
<GoalInfo/>
```

---

### Occurrence Processing

The XML Builder shall process occurrences sequentially.

Example:

```text
GoalInfo[0]

GoalInfo[1]

GoalInfo[2]
```

Each occurrence shall be treated as an independent runtime context.

---

## 23. Repeatable Master Data Resolution

Repeatable GROUP nodes may use different Master Data for each occurrence.

Example:

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
    }
  ]
}
```

---

Runtime Resolution:

```text
GoalInfo[0]
    ↓
PLAYER 1001

GoalInfo[1]
    ↓
PLAYER 1002
```

---

### Matching Rule

```text
input_data_json[index]

        ↕

selected_master_data_json[index]
```

Occurrence indexes must match.

---

### Validation

Occurrence matching validation shall only apply when the GROUP contains at least one:

```text
MASTER_DATA
```

field.

---

## 24. Attribute Generation

ATTRIBUTE nodes shall be generated together with their parent element.

Example:

Schema:

```text
Player

 └─ ID (ATTRIBUTE)
```

Resolved Value:

```text
1001
```

Generated XML:

```xml
<Player ID="1001">
</Player>
```

---

### Attribute Ordering

Attributes have no displayOrder.

The XML Builder may generate attributes in implementation-defined order.

---

## 25. Empty Value Handling

After resolution, values may be:

```text
null
```

The XML Builder shall apply:

```text
emptyValueRule
```

---

### OMIT_IF_EMPTY

Result:

```text
Node Not Generated
```

---

### EMPTY_TAG_IF_EMPTY

Result:

```xml
<Node></Node>
```

---

### ZERO_IF_EMPTY

Result:

```xml
<Node>0</Node>
```

---

### REQUIRED

Validation must already have succeeded.

The XML Builder shall assume a valid value exists.

---

## 26. XML Node Ordering

Sibling nodes shall be generated according to:

```text
displayOrder
```

Example:

```text
GameID      1

GameDate    2

Weather     3
```

Generated XML:

```xml
<GameID/>

<GameDate/>

<Weather/>
```

---

## 27. XML Escaping Rules

The XML Builder shall escape:

```text
&
<
>
"
'
```

before generating XML.

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

## 28. XML Output Rules

The generated XML shall:

* Follow compiled_schema_json
* Preserve displayOrder
* Preserve occurrence order
* Preserve attribute relationships
* Apply XML escaping
* Apply empty value rules
* Include XML Declaration
* Use UTF-8 encoding

---

### XML Declaration

Every generated XML document shall begin with:

```xml
<?xml version="1.0" encoding="UTF-8"?>
```

before the root element.

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<LiveGame>
    <GameDate>20260618</GameDate>
</LiveGame>
```

---

### Encoding

The XML Builder shall generate XML using:

```text
UTF-8
```

encoding.

All generated files and preview responses shall use UTF-8.

---

The XML Builder shall not:

* Reorder nodes
* Modify user data
* Modify Master Data values
* Inject additional XML nodes
* Omit XML Declaration

```
```


---

## 29. Runtime Output

The final output of the XML Builder is:

```text
XML Text Stream
```

The XML Text Stream shall contain:

```text
XML Declaration

+

Root Element

+

Generated XML Content
```

Example:

```xml
<?xml version="1.0" encoding="UTF-8"?>

<LiveGame>
    <GameDate>20260618</GameDate>
</LiveGame>
```

The XML Text Stream is passed to:

```text
PREVIEW Response

or

Export Processor
```

depending on:

```text
ExecutionMode
```
