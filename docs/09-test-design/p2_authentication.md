# Part 2. Authentication

---

# AUTH-001 User Login

**Objective**

Verify that users can authenticate securely and access the system according to the defined authentication rules.

**Priority**

High

**Related Documents**

* 01-requirement.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                                 | Priority | Automation |
| ----------- | -------------------------------------------------------- | -------- | ---------- |
| AUTH-001-01 | Login successfully with valid credentials                | High     | Yes        |
| AUTH-001-02 | Login with incorrect username                            | High     | Yes        |
| AUTH-001-03 | Login with incorrect password                            | High     | Yes        |
| AUTH-001-04 | Login with both username and password incorrect          | High     | Yes        |
| AUTH-001-05 | Verify generic error message (prevent user enumeration)  | High     | Yes        |
| AUTH-001-06 | Username is empty                                        | Medium   | Yes        |
| AUTH-001-07 | Password is empty                                        | Medium   | Yes        |
| AUTH-001-08 | Username and password are empty                          | Medium   | Yes        |
| AUTH-001-09 | Prevent multiple login requests by double-clicking Login | Medium   | Yes        |
| AUTH-001-10 | Display loading state during authentication              | Low      | Yes        |
| AUTH-001-11 | Unexpected server error during login                     | Medium   | Yes        |
| AUTH-001-12 | Network timeout during login                             | Medium   | Yes        |

---

# AUTH-002 User Logout

**Objective**

Verify that users can securely terminate an authenticated session.

**Priority**

High

**Related Documents**

* 01-requirement.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                                  | Priority | Automation |
| ----------- | --------------------------------------------------------- | -------- | ---------- |
| AUTH-002-01 | Logout successfully                                       | High     | Yes        |
| AUTH-002-02 | Logout when server returns an error (Fail-Safe Logout)    | High     | Yes        |
| AUTH-002-03 | Local authentication state is always cleared after logout | High     | Yes        |
| AUTH-002-04 | User is redirected to Login screen after logout           | Medium   | Yes        |

---

# AUTH-003 Session Expiration

**Objective**

Verify application behavior when the authentication session expires.

**Priority**

High

**Related Documents**

* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                                          | Priority | Automation |
| ----------- | ----------------------------------------------------------------- | -------- | ---------- |
| AUTH-003-01 | Session expires while browsing                                    | High     | Yes        |
| AUTH-003-02 | Session expires during API request                                | High     | Yes        |
| AUTH-003-03 | Redirect user to Login page after receiving Unauthorized response | High     | Yes        |
| AUTH-003-04 | Preserve unsaved work whenever possible before redirecting        | High     | No         |
| AUTH-003-05 | Restore workflow after successful re-authentication               | Medium   | No         |

---

# AUTH-004 Authorization

**Objective**

Verify that users can access only authorized resources.

**Priority**

High

**Related Documents**

* 01-requirement.md
* 06-api-design.md
* 07-ui-screen-design.md

| Scenario ID | Scenario                                                      | Priority | Automation |
| ----------- | ------------------------------------------------------------- | -------- | ---------- |
| AUTH-004-01 | Administrator accesses Administrator features                 | High     | Yes        |
| AUTH-004-02 | Standard User accesses User features                          | High     | Yes        |
| AUTH-004-03 | User attempts to access Administrator pages directly          | High     | Yes        |
| AUTH-004-04 | Hidden navigation cannot be accessed through URL manipulation | High     | Yes        |
| AUTH-004-05 | Backend rejects unauthorized API requests                     | High     | Yes        |
| AUTH-004-06 | Access Denied page is displayed correctly                     | Medium   | Yes        |

---

# Authentication Coverage Summary

| Viewpoint                   |        Scenarios |
| --------------------------- | ---------------: |
| AUTH-001 User Login         |               12 |
| AUTH-002 User Logout        |                4 |
| AUTH-003 Session Expiration |                5 |
| AUTH-004 Authorization      |                6 |
| **Total**                   | **27 Scenarios** |
