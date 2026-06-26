# Part 3 - Master Data Management & User Data Management

## 10. Master Data Management

### Master Data Overview

Master Data contains reusable business values shared across multiple XML files.

Master Data is managed by administrators and can be used by all authenticated users during XML generation.

Examples:

* GAME_KIND
* TEAM
* PLAYER
* STADIUM
* COMPETITION
* SEASON

---

### Master Data Management

Administrators shall be able to:

* Create Master Data
* Edit Master Data
* Delete Master Data
* Configure Master Data Types
* Configure Master Data Type Mapping

Users shall be able to:

* View Master Data
* Search Master Data
* Select Master Data during XML generation

Users shall not modify:

* Master Data Definitions
* Master Data Records
* Master Data Type Configuration
* Master Data Type Mapping

---

### Master Data Search

The system shall provide Master Data search capabilities.

Users shall be able to:

* Search by keyword
* Filter by Master Data Type
* Select from dropdown list

This is especially important for large datasets such as:

* PLAYER
* TEAM
* STADIUM

---

## 11. Master Data Mapping

### Master Data Field Mapping

Administrators shall be able to configure which XML fields are linked to a Master Data definition.

The system shall use this configuration during XML generation.

Example:

Master Data:

```json
{
  "game_kind_id": 2,
  "game_kind_name": "J1"
}
```

Mapping Configuration:

```json
{
  "masterData": "GAME_KIND",
  "mappedFields": [
    "GameKindID",
    "GameKindName"
  ]
}
```

When a user selects the GAME_KIND master data record:

```json
{
  "game_kind_id": 2,
  "game_kind_name": "J1"
}
```

the system shall automatically populate:

```xml
<GameKindID>2</GameKindID>
<GameKindName>J1</GameKindName>
```

without requiring manual input.

---

### Master Data Type Mapping

XML field mapping shall be configured at the Master Data Type level rather than individual Master Data records.

Example:

```text
Master Data Type

GAME_KIND
 ├─ game_kind_id
 └─ game_kind_name

Mapping

game_kind_id   → GameKindID
game_kind_name → GameKindName
```

All records belonging to the same Master Data Type shall inherit the same XML field mapping configuration.

Example:

```json
{
  "masterDataType": "GAME_KIND",
  "fields": [
    {
      "sourceField": "game_kind_id",
      "targetXmlField": "GameKindID"
    },
    {
      "sourceField": "game_kind_name",
      "targetXmlField": "GameKindName"
    }
  ]
}
```

---

### Benefits

This approach provides:

* Centralized Mapping Management
* Consistent XML Generation
* Easier Maintenance
* Simplified Administration
* Support for Future Master Data Types

Examples of future Master Data Types:

* GAME_KIND
* SEASON
* STADIUM
* TEAM
* PLAYER
* COMPETITION

---

### Master Data Selection

Users shall be able to select one or more Master Data records during XML generation.

The system shall automatically populate all XML fields configured in the corresponding Master Data Type Mapping.

The XML field mapping shall be determined by the Master Data Type configuration and shall not be configured at the individual record level.

---

## 12. Deleted Master Data Handling

### Data Integrity Rules

If a Master Data record is deleted after it has been referenced by saved user input data:

* Existing Saved Input Data shall remain unchanged.
* Existing Export History shall remain unchanged.
* Existing Generated XML Files shall remain unchanged.
* The deleted Master Data shall no longer appear in selection lists.

---

### Loading Saved Data

When loading saved input data:

#### Referenced Master Data Exists

The system shall load data normally.

#### Referenced Master Data Does Not Exist

The system shall display a warning message.

Example:

```text
Selected Master Data no longer exists.
Please select a replacement value.
```

The user shall be required to select a replacement Master Data record before generating XML.

---

## 13. User Input Management

### User Input Data

The system shall support:

* Enter Data
* Edit Data
* Save Data

Input data shall be associated with:

* User
* Template
* Selected Master Data

---

### Data Persistence

The system shall save user input data for future reuse.

Saved data shall be isolated by user.

Users shall only access their own saved data.

---

## 14. Auto Fill Previous Data

### Auto Fill Overview

The system shall automatically load the most recently saved data for the selected template.

Users are not required to manually load previous data.

---

### Auto Fill Behavior

#### Existing User Data

If the user has previously used the template:

* Load the most recently saved data.

#### First Time Usage

If the user has never used the template:

* Load default values configured by the system.

Users may modify the loaded values before generating XML.

---

### Purpose

The Auto Fill feature aims to:

* Reduce repetitive data entry
* Improve productivity
* Reduce user errors
* Accelerate XML generation workflows

---

### Auto Fill Scope

The Auto Fill feature may include:

* Previously Entered Input Data
* Previously Selected Master Data
* Default Template Values

The exact values loaded shall depend on the template configuration and available saved user data.
