# Part 2. Domain Model Classes

---

# 10. Purpose

## Responsibility

Define the core Domain Model of the XML Generator System.

Domain Models represent business concepts independent of:

* HTTP
* Database implementation
* UI
* XML serialization

They are the foundation for the Service Layer and XML Generation Engine.

---

## Scope

This Part defines:

* Domain Entities
* Entity Responsibilities
* Relationships
* Aggregate Roots
* Entity Lifecycles

Business logic is intentionally excluded.

---

# 11. Domain Model Overview

The system consists of the following Domain Entities.

```text
User

Template
    └── TemplateField

MasterDataType
    └── MasterDataRecord

SavedInput

ExportHistory
```

These entities model the persistent business state of the application.

---

# 12. Aggregate Roots

The following entities are Aggregate Roots.

| Aggregate Root | Child Entities   |
| -------------- | ---------------- |
| User           | —                |
| Template       | TemplateField    |
| MasterDataType | MasterDataRecord |
| SavedInput     | —                |
| ExportHistory  | —                |

---

## Aggregate Rules

Child entities shall always belong to exactly one Aggregate Root.

Business invariants shall always be enforced by the owning Aggregate Root.

---

## Repository Rules

Repositories are primarily defined for Aggregate Roots.

However, child entities representing large collections may expose dedicated repositories for efficient querying and persistence.

Examples include:

* Pagination
* Search
* Bulk insertion
* Direct lookup

Such repositories shall not implement business rules.

Business validation shall always remain the responsibility of the Aggregate Root and its corresponding Service.

This approach allows efficient persistence while preserving aggregate consistency.


---

# 13. User

## Responsibility

Represent an authenticated system user.

The User owns all business data created within the system.

---

## Attributes

| Attribute    | Description           |
| ------------ | --------------------- |
| id           | Unique identifier     |
| username     | Login name            |
| passwordHash | Encrypted password    |
| createdAt    | Creation timestamp    |
| updatedAt    | Last update timestamp |

---

## Relationships

```text
User

├── Templates

├── MasterDataTypes

├── SavedInputs

└── ExportHistories
```

---

## Aggregate Root

Yes.

---

## Lifecycle

```text
Register

↓

Login

↓

Use System

↓

Logout
```

---

# 14. Template

## Responsibility

Represent an XML Template configuration.

A Template defines:

* XML structure
* Field definitions
* Mapping configuration
* File naming pattern

---

## Attributes

| Attribute       | Description                |
| --------------- | -------------------------- |
| id              | Unique identifier          |
| templateCode    | Business identifier        |
| templateName    | Display name               |
| fileNamePattern | XML filename pattern       |
| compiledSchema  | Compiled runtime schema    |
| version         | Optimistic locking version |
| status          | Active / Inactive          |
| createdAt       | Creation timestamp         |
| updatedAt       | Last update timestamp      |

---

## Relationships

```text
Template

├── TemplateFields

├── SavedInputs

└── ExportHistories
```

---

## Aggregate Root

Yes.

---

## Lifecycle

```text
Create

↓

Edit

↓

Compile

↓

Use

↓

Disable
```

---

# 15. TemplateField

## Responsibility

Represent one configurable field within a Template.

A TemplateField describes XML generation behavior but does not contain runtime values.

---

## Attributes

| Attribute    | Description                  |
| ------------ | ---------------------------- |
| id           | Unique identifier            |
| templateId   | Parent Template              |
| fieldCode    | Internal identifier          |
| displayName  | UI label                     |
| dataType     | Value type                   |
| sourceType   | INPUT / MASTER_DATA / STATIC |
| xmlPath      | XML mapping path             |
| displayOrder | UI & XML order               |
| required     | Required flag                |

---

## Relationships

```text
Template

↓

TemplateField
```

---

## Aggregate Root

No.

Owned by Template.

---

# 16. MasterDataType

## Responsibility

Represent one configurable Master Data definition.

Defines the schema shared by many Master Data Records.

---

## Attributes

| Attribute | Description                |
| --------- | -------------------------- |
| id        | Unique identifier          |
| typeCode  | Business identifier        |
| typeName  | Display name               |
| schema    | Dynamic field schema       |
| version   | Optimistic locking version |
| createdAt | Creation timestamp         |
| updatedAt | Last update timestamp      |

---

## Relationships

```text
MasterDataType

↓

MasterDataRecord
```

---

## Aggregate Root

Yes.

---

## Lifecycle

```text
Create

↓

Edit Schema

↓

Create Records

↓

Use

↓

Disable
```

---

# 17. MasterDataRecord

## Responsibility

Represent one reusable business record belonging to a Master Data Type.

---

## Attributes

| Attribute        | Description           |
| ---------------- | --------------------- |
| id               | Unique identifier     |
| masterDataTypeId | Parent Type           |
| values           | Dynamic field values  |
| createdAt        | Creation timestamp    |
| updatedAt        | Last update timestamp |

---

## Relationships

```text
MasterDataType

↓

MasterDataRecord
```

---

## Aggregate Root

No.

MasterDataRecord belongs to exactly one MasterDataType.

---

## Repository Access

Although MasterDataRecord is not an Aggregate Root, the implementation may provide a dedicated Repository to support:

* Pagination
* Search
* Direct retrieval
* Efficient persistence of large collections

This Repository is an infrastructure optimization only.

It shall not enforce business rules or aggregate invariants.

All business validation shall remain the responsibility of the owning MasterDataType and the corresponding Application Service.


---

# 18. SavedInput

## Responsibility

Represent the latest Draft saved by one User for one Template.

The SavedInput stores unfinished work and is independent of XML generation.

---

## Attributes

| Attribute          | Description          |
| ------------------ | -------------------- |
| id                 | Unique identifier    |
| userId             | Owner                |
| templateId         | Related Template     |
| inputData          | User input JSON      |
| selectedMasterData | Selected Master Data |
| createdAt          | Creation timestamp   |
| updatedAt          | Last saved timestamp |

---

## Relationships

```text
User

↓

SavedInput

↑

Template
```

---

## Aggregate Root

Yes.

---

## Lifecycle

```text
Create

↓

Update

↓

Load

↓

Delete
```

---

# 19. ExportHistory

## Responsibility

Represent one completed XML Export operation.

ExportHistory preserves audit information independently of the physical XML file.

---

## Attributes

| Attribute          | Description               |
| ------------------ | ------------------------- |
| id                 | Unique identifier         |
| userId             | Export owner              |
| templateId         | Source Template           |
| fileName           | Generated filename        |
| filePath           | Storage location          |
| fileSize           | XML file size             |
| inputSnapshot      | Saved input snapshot      |
| masterDataSnapshot | Master Data snapshot      |
| exportedAt         | Export timestamp          |
| expiredAt          | File expiration timestamp |

---

## Relationships

```text
User

↓

ExportHistory

↑

Template
```

---

## Aggregate Root

Yes.

---

## Lifecycle

```text
Export

↓

Download

↓

Expire File

↓

Keep Audit Record
```

The ExportHistory remains available after the XML file has expired.

---

# 20. Domain Design Rules

The Domain Model follows the principles below.

---

## Persistence Ignorance

Domain Entities shall not contain:

* HTTP concepts
* DTOs
* Controllers
* Database implementation details

---

## Aggregate Integrity

Child entities shall never exist independently of their Aggregate Root.

---

## Identity

Every Aggregate Root shall have a globally unique identifier.

---

## Ownership

All business data belongs to exactly one authenticated User.

Cross-user ownership is prohibited.

---

## XML Independence

Domain Entities describe business concepts only.

They shall not generate XML directly.

XML generation is delegated to the XML Engine defined in Part 5.

---

## No Infrastructure Dependencies

Domain Entities shall never depend on:

* Repository
* Service
* Controller
* XML Builder
* DTO
* Database Driver

The Domain Layer must remain independent from infrastructure concerns.
