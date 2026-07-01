# Domain Model

## 1. Overview

The Domain Model defines the core business entities and relationships used by the XML Generator System.

The design supports:

* Template-based XML generation
* Master Data reuse
* User data isolation
* Export history management
* Future template expansion
* Future XML-to-Template conversion (Phase 3)

---

## 2. Domain Overview

```text
User
│
├── WorkspaceMember
│       └── Workspace
│              ├── Template
│              │      ├── TemplateField
│              │      ├── TemplateMapping
│              │      └── TemplateGuide
│              ├── MasterDataType
│              │      ├── MasterDataField
│              │      └── MasterDataRecord
│              ├── SavedInput
│              └── ExportHistory
```

> **Phase 7.1.0:** Workspace ownership is defined in [p5_workspace-ownership.md](./p5_workspace-ownership.md) and [ADR-003](../adr/ADR-003-workspace-ownership.md). Implementation begins in Phase 7.1.1.

### Legacy overview (pre-7.1 — superseded for ownership)

```text
User
│
├── SavedInput
├── ExportHistory
│
Template
│
├── TemplateField
├── TemplateMapping
└── TemplateGuide
│
MasterDataType
│
├── MasterDataField
└── MasterDataRecord
```

---

## 3. User

Represents a system user.

### Fields

```text
User
------
id

username

password_hash

role

created_at
updated_at
```

### Role

```text
ADMIN
USER
```

### Relationships

```text
User
 ├─ SavedInputs
 └─ ExportHistories
```

### Notes

The system uses simple username/password authentication.

The following features are intentionally excluded from Phase 1:

* Email Address
* Email Verification
* Password Reset
* OAuth Login
* Refresh Token
* Session Management
* Remember Me

Authentication is only used to identify the current user and apply role-based permissions.

---

## 4. Template

Represents an XML template.

Examples:

```text
LIVE_GAME
PLAYER_STATS
TEAM_STATS
```

### Fields

```text
Template
------
id

code
name

description

status

created_by

created_at
updated_at
```

### Relationships

```text
Template
 ├─ TemplateFields
 ├─ TemplateMappings
 └─ TemplateGuide
```

---

## 5. TemplateField

Represents a node within the XML structure.

Examples:

```xml
<GameID>

<StateID>

<GoalInfo No="1">
```

### Fields

```text
TemplateField
------
id

template_id

parent_id

field_name

display_name

xml_name

node_type

value_type

source_type

static_value

default_value

occurrence_rule

empty_handling

required_when_parent_exists

trigger_activation

display_order

xml_path

namespace

description
```

`field_name` is the internal stable identifier used for form binding and
`input_data_json` keys. It is never emitted in generated XML.

In API requests and responses, hierarchy is expressed as `parentFieldName`
referencing another field's `field_name`. Persistence stores `parent_id`.

`xml_name` is the actual XML node or attribute name.

`display_name` is the UI label for Dynamic Form Rendering.

`TemplateField` describes XML structure only. It must not contain master data
field references. See `TemplateMapping`.

When `source_type = MASTER_DATA`, exactly one `TemplateMapping` must reference
the field. Compilation fails if the mapping is missing. `source_type` is stored
explicitly and is not derived from mapping existence.

`trigger_activation` declares whether a child node's resolved value activates an
optional parent GROUP. Defaults: `INPUT → true`, `MASTER_DATA → false`,
`STATIC → false`. Nullable on `TemplateField`; when null, the default for the
field's `source_type` applies.

### Node Type

```text
GROUP
ELEMENT
ATTRIBUTE
```

### Occurrence Rule

```text
ONE_OR_MORE
ZERO_OR_MORE
ZERO_OR_ONE
```

Mapping:

```text
１回以上出現 → ONE_OR_MORE
*            → ZERO_OR_MORE
?            → ZERO_OR_ONE
```

### Empty Handling

```text
REQUIRED
OMIT_IF_EMPTY
EMPTY_TAG_IF_EMPTY
ZERO_IF_EMPTY
```

### Parent-Child Relationship

TemplateField supports self-referencing relationships.

Example:

```text
GoalInfo
 ├─ StateID
 ├─ StateName
 ├─ Time
 └─ TeamID
```

---

## 6. TemplateGuide

Represents documentation associated with a template.

Supported file types:

```text
PDF
DOCX
XLSX
```

### Fields

```text
TemplateGuide
------
id

template_id

file_name
file_path

file_type

uploaded_by

created_at
updated_at
```

---

## 7. MasterDataType

Represents a category of reusable business data.

Examples:

```text
GAME_KIND
TEAM
PLAYER
STADIUM
SEASON
COMPETITION
```

### Fields

```text
MasterDataType
------
id

code
name

description

created_at
updated_at
```

### Relationships

```text
MasterDataType
 ├─ MasterDataFields
 └─ MasterDataRecords
```

---

## 8. MasterDataField

Defines the schema of a Master Data Type.

Example:

```text
GAME_KIND

game_kind_id
game_kind_name
```

### Fields

```text
MasterDataField
------
id

master_data_type_id

field_name

data_type

display_order
```

---

## 9. MasterDataRecord

Represents actual reusable business data.

Example:

```json
{
  "game_kind_id": 2,
  "game_kind_name": "J1"
}
```

### Fields

```text
MasterDataRecord
------
id

master_data_type_id

data_json

created_at
updated_at
```

---

## 10. TemplateMapping

Defines how a `MasterDataField` is mapped to a `TemplateField`.

`TemplateMapping` is a separate metadata entity. Neither `TemplateField` nor
`MasterDataField` contains mapping information.

Example:

```text
MasterDataField: GAME_KIND.game_kind_id
        ↓
TemplateMapping
        ↓
TemplateField: GameKindID (xml_name)
```

### Fields

```text
TemplateMapping
------
id

template_id

template_field_id

master_data_field_id

created_at
updated_at
```

### Relationships

```text
Template
 └─ TemplateMappings

TemplateMapping
 ├─ TemplateField
 └─ MasterDataField
```

Deleting a `MasterDataField` must not be blocked by a mapping. The foreign key
uses `ON DELETE SET NULL`. Compile-time validation for invalid mappings is
deferred to the dedicated compile-validation phase.

---

## 11. SavedInput

Stores user input data for reuse and auto-fill.

### Fields

```text
SavedInput
------
id

user_id

template_id

input_data_json

selected_master_data_json

created_at
updated_at
```

### Constraints

```text
UNIQUE(user_id, template_id)
```

Only one SavedInput record shall exist for each User + Template combination.

When a user saves data:

* Existing record → UPDATE
* No existing record → INSERT

The system shall not create a new SavedInput version for every save operation.

### Purpose

Used for:

* Auto Fill Previous Data
* Draft Recovery
* User Productivity

### Notes

SavedInput is designed as a working draft.

Retention policies do not apply to SavedInput in Phase 1.

The following features are intentionally excluded:

* SavedInput Versioning
* Draft History
* Draft Rollback
* Draft Comparison

---

## 12. ExportHistory

Stores generated XML export history.

### Fields

```text
ExportHistory
------
id

user_id

template_id

saved_input_id

status

file_name
file_path
file_size

error_message

generated_at
expired_at
```

### Status

```text
PROCESSING
SUCCESS
FAILED
```

### Status Flow

```text
PROCESSING
    ↓
 SUCCESS

PROCESSING
    ↓
 FAILED
```

### Purpose

Used for:

* Export History
* File Download
* Retention Policy
* Audit Trail

### Notes

ExportHistory stores immutable snapshots.

Historical records must not be modified after creation.

Retention policy may automatically remove records after expiration.

---

## 13. Entity Relationship Summary

```text
User
 └─ WorkspaceMember
        └─ Workspace
               ├─ Template
               │     ├─ TemplateField
               │     ├─ TemplateMapping
               │     └─ TemplateGuide
               ├─ MasterDataType
               │     ├─ MasterDataField
               │     └─ MasterDataRecord
               ├─ SavedInput
               └─ ExportHistory
```

See [p5_workspace-ownership.md](./p5_workspace-ownership.md) for the approved Phase 7.1 model.

### Pre-7.1 reference

```text
User
 ├─ SavedInput
 └─ ExportHistory

Template
 ├─ TemplateField
 ├─ TemplateMapping
 └─ TemplateGuide

MasterDataType
 ├─ MasterDataField
 └─ MasterDataRecord

TemplateMapping
 ├─ Template
 ├─ TemplateField
 └─ MasterDataField

SavedInput
 ├─ User
 └─ Template

ExportHistory
 ├─ User
 ├─ Template
 └─ SavedInput
```

---

## 14. MVP Design Decisions

### Authentication

Not included:

* Email Login
* Session Tracking
* Refresh Token
* OAuth
* Password Reset

### SavedInput

Not included:

* Version History
* Snapshot History
* Rollback
* Draft Comparison

A single SavedInput record is maintained per User + Template.

### ExportHistory

Includes:

* Snapshot Storage
* Status Tracking
* Retention Support

---

## 15. Future Considerations

SavedInput is designed as a working draft.

Retention policies do not apply to SavedInput in Phase 1.

Administrative cleanup of inactive user data may be introduced in future releases.

The following entities are intentionally excluded from Phase 1:

```text
SavedInputVersion
TemplateVersion
FieldValidationRule
RolePermission
UserPermission
```

Reason:

* Not required for current business scope
* Would increase complexity unnecessarily
* Can be introduced in future phases if needed

The current Domain Model is intentionally optimized for MVP implementation while remaining extensible for:

* Phase 2 (Master Data Import)
* Phase 3 (XML Upload → Template Generation)
* Additional Template Types
* Advanced Validation Rules
* Version Management

```
```
