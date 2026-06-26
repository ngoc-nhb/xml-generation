# XML Generator System - Phase 1 Requirements & Scope

## 1. Overview

The XML Generator System allows users to generate XML files from configurable templates and user-provided data.

The system follows a template-driven architecture, enabling new XML templates to be added without modifying the XML generation engine.

During the initial implementation, only one template (`LIVE_GAME`) will be provided. However, the system architecture must support multiple templates in the future.

---

## 2. Objectives

### Primary Objectives

- Generate XML files from predefined templates.
- Allow users to manage reusable XML templates.
- Allow users to manage reusable master data.
- Allow users to preview XML before downloading.
- Allow users to reuse previously entered data.
- Maintain export history.
- Support multiple users with isolated data.

### Future Objectives

- Import Master Data from CSV files.
- Generate templates automatically from uploaded XML files.
- Support batch XML generation.
- Support additional XML template types.

---

## 3. Scope

### In Scope (Phase 1)

#### User Interface

The application supports Light Mode only.

Dark Mode is not included in Phase 1.

All screens, components, and previews shall be designed and tested for Light Mode only.

#### Authentication & Authorization

The system shall support:

- User Registration
- User Login
- User Logout

The system shall support two roles:

- Admin
- User

#### Template Management

The system shall support:

- Template Management
- Template Search
- Template Guide
- XML Mapping Configuration

#### Master Data Management

The system shall support:

- Master Data Management
- Master Data Type Management
- Master Data Mapping
- Master Data Search

#### User Data Management

The system shall support:

- Input Data Entry
- Input Data Persistence
- Auto Fill Previous Data

#### XML Processing

The system shall support:

- XML Validation
- XML Preview
- XML Generation
- XML Download

#### Export History

The system shall support:

- Export History
- Export Detail View
- Export File Download

#### Error Handling

The system shall provide:

- Standardized Error Responses
- Validation Errors
- Authorization Errors
- XML Generation Errors
- Storage Errors

---

### Out of Scope (Phase 1)

#### CSV Import

Importing Master Data from CSV files.

Planned for Phase 2.

---

#### XML Upload and Parsing

Uploading XML files and automatically generating templates.

Planned for Phase 3.

---

#### Batch XML Generation

Generating multiple XML files in a single operation.

Planned for a future phase.

---

#### Dark Mode

Dark Mode is not supported in Phase 1.

The application operates in Light Mode only.

---

## 4. Actors

### Admin

The administrator can:

- Login
- Manage Templates
- Manage Template Fields
- Configure XML Mappings
- Manage Template Guide Content
- Manage Master Data Types
- Manage Master Data Records
- Configure Master Data Type Mappings
- View Templates
- Search Templates
- View Template Guide
- Select Templates
- View Master Data
- Search Master Data
- Select Master Data
- Enter Data
- Save Data
- Reuse Previous Data
- Preview XML
- Generate XML
- Download XML
- View Export History
- View Export Details

---

### User

The user can:

- Register
- Login
- View Templates
- Search Templates
- View Template Guide
- Select Templates
- View Master Data
- Search Master Data
- Select Master Data
- Enter Data
- Save Data
- Reuse Previous Data
- Preview XML
- Generate XML
- Download XML
- View Export History
- View Export Details

---

## 5. Assumptions

- The initial implementation includes one template (`LIVE_GAME`).
- Additional templates will be added later.
- XML structure is defined by template configuration.
- Users work independently.
- Master Data is managed manually during Phase 1.
- Template Guide content is managed by administrators.
- Generated XML files are stored in file storage.

---

## 6. Future Expansion

### Phase 2 - Master Data Import

Flow:

```text
CSV File
    ↓
Import
    ↓
Database
    ↓
Master Data Selection
```

### Phase 3 - Template Generation from XML

Flow:

```text
XML File
    ↓
XML Parser
    ↓
Template Definition
    ↓
Database
```

The architecture should support future phases without requiring major changes to the XML generation engine, database schema, or core business logic.