# Part 2. Authentication APIs

---

## 13. Overview

The Authentication APIs allow users to access the XML Generator System.

Authentication is intentionally designed to be simple for the MVP.

Supported features:

* Login
* Logout

Not supported:

* Email Login
* OAuth
* Refresh Token
* Password Reset
* Remember Me
* Multi-Factor Authentication (MFA)

---

## 14. Authentication Flow

The Authentication APIs verify the identity of a user before allowing access to protected resources.

The authentication flow is shown below.

```text
User

↓

POST /api/v1/auth/login

↓

Validate Username & Password

↓

Authentication Success

↓

Authentication Context Established

↓

Access Protected APIs
```

---

### Authentication Context

After successful authentication, subsequent requests shall be associated with the authenticated user.

The mechanism used to propagate the authentication context (for example, session cookies, bearer tokens, or other infrastructure-specific approaches) is outside the scope of this document.

This document defines the API behavior only and does not mandate a specific authentication transport mechanism.


---

## 15. POST /api/v1/auth/login

Authenticates a user using username and password.

### Request

```http
POST /api/v1/auth/login
```

---

### Request Body

```json
{
    "username": "john",
    "password": "password123"
}
```

---

### Validation

The API shall validate:

* username is required
* password is required

If validation fails:

```http
400 Bad Request
```

shall be returned.

---

### Authentication

The Backend shall:

1. Find the user by username.

2. Compare the provided password with:

```text
password_hash
```

stored in the database.

3. Return the authentication result.

---

### Success Response

```http
200 OK
```

```json
{
    "success": true,
    "data": {
        "userId": 1,
        "username": "john",
        "isAdmin": false
    }
}
```

---

### Invalid Credentials

```http
401 Unauthorized
```

```json
{
    "success": false,
    "errors": [
        {
            "code": "INVALID_CREDENTIALS"
        }
    ]
}
```

---

### Notes

The API shall never indicate whether:

* username exists
* password is incorrect

Both cases shall return:

```text
INVALID_CREDENTIALS
```

to prevent user enumeration.

---

## 16. POST /api/v1/auth/logout

Logs out the current user.

### Request

```http
POST /api/v1/auth/logout
```

---

### Request Body

None.

---

### Processing

For the MVP:

* No server-side session exists.
* No refresh token exists.
* No token blacklist exists.

The Backend simply acknowledges the logout request.

The Frontend is responsible for:

* Clearing local authentication state
* Redirecting the user to the Login page

---

### Success Response

```http
200 OK
```

```json
{
    "success": true
}
```

---

## 17. Authorization Rules

Authentication determines the current user.

Authorization determines what the user is allowed to access.

---

### Admin APIs

Only administrators may access:

```text
Template Management

Master Data Management
```

---

### User APIs

Authenticated users may access:

```text
Saved Input

XML Preview

XML Export

Export History
```

---

### Authorization Failure

If a user attempts to access an API without sufficient permissions:

```http
403 Forbidden
```

shall be returned.

Example:

```json
{
    "success": false,
    "errors": [
        {
            "code": "FORBIDDEN"
        }
    ]
}
```

---

## 18. Authentication Error Codes

The Authentication APIs may return the following error codes.

| Error Code            | Description                        |
| --------------------- | ---------------------------------- |
| INVALID_CREDENTIALS   | Username or password is incorrect. |
| VALIDATION_FAILED     | Request validation failed.         |
| FORBIDDEN             | User does not have permission.     |
| INTERNAL_SERVER_ERROR | Unexpected server error.           |

---

## 19. Security Considerations

The Authentication APIs shall follow these security principles.

### Password Storage

Passwords shall never be stored in plain text.

Only:

```text
password_hash
```

shall be stored in the database.

---

### Password Exposure

The API shall never:

* Return passwords
* Return password hashes
* Log passwords
* Include passwords in error responses

---

### User Enumeration Protection

The API shall not reveal whether:

* a username exists
* a password is incorrect

Authentication failures shall always return:

```text
INVALID_CREDENTIALS
```

---

### Logging

Authentication logs should include:

* Timestamp
* Username
* Authentication result

Authentication logs shall not include:

* Password
* Password hash

---

## 20. MVP Scope

Authentication for Phase 1 intentionally remains lightweight.

Included:

* Username authentication
* Password verification
* Basic authorization (Admin/User)

This document does not prescribe the transport mechanism used to maintain the authenticated user context after login.

The authentication transport (for example, session cookies or bearer tokens) shall be determined by the deployment architecture and infrastructure requirements.

Excluded from MVP:

* OAuth
* Refresh Token
* Remember Me
* Multi-Factor Authentication (MFA)
* Single Sign-On (SSO)

These capabilities may be introduced in future phases if business requirements evolve.

