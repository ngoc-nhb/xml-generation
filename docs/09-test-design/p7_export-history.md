# Part 7. Export History

---

# EXP-001 Export History List

**Objective**

Verify that users can retrieve Export History records correctly.

**Priority**

Medium

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                             | Priority | Automation |
| ----------- | ---------------------------------------------------- | -------- | ---------- |
| EXP-001-01  | Retrieve Export History list successfully            | Medium   | Yes        |
| EXP-001-02  | Retrieve empty Export History list                   | Low      | Yes        |
| EXP-001-03  | Verify pagination                                    | Medium   | Yes        |
| EXP-001-04  | Verify sorting                                       | Low      | Yes        |
| EXP-001-05  | Retrieve only current user's Export History          | High     | Yes        |
| EXP-001-06  | Unauthorized access to another user's Export History | High     | Yes        |

---

# EXP-002 Export History Detail

**Objective**

Verify retrieval of Export History details.

**Priority**

Medium

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                                    | Priority | Automation |
| ----------- | ------------------------------------------- | -------- | ---------- |
| EXP-002-01  | Retrieve Export History detail successfully | Medium   | Yes        |
| EXP-002-02  | Retrieve non-existing Export History        | Medium   | Yes        |
| EXP-002-03  | Access another user's Export History        | High     | Yes        |
| EXP-002-04  | Verify exported file metadata               | Medium   | Yes        |
| EXP-002-05  | Verify stored snapshot data                 | High     | Yes        |

---

# EXP-003 Download XML File

**Objective**

Verify XML download behavior.

**Priority**

Critical

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                    | Priority | Automation |
| ----------- | ------------------------------------------- | -------- | ---------- |
| EXP-003-01  | Download XML successfully                   | Critical | Yes        |
| EXP-003-02  | Verify Content-Disposition header           | High     | Yes        |
| EXP-003-03  | Verify original generated file name         | High     | Yes        |
| EXP-003-04  | Download large XML file using streaming     | Critical | Yes        |
| EXP-003-05  | Verify XML content integrity after download | Critical | Yes        |
| EXP-003-06  | Download non-existing file                  | High     | Yes        |

---

# EXP-004 Expired File Handling

**Objective**

Verify behavior after XML files have expired.

**Priority**

High

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                                       | Priority | Automation |
| ----------- | -------------------------------------------------------------- | -------- | ---------- |
| EXP-004-01  | Download expired file                                          | High     | Yes        |
| EXP-004-02  | Display expired file message                                   | High     | Yes        |
| EXP-004-03  | Disable Download button for expired files                      | Medium   | No         |
| EXP-004-04  | Verify Export History remains accessible after file expiration | High     | Yes        |
| EXP-004-05  | File removed from Storage after retention period               | High     | Yes        |

---

# EXP-005 Immutable History

**Objective**

Verify that Export History records are immutable.

**Priority**

High

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                          | Priority | Automation |
| ----------- | --------------------------------- | -------- | ---------- |
| EXP-005-01  | Export History cannot be modified | High     | Yes        |
| EXP-005-02  | Export History cannot be deleted  | High     | Yes        |
| EXP-005-03  | Verify no Update API exists       | Medium   | Yes        |
| EXP-005-04  | Verify no Delete API exists       | Medium   | Yes        |

---

# EXP-006 Export Snapshot

**Objective**

Verify snapshot preservation at export time.

**Priority**

High

**Related Documents**

* 05-xml-generation-engine.md
* 06-api-design.md

| Scenario ID | Scenario                                                  | Priority | Automation |
| ----------- | --------------------------------------------------------- | -------- | ---------- |
| EXP-006-01  | Snapshot stores Template version used during Export       | High     | Yes        |
| EXP-006-02  | Snapshot preserves input values used during Export        | High     | Yes        |
| EXP-006-03  | Template changes do not affect previous Export History    | High     | Yes        |
| EXP-006-04  | Master Data changes do not affect previous Export History | High     | Yes        |

---

# EXP-007 Export Status Lifecycle

**Objective**

Verify Export History status transitions throughout the Export process.

**Priority**

High

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                                                                  | Priority | Automation |
| ----------- | ------------------------------------------------------------------------- | -------- | ---------- |
| EXP-007-01  | Initial status is PROCESSING                                              | High     | Yes        |
| EXP-007-02  | Status changes to SUCCESS after Export completes                          | High     | Yes        |
| EXP-007-03  | Status changes to FAILED when Export fails                                | High     | Yes        |
| EXP-007-04  | Verify failure reason is recorded                                         | Medium   | Yes        |
| EXP-007-05  | Verify stuck PROCESSING records are handled according to retention policy | Medium   | No         |

---

# Export History Coverage Summary

| Viewpoint                       |        Scenarios |
| ------------------------------- | ---------------: |
| EXP-001 Export History List     |                6 |
| EXP-002 Export History Detail   |                5 |
| EXP-003 Download XML File       |                6 |
| EXP-004 Expired File Handling   |                5 |
| EXP-005 Immutable History       |                4 |
| EXP-006 Export Snapshot         |                4 |
| EXP-007 Export Status Lifecycle |                5 |
| **Total**                       | **35 Scenarios** |
