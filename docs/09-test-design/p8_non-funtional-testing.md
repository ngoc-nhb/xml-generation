# Part 8. Non-functional Testing

---

# NFT-001 Performance

**Objective**

Verify that the system satisfies the defined performance requirements.

**Priority**

High

**Related Documents**

* 01-requirement.md
* 05-xml-generation-engine.md
* 06-api-design.md

| Scenario ID | Scenario                                             | Priority | Automation |
| ----------- | ---------------------------------------------------- | -------- | ---------- |
| NFT-001-01  | Preview XML with normal payload                      | High     | Yes        |
| NFT-001-02  | Export XML with normal payload                       | High     | Yes        |
| NFT-001-03  | Generate large XML document                          | High     | Yes        |
| NFT-001-04  | Load large Template Schema                           | Medium   | Yes        |
| NFT-001-05  | Load large Master Data set                           | Medium   | Yes        |
| NFT-001-06  | Verify Streaming XML generation reduces memory usage | High     | Yes        |
| NFT-001-07  | Verify Streaming Download reduces memory usage       | High     | Yes        |

---

# NFT-002 Boundary Testing

**Objective**

Verify system behavior at supported boundaries.

**Priority**

High

**Related Documents**

* 04-template-schema.md
* 05-xml-generation-engine.md

| Scenario ID | Scenario                           | Priority | Automation |
| ----------- | ---------------------------------- | -------- | ---------- |
| NFT-002-01  | Minimum required input             | High     | Yes        |
| NFT-002-02  | Maximum supported input size       | High     | Yes        |
| NFT-002-03  | Maximum Template depth             | Medium   | Yes        |
| NFT-002-04  | Maximum number of Template Fields  | Medium   | Yes        |
| NFT-002-05  | Maximum Repeatable Group size      | Medium   | Yes        |
| NFT-002-06  | Maximum Master Data Records        | Medium   | Yes        |
| NFT-002-07  | Maximum supported file name length | Low      | Yes        |

---

# NFT-003 Security

**Objective**

Verify compliance with security requirements.

**Priority**

Critical

**Related Documents**

* 01-requirement.md
* 06-api-design.md

| Scenario ID | Scenario                                           | Priority | Automation |
| ----------- | -------------------------------------------------- | -------- | ---------- |
| NFT-003-01  | Authentication required for protected APIs         | Critical | Yes        |
| NFT-003-02  | Authorization enforced for all protected resources | Critical | Yes        |
| NFT-003-03  | Prevent access to another user's resources (IDOR)  | Critical | Yes        |
| NFT-003-04  | Prevent User Enumeration during login              | High     | Yes        |
| NFT-003-05  | Password is never exposed in API responses         | High     | Yes        |
| NFT-003-06  | Protected APIs reject unauthenticated requests     | Critical | Yes        |

---

# NFT-004 Reliability

**Objective**

Verify that the system behaves correctly under failure conditions.

**Priority**

High

**Related Documents**

* 05-xml-generation-engine.md
* 06-api-design.md

| Scenario ID | Scenario                                                        | Priority | Automation |
| ----------- | --------------------------------------------------------------- | -------- | ---------- |
| NFT-004-01  | Recover from XML generation failure                             | High     | Yes        |
| NFT-004-02  | Export failure updates Export History correctly                 | High     | Yes        |
| NFT-004-03  | Validation failure never produces XML output                    | High     | Yes        |
| NFT-004-04  | Unexpected server exception returns standardized error response | High     | Yes        |
| NFT-004-05  | Network interruption during download                            | Medium   | No         |

---

# NFT-005 Data Integrity

**Objective**

Verify consistency and integrity of business data.

**Priority**

High

**Related Documents**

* 03-database-design.md
* 06-api-design.md

| Scenario ID | Scenario                                     | Priority | Automation |
| ----------- | -------------------------------------------- | -------- | ---------- |
| NFT-005-01  | Referential Integrity is preserved           | High     | Yes        |
| NFT-005-02  | Export Snapshot remains immutable            | High     | Yes        |
| NFT-005-03  | XML generation does not modify business data | High     | Yes        |
| NFT-005-04  | Preview never persists data                  | High     | Yes        |
| NFT-005-05  | Export persists only expected artifacts      | High     | Yes        |

---

# NFT-006 Architecture Compliance

**Objective**

Verify implementation complies with the approved architecture.

**Priority**

Medium

**Related Documents**

* 05-xml-generation-engine.md
* 08-class-diagram.md

| Scenario ID | Scenario                                      | Priority | Automation |
| ----------- | --------------------------------------------- | -------- | ---------- |
| NFT-006-01  | XML Engine operates without Repository access | Medium   | Yes        |
| NFT-006-02  | DTOs do not enter XML Engine                  | Medium   | Yes        |
| NFT-006-03  | Controllers do not contain business logic     | Medium   | Yes        |
| NFT-006-04  | Services own transaction boundaries           | Medium   | Yes        |
| NFT-006-05  | Circular dependencies are not introduced      | Low      | No         |

---

# Overall Coverage Summary

| Module                 | Viewpoints |         Scenarios |
| ---------------------- | ---------: | ----------------: |
| Authentication         |          4 |                27 |
| Template Management    |          7 |                46 |
| Master Data Management |          8 |                48 |
| Saved Input Management |          6 |                31 |
| XML Generation         |          8 |                59 |
| Export History         |          7 |                35 |
| Non-functional Testing |          6 |                33 |
| **Total**              |     **46** | **279 Scenarios** |

---

# Coverage Principles

The complete Test Design provides coverage for:

* Functional Requirements
* Business Rules
* XML Generation Rules
* API Contracts
* UI Behaviors
* Security Requirements
* Performance Requirements
* Architectural Constraints

Every approved requirement shall be traceable to at least one Test Viewpoint and one Test Scenario.

Detailed manual test cases are intentionally excluded from this document and shall be generated from the approved scenarios when implementation or testing begins.

---

# Phase 1 Test Design Decisions

| Topic                    | Decision                         |
| ------------------------ | -------------------------------- |
| Test Artifact            | Test Viewpoints + Test Scenarios |
| Detailed Test Cases      | Deferred                         |
| Requirement Traceability | Required                         |
| Automation Candidate     | Recorded                         |
| Regression Source        | Test Scenarios                   |
| Manual Test Steps        | Generated on demand              |
| Scenario-first Approach  | Required                         |
| Single Source of Truth   | 09-test-design.md                |
