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
├── SavedInput
├── ExportHistory
│
Template
│
├── TemplateField
├── TemplateGuide
└── TemplateMasterDataMapping
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
 ├─ TemplateGuide
 └─ TemplateMasterDataMappings
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

name

field_type

occurrence_rule

empty_value_rule

required_when_parent_exists

display_order

xml_path

description
```

### Field Type

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

### Empty Value Rule

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

## 10. TemplateMasterDataMapping

Defines how Master Data fields are mapped to XML template fields.

Example:

```text
game_kind_id
    ↓
GameKindID

game_kind_name
    ↓
GameKindName
```

### Fields

```text
TemplateMasterDataMapping
------
id

template_id

master_data_type_id

source_field

target_template_field
```

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
 ├─ SavedInput
 └─ ExportHistory

Template
 ├─ TemplateField
 ├─ TemplateGuide
 └─ TemplateMasterDataMapping

MasterDataType
 ├─ MasterDataField
 └─ MasterDataRecord

TemplateMasterDataMapping
 ├─ Template
 └─ MasterDataType

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
