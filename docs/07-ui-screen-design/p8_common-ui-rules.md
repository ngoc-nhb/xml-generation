# Part 8. Common UI Rules

---

# 58. Purpose

This section defines common user interface rules shared across all application screens.

These rules ensure:

* Consistent user experience
* Predictable behavior
* Unified interaction patterns
* Reduced implementation inconsistencies

Unless otherwise specified, these rules apply to every screen in the application.

---

# 59. Page Layout

All authenticated screens shall follow a consistent application layout.

```text
+------------------------------------------------------------------+

Header

------------------------------------------------------------

Sidebar

Main Content

------------------------------------------------------------

Footer (Optional)

+------------------------------------------------------------------+
```

---

## Header

The Header should display:

* Application Name
* Current User
* Logout Button

---

## Sidebar

The Sidebar provides primary navigation.

Menu visibility shall depend on the authenticated user's role.

---

## Main Content

The Main Content area displays the active screen.

Only one primary screen shall be active at a time.

---

# 60. Form Design

All forms shall follow a consistent layout.

---

## Labels

Each input field shall have a visible label.

Required fields shall be clearly indicated.

---

## Validation Messages

Validation messages should appear directly below the corresponding input field whenever possible.

Example:

```text
Game Date

[____________]

This field is required.
```

---

## Button Placement

Primary actions should appear on the right.

Examples:

```text
Cancel      Save
```

or

```text
Preview      Export
```

---

## Read-Only Fields

Read-only fields shall be visually distinguishable from editable fields.

---

# 61. Dialogs

Confirmation dialogs shall be used before irreversible operations.

Examples include:

* Delete
* Leave page with Unsaved Changes
* Switch Template with Unsaved Changes

---

## Confirmation Dialog

Example:

```text
Are you sure you want to continue?

[ Cancel ]     [ Confirm ]
```

---

## Modal Behavior

Dialogs shall:

* Block background interaction.
* Support keyboard navigation.
* Close only after explicit user action.

---

# 62. Notifications

The UI shall provide feedback after user actions.

---

## Success

Example:

```text
Template saved successfully.
```

---

## Warning

Example:

```text
The selected file has expired.
```

---

## Error

Example:

```text
Unable to complete the requested operation.
```

---

## Information

Example:

```text
Loading...
```

---

Notifications should be concise and easily understandable.

Localized messages should be generated using Backend Error Codes.

---

# 63. Loading Indicators

The UI shall indicate background processing.

Examples:

* Page Loading
* Table Loading
* Saving
* Compiling
* Previewing
* Exporting

---

## Processing State

While processing:

* Disable duplicate actions.
* Display a loading indicator.
* Prevent repeated submissions.

---

## Long Operations

For operations that may require noticeable processing time, the UI should provide continuous visual feedback until completion.

---

# 64. Empty States

Empty states shall be displayed when no data is available.

Examples:

```text
No Templates found.

No Master Data found.

No Export History found.
```

Empty states should explain the situation and guide users toward the next appropriate action whenever applicable.

---

# 65. Error Pages

The application should provide dedicated error pages.

Examples include:

* Access Denied
* Page Not Found
* Unexpected Error

---

## Access Denied

Displayed when a user lacks permission.

---

## Not Found

Displayed when the requested resource does not exist.

---

## Unexpected Error

Displayed when an unexpected system error occurs.

Sensitive implementation details shall never be exposed to users.

---

# 66. Pagination

List screens should support pagination when displaying large datasets.

Pagination shall follow the API specification defined in:

```text
06-api-design.md
```

---

## Pagination Controls

The UI should provide:

* Previous
* Next
* Current Page
* Page Size

Example:

```text
< Previous

1

2

3

Next >
```

---

# 67. Accessibility

The application should follow common accessibility practices.

Examples include:

* Keyboard navigation
* Visible focus indicators
* Accessible labels
* Sufficient color contrast

Accessibility improvements beyond these basic practices may be introduced in future phases.

---

# 68. Responsive Design

Phase 1 supports:

* Desktop
* Laptop
* Tablet

Mobile phone optimization is outside the scope of Phase 1.

The layout should adapt gracefully to supported screen sizes.

---

# 69. Design Principles

The UI follows the principles below.

---

## Consistency

All screens shall use consistent:

* Layout
* Terminology
* Buttons
* Icons
* Navigation
* Validation behavior

---

## Predictability

Users should always understand:

* What is happening.
* Why it happened.
* What action is expected next.

---

## Backend-Driven Business Logic

Business rules belong to the Backend.

The UI is responsible only for:

* Rendering
* User interaction
* Basic client-side validation

---

## Data Preservation

The UI shall prioritize protecting user-entered data.

Whenever possible:

* Warn before discarding data.
* Preserve temporary UI state.
* Encourage explicit Save Draft operations.

---

## Progressive Feedback

Every significant user action should produce immediate visual feedback.

Examples include:

* Loading indicators
* Success notifications
* Validation messages
* Error notifications

---

## Simplicity

The UI should minimize unnecessary user actions.

Frequently used operations should be completed with as few interactions as practical.

---

## Separation of Responsibilities

Each screen shall focus on a single primary responsibility.

The application shall avoid combining unrelated business functions into a single interface.

---

# 70. Final UI Decisions

The following decisions are fixed for Phase 1.

| Topic                           | Decision      |
| ------------------------------- | ------------- |
| Dynamic Forms                   | Supported     |
| Dynamic Master Data Forms       | Supported     |
| Save Draft                      | Manual only   |
| Auto Save                       | Not supported |
| XML Preview                     | Supported     |
| XML Export                      | Supported     |
| Export History                  | Read-only     |
| Mobile Optimization             | Not supported |
| Dark Mode                       | Not supported |
| Multi-language UI               | Future Phase  |
| Drag & Drop XML Tree            | Supported     |
| Optimistic Locking              | Supported     |
| Streaming Download              | Supported     |
| Route Guard for Unsaved Changes | Supported     |

These decisions define the baseline user experience for the Phase 1 implementation.
