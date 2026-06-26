# Part 3. Template Management

---

# 16. Purpose

The Template Management module allows administrators to create, configure, maintain, and compile XML Templates.

A Template consists of:

* Template Metadata
* XML Structure
* Field Mapping
* Validation Rules
* File Name Pattern
* Compiled Runtime Schema

Template Management is available only to users with the **Administrator** role.

---

# 17. Template List

## Purpose

Display all Templates available in the system.

Administrators may:

* Search Templates
* Create Templates
* Edit Templates
* Compile Templates
* Delete Templates

---

## Screen Layout

```text
+-------------------------------------------------------------------+

Template Management

------------------------------------------------------------

Search

[ Keyword............................... ] [ Search ]

------------------------------------------------------------

+---------------------------------------------------------------+

Name

Template Code

Status

Version

Last Updated

Actions

---------------------------------------------------------------

LIVE_GAME

ACTIVE

v8

Edit

Compile

Delete

---------------------------------------------------------------

[ Previous ]     1     2     3     [ Next ]

+-------------------------------------------------------------------+
```

---

## UI Components

| Component      | Description                     |
| -------------- | ------------------------------- |
| Search Box     | Search Template by name or code |
| Template Table | Display all Templates           |
| Create Button  | Create new Template             |
| Edit Button    | Open Template Editor            |
| Compile Button | Compile Template                |
| Delete Button  | Delete Template                 |
| Pagination     | Navigate result pages           |

---

## User Actions

Administrators may:

* Search
* Open Template
* Create Template
* Compile Template
* Delete Template

---

## API Integration

```http
GET /api/v1/templates

POST /api/v1/templates

DELETE /api/v1/templates/{id}

POST /api/v1/templates/{id}/compile
```

Reference:

```
06-api-design.md
```

---

# 18. Create Template

## Purpose

Create a new Template before defining its XML structure.

---

## Screen Layout

```text
Template Information

Template Code

[____________________]

Template Name

[____________________]

Description

[____________________]

File Name Pattern

[ live_game.xml ]

[ Cancel ]        [ Create ]
```

---

## Validation

Required fields:

* Template Code
* Template Name
* File Name Pattern

Template Code shall be unique.

File Name Pattern shall follow the rules defined in:

```
04-template-schema.md
```

---

## Processing

Create only the Template metadata.

No XML structure is created automatically.

No compilation is performed.

---

## API Integration

```http
POST /api/v1/templates
```

---

### XML Tree

Displays the editable XML hierarchy.

Supports:

* Expand
* Collapse
* Drag & Drop
* Reorder

---

### Root Node

Each Template shall contain exactly one Root Node.

When a new Template is created, the editor shall automatically create an initial Root Node.

Example:

```xml
<Root>

</Root>
```

The Root Node represents the top-level XML element used during XML Generation.

The Root Node:

* Cannot be deleted.
* Cannot be duplicated.
* Cannot have sibling Root Nodes.
* May be renamed by the administrator.

The editor shall prevent any operation that would violate the requirement of having exactly one Root Node.

Structural violations shall be prevented immediately by the UI before compilation.


---

# 20. Compile Template

## Purpose

Validate the editable Template Schema and generate the runtime compiled schema.

Compilation is an explicit administrator action.

---

## User Flow

```text
Save Schema

↓

Compile

↓

Validation

↓

Success

or

Error List
```

---

## Success

Display:

```text
Template compiled successfully.
```

The compiled schema becomes available for XML Generation.

---

## Failure

Compilation errors shall be displayed in a dedicated panel.

Examples:

* Circular Reference
* Invalid Mapping
* Duplicate Display Order
* Invalid XML Hierarchy

The editable schema remains unchanged.

---

## API Integration

```http
POST /api/v1/templates/{id}/compile
```

---

## 21. Concurrent Editing

The Template Editor supports optimistic locking.

Every editable Template contains a Version value.

---

### Save Flow

```text
Open Template

↓

Version = 8

↓

Edit

↓

Save

↓

Version Check

↓

Success

or

Conflict
```

---

### Version Conflict

If another administrator has modified the Template:

```text
Database Version

≠

Client Version
```

the update shall be rejected.

---

### Conflict Handling

When a version conflict occurs, the UI shall:

* Preserve all unsaved local editor changes.
* Prevent overwriting newer server changes.
* Display a version conflict dialog.
* Allow the administrator to open the latest server version for comparison.
* Allow the administrator to manually reconcile differences before attempting another save.

The editor shall not automatically discard the administrator's current work.

Reloading the latest Template shall always require explicit user confirmation.

---

### API Response

```http
409 Conflict
```

Error Code:

```text
TEMPLATE_VERSION_CONFLICT
```


---

# 22. Design Principles

The Template Management screens follow the principles below.

---

## Metadata and Schema Separation

Template metadata and Template Schema are edited independently.

Updating metadata shall not modify the XML structure.

---

## Draft Editing

Template editing supports iterative development.

Administrators may save incomplete Template Schemas.

Business validation occurs only during compilation.

---

## Explicit Compilation

Compilation is never performed automatically.

Administrators explicitly decide when a Template becomes executable.

---

## Large Tree Support

The XML Tree should efficiently support Templates containing hundreds of nodes.

The UI should provide:

* Expand / Collapse
* Tree Navigation
* Automatic Scrolling
* Node Highlighting

---

## Backend-Driven Validation

The UI performs only basic input validation.

Structural validation and compilation rules are enforced by the Backend.

---

## Version Protection

Concurrent updates shall be prevented through optimistic locking.

The UI shall never overwrite newer Template changes without user confirmation.
