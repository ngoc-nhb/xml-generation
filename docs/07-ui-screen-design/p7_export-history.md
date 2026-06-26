# Part 7. Export History

---

# 49. Purpose

The Export History module allows users to view and download previously exported XML files.

Each Export History represents one successful XML Export operation.

Export History is read-only.

Users cannot modify historical export records.

---

# 50. Export History List

## Purpose

Display previously exported XML files belonging to the authenticated user.

---

## Screen Layout

```text
+-----------------------------------------------------------------------+

Export History

------------------------------------------------------------

Search

[ Template ▼ ]

[ Date From ]

[ Date To ]

[ Search ]

------------------------------------------------------------

Export Time

Template

File Name

Status

Actions

------------------------------------------------------------

2026-06-25

LIVE_GAME

live_game.xml

SUCCESS

View

Download

------------------------------------------------------------

[ Previous ]     1     2     3     [ Next ]

+-----------------------------------------------------------------------+
```

---

## UI Components

| Component       | Description            |
| --------------- | ---------------------- |
| Search Filters  | Filter Export History  |
| History Table   | Display export records |
| View Button     | Open export detail     |
| Download Button | Download XML file      |
| Pagination      | Navigate pages         |

---

## Download Availability

The UI shall determine whether the Download action is available using the metadata returned by the Backend.

If the Export History indicates that the XML file has expired (for example, `currentTime > expiredAt`), the Download button shall be disabled.

The UI should visually distinguish unavailable downloads from available ones.

The UI may display a tooltip such as:

```text
File has expired.
```

Disabled Download actions shall not invoke the Download API.

---

## Search Filters

The UI should support:

* Template
* Export Date
* Export Status (Future)

---

## API Integration

```http
GET /api/v1/export-histories
```

Reference:

```
06-api-design.md
```


---

# 51. Export History Detail

## Purpose

Display detailed information about one Export History.

---

## Screen Layout

```text
Export Detail

--------------------------------------

Export Time

Template

File Name

Status

Created By

--------------------------------------

Download XML
```

---

## Display Information

The Detail screen may display:

* Export Time
* Template Name
* Template Version
* File Name
* Export Status
* File Expiration Time

---

## API Integration

```http
GET /api/v1/export-histories/{id}
```

---

# 52. Download XML

## Purpose

Download the XML file associated with an Export History.

---

## User Flow

```text
Click Download

↓

GET Download API

↓

Stream XML

↓

Browser Download
```

---

## API Integration

```http
GET /api/v1/export-histories/{id}/download
```

---

## Processing

The UI shall:

* Request the Download API.
* Allow the browser to download the streamed XML file.

The UI shall not attempt to parse or modify the downloaded XML.

---

## Download Headers

The browser should receive the filename from:

```http
Content-Disposition
```

The UI should rely on the response headers provided by the Backend.

---

# 53. Expired Files

XML files may expire according to the system retention policy.

Export History records remain available even after the physical XML file has been removed.

---

## Download Availability

The UI should proactively determine download availability using the expiration information returned by the Backend.

If the file has already expired:

* The Download button shall be disabled.
* The user shall be informed that the file is no longer available.

The UI shall avoid sending unnecessary Download requests for known expired files.

---

## User Flow

```text
History Loaded

↓

File Expired?

├── No

│     ↓

│  Download Enabled

│

└── Yes

      ↓

 Disable Download

↓

Display Expired Status
```

---

## Expired File Message

Example:

```text
The XML file is no longer available because it has expired.
```

The Export History record shall remain visible for audit purposes even when the XML file is no longer downloadable.

If a Download request is still attempted (for example, due to stale UI data), the UI shall gracefully handle the Backend error and display the expiration message.


---

# 54. Empty States

If no Export History exists:

```text
No export history found.
```

The UI should encourage users to generate their first XML export.

---

## No Search Results

If filters return no matching records:

```text
No matching export history found.
```

The current search conditions should remain visible.

---

# 55. Error Handling

The UI shall gracefully handle common failures.

Examples include:

| Scenario                 | Expected Behavior          |
| ------------------------ | -------------------------- |
| Network Failure          | Display retry message      |
| Download Failed          | Display error notification |
| Export History Not Found | Display error page         |
| File Expired             | Display expiration message |

The UI shall never crash because an Export History cannot be loaded.

---

# 56. Loading States

Loading indicators shall be displayed while:

* Loading Export History
* Loading Detail
* Downloading XML

The UI shall prevent duplicate download requests while a download is already in progress.

---

# 57. Design Principles

The Export History screens follow the principles below.

---

## Read-Only Interface

Users may:

* View Export History
* Download XML

Users shall not modify historical records.

---

## Immutable History

Historical export information shall remain unchanged after creation.

The UI shall never expose editing capabilities.

---

## Separation of History and File

The UI shall treat the Export History record and the XML file as separate resources.

A History record may exist even when the XML file has already expired.

---

## Backend-Driven Retention

The UI shall not determine whether a file has expired.

Expiration status is determined by the Backend.

---

## Efficient Navigation

The History List shall support:

* Search
* Pagination
* Fast navigation

to allow users to efficiently locate previous exports.

---

## Streaming Download

XML downloads shall always use the Download API.

The UI shall rely on browser download behavior and shall not load the entire XML file into client-side memory.
