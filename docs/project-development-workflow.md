# Project Development Workflow

## Purpose

This document defines the mandatory development workflow for the XMLGen project.

It serves as the highest-level implementation guide for all contributors, including AI assistants and human developers.

Every implementation task must follow this workflow unless explicitly overridden by an approved architecture decision.

If any implementation request conflicts with this document, implementation must stop and request clarification before proceeding.
---
## Documentation Separation Principle

Project documentation should be separated by purpose.

- Workflow documents contain reusable development processes and architectural principles.
- ADR documents contain architectural decisions and rationale.
- Domain/Database documents describe the system design.
- Task reports contain implementation results, test results, technical debt, and recommendations.

Do not mix task execution reports into reusable project documentation.
---

# 1. Development Lifecycle

Every feature, enhancement, refactor, or bug fix must follow the lifecycle below.

```
Requirements
        ↓
Architecture Design
        ↓
Architecture Review
        ↓
Implementation
        ↓
Build + Test
        ↓
Implementation Review
        ↓
Architecture Review
        ↓
Technical Debt Review
        ↓
Documentation Update
        ↓
Commit
        ↓
Git Tag (Milestone only)
```

Skipping any step is not allowed unless explicitly approved.

---

# 2. Scope Control

Every implementation task must clearly define

* Goal
* Scope
* Out of Scope

Do not implement features outside the approved scope.

If additional work is discovered during implementation,

report it,

do not implement it without approval.

---

# 3. Pre-Implementation Review (Mandatory)

Before writing any code,

review the implementation request against the current project architecture and documentation.

If any issue is found,

STOP.

Do not begin implementation.

Examples include

* conflicts with ADRs
* conflicts with architecture
* conflicts with documentation
* inconsistent APIs
* poor database design
* duplicated responsibilities
* scalability concerns
* ambiguous business rules
* missing requirements
* opportunities to simplify the design

Do not silently work around these issues.

Report

1. Root cause
2. Why it is a problem
3. Recommended solution
4. Scope impact
5. Files that need to change

Implementation begins only after approval.

---

# 4. Architecture Protection Principle

Implementation is not only about writing code.

Every contributor is responsible for protecting the long-term architecture.

If a requested implementation would weaken the architecture,

STOP.

Explain

* why it is problematic
* the long-term impact
* the recommended solution

Never implement a poor architectural decision simply because it was requested.

Architecture decisions require explicit approval.

---

# 5. Implementation Principles

During implementation

* Keep changes focused.
* Prefer small, reviewable commits.
* Avoid unrelated refactoring.
* Respect existing ADRs.
* Keep implementation aligned with the roadmap.

When a task becomes larger than expected,

stop and propose splitting it into smaller tasks.

---

# 6. Implementation Review

After implementation,

always report

### Files Changed

List every modified file.

### Build Result

Example

```
./gradlew compileJava
BUILD SUCCESSFUL
```

### Test Result

Example

```
./gradlew test
BUILD SUCCESSFUL
```

### Manual Test Result

If manual verification is required,

report

* test steps
* expected result
* actual result

If manual testing is not required,

explicitly state

```
Manual testing not required.
```

### Technical Debt

List any technical debt introduced or discovered.

### Assumptions

List any assumptions made because the specification was incomplete.

### Deviations

Report any deviation from the approved design.

### Recommendations

Provide improvement suggestions separately from the implementation.

Do not hide compromises.

---

# 7. Persistence Layer Purity

Persistence layers have clearly separated responsibilities.

## Migration

Defines the database contract.

## Entity

Reflects the database contract.

Entities are persistence models only.

Entities must not contain

* business rules
* validation logic
* XML generation
* metadata compilation
* mapping logic
* orchestration
* helper methods with business meaning

## Repository

Responsible only for

* persistence
* querying
* filtering
* pagination

Repositories must not contain business decisions.

## Service

Responsible for

* business rules
* validation
* orchestration
* transaction management
* compilation
* runtime workflow

---

# 8. MVP First Principle

Always prioritize

correctness

over

optimization.

Do not introduce

* premature optimization
* unnecessary abstraction
* speculative scalability

without measurable evidence.

Examples

Acceptable

* simple sequential scan for MVP

Not acceptable

* redesigning the persistence model because of hypothetical future scale

Follow the approved roadmap.

---

# 9. Metadata-Driven Principle

Business logic must never depend on hardcoded field names.

The system must always derive behavior from metadata.

Examples

Validation

↓

Metadata

Mapping

↓

Metadata

XML Generation

↓

Metadata

Runtime Resolution

↓

Metadata

This principle applies across the entire project.

---

# 10. Schema–Mapping Separation Principle

Schema and Mapping are independent concerns.

## TemplateField

Describes XML.

## MasterDataField

Describes Master Data.

## TemplateMapping

Connects the two.

Neither schema nor master data owns the mapping.

Changing XML Schema must not require changing Master Data.

Changing Master Data must not require changing XML Schema.

Only TemplateMapping should change.

---

# 11. Single Save Principle

A Template definition consists of

* Template
* TemplateField
* TemplateMapping

These objects form one metadata definition.

They must always be persisted inside one transaction.

After persistence

↓

Schema Compiler runs

↓

compiled_schema_json is regenerated.

compiled_schema_json is a generated artifact.

It must never be manually edited.

If compilation fails,

the entire transaction must be rolled back.

Partial metadata state is not allowed.

---

# 12. Documentation Rule

Architecture and documentation must remain synchronized.

Whenever implementation changes

* architecture
* metadata model
* database design
* validation model
* API contract

the corresponding documentation must be updated within the same task.

Documentation includes

* ADRs
* Domain Model
* Database Design
* Template Schema
* Implementation Guide

Documentation must never lag behind implementation.

---

# 13. AI Collaboration Rule

AI assistants are expected to act as senior engineers.

Their responsibility is not only to implement tasks,

but also to identify architectural risks.

If implementation reveals

* architectural weaknesses
* conflicting documentation
* missing requirements
* better alternatives

the AI must stop and report them before implementation.

Do not invent business rules.

Do not silently modify architecture.

Final architectural decisions require approval.

---

# 14. Manual Testing Policy

Manual testing is required only when runtime behavior exists.

| Layer            | Manual Test                         |
| ---------------- | ----------------------------------- |
| Flyway Migration | No                                  |
| Entity           | No                                  |
| Repository       | Only if custom/native queries exist |
| Service          | If business logic changes           |
| Controller / API | Yes                                 |
| Parser           | Yes                                 |
| Compiler         | Yes                                 |
| XML Generator    | Yes                                 |

Always state whether manual testing was required.

---

# 15. Task Completion Checklist

Every implementation task must report

* Files changed
* Build result
* Test result
* Manual test result (or why it is not required)
* Technical debt
* Assumptions
* Deviations
* Recommendations

A task is **not considered complete** until every applicable checklist item has been addressed.

---

# 16. Standard Implementation Prompt

Every implementation prompt should begin with:

```
Before starting this task,

read and follow

docs/project-development-workflow.md

This workflow is mandatory.

If this task conflicts with the workflow,

stop implementation,

report the conflict,

and wait for approval before proceeding.
```
---
# 17. Architecture Feedback Loop

Architecture reviews are cumulative.

Every implementation task must inherit architectural decisions made in previous reviews.

Before starting implementation,

review the latest architecture feedback.

If previous tasks introduced

* approved architectural decisions
* rejected approaches
* implementation conventions
* documentation updates

those decisions become part of the current task.

They must not be re-evaluated unless explicitly requested.

---

When ChatGPT provides review feedback,

classify each finding as one of the following.

## Approved Decision

A decision that becomes part of the project architecture.

Future implementations must follow it.

Example

TemplateField owns XML Schema only.

TemplateMapping owns Mapping.

---

## Rejected Decision

A rejected design or architectural direction.

Future implementations must not introduce it again.

Rejected approaches must not be tracked as technical debt.

Examples

- Do not inline Mapping into TemplateField.
- Do not implement lazy migration of legacy `compiled_schema_json` into metadata tables.

---

## Documentation Update

A change that should be reflected in

* ADR
* Domain Model
* Database Design
* Template Schema
* Project Development Workflow

Documentation updates should be completed as part of the next appropriate task.

---

## Technical Debt

An accepted limitation.

Do not fix it unless the roadmap explicitly reaches that phase.

---

Every implementation prompt should include

Approved Decisions

Rejected Decisions

Documentation Updates

relevant to that task.

Do not repeat previously rejected approaches.

---

# 18. Architectural Principles

## Runtime Model Principle

Runtime models represent business structure,

not persistence structure.

Runtime models must not expose

- database identifiers
- repository concepts
- persistence implementation details

Parser may use indexes and lookup maps internally,

but those structures must not become part of the Runtime Model.

Runtime Models should remain stable even if the persistence layer changes.

---

## Context Object Principle

When a component is expected to require additional inputs in future phases, group
those inputs into a Context object instead of continually expanding the method
signature.

Context objects represent orchestration concerns rather than business entities.

This keeps component contracts stable while allowing future extension.
---
## Module Boundary Principle

A component must not depend on another module unless its primary responsibility requires it.

Template Parser is responsible only for parsing Template metadata.

It must not depend on Master Data metadata.

Cross-module resolution belongs to higher-level orchestration components.

---

## Pure Transformation Principle

Components whose primary responsibility is transformation should remain pure.

A transformation component should:

- accept input objects
- return output objects

It should not:

- access repositories
- manage transactions
- perform persistence
- invoke external services

Examples: Parser, Compiler, XML Formatter.

Persistence and orchestration belong to higher-level services.

---

## Orchestration Principle

Business workflows that coordinate multiple independent components should be
implemented in dedicated orchestration services.

Orchestrators may:

- load data
- resolve cross-module dependencies
- manage transactions
- invoke multiple components

Orchestrators should not:

- contain parsing logic
- contain compilation logic
- contain generation logic

Business logic remains inside dedicated components.

---

## Replace Semantics Principle

Metadata updates represent the complete definition.

Do not merge, patch, or partially update metadata rows.

Replace semantics mean:

1. Remove obsolete metadata.
2. Persist the new complete definition.
3. Recompile from the new metadata.

Partial metadata state is not allowed.

---

## Runtime Metadata Principle

Editable metadata is the source of truth.

- `TemplateField` owns XML Schema.
- `TemplateMapping` owns Mapping.
- `compiled_schema_json` is a generated artifact only.

Generated artifacts must never be manually edited or used as the editable source of truth.

---

## Read API Principle

Runtime read APIs must reconstruct business responses from editable metadata.

They must load:

- Template
- TemplateField
- TemplateMapping

They must not deserialize `compiled_schema_json` as the runtime schema source.

If metadata does not exist, return an empty schema representation such as `schema: null`.

Do not fall back to legacy JSON when metadata is absent.

---

# 19. Lessons Learned

When implementation reveals a reusable architectural lesson,

evaluate whether it belongs in this workflow document.

Add it only when the lesson is:

- project-wide
- stable
- expected to apply to future tasks

Do not add task-specific reports, milestone summaries, or one-off compromises.

If a lesson overlaps an existing principle, merge it instead of duplicating it.

Use the classification rules in **Architecture Feedback Loop** when deciding how to record a finding.

Project-wide rules belong in this document. Technical debt belongs in the technical debt register.

---
