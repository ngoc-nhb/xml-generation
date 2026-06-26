# Part 5. Saved Input Management

---

# SDI-001 Load Saved Draft

**Objective**

Verify that users can load previously saved Draft data correctly.

**Priority**

High

**Related Documents**

* 01-requirement.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                    | Priority | Automation |
| ----------- | ------------------------------------------- | -------- | ---------- |
| SDI-001-01  | Load existing Draft successfully            | High     | Yes        |
| SDI-001-02  | Load when no Draft exists                   | Medium   | Yes        |
| SDI-001-03  | Load Draft for non-existing Template        | Medium   | Yes        |
| SDI-001-04  | Unauthorized access to another user's Draft | High     | Yes        |
| SDI-001-05  | Load Draft after Template metadata update   | Medium   | Yes        |

---

# SDI-002 Save Draft

**Objective**

Verify that users can save Draft data independently from XML validation.

**Priority**

High

**Related Documents**

* 05-xml-generation-engine.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                          | Priority | Automation |
| ----------- | --------------------------------- | -------- | ---------- |
| SDI-002-01  | Save Draft successfully           | High     | Yes        |
| SDI-002-02  | Update existing Draft (Upsert)    | High     | Yes        |
| SDI-002-03  | Save incomplete input             | High     | Yes        |
| SDI-002-04  | Save with missing required fields | High     | Yes        |
| SDI-002-05  | Save with invalid business data   | High     | Yes        |
| SDI-002-06  | Save large Draft payload          | Medium   | Yes        |
| SDI-002-07  | Unauthorized save request         | High     | Yes        |

---

# SDI-003 Delete Draft

**Objective**

Verify Draft deletion behavior.

**Priority**

Medium

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                               | Priority | Automation |
| ----------- | -------------------------------------- | -------- | ---------- |
| SDI-003-01  | Delete existing Draft                  | Medium   | Yes        |
| SDI-003-02  | Delete non-existing Draft (Idempotent) | Medium   | Yes        |
| SDI-003-03  | Unauthorized delete request            | High     | Yes        |

---

# SDI-004 Schema Drift Handling

**Objective**

Verify compatibility between Saved Drafts and updated Template Schemas.

**Priority**

High

**Related Documents**

* 04-template-schema.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                    | Priority | Automation |
| ----------- | ------------------------------------------- | -------- | ---------- |
| SDI-004-01  | Ignore unknown fields removed from Template | High     | Yes        |
| SDI-004-02  | Initialize newly added optional fields      | High     | Yes        |
| SDI-004-03  | Initialize newly added required fields      | High     | Yes        |
| SDI-004-04  | Preserve valid existing field values        | High     | Yes        |
| SDI-004-05  | Load Draft after major Schema evolution     | High     | No         |

---

# SDI-005 Unsaved Changes

**Objective**

Verify that the application protects users from accidental data loss.

**Priority**

High

**Related Documents**

* 07-ui-screen-design.md

| Scenario ID | Scenario                                     | Priority | Automation |
| ----------- | -------------------------------------------- | -------- | ---------- |
| SDI-005-01  | Detect Unsaved Changes after editing         | High     | Yes        |
| SDI-005-02  | Leave page with Unsaved Changes              | High     | No         |
| SDI-005-03  | Switch Template with Unsaved Changes         | High     | No         |
| SDI-005-04  | Browser refresh with Unsaved Changes         | Medium   | No         |
| SDI-005-05  | Browser/tab close with Unsaved Changes       | Medium   | No         |
| SDI-005-06  | Continue editing after cancelling navigation | Medium   | No         |

---

# SDI-006 Draft Independence

**Objective**

Verify that XML Preview and Export operate only on the current request payload and never load Drafts automatically.

**Priority**

High

**Related Documents**

* 05-xml-generation-engine.md
* 06-api-design.md
* 08-class-diagram.md

| Scenario ID | Scenario                                                         | Priority | Automation |
| ----------- | ---------------------------------------------------------------- | -------- | ---------- |
| SDI-006-01  | Preview uses current request payload only                        | High     | Yes        |
| SDI-006-02  | Export uses current request payload only                         | High     | Yes        |
| SDI-006-03  | Preview ignores existing Saved Draft                             | High     | Yes        |
| SDI-006-04  | Export ignores existing Saved Draft                              | High     | Yes        |
| SDI-006-05  | Modified input is used even when Draft contains different values | High     | Yes        |

---

# Saved Input Management Coverage Summary

| Viewpoint                     |        Scenarios |
| ----------------------------- | ---------------: |
| SDI-001 Load Saved Draft      |                5 |
| SDI-002 Save Draft            |                7 |
| SDI-003 Delete Draft          |                3 |
| SDI-004 Schema Drift Handling |                5 |
| SDI-005 Unsaved Changes       |                6 |
| SDI-006 Draft Independence    |                5 |
| **Total**                     | **31 Scenarios** |
