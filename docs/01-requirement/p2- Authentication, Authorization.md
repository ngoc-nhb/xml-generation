# Part 2 - Authentication, Authorization & Template Management

## 7. Authentication & Authorization

### Authentication

The system shall support:

* User Registration
* User Login
* User Logout

Authenticated users shall be required to access protected resources.

Unauthenticated users shall not access:

* Templates
* Master Data
* Saved User Data
* Export History
* Generated XML Files

---

### Authorization

The system supports two roles:

* Admin
* User

---

### Admin Permissions

Administrators can:

* Create Templates
* Update Templates
* Delete Templates
* Configure Template Fields
* Configure XML Mapping Rules
* Configure Validation Rules
* Manage Template Guide Content
* Manage Master Data Types
* Manage Master Data Records
* Configure Master Data Type Mapping

---

### User Permissions

Users can:

* View Templates
* Search Templates
* View Template Guide
* Select Templates
* View Master Data
* Search Master Data
* Select Master Data
* Enter Data
* Save Data
* Reuse Previously Saved Data
* Preview XML
* Generate XML
* Download XML
* View Export History
* View Export Details

Users shall not modify:

* Templates
* Template Fields
* XML Mapping Rules
* Validation Rules
* Master Data Definitions
* Master Data Type Mappings

---

### Data Access Rules

The system shall separate data into two categories.

#### Shared Data

Shared data is managed by administrators and can be accessed by all authenticated users.

Shared data includes:

* Templates
* Template Guides
* Master Data
* Master Data Type Mapping
* XML Mapping Configuration

Example:

```text
LIVE_GAME Template
GAME_KIND Master Data
TEAM Master Data
STADIUM Master Data
```

#### User-Owned Data

User-owned data is private and accessible only by its owner.

User-owned data includes:

* Saved Input Data
* Export History
* Generated XML Files

Example:

```text
User A
 ├─ Saved Input Data
 ├─ Export History
 └─ Generated XML Files

User B
 ├─ Saved Input Data
 ├─ Export History
 └─ Generated XML Files
```

Users shall not access, modify, or delete data belonging to other users.

---

## 8. Template Management

### Template Overview

A template defines the XML structure, validation rules, field definitions, and XML mapping configuration required for XML generation.

The initial implementation includes one template:

* LIVE_GAME

The architecture shall support additional templates without modifying the XML generation engine.

---

### Template Management

Administrators shall be able to:

* Create Template
* Edit Template
* Delete Template
* Configure Template Fields
* Configure XML Mapping Rules
* Configure Validation Rules

Users shall be able to:

* View Templates
* Search Templates
* Select Templates for XML generation

---

### Template Structure

A template is composed of hierarchical XML nodes.

Each node must be classified as one of the following types:

| Type      | Description                                      |
| --------- | ------------------------------------------------ |
| Group     | A container node that may contain child nodes    |
| Element   | A standard XML element                           |
| Attribute | An XML attribute belonging to a Group or Element |

Example:

```xml
<GoalInfo No="1">
    <StateID>1</StateID>
</GoalInfo>
```

Mapping:

| XML Node | Type      |
| -------- | --------- |
| GoalInfo | Group     |
| No       | Attribute |
| StateID  | Element   |

---

### Template Ownership

Template structure is managed by administrators only.

Administrators can configure:

* XML Node Definitions
* XML Hierarchy
* Required Fields
* Optional Fields
* Occurrence Rules
* Empty Value Rules
* XML Mapping Rules
* Validation Rules

Users cannot:

* Add Fields
* Remove Fields
* Modify Field Definitions
* Modify Validation Rules
* Modify XML Mappings

Users can only provide data for the configured template fields.

---

### Template Search

The system shall provide template search capabilities.

Users shall be able to search templates using:

* Template Name Search
* Template Selection Dropdown

Template Name Search shall support:

* Partial Match Search
* Case-insensitive Search

Example:

Input:

```text
live
```

Result:

```text
LIVE_GAME
```

---

## 9. Template Guide

### Template Guide Overview

The system shall provide a Template Guide for each template.

Users shall be able to view the Template Guide while creating XML files.

The Template Guide shall contain:

* Template Description
* XML Structure Overview
* Field Name
* Field Description
* Data Type
* Allowed Values
* Required / Optional Rule
* Occurrence Rule
* Empty Value Rule
* Sample Value
* Additional Notes

---

### Template Guide Access

The system shall provide a Template Guide button on the XML generation screen.

When clicked, the system shall display the documentation for the currently selected template.

Users shall be able to access the guide without leaving the current page.

The guide may be displayed as:

* Side Panel
* Modal Dialog
* Dedicated Documentation Page

---

### Template Guide Management

Administrators shall be able to manage Template Guide content.

Supported guide formats:

* PDF
* DOCX
* XLSX

Administrators shall be able to:

* Upload Guide Files
* Associate Guide Files with Templates
* Update Guide Files
* Remove Guide Files

One guide may be linked to one or more templates.

---

### Template Guide Behavior

#### Guide Exists

The Template Guide button shall be enabled.

Clicking the button shall display the associated guide content.

#### Guide Does Not Exist

The Template Guide button shall be disabled.

Hover text:

```text
No guideline
```

If a guide is added later by an administrator, users shall refresh the page to retrieve the latest guide configuration.
