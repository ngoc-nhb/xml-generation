# Part 2. Login & Dashboard

---

# 11. Login Screen

## Purpose

The Login Screen authenticates users before granting access to the XML Generation System.

It is the only public screen in the application.

All other screens require successful authentication.

---

## Screen Layout

The Login Screen consists of:

- Application Logo
- System Name
- Username
- Password
- Login Button
- Error Message Area

Example layout:

```text
+--------------------------------------------------+

                XML Generation System

                 [ Username           ]

                 [ Password           ]

                 [    Login Button    ]

             Invalid username/password

+--------------------------------------------------+
```

---

## UI Components

| Component | Type | Required |
|------------|------|----------|
| Username | Text Box | Yes |
| Password | Password Box | Yes |
| Login | Primary Button | Yes |
| Error Message | Label | No |

---

## User Actions

Users may:

- Enter Username
- Enter Password
- Press Login
- Press Enter to submit

---

## Validation

Before calling the API:

- Username is required.
- Password is required.

If validation fails:

- No API request shall be sent.
- Validation messages shall be displayed immediately.

---

## API Integration

API

```http
POST /api/v1/auth/login
```

Reference

```
06-api-design.md
```

---

## Success Flow

```text
Enter Credentials

↓

Login API

↓

Authentication Success

↓

Dashboard
```

---

## Failure Flow

```text
Enter Credentials

↓

Login API

↓

Authentication Failed

↓

Display Error

↓

Remain on Login Screen
```

---

## Error Handling

The Backend returns only:

```text
INVALID_CREDENTIALS
```

The UI is responsible for displaying localized messages.

Example:

```text
Invalid username or password.
```

The UI shall not distinguish:

- Username does not exist.
- Password is incorrect.

---

## Loading State

While Login is executing:

- Disable Login Button.
- Disable Username.
- Disable Password.
- Show Loading Indicator.

Multiple concurrent Login requests shall not be allowed.

---

## Security Requirements

The Password field shall:

- Mask all characters.
- Never display the password after submission.
- Never log the password.

Authentication information shall never be stored in browser Local Storage unless defined by the Security Architecture.

---

# 12. Dashboard

## Purpose

The Dashboard serves as the application's home screen after successful authentication.

It provides access to all available features based on the current user's role.

---

## Screen Layout

```text
+----------------------------------------------------------+

Logo              XML Generation System          User

------------------------------------------------------------

Sidebar

• Dashboard

• Templates (*Admin*)

• Master Data (*Admin*)

• XML Generator

• Export History

------------------------------------------------------------

Main Content

Welcome

Quick Actions

Recent Activity (Optional)

+----------------------------------------------------------+
```

---

## UI Components

| Component | Description |
|------------|-------------|
| Header | Application title and user information |
| Sidebar | Main navigation |
| Content Area | Screen content |
| Logout | Logout action |

---

## Dashboard Content

The Dashboard may display:

- Welcome message
- Current user
- Quick links
- Recently used Templates (Future)
- Recent Export History (Future)

For Phase 1, the Dashboard is primarily a navigation hub.

---

## Quick Actions

Administrators may see:

- Create Template
- Manage Master Data
- XML Generator

Users may see:

- XML Generator
- Export History

---

## Navigation

Selecting a menu item shall navigate to the corresponding screen.

Navigation shall preserve authentication state.

---

## Authorization

Menu visibility depends on the authenticated user's role.

| Menu | Admin | User |
|------|:-----:|:----:|
| Templates | ✅ | ❌ |
| Master Data | ✅ | ❌ |
| XML Generator | ✅ | ✅ |
| Export History | ✅ | ✅ |

The UI shall hide unauthorized menus.

The Backend shall still validate authorization for every API request.

---

## Responsive Behavior

The Dashboard shall support:

- Desktop
- Laptop
- Tablet

Phase 1 does not require mobile phone optimization.

---

# 13. Logout

## Purpose

Logout terminates the current authenticated session.

---

## User Flow

```text
Click Logout

↓

Confirmation (Optional)

↓

Logout API

↓

Authentication Cleared

↓

Redirect Login Screen
```

---

## API Integration

```http
POST /api/v1/auth/logout
```

Reference

```
06-api-design.md
```

---

## Processing

The UI shall:

- Clear the current authentication context.
- Remove cached user information.
- Clear in-memory application state.
- Redirect to Login.

The UI shall not retain sensitive application data after logout.

---

## Logout Failure

If the Logout API fails due to a network issue:

- Clear local authentication state.
- Redirect to Login.

The user shall not remain in an indeterminate authenticated state.

---

## 14. Session Behavior

### Initial Load

When the application starts:

```text
Application Start

↓

Authentication Check

↓

Authenticated?

├── Yes → Dashboard

└── No → Login
```

---

### Session Expiration

If authentication expires during API execution:

* The Backend returns an authentication failure.
* The UI shall redirect the user to the Login screen.
* The original destination should be preserved whenever possible to support post-login navigation.

---

### Recoverable User Work

If authentication expires while the user is entering data, the application should preserve recoverable user work whenever possible.

Examples include:

* Current form values
* Selected Template
* Selected Master Data

The mechanism used to preserve temporary UI state is implementation-defined and may vary depending on the frontend architecture.

Temporary UI state preservation is intended only to improve the user experience after re-authentication.

For persistent recovery across browser refreshes or future sessions, users should explicitly use the **Save Draft** feature.

---

### Post-Login Navigation

After successful re-authentication, the application should restore the user's original navigation context whenever possible.

Examples include:

* Returning to the previously requested screen.
* Restoring temporary UI state if available.
* Reloading previously saved drafts when appropriate.

---

### Unauthorized Access

If a user attempts to access an unauthorized screen:

```text
Navigate

↓

Authorization Check

↓

Forbidden

↓

Access Denied

or

Dashboard
```

The UI shall not expose administrative functionality to unauthorized users.


---

# 15. Design Principles

The Login and Dashboard screens follow the principles below.

## Security First

Authentication shall always be verified by the Backend.

The UI shall never rely solely on hidden menus for access control.

---

## Simple Navigation

The Dashboard serves only as a navigation hub.

Business operations are performed on dedicated screens.

---

## Role-Based Experience

The interface adapts to the authenticated user's permissions.

Only relevant functionality shall be displayed.

---

## Stateless Client

The UI shall treat every API request as independently authenticated.

Authentication state management shall follow the Security Architecture defined separately from this document.

---

## Consistent User Experience

All authentication-related interactions shall provide:

- Clear loading feedback.
- Consistent error presentation.
- Predictable navigation behavior.
- Immediate response to session expiration.