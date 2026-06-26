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

## Build & Run

Prerequisites:

* JDK 21 on `PATH` (the Gradle toolchain can provision it if not).
* Docker running locally (required by integration tests via Testcontainers).
* PostgreSQL 15+ for `dev` / `prod` profiles. Tests use a Testcontainers PostgreSQL automatically.

### Build

```bash
./gradlew clean build
```

### Run tests

```bash
./gradlew test
```

### Run locally (`dev` profile)

```bash
export SPRING_DATASOURCE_URL=jdbc:postgresql://localhost:5432/xmlgen
export SPRING_DATASOURCE_USERNAME=xmlgen
export SPRING_DATASOURCE_PASSWORD=xmlgen
./gradlew bootRun
```

### Health check

```bash
curl http://localhost:8080/actuator/health
```

All other endpoints return `401 Unauthorized` until the Authentication module is implemented.

## Project Layout

```text
src/main/java/com/company/xmlgen
├── XmlgenApplication.java
├── common/         Shared API contract types
├── config/         Framework configuration
├── engine/         XML Generation Engine (added in a later milestone)
├── exception/      Application exceptions and global handler
├── infrastructure/ Storage, security, external integrations
└── <module>/       Feature modules: authentication, template, masterdata,
                    xmlgeneration, savedinput, exporthistory
```

Refer to `docs/12-project-structure/project-structure.md` for the authoritative layout.
