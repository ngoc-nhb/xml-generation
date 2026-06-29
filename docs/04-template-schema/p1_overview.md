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
TemplateMapping

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
* Persist `TemplateField` and `TemplateMapping` atomically with immediate
  compilation (Single Save Principle, ADR-002)
* Declare `source_type` explicitly on each field; do not infer it from mappings

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

Requires exactly one `TemplateMapping` at compile time. `source_type` is stored
on `TemplateField` and is not derived from mapping existence.

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

## 7. Empty Handling Rules

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
