# Part 4 - XML Processing, Validation, Export History, Error Handling & Requirements

## 15. XML Processing

### XML Generation

The system shall:

1. Load Selected Template
2. Load User Input Data
3. Load Selected Master Data
4. Apply Master Data Mapping
5. Validate Template Rules
6. Generate XML Structure
7. Display XML Preview
8. Generate Downloadable XML File

---

### XML Generation Rules

The generated XML shall be determined by:

* Template Structure
* XML Mapping Rules
* User Input Data
* Selected Master Data
* Validation Rules
* Empty Value Rules
* Occurrence Rules

---

## 16. Template Configuration Rules

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

---

### Field Occurrence Rules

Each field or group shall define an occurrence rule.

| Rule         | Description                   |
| ------------ | ----------------------------- |
| ONE_OR_MORE  | Must appear at least once     |
| ZERO_OR_MORE | May appear zero or more times |
| ZERO_OR_ONE  | May appear zero or one time   |

Specification Mapping:

| Specification | Internal Rule |
| ------------- | ------------- |
| １回以上出現        | ONE_OR_MORE   |
| *             | ZERO_OR_MORE  |
| ?             | ZERO_OR_ONE   |

---

### Empty Value Rules

Each field shall define behavior when data is not provided.

| Rule               | Description                              |
| ------------------ | ---------------------------------------- |
| REQUIRED           | Value must be provided                   |
| OMIT_IF_EMPTY      | XML tag is not generated                 |
| EMPTY_TAG_IF_EMPTY | XML tag is generated with an empty value |
| ZERO_IF_EMPTY      | XML tag is generated with value 0        |

---

### Conditional Group Validation

Some XML groups are optional.

Users may choose whether to provide data for the group.

If the group is not used:

```xml
<GoalInfo No="1"></GoalInfo>
```

the XML is considered valid.

If the group is used, all required child fields defined by the template must be provided.

Example:

```xml
<GoalInfo No="1">
    <StateID>1</StateID>
    <StateName>前半</StateName>
    <Time>17</Time>
</GoalInfo>
```

Validation Rules:

* Group is not used → Child fields are not required.
* Group is used → All required child fields must pass validation.
* XML generation must fail if a required child field is missing.

---

### Repeatable Groups

The system shall support repeatable groups.

Examples:

* GoalInfo
* TeamInfo
* PlayerInfo
* PKInfo

A repeatable group may appear multiple times within the same XML document based on its occurrence rule.

Example:

```xml
<GoalInfo No="1"></GoalInfo>
<GoalInfo No="2"></GoalInfo>
```

---

## 17. XML Preview

### XML Preview

The system shall allow users to preview generated XML before downloading.

The preview content shall match the final downloadable XML.

Users shall be able to:

* Review XML
* Return to Editing
* Regenerate Preview

---

### XML Preview Validation

Before generating the final XML file, the system shall validate:

* Required Fields
* Occurrence Rules
* Conditional Group Rules
* Empty Value Rules
* Master Data Mapping Rules

Validation errors must be displayed before XML generation is completed.

---

### XML Preview Performance

The XML Preview screen shall display formatted XML content.

Recommended implementation:

* Syntax Highlighting
* Expand / Collapse XML Nodes
* Virtual Scrolling for Large XML Files

For large XML files:

* Preview shall render incrementally.
* XML generation and XML preview shall use separate processing steps.
* Preview rendering shall not block XML generation.

If preview rendering exceeds acceptable response time:

* A loading indicator may be displayed while preparing preview content.

---

## 18. Export History

### Export History

The system shall store every XML export operation.

Each export history record shall contain:

* Template Information
* Input Data Snapshot
* Master Data Snapshot
* File Name
* File Path
* File Size
* Export Timestamp

Users shall be able to:

* View Export History
* View Export Details
* Download Previously Generated XML Files

Generated XML files shall be stored in file storage rather than directly in the database.

---

### Export History Retention Policy

Default retention period:

```text
30 days
```

The system shall automatically remove expired records and files.

The retention policy applies to:

* Export History Records
* Generated XML Files

The retention period may become configurable in future releases.

---

## 19. Error Handling

### Error Response Format

The system shall return standardized error responses.

Example:

```json
{
  "code": "VAL_001",
  "message": "Required field is missing"
}
```

---

### AUTH_001

HTTP Status: 401 Unauthorized

Description:

User is not authenticated.

Examples:

* Missing Access Token
* Invalid Access Token
* Expired Access Token

---

### AUTHZ_001

HTTP Status: 403 Forbidden

Description:

User does not have permission to perform the requested action.

Examples:

* User attempts to modify a template
* User attempts to modify master data

---

### TPL_001

HTTP Status: 404 Not Found

Description:

Template not found.

Examples:

* Requested template does not exist
* Template has been deleted

---

### VAL_001

HTTP Status: 400 Bad Request

Description:

Required field is missing.

Example:

```json
{
  "field": "GameID",
  "message": "GameID is required"
}
```

---

### VAL_002

HTTP Status: 400 Bad Request

Description:

Conditional group validation failed.

Example:

```text
GoalInfo exists but required child field StateID is missing.
```

---

### FILE_001

HTTP Status: 500 Internal Server Error

Description:

Failed to save generated XML file.

Examples:

* Storage unavailable
* File write failed

---

### SYS_001

HTTP Status: 500 Internal Server Error

Description:

Unexpected system error.

Examples:

* Unhandled exception
* Internal processing failure

---

### Logging

The system shall log:

* Authentication Failures
* Authorization Failures
* Validation Failures
* XML Generation Failures
* File Storage Failures
* Unexpected Server Errors

Each log entry should contain:

* Timestamp
* User ID
* Error Code
* Error Message
* Request ID (optional)

---

## 20. Functional Requirements

### FR-01 User Registration

Users can create an account.

### FR-02 User Login

Users can authenticate and access the system.

### FR-03 Create Template

Administrators can create templates.

### FR-04 Manage Template Fields

Administrators can add, update, and remove template fields.

### FR-05 Manage Master Data

Administrators can create, update, and delete master data records.

### FR-06 Manage Master Data Type Mapping

Administrators can configure mappings between Master Data fields and XML fields.

### FR-07 Select Template

Users can select a template for XML generation.

### FR-08 View Template Guide

Users can view the Template Guide for the selected template.

### FR-09 Select Master Data

Users can select one or more Master Data records.

### FR-10 Enter Data

Users can enter values for template fields.

### FR-11 Save Input Data

The system shall save user input data.

### FR-12 Auto Fill Previous Data

The system shall automatically load the most recently saved data for the selected template.

### FR-13 Preview XML

The system shall validate and display XML preview before download.

### FR-14 Generate XML

The system shall generate XML using:

* Template Configuration
* User Input Data
* Selected Master Data
* XML Mapping Rules

### FR-15 Download XML

Users can download generated XML files.

### FR-16 Export History

The system shall record every XML export.

### FR-17 Conditional Group Validation

The system shall validate conditional groups according to template configuration.

### FR-18 XML Structure Validation

The system shall validate:

* Required Fields
* Occurrence Rules
* Empty Value Rules
* Group Validation Rules
* Master Data Mappings

before generating the final XML file.

### FR-19 Search Templates

Users shall be able to search templates by:

* Typing Template Name
* Selecting from Dropdown List

### FR-20 Manage Template Guide

Administrators shall be able to:

* Upload Guide Files
* Associate Guide Files with Templates
* Update Guide Files
* Remove Guide Files

### FR-21 Master Data Integrity Validation

The system shall detect deleted Master Data references when loading saved user data.

Users shall be prompted to select a replacement Master Data record before generating XML.

### FR-22 Export History Retention

The system shall automatically remove export history records and generated XML files that exceed the configured retention period.

Default retention period:

```text
30 days
```

---

## 21. Non-Functional Requirements

### Performance

XML generation should complete within 3 seconds under normal usage.

---

### Scalability

The architecture shall support adding new templates without modifying the XML generation engine.

---

### Storage

Generated XML files shall be stored in file storage.

Database records shall only store metadata and snapshots.

---

### Large XML Support

The system shall support large XML files.

XML content shall not be stored directly in the database.

Preview rendering shall support large XML documents without blocking XML generation.

---

### Security

Users shall not access data belonging to other users.

Role-based access control shall be enforced for all administrative functions.
