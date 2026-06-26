# 07. UI Screen Design

# Part 1. Screen Map & Navigation

---

# 1. Purpose

This document defines the user interface structure of the XML Generation System.

The objectives are:

- Define all application screens.
- Define screen navigation.
- Define user roles and access.
- Define the responsibilities of each screen.
- Provide a foundation for UI implementation before Class Diagram and Sequence Diagram.

---

# 2. User Roles

The system defines two user roles.

| Role | Description |
|------|-------------|
| Admin | System administrator responsible for configuring Templates and Master Data. |
| User | Business user responsible for generating XML files. |

---

## 3. Screen Map

The overall screen hierarchy is shown below.

```text
Login
│
└── Dashboard
    │
    ├── Template Management (Admin)
    │   ├── Template List
    │   ├── Create Template
    │   ├── Edit Template
    │   ├── Template Editor
    │   └── Compile Template
    │
    ├── Master Data (Admin)
    │   ├── Master Data Type List
    │   ├── Master Data Type Editor
    │   ├── Master Data Record List
    │   └── Master Data Record Editor
    │
    ├── XML Generator
    │   ├── Select Template
    │   ├── Input Form
    │   ├── Save Draft
    │   ├── Load Draft
    │   ├── XML Preview
    │   └── Export XML
    │
    └── Export History
        ├── History List
        ├── History Detail
        └── Download XML
```


---

# 4. Screen List

| ID | Screen | Role |
|----|--------|------|
| SCR-01 | Login | Admin, User |
| SCR-02 | Dashboard | Admin, User |
| SCR-03 | Template List | Admin |
| SCR-04 | Template Editor | Admin |
| SCR-05 | Master Data Type List | Admin |
| SCR-06 | Master Data Record List | Admin |
| SCR-07 | XML Generator | Admin, User |
| SCR-08 | Export History | Admin, User |

---

# 5. Navigation Flow

## Administrator

```text
Login

↓

Dashboard

├── Template Management

├── Master Data

├── XML Generator

└── Export History
```

---

## User

```text
Login

↓

Dashboard

├── XML Generator

└── Export History
```

Users cannot access:

- Template Management
- Master Data Management

---

## XML Generator

### Purpose

Generate XML using configured Templates.

### Responsibilities

* Select Template
* Enter Input Data
* Select Master Data
* Save Draft
* Load Draft
* Preview XML
* Export XML

Saved Draft functionality enables users to preserve unfinished work and resume editing later.

Loading a Draft restores the previously saved input for the selected Template.

Available to all authenticated users.


---

# 7. Access Matrix

| Screen | Admin | User |
|---------|:-----:|:----:|
| Login | ✅ | ✅ |
| Dashboard | ✅ | ✅ |
| Template Management | ✅ | ❌ |
| Master Data Management | ✅ | ❌ |
| XML Generator | ✅ | ✅ |
| Export History | ✅ | ✅ |

The UI shall hide navigation entries that are not accessible to the current user.

Backend authorization shall still be enforced regardless of UI visibility.

---

# 8. Navigation Rules

After successful login:

- Administrators shall be redirected to the Dashboard with access to all available menus.
- Users shall be redirected to the Dashboard with access only to permitted features.

Unauthorized navigation attempts shall result in:

- Redirect to an Access Denied page, or
- Redirect back to the Dashboard.

The UI shall not rely solely on menu visibility for security.

All protected operations shall also be validated by the Backend.

---

## 9. General Navigation Principles

The application shall maintain a consistent navigation structure across all screens.

Navigation shall follow these principles:

* Users should always know their current location.
* Primary navigation shall remain visible after login.
* Breadcrumb navigation should be displayed on management screens.
* Back navigation should preserve the current UI state whenever possible.
* Page transitions shall not discard unsaved user input without explicit confirmation.

---

### UI State Preservation

The UI may preserve temporary user input during navigation using client-side state management.

This behavior is intended only for short-lived navigation scenarios, such as:

* Browser Back
* Browser Forward
* Navigation between application screens

Client-side state preservation is not guaranteed across:

* Browser Refresh
* Browser Restart
* Device Change

---

### Saved Draft

Persistent recovery of user input is provided exclusively through the **Save Draft** feature.

Only data explicitly saved through the Saved Input APIs is guaranteed to be recoverable after page refreshes or future user sessions.

The UI shall not assume temporary client-side state is a substitute for Saved Draft.


---

# 10. Design Principles

The UI design follows the principles below.

## Role-Based Navigation

Users shall only see features they are authorized to use.

---

## Separation of Responsibilities

Each screen shall have a single primary responsibility.

Examples:

- Template Management
- Master Data Management
- XML Generation
- Export History

The UI shall avoid combining unrelated functions into a single screen.

---

## Consistency

All screens shall follow consistent layouts, terminology, navigation patterns, button placement, and interaction behavior.

---

## Simplicity

Frequently used operations should require the minimum number of user actions.

Complex configuration workflows shall be isolated within dedicated management screens.

---

## Backend-Driven Behavior

The UI shall interact exclusively through the REST APIs defined in:

```
06-api-design.md
```

The UI shall not implement business rules independently.

Business validation shall always be performed by the Backend.