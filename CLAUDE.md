# CLAUDE.md

## Project Overview

This repository implements the XML Generation System.

The software design has been completed and is considered the single source of truth.

Do not invent new business rules.

---

# Working Principles

* Read the relevant design documents before writing code.
* Follow the architecture defined in the documentation.
* Keep changes small and focused.
* Do not modify unrelated modules.
* Explain the implementation plan before generating code.
* Prefer production-ready code over quick prototypes.

---

# Architecture Rules

* Follow Layered Architecture.
* Organize code by feature.
* Keep Controllers thin.
* Business logic belongs only in Services.
* Repositories perform persistence only.
* The XML Engine must remain infrastructure-independent.
* DTOs must never be passed into the XML Engine.

---

# Coding Rules

* Java 21
* Spring Boot 3
* Gradle
* Spring Data JPA
* Flyway
* Bean Validation
* JUnit 5
* Mockito

Use constructor injection.

Avoid circular dependencies.

Do not introduce unnecessary libraries.

---

# Before Writing Code

Always:

1. Explain the implementation plan.
2. List the files to be created or modified.
3. Wait for approval before generating code.

---

# While Implementing

* Implement only the requested scope.
* Do not refactor unrelated code.
* Keep methods focused and readable.
* Follow the Development Standards.
* Follow the Module Specification.

---

# After Implementation

Always:

1. Verify architecture consistency.
2. Check for dependency violations.
3. Check for unnecessary code.
4. Ensure the project can compile.

---

# Documents

Use these documents as the source of truth.

Priority:

1. Module Specifications
2. Development Standards
3. Project Structure
4. API Design
5. Database Design
6. Remaining design documents

If documents conflict, stop and ask for clarification instead of making assumptions.
