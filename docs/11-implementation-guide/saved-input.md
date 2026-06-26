# 04. Saved Input Module

---

# 1. Purpose

Manage user Drafts for XML Generation.

This module is responsible for:

* Save Draft
* Load Draft
* Delete Draft
* Draft lifecycle management

---

# 2. Scope

Included

* Save Draft
* Load Draft
* Delete Draft

Excluded

* XML Preview
* XML Export
* Template management
* Master Data management

---

# 3. Components

| Component            | Responsibility         |
| -------------------- | ---------------------- |
| SavedInputController | Expose Draft APIs      |
| SavedInputService    | Manage Draft lifecycle |
| SavedInputRepository | Persist Drafts         |
| SavedInputMapper     | DTO conversion         |

---

# 4. Responsibilities

## SavedInputService

Responsible for:

* Save Draft
* Load Draft
* Delete Draft
* Retrieve Draft by Template

The service owns all business rules related to Draft management.

---

# 5. Dependencies

```text
SavedInputController
          │
          ▼
SavedInputService
          │
          ▼
SavedInputRepository
```

---

# 6. Public Interfaces

## SavedInputService

* save()
* findByTemplate()
* delete()

---

# 7. Domain Objects

* SavedInput

A SavedInput represents the latest Draft for a specific user and Template.

The uniqueness of a Draft is defined by:

* User
* Template

Only one Draft shall exist for each User–Template combination.

---

# 8. Repository Responsibilities

## SavedInputRepository

Responsible for:

* Save Draft
* Retrieve Draft
* Delete Draft

Repositories are responsible only for persistence.

---

# 9. Exceptions

* ValidationException
* NotFoundException
* BusinessException

---

# 10. Validation Rules

SavedInputService validates:

- Template exists
- User ownership
- Draft consistency
- Maximum payload size

Business validation for XML generation shall not be performed during Draft operations.

---

# 11. Implementation Notes

- Saving a Draft shall overwrite the previous Draft for the same User and Template.
- Draft operations shall be independent of XML Preview and Export.
- Loading a Draft shall return the stored Draft without modifying its content.
- The service shall enforce ownership before accessing a Draft.
- The authenticated user shall always be resolved from the Security Context.
- Request DTOs shall never contain or override the authenticated User identifier.
- Draft payload size shall be validated before persistence to protect system resources.

---

# 12. Unit Test Strategy

Minimum coverage:

SavedInputService

* Save Draft
* Update existing Draft
* Load Draft
* Delete Draft
* User ownership validation

SavedInputRepository

* CRUD operations

---

# 13. Implementation Checklist

* Create SavedInputController
* Create SavedInputService
* Create SavedInputRepository
* Create DTOs
* Create Mapper
* Implement save()
* Implement findByTemplate()
* Implement delete()
* Write unit tests

---

# 14. Phase 1 Decisions

| Topic               | Decision                        |
| ------------------- | ------------------------------- |
| Draft Strategy      | One Draft per User and Template |
| Save Operation      | Upsert                          |
| XML Validation      | Excluded                        |
| Business Validation | Excluded                        |
| Auto Save           | Excluded                        |
| Draft Versioning    | Excluded                        |
