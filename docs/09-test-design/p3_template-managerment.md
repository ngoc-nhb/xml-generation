# Part 3. Template Management

---

# TMP-001 Create Template

**Objective**

Verify that administrators can create new XML Templates.

**Priority**

High

**Related Documents**

* 01-requirement.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                       | Priority | Automation |
| ----------- | ---------------------------------------------- | -------- | ---------- |
| TMP-001-01  | Create Template successfully                   | High     | Yes        |
| TMP-001-02  | Create Template with duplicate Template Code   | High     | Yes        |
| TMP-001-03  | Create Template with missing Template Code     | High     | Yes        |
| TMP-001-04  | Create Template with missing Template Name     | High     | Yes        |
| TMP-001-05  | Create Template with invalid File Name Pattern | High     | Yes        |
| TMP-001-06  | Create Template with optional fields omitted   | Medium   | Yes        |
| TMP-001-07  | Unauthorized user attempts to create Template  | High     | Yes        |

---

# TMP-002 Update Template Metadata

**Objective**

Verify that Template metadata can be updated correctly.

**Priority**

High

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                              | Priority | Automation |
| ----------- | ------------------------------------- | -------- | ---------- |
| TMP-002-01  | Update Template Name successfully     | High     | Yes        |
| TMP-002-02  | Update File Name Pattern successfully | High     | Yes        |
| TMP-002-03  | Update with duplicate Template Code   | High     | Yes        |
| TMP-002-04  | Update with invalid metadata          | Medium   | Yes        |
| TMP-002-05  | Update non-existing Template          | Medium   | Yes        |
| TMP-002-06  | Unauthorized update request           | High     | Yes        |

---

# TMP-003 Edit Template Schema

**Objective**

Verify that administrators can edit Template Schema safely.

**Priority**

High

**Related Documents**

* 04-template-schema.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                              | Priority | Automation |
| ----------- | ------------------------------------- | -------- | ---------- |
| TMP-003-01  | Save Schema successfully              | High     | Yes        |
| TMP-003-02  | Save incomplete Draft Schema          | High     | Yes        |
| TMP-003-03  | Add new XML Element                   | High     | Yes        |
| TMP-003-04  | Add new XML Attribute                 | High     | Yes        |
| TMP-003-05  | Update existing Field                 | High     | Yes        |
| TMP-003-06  | Delete editable Field                 | High     | Yes        |
| TMP-003-07  | Attempt to delete Root Node           | High     | Yes        |
| TMP-003-08  | Attempt to create multiple Root Nodes | High     | Yes        |
| TMP-003-09  | Save large Template Schema            | Medium   | Yes        |
| TMP-003-10  | Unauthorized schema update            | High     | Yes        |

---

# TMP-004 Compile Template

**Objective**

Verify Template compilation behavior.

**Priority**

High

**Related Documents**

* 04-template-schema.md
* 05-xml-generation-engine.md
* 06-api-design.md

| Scenario ID | Scenario                          | Priority | Automation |
| ----------- | --------------------------------- | -------- | ---------- |
| TMP-004-01  | Compile successfully              | High     | Yes        |
| TMP-004-02  | Compile invalid Schema            | High     | Yes        |
| TMP-004-03  | Compile incomplete Draft          | High     | Yes        |
| TMP-004-04  | Recompile existing Template       | Medium   | Yes        |
| TMP-004-05  | Compile after Schema modification | Medium   | Yes        |
| TMP-004-06  | Compile non-existing Template     | Medium   | Yes        |
| TMP-004-07  | Unauthorized compile request      | High     | Yes        |

---

# TMP-005 Concurrent Editing

**Objective**

Verify optimistic locking and concurrent editing protection.

**Priority**

High

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md
* 08-class-diagram.md

| Scenario ID | Scenario                                               | Priority | Automation |
| ----------- | ------------------------------------------------------ | -------- | ---------- |
| TMP-005-01  | Update Schema with latest version                      | High     | Yes        |
| TMP-005-02  | Update Schema with outdated version                    | High     | Yes        |
| TMP-005-03  | Two administrators edit the same Template concurrently | High     | No         |
| TMP-005-04  | Display conflict message after version mismatch        | Medium   | No         |
| TMP-005-05  | Preserve local draft after conflict                    | Medium   | No         |

---

# TMP-006 Delete Template

**Objective**

Verify Template deletion rules.

**Priority**

High

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                                       | Priority | Automation |
| ----------- | ---------------------------------------------- | -------- | ---------- |
| TMP-006-01  | Delete unused Template successfully            | High     | Yes        |
| TMP-006-02  | Delete Template referenced by Saved Inputs     | High     | Yes        |
| TMP-006-03  | Delete Template referenced by Export Histories | High     | Yes        |
| TMP-006-04  | Delete non-existing Template                   | Medium   | Yes        |
| TMP-006-05  | Unauthorized delete request                    | High     | Yes        |

---

# TMP-007 Template List & Detail

**Objective**

Verify Template retrieval and browsing.

**Priority**

Medium

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                               | Priority | Automation |
| ----------- | -------------------------------------- | -------- | ---------- |
| TMP-007-01  | Retrieve Template list                 | Medium   | Yes        |
| TMP-007-02  | Retrieve Template detail               | Medium   | Yes        |
| TMP-007-03  | Retrieve Template with complete Schema | High     | Yes        |
| TMP-007-04  | Retrieve non-existing Template         | Medium   | Yes        |
| TMP-007-05  | Verify pagination                      | Medium   | Yes        |
| TMP-007-06  | Verify sorting                         | Low      | Yes        |

---

# Template Management Coverage Summary

| Viewpoint                        |        Scenarios |
| -------------------------------- | ---------------: |
| TMP-001 Create Template          |                7 |
| TMP-002 Update Template Metadata |                6 |
| TMP-003 Edit Template Schema     |               10 |
| TMP-004 Compile Template         |                7 |
| TMP-005 Concurrent Editing       |                5 |
| TMP-006 Delete Template          |                5 |
| TMP-007 Template List & Detail   |                6 |
| **Total**                        | **46 Scenarios** |
