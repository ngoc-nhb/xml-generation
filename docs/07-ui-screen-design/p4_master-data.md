# Part 4. Master Data Management

---

# 23. Purpose

The Master Data Management module allows administrators to configure reusable reference data used by XML Templates.

Master Data consists of two levels:

* Master Data Type
* Master Data Records

Each Master Data Type defines its own dynamic schema.

Each Master Data Record stores data conforming to that schema.

Only users with the **Administrator** role may access this module.

---

# 24. Master Data Type List

## Purpose

Display all configured Master Data Types.

Administrators may:

* Create Master Data Type
* Edit Master Data Type
* Configure Master Data Schema
* Delete Master Data Type
* Open Master Data Records

---

## Screen Layout

```text
+----------------------------------------------------------------+

Master Data Types

------------------------------------------------------------

Search

[ Keyword........................ ] [ Search ]

------------------------------------------------------------

Type Name

Code

Records

Updated

Actions

------------------------------------------------------------

Game Kind

GAME_KIND

15

Edit

Records

Delete

------------------------------------------------------------

[ Previous ]     1     2     3     [ Next ]

+----------------------------------------------------------------+
```

---

## UI Components

| Component      | Description               |
| -------------- | ------------------------- |
| Search Box     | Search Master Data Types  |
| Table          | Display Master Data Types |
| Create Button  | Create new Type           |
| Edit Button    | Edit Type                 |
| Records Button | Open Record List          |
| Delete Button  | Delete Type               |
| Pagination     | Navigate pages            |

---

## User Actions

Administrators may:

* Search
* Create
* Edit
* Delete
* View Records

---

## API Integration

```http
GET /api/v1/master-data-types

POST /api/v1/master-data-types

PUT /api/v1/master-data-types/{id}

DELETE /api/v1/master-data-types/{id}
```

---

# 25. Master Data Type Editor

## Purpose

Configure the metadata and schema of a Master Data Type.

---

## Screen Layout

```text
Master Data Type

Type Code

[________________]

Type Name

[________________]

Description

[________________]

-----------------------------------

Schema Editor

Field Name

Data Type

Required

Default Value

-----------------------------------

[ Add Field ]

-----------------------------------

[ Save ]
```

---

## Editable Metadata

* Type Code
* Type Name
* Description

---

## Schema Editor

Administrators may define:

* Field Name
* Data Type
* Required
* Default Value

Supported data types include:

* STRING
* INTEGER
* DECIMAL
* BOOLEAN
* DATE
* DATETIME

---

## User Actions

Administrators may:

* Add Field
* Edit Field
* Delete Field
* Reorder Fields
* Save Schema

---

## API Integration

```http
GET /api/v1/master-data-types/{id}

PUT /api/v1/master-data-types/{id}/schema
```

---

# 26. Master Data Record List

## Purpose

Display all Records belonging to a selected Master Data Type.

---

## Screen Layout

```text
+---------------------------------------------------------------+

Master Data Records

Type

GAME_KIND

---------------------------------------------------------------

Search

[ Keyword............... ]

---------------------------------------------------------------

ID

Code

Name

Updated

Actions

---------------------------------------------------------------

1

LEAGUE

J1 League

Edit

Delete

---------------------------------------------------------------

[ Previous ]     1     2     3     [ Next ]

+---------------------------------------------------------------+
```

---

## UI Components

| Component     | Description     |
| ------------- | --------------- |
| Search        | Search Records  |
| Table         | Display Records |
| Create Button | Create Record   |
| Edit Button   | Edit Record     |
| Delete Button | Delete Record   |

---

## API Integration

```http
GET /api/v1/master-data-types/{id}/records

POST /api/v1/master-data-records

PUT /api/v1/master-data-records/{id}

DELETE /api/v1/master-data-records/{id}
```

---

## 27. Master Data Record Editor

### Purpose

Create or edit a Master Data Record.

The editor is generated dynamically based on the configured Master Data Schema.

---

### Dynamic Form

The UI shall generate input controls according to the current Master Data Schema.

Examples:

| Data Type | UI Control       |
| --------- | ---------------- |
| STRING    | Text Box         |
| INTEGER   | Number Box       |
| DECIMAL   | Decimal Input    |
| BOOLEAN   | Checkbox         |
| DATE      | Date Picker      |
| DATETIME  | Date Time Picker |

The UI shall not hard-code any field definitions.

---

### Schema Compatibility

When editing an existing Master Data Record, the current Schema may contain fields that did not exist when the Record was originally created.

The UI shall compare the stored Record with the current Master Data Schema.

If a field defined in the current Schema does not exist in the stored Record:

* The field shall be added to the generated form.
* If the Schema defines a Default Value, the UI shall initialize the field with that value.
* Otherwise, the field shall be initialized as empty.

Current validation rules shall immediately apply to the generated field.

For example:

* Required fields shall be marked as required.
* Data type validation shall follow the current Schema.

This behavior allows older Records to remain editable while conforming to the latest Schema definition.

---

### Validation

Before submission, the UI performs:

* Required field validation
* Basic data type validation

Business validation remains the responsibility of the Backend.

---

### User Actions

Administrators may:

* Create Record
* Edit Record
* Save
* Cancel

---

### API Integration

```http
POST /api/v1/master-data-records

PUT /api/v1/master-data-records/{id}
```


---

# 28. Delete Operations

Deletion requires explicit user confirmation.

---

## Delete Master Data Type

The Backend may reject deletion if the Type is referenced by:

* Templates
* Saved Inputs
* Other protected resources

Error example:

```text
MASTER_DATA_TYPE_IN_USE
```

---

## Delete Record

The Backend may reject deletion if the Record is protected by business rules.

Error example:

```text
MASTER_DATA_RECORD_IN_USE
```

---

## Confirmation Dialog

Before deletion, the UI shall display a confirmation dialog.

Deletion shall never occur immediately after clicking the Delete button.

---

# 29. Concurrent Editing

Master Data Schema supports optimistic locking.

The UI shall include the current Version when saving changes.

If the Backend returns:

```http
409 Conflict
```

the UI shall:

* Preserve local changes.
* Display a version conflict dialog.
* Prevent overwriting newer server data.
* Allow the administrator to reload the latest version.

---

# 30. Design Principles

The Master Data Management screens follow the principles below.

---

## Dynamic UI

The Record Editor shall be generated dynamically from the configured Master Data Schema.

The UI shall not hard-code individual fields.

---

## Metadata and Records Separation

Master Data Type configuration and Master Data Records are managed independently.

Updating the schema does not automatically modify existing records.

---

## Backend-Driven Validation

The UI performs only basic validation.

Business rules and schema validation are enforced by the Backend.

---

## Referential Integrity

The UI shall gracefully handle Backend responses indicating that a Type or Record cannot be deleted because it is currently in use.

The UI shall display the corresponding error without attempting to bypass the constraint.

---

## Optimistic Locking

Concurrent updates shall be protected using optimistic locking.

The UI shall never silently overwrite newer changes.

---

## Scalability

The screens shall support Master Data Types containing large numbers of Records.

The UI should provide:

* Pagination
* Search
* Fast navigation
* Consistent table layouts
