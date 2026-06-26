# XMLGen

XMLGen is a template-driven XML generation system designed to generate structured XML documents from configurable templates and user input.

## Project Status

Current Phase: **Implementation**

The software design has been completed and frozen.

Implementation must follow the design documents under the `docs/` directory.

## Documentation

| Document | Description           |
| -------- | --------------------- |
| 01       | Requirement           |
| 02       | Domain Model          |
| 03       | Database Design       |
| 04       | Template Schema       |
| 05       | XML Generation Engine |
| 06       | API Design            |
| 07       | UI Screen Design      |
| 08       | Class Diagram         |
| 09       | Test Design           |
| 10       | Development Standards |
| 11       | Module Specifications |
| 12       | Project Structure     |

## Technology Stack

* Java 21
* Spring Boot 3
* PostgreSQL
* Spring Data JPA
* Flyway
* Gradle
* JUnit 5
* Mockito

## Project Principles

* Follow Layered Architecture.
* Organize code by feature.
* Keep Controllers thin.
* Keep business logic inside Services.
* Keep the XML Engine independent from infrastructure.
* Do not introduce business rules not defined in the design documents.

## Development Workflow

1. Read the relevant design documents.
2. Implement one module at a time.
3. Build after every implementation.
4. Run unit tests.
5. Commit after each completed task.
