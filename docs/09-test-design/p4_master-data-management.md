# Part 4. Master Data Management

---

# MDT-001 Create Master Data Type

**Objective**

Verify that administrators can create new Master Data Types.

**Priority**

High

**Related Documents**

* 02-domain-model.md
* 03-database-design.md
* 06-api-design.md

| Scenario ID | Scenario                                  | Priority | Automation |
| ----------- | ----------------------------------------- | -------- | ---------- |
| MDT-001-01  | Create Master Data Type successfully      | High     | Yes        |
| MDT-001-02  | Create with duplicate Type Code           | High     | Yes        |
| MDT-001-03  | Create with missing required fields       | High     | Yes        |
| MDT-001-04  | Create with invalid field definitions     | High     | Yes        |
| MDT-001-05  | Create with duplicate field names         | High     | Yes        |
| MDT-001-06  | Unauthorized user attempts to create Type | High     | Yes        |

---

# MDT-002 Update Master Data Type

**Objective**

Verify that Master Data Type metadata and schema can be updated correctly.

**Priority**

High

**Related Documents**

* 03-database-design.md
* 06-api-design.md

| Scenario ID | Scenario                        | Priority | Automation |
| ----------- | ------------------------------- | -------- | ---------- |
| MDT-002-01  | Update metadata successfully    | High     | Yes        |
| MDT-002-02  | Update schema successfully      | High     | Yes        |
| MDT-002-03  | Update with duplicate Type Code | High     | Yes        |
| MDT-002-04  | Update with invalid schema      | High     | Yes        |
| MDT-002-05  | Update non-existing Type        | Medium   | Yes        |
| MDT-002-06  | Unauthorized update request     | High     | Yes        |

---

# MDT-003 Schema Evolution

**Objective**

Verify behavior when Master Data Type schema changes.

**Priority**

High

**Related Documents**

* 03-database-design.md
* 06-api-design.md

| Scenario ID | Scenario                                          | Priority | Automation |
| ----------- | ------------------------------------------------- | -------- | ---------- |
| MDT-003-01  | Add new optional field                            | High     | Yes        |
| MDT-003-02  | Add new required field                            | High     | Yes        |
| MDT-003-03  | Change field data type when no Records exist      | High     | Yes        |
| MDT-003-04  | Change field data type when Records already exist | High     | Yes        |
| MDT-003-05  | Delete unused field                               | Medium   | Yes        |
| MDT-003-06  | Delete field referenced by existing Records       | High     | Yes        |
| MDT-003-07  | Verify existing Records after schema update       | High     | Yes        |

---

# MDT-004 Delete Master Data Type

**Objective**

Verify deletion constraints for Master Data Types.

**Priority**

High

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                                   | Priority | Automation |
| ----------- | ------------------------------------------ | -------- | ---------- |
| MDT-004-01  | Delete unused Type successfully            | High     | Yes        |
| MDT-004-02  | Delete Type referenced by Template Mapping | High     | Yes        |
| MDT-004-03  | Delete Type containing Records             | High     | Yes        |
| MDT-004-04  | Delete non-existing Type                   | Medium   | Yes        |
| MDT-004-05  | Unauthorized delete request                | High     | Yes        |

---

# MDR-001 Create Master Data Record

**Objective**

Verify that administrators can create Master Data Records.

**Priority**

High

**Related Documents**

* 03-database-design.md
* 06-api-design.md

| Scenario ID | Scenario                            | Priority | Automation |
| ----------- | ----------------------------------- | -------- | ---------- |
| MDR-001-01  | Create Record successfully          | High     | Yes        |
| MDR-001-02  | Missing required field              | High     | Yes        |
| MDR-001-03  | Invalid data type                   | High     | Yes        |
| MDR-001-04  | Invalid format                      | High     | Yes        |
| MDR-001-05  | Invalid field length                | Medium   | Yes        |
| MDR-001-06  | Unknown field in payload            | Medium   | Yes        |
| MDR-001-07  | Create Record for non-existing Type | Medium   | Yes        |
| MDR-001-08  | Unauthorized create request         | High     | Yes        |

---

# MDR-002 Update Master Data Record

**Objective**

Verify update operations for Master Data Records.

**Priority**

High

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                       | Priority | Automation |
| ----------- | ------------------------------ | -------- | ---------- |
| MDR-002-01  | Update Record successfully     | High     | Yes        |
| MDR-002-02  | Update with invalid data type  | High     | Yes        |
| MDR-002-03  | Update with invalid format     | High     | Yes        |
| MDR-002-04  | Update required field to empty | High     | Yes        |
| MDR-002-05  | Update non-existing Record     | Medium   | Yes        |
| MDR-002-06  | Unauthorized update request    | High     | Yes        |

---

# MDR-003 Delete Master Data Record

**Objective**

Verify deletion rules for Master Data Records.

**Priority**

High

**Related Documents**

* 06-api-design.md

| Scenario ID | Scenario                                   | Priority | Automation |
| ----------- | ------------------------------------------ | -------- | ---------- |
| MDR-003-01  | Delete Record successfully                 | High     | Yes        |
| MDR-003-02  | Delete Record referenced by business rules | High     | Yes        |
| MDR-003-03  | Delete non-existing Record                 | Medium   | Yes        |
| MDR-003-04  | Unauthorized delete request                | High     | Yes        |

---

# MDR-004 Query Master Data Records

**Objective**

Verify retrieval of Master Data Records.

**Priority**

Medium

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                            | Priority | Automation |
| ----------- | ----------------------------------- | -------- | ---------- |
| MDR-004-01  | Retrieve Record list successfully   | Medium   | Yes        |
| MDR-004-02  | Retrieve Record detail successfully | Medium   | Yes        |
| MDR-004-03  | Pagination                          | Medium   | Yes        |
| MDR-004-04  | Sorting                             | Low      | Yes        |
| MDR-004-05  | Retrieve non-existing Record        | Medium   | Yes        |
| MDR-004-06  | Empty result set                    | Low      | Yes        |

---

# Master Data Management Coverage Summary

| Viewpoint                         |        Scenarios |
| --------------------------------- | ---------------: |
| MDT-001 Create Master Data Type   |                6 |
| MDT-002 Update Master Data Type   |                6 |
| MDT-003 Schema Evolution          |                7 |
| MDT-004 Delete Master Data Type   |                5 |
| MDR-001 Create Master Data Record |                8 |
| MDR-002 Update Master Data Record |                6 |
| MDR-003 Delete Master Data Record |                4 |
| MDR-004 Query Master Data Records |                6 |
| **Total**                         | **48 Scenarios** |
