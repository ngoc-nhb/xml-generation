# 03-database-design.md

# 1. Overview

This document defines the physical database design for the XML Generator System.

The design supports:

* Template-based XML generation
* Master Data management
* Template Guide management
* User Draft management
* Export History management
* Template Pre-compilation
* Future CSV Import
* Future XML-to-Template conversion

Recommended Database:

```text
PostgreSQL
```

Recommended JSON Type:

```text
JSONB
```

---

# 2. Database Overview

```text
users

workspaces
workspace_members

templates
template_fields
template_mappings
template_guides

master_data_types
master_data_fields
master_data_records

template_master_data_mappings

saved_inputs

export_histories
```

> **Phase 7.1.0 (planned):** `workspaces` and `workspace_members` tables plus `workspace_id` on owned entities. See §4.11–4.12. Not yet migrated.

---

# 3. Pre-compilation Architecture

The system adopts a Pre-compilation strategy.

When an Admin creates or updates a Template:

```text
Template
    +
TemplateField
    +
TemplateMapping

        ↓

Compiled Schema

        ↓

templates.compiled_schema_json
```

The XML Generation Engine shall use:

```text
compiled_schema_json
```

instead of rebuilding:

```text
Template Tree
+
Mapping Rules
```

for every request.

Benefits:

* Faster XML generation
* Reduced database queries
* Reduced recursive processing
* Simpler runtime logic

---

# 4. Table Definitions

## 4.1 users

Stores system users.

| Column        | Type         | Constraint             |
| ------------- | ------------ | ---------------------- |
| id            | bigint       | PK                     |
| username      | varchar(100) | UNIQUE, NOT NULL       |
| password_hash | varchar(255) | NOT NULL               |
| is_admin      | boolean      | NOT NULL DEFAULT false |
| is_active     | boolean      | NOT NULL DEFAULT true  |
| created_at    | timestamp    | NOT NULL               |
| updated_at    | timestamp    | NOT NULL               |
| deleted_at    | timestamp    | NULL                   |

Indexes:

```text
UNIQUE(username)
INDEX(is_active)
```

---

## 4.2 templates

Stores XML Templates.

| Column               | Type         | Constraint       |
| -------------------- | ------------ | ---------------- |
| id                   | bigint       | PK               |
| code                 | varchar(100) | UNIQUE, NOT NULL |
| name                 | varchar(255) | NOT NULL         |
| description          | text         | NULL             |
| status               | varchar(20)  | NOT NULL         |
| compiled_schema_json | jsonb        | NULL             |
| created_by           | bigint       | FK -> users.id   |
| created_at           | timestamp    | NOT NULL         |
| updated_at           | timestamp    | NOT NULL         |
| deleted_at           | timestamp    | NULL             |

Status:

```text
ACTIVE
INACTIVE
```

Indexes:

```text
UNIQUE(code)

INDEX(status)
```

---

## 4.3 template_fields

Stores XML node definitions. `TemplateField` describes XML structure only; it does
not contain master data mapping references.

| Column                      | Type          | Constraint               |
| --------------------------- | ------------- | ------------------------ |
| id                          | bigint        | PK                       |
| template_id                 | bigint        | FK                       |
| parent_id                   | bigint        | FK -> template_fields.id |
| field_name                  | varchar(255)  | NOT NULL                 |
| display_name                | varchar(255)  | NULL                     |
| xml_name                    | varchar(255)  | NOT NULL                 |
| node_type                   | varchar(20)   | NOT NULL                 |
| value_type                  | varchar(50)   | NULL                     |
| source_type                 | varchar(20)   | NULL                     |
| static_value                | text          | NULL                     |
| default_value               | text          | NULL                     |
| occurrence_rule             | varchar(30)   | NULL                     |
| empty_handling              | varchar(30)   | NOT NULL                 |
| required_when_parent_exists | boolean       | NOT NULL                 |
| trigger_activation          | boolean       | NULL                     |
| display_order               | integer       | NOT NULL                 |
| xml_path                    | varchar(1000) | NULL                     |
| namespace                   | varchar(255)  | NULL                     |
| description                 | text          | NULL                     |
| created_at                  | timestamp     | NOT NULL                 |
| updated_at                  | timestamp     | NOT NULL                 |

Node Type:

```text
GROUP
ELEMENT
ATTRIBUTE
```

Value Type (value_type):

```text
STRING
INTEGER
LONG
DECIMAL
BOOLEAN
DATE
DATETIME
```

Occurrence Rule:

```text
ONE_OR_MORE
ZERO_OR_MORE
ZERO_OR_ONE
```

Empty Handling:

```text
REQUIRED
OMIT_IF_EMPTY
EMPTY_TAG_IF_EMPTY
ZERO_IF_EMPTY
```

Source Type:

```text
INPUT
MASTER_DATA
STATIC
```

`trigger_activation` (nullable boolean): when true, a resolved child value may
activate an optional parent GROUP. When null, defaults apply by `source_type`
(`INPUT → true`, `MASTER_DATA` / `STATIC → false`). See `04-template-schema`
Part 2 §13.

Indexes:

```text
INDEX(template_id)

INDEX(parent_id)

INDEX(template_id, display_order)
```

---

## 4.4 template_guides

Stores template guide documents.

| Column      | Type          | Constraint     |
| ----------- | ------------- | -------------- |
| id          | bigint        | PK             |
| template_id | bigint        | FK             |
| file_name   | varchar(255)  | NOT NULL       |
| file_path   | varchar(1000) | NOT NULL       |
| file_type   | varchar(20)   | NOT NULL       |
| uploaded_by | bigint        | FK -> users.id |
| created_at  | timestamp     | NOT NULL       |
| updated_at  | timestamp     | NOT NULL       |

Supported Types:

```text
PDF
DOCX
XLSX
```

Indexes:

```text
INDEX(template_id)
```

---

## 4.5 master_data_types

Stores Master Data categories.

| Column      | Type         | Constraint |
| ----------- | ------------ | ---------- |
| id          | bigint       | PK         |
| code        | varchar(100) | UNIQUE     |
| name        | varchar(255) | NOT NULL   |
| description | text         | NULL       |
| created_at  | timestamp    | NOT NULL   |
| updated_at  | timestamp    | NOT NULL   |
| deleted_at  | timestamp    | NULL       |

Indexes:

```text
UNIQUE(code)
```

---

## 4.6 master_data_fields

Stores schema definitions for Master Data Types.

| Column              | Type         | Constraint             |
| ------------------- | ------------ | ---------------------- |
| id                  | bigint       | PK                     |
| master_data_type_id | bigint       | FK                     |
| field_name          | varchar(255) | NOT NULL               |
| data_type           | varchar(50)  | NOT NULL               |
| is_required         | boolean      | NOT NULL DEFAULT false |
| display_order       | integer      | NOT NULL               |
| created_at          | timestamp    | NOT NULL               |
| updated_at          | timestamp    | NOT NULL               |

Data Type:

```text
STRING
INTEGER
LONG
DECIMAL
BOOLEAN
DATE
DATETIME
```

Indexes:

```text
INDEX(master_data_type_id)

UNIQUE(master_data_type_id, field_name)
```

---

## 4.7 master_data_records

Stores actual Master Data values.

| Column              | Type      | Constraint |
| ------------------- | --------- | ---------- |
| id                  | bigint    | PK         |
| master_data_type_id | bigint    | FK         |
| data_json           | jsonb     | NOT NULL   |
| created_at          | timestamp | NOT NULL   |
| updated_at          | timestamp | NOT NULL   |
| deleted_at          | timestamp | NULL       |

Example:

```json
{
  "game_kind_id": 2,
  "game_kind_name": "J1"
}
```

Indexes:

```text
INDEX(master_data_type_id)

GIN(data_json)
```

---

## 4.8 template_mappings

Defines mappings between `MasterDataField` and `TemplateField`.

`TemplateMapping` is separate from `TemplateField`. Deleting a `MasterDataField`
must not be blocked by a mapping.

| Column               | Type      | Constraint                          |
| -------------------- | --------- | ----------------------------------- |
| id                   | bigint    | PK                                  |
| template_id          | bigint    | FK -> templates.id                  |
| template_field_id    | bigint    | FK -> template_fields.id            |
| master_data_field_id | bigint    | FK -> master_data_fields.id, SET NULL |
| created_at           | timestamp | NOT NULL                            |
| updated_at           | timestamp | NOT NULL                            |

Example:

```text
MasterDataField: GAME_KIND.game_kind_id
        ↓
TemplateMapping
        ↓
TemplateField: GameKindID (xml_name)
```

Foreign keys:

```text
template_id          ON DELETE CASCADE
template_field_id    ON DELETE CASCADE
master_data_field_id ON DELETE SET NULL
```

Indexes:

```text
INDEX(template_id)

INDEX(master_data_field_id)

INDEX(template_field_id)
```

---

## 4.9 saved_inputs

Stores user draft data.

| Column                    | Type      | Constraint |
| ------------------------- | --------- | ---------- |
| id                        | bigint    | PK         |
| user_id                   | bigint    | FK         |
| template_id               | bigint    | FK         |
| input_data_json           | jsonb     | NOT NULL   |
| selected_master_data_json | jsonb     | NULL       |
| created_at                | timestamp | NOT NULL   |
| updated_at                | timestamp | NOT NULL   |
| expired_at                | timestamp | NULL       |

Constraints:

```text
UNIQUE(user_id, template_id)
```

Behavior:

```text
Existing Draft
    ↓
UPDATE

No Draft
    ↓
INSERT
```

Indexes:

```text
INDEX(user_id)

INDEX(template_id)

INDEX(expired_at)

UNIQUE(user_id, template_id)
```

---

## 4.10 export_histories

Stores immutable XML export history.

| Column                    | Type          | Constraint |
| ------------------------- | ------------- | ---------- |
| id                        | bigint        | PK         |
| user_id                   | bigint        | FK         |
| template_id               | bigint        | FK         |
| status                    | varchar(20)   | NOT NULL   |
| input_data_json           | jsonb         | NOT NULL   |
| selected_master_data_json | jsonb         | NULL       |
| file_name                 | varchar(255)  | NULL       |
| file_path                 | varchar(1000) | NULL       |
| file_size                 | bigint        | NULL       |
| error_message             | text          | NULL       |
| generated_at              | timestamp     | NOT NULL   |
| expired_at                | timestamp     | NULL       |

Status:

```text
PROCESSING
SUCCESS
FAILED
```

Purpose:

* Export History
* XML Download
* Audit Trail
* Snapshot Recovery

Indexes:

```text
INDEX(user_id)

INDEX(template_id)

INDEX(status)

INDEX(expired_at)
```

---

## 4.11 workspaces

Stores workspace (tenant) boundaries.

| Column      | Type         | Constraint             |
| ----------- | ------------ | ---------------------- |
| id          | bigint       | PK                     |
| code        | varchar(100) | UNIQUE, NOT NULL       |
| name        | varchar(255) | NOT NULL               |
| description | text         | NULL                   |
| status      | varchar(20)  | NOT NULL               |
| created_by  | bigint       | FK -> users.id         |
| created_at  | timestamp    | NOT NULL               |
| updated_at  | timestamp    | NOT NULL               |
| deleted_at  | timestamp    | NULL                   |

Status: `ACTIVE`, `INACTIVE`

Indexes:

```text
UNIQUE(code)
INDEX(status)
INDEX(created_by)
```

---

## 4.12 workspace_members

Links users to workspaces.

| Column       | Type        | Constraint                    |
| ------------ | ----------- | ----------------------------- |
| id           | bigint      | PK                            |
| workspace_id | bigint      | FK -> workspaces.id, CASCADE  |
| user_id      | bigint      | FK -> users.id, CASCADE       |
| role         | varchar(30) | NOT NULL                      |
| joined_at    | timestamp   | NOT NULL                      |
| created_at   | timestamp   | NOT NULL                      |
| updated_at   | timestamp   | NOT NULL                      |

Role: `WORKSPACE_ADMIN`, `WORKSPACE_USER`

Constraints:

```text
UNIQUE(workspace_id, user_id)
```

Indexes:

```text
INDEX(workspace_id)
INDEX(user_id)
```

---

## 4.13 workspace_id FK additions

The following tables gain `workspace_id BIGINT NOT NULL FK -> workspaces.id`:

| Table             | Unique constraint change                          |
| ----------------- | ------------------------------------------------- |
| templates         | `UNIQUE(workspace_id, code)` replaces `UNIQUE(code)` |
| master_data_types | `UNIQUE(workspace_id, code)` replaces global code unique |
| saved_inputs      | `UNIQUE(workspace_id, user_id, template_id)`      |
| export_histories  | (no unique change; add `INDEX(workspace_id)`)     |

Migration backfill: all existing rows → `workspace_id = 1` (Default Workspace).

Until Phase 7.1.3 wires workspace context into services, new rows receive `workspace_id = 1` via column `DEFAULT 1` on owned tables.

See [phase-7.1.0-workspace-architecture.md](../release/phase-7.1.0-workspace-architecture.md) §8.

---

# 5. Performance Strategy

## Database Optimization

Required:

```text
B-Tree Index on all Foreign Keys

GIN Index on JSONB columns
```

Examples:

```sql
CREATE INDEX idx_master_data_records_json
ON master_data_records
USING GIN (data_json);
```

---

## Runtime Optimization

The system uses:

```text
Pre-compilation
```

instead of:

```text
Recursive Template Build
on every Generate request
```

Flow:

```text
Admin Save Template
        ↓
Build Compiled Schema
        ↓
Store compiled_schema_json
        ↓
Generate XML
        ↓
Read compiled_schema_json
        ↓
Merge Data
        ↓
Output XML
```

Redis Cache is intentionally excluded from Phase 1.
