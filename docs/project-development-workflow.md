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
- Do not expose a standalone `POST /templates/{id}/compile` endpoint; compilation runs inline on schema save.

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

## Canonical Runtime Model Principle

`RuntimeTemplate` is the canonical runtime model.

Additional execution models may be introduced only when they provide behavior beyond simple structural transformation.

Examples of behavior that would justify a separate execution model:

- optimization
- dependency analysis
- conditional execution
- parallelism
- execution scheduling

Until such behavior exists, internal abstractions that mirror `RuntimeTemplate` structure remain implementation details. They must not be promoted to core architectural layers in the runtime pipeline.

If a future phase introduces meaningful execution semantics, promoting such an abstraction to a first-class architectural concept requires architecture review and explicit approval.

---

## Runtime Execution Artifact Principle

`RuntimeTemplate` is the only canonical runtime model.

Value Resolution may produce a runtime execution artifact (for example, `RuntimeExecutionTree`) that materializes runtime occurrences and resolved values for downstream execution.

Execution artifacts:

- are generated
- are immutable
- are not editable
- are not canonical runtime models
- exist only to simplify downstream execution

This is conceptually similar to `compiled_schema_json`: a generated artifact produced from canonical inputs, not a new source of truth.

---

## API Boundary Principle

Application APIs expose business contracts only.

Runtime Engine models are implementation details and must never become part of public API
contracts unless explicitly approved as debug artifacts.

This applies to every runtime-facing entry point:

- Preview
- Export
- Batch Export
- CLI
- Scheduler

Default public responses shall expose stable business outputs only (for example, generated
XML, export metadata, validation errors). Generated artifacts such as
`compiled_schema_json` and `RuntimeExecutionTree` remain internal.

Debug visibility of runtime artifacts, if ever required, must be opt-in (for example, a
debug flag or admin-only endpoint) and must not become part of the default contract.

`PreviewService`, `ExportService`, and their REST endpoints are the reference
implementation of this principle.

The Runtime REST API contract (`xml` on success, validation `errors` on failure) is
frozen as of Phase 5.5. Do not modify it unless a future ADR explicitly approves the
change. See `docs/11-implementation-guide/xml-generation.md` §15–§16.

---

## XML Engine Complete (v1.0)

Phase 5.5 marks completion of the XML Engine:

```text
Metadata → Compile Engine → Runtime Engine → Application Layer → Preview API → Export API
```

Suggested git tag: `v1.0.0` — **XML Engine Complete**.

Work after this milestone is business capability and infrastructure, not core engine
architecture.

---

## Engine vs Business Features

**Core Engine (complete):** Metadata, Compile Engine, Runtime Engine, Application Layer,
Preview/Export REST APIs.

**Business Features (remaining):** Saved Inputs, Export History, Storage, file download,
Import XML, Template versioning, Batch Export.

Business features extend higher application layers. They must not bypass
`RuntimeExecutionOrchestrator`, modify the Runtime Engine pipeline, or change the frozen
Runtime REST contract without ADR approval.

See `docs/11-implementation-guide/xml-generation.md` §17.

---

## Frontend Architecture Principle

The frontend is a **REST client only**. It must not depend on Runtime Engine artifacts
(`RuntimeTemplate`, `RuntimeExecutionTree`, `compiled_schema_json`) or backend
implementation structure.

UI behavior is metadata-driven: dynamic forms and validation display follow API
responses. Business rules remain on the backend.

Full frontend architecture: `docs/13-ui-design/`.

Phase 6.0 produces design documentation only. Frontend architecture is frozen per
`docs/13-ui-design/12-frontend-stable-architecture.md`. Implementation follows the same
development lifecycle as backend features (architecture review before code).

---

## Feature Isolation Principle

Each feature under `features/` owns its pages, hooks, API module, components, and types.
Shared `components/` remain generic. Each feature must be independently removable.
See `docs/13-ui-design/12-frontend-stable-architecture.md` §4.

---

## Feature Public API Principle

Each feature exposes a **public surface** through `features/<name>/index.ts`.

Cross-feature imports are allowed only through that public module — not through internal
paths such as `api/`, `utils/`, or private components.

```text
✔  import { TemplateListPage } from '@/features/templates'
✘  import { buildFieldTree } from '@/features/templates/utils/schemaTree'
```

Internal implementation details remain private to the feature. This prevents
cross-feature coupling as modules grow.

See `docs/13-ui-design/06-component-architecture.md` §11.

---

## Cross-Feature Integration Principle

Features may collaborate **only** through another feature's public API
(`features/<name>/index.ts`). Features communicate through public hooks and types — never
through internal implementation.

```text
✔  Template → @/features/master-data → useMasterDataFieldPickerOptions() → REST
✘  Template → @/features/master-data/api/fields.api.ts
✘  Template → @/features/master-data/utils/fieldPicker.ts
✘  Template → @/features/master-data/components/…
```

This preserves feature isolation while allowing business integration.

Validated in Phase 6.3.5: Template schema mapping editor uses Master Data public hooks;
no master-data internals imported.

See `docs/13-ui-design/06-component-architecture.md` §15 and
`12-frontend-stable-architecture.md` §4.1.

---

## Consumer-Owned Integration Components Principle

UI that integrates data from another feature belongs in the **consumer** feature, not the
provider.

Example: `MasterDataFieldPicker` lives in `features/templates/` because its responsibility
is *select a mapping for Template schema* — not *manage Master Data*.

It uses `@/features/master-data` public hooks only. Do **not** move it into the Master Data
feature.

Future consumers (XML Generation, Saved Inputs, etc.) implement their own integration
components using the same public API pattern.

See `docs/13-ui-design/06-component-architecture.md` §15.

---

## No Premature Reference Picker Abstraction

Do not create shared pickers such as `SharedEntityPicker`, `LookupPicker`, or
`GenericReferencePicker` until Rule of Three is satisfied across independent business
features.

Phase 6.3.5 proves the Master Data public API is sufficient without a generic abstraction.

---

## Dynamic Form Ownership Principle

Metadata-driven form rendering belongs to the feature that owns the metadata. Do not
extract a generic `DynamicForm` to shared `components/` until Rule of Three is satisfied
across features.

Phase 6.3: `DynamicRecordForm` in `features/master-data/` renders record fields from
master data field definitions. It must remain inside that feature until another business
module genuinely requires the same abstraction.

See `docs/13-ui-design/06-component-architecture.md` §15.

---

## Dynamic Record Model Principle

`DynamicRecordForm` edits **Master Data Records** only. It must never become responsible
for preview, XML generation, runtime execution, or template schema editing.

```text
Master Data (records) → Template (schema) → Runtime → XML
```

Each layer owns its editable surface. See `12-frontend-stable-architecture.md` §6.2.

---

## Schema Editor Boundary Principle

The Template Schema Editor is a **metadata editor only**. It must never:

- execute preview or export
- validate runtime input values
- depend on `RuntimeExecutionTree` or `compiled_schema_json`

It edits metadata (fields, hierarchy, mappings) and persists via
`PUT /templates/{id}/schema`. Preview and export belong exclusively to the XML
Generation feature.

See `docs/13-ui-design/12-frontend-stable-architecture.md` §6.1.

---

## Execution Session Principle

During one XML Generation **execution session**, preserve user inputs and context:

- Selected template
- Selected master data
- Input JSON
- Latest preview/export XML result
- Latest validation errors

Do **not** reset these automatically after Preview or Export.

Reset only when:

- The user presses **Reset** (input JSON) or removes master data selections explicitly
- The user **changes template** (full session reset for that workflow)

This minimizes repeated work during iterative preview/export cycles.

Validated in Phase 6.4: `ExecutionPanel` in `features/xml-generation/`.

See `12-frontend-stable-architecture.md` §6.3.

---

## Execution Screen Principle

Execution-oriented state (template selection, input payload, master data selections,
preview/export results) belongs inside the **execution feature** — never in global
context.

```text
✔  ExecutionPanel → local state → Preview / Export mutations
✘  Global Context → execution state shared across routes
```

`features/xml-generation/` owns XML Generation execution state. Future execution features
(e.g. batch export) follow the same ownership model within their feature module.

See `12-frontend-stable-architecture.md` §6.4.

---

## Backend Single Source of Truth Principle

The frontend must never duplicate Runtime Engine behavior.

| Forbidden on frontend | Allowed on frontend |
| --------------------- | ------------------- |
| Runtime validation | JSON syntax validation |
| XML escaping / serialization | UX formatting (pretty-print JSON) |
| Occurrence expansion | Loading and empty states |
| Value or mapping resolution | Error presentation from `errors[]` |
| XML formatting rules | Read-only display of `data.xml` |

Business logic always belongs to the backend. The frontend orchestrates public REST APIs only.

See `12-frontend-stable-architecture.md` §3 and §8.

---

## Execution API Symmetry Principle

Preview and Export remain symmetrical at the REST boundary:

| | Preview | Export |
| --- | --- | --- |
| Endpoint | `POST /templates/{id}/preview` | `POST /templates/{id}/export` |
| Request body | `{ inputData, selectedMasterData }` | Same |
| Validation failures | `success: false`, `errors[]` | Same |
| Success payload | `{ xml }` | `{ xml }` |

Both share the runtime pipeline on the backend; only downstream business output differs
(MVP: both return XML in JSON). Preserve this symmetry in future API evolution.

See `docs/11-implementation-guide/xml-generation.md` and `docs/06-api-design/p3_template-api.md` §26A–§26B.

---

## Feature Completeness Principle

A frontend feature is **complete** only when it owns:

- `pages/`, `components/`, `hooks/`, `api/`, `types/`
- `index.ts` public API
- Architecture documentation alignment

Not merely when UI screens exist.

| Feature | Complete |
| ------- | -------- |
| Auth | ✅ |
| Templates | ✅ |
| Master Data | ✅ |
| XML Generation | ✅ |

---

## API Ownership Principle

All HTTP calls flow through feature API modules → shared `api/client.ts` → REST.

Forbidden: components, dialogs, or pages calling `fetch()` / axios directly.

See `docs/13-ui-design/12-frontend-stable-architecture.md` §5.

---

## Editable vs Generated UI Principle

Editable views (forms, schema editors) accept user input. Generated views (`XmlViewer`,
preview/export panels) display `data.xml` read-only and must never become editable.

See `docs/13-ui-design/12-frontend-stable-architecture.md` §6.

---

## Runtime Validation Scope Principle

Runtime Validation validates the **integrity of the runtime model** — not compile-time metadata invariants that are already guaranteed before `compiled_schema_json` is produced.

Runtime Validation should focus on:

- corrupted runtime artifacts
- impossible runtime states
- illegal runtime structures

Use this guideline:

> Runtime Validation should validate executable runtime integrity. Compile-time guarantees should not be duplicated unless runtime artifacts may originate from external or untrusted sources.

Some overlap with compile-time rules is acceptable when loaded runtime artifacts may bypass the normal compile path (for example, imported or externally modified compiled schemas). Do not treat that overlap as a reason to expand Runtime Validation into compile-time or mapping concerns.

`RuntimeTemplate` intentionally excludes mapping metadata. Mapping validation belongs to compile or orchestration phases where mapping metadata is still available.

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
# Application Orchestration Principle

Application Services own orchestration only.

They coordinate existing runtime components but must never duplicate business logic already implemented by the Runtime Engine.

Application Services may:

- load data
- invoke runtime pipeline
- persist results
- coordinate transactions
- map API DTOs

Application Services must never:

- resolve values
- perform runtime validation
- generate XML directly
- traverse runtime trees
- implement serialization
- implement compile logic

Whenever new behavior is needed, it should first be evaluated as a responsibility of an existing runtime component before adding logic to an application service.

## Reference implementation

Runtime-facing application services follow this pattern:

```text
                TemplateCompileMappingResolver
                           ▲
                           │
     ┌─────────────────────┼─────────────────────┐
     │                     │                     │
     ▼                     ▼                     ▼
TemplateCompilation   PreviewService     ExportService
   Orchestrator              │                     │
     │                       └──────────┬──────────┘
     │                                  ▼
     │                    RuntimeExecutionOrchestrator
     │                                  │
     └──────────────────────────────────┼──────────► Runtime Engine
                                        ▼
                              (Loader → Validation →
                               ValueResolution → XMLGeneration)
```

`TemplateCompileMappingResolver` is Application Layer only. It is repository-aware,
shared by compile orchestration and runtime services, and independent from the Runtime
Engine. No Runtime Engine component depends on it.

`PreviewService` and `ExportService` load application data, resolve mappings via the
resolver, build `RuntimeExecutionRequest`, invoke `RuntimeExecutionOrchestrator`, and
map `RuntimeExecutionResult` to application responses. Preview, Export, and compile
orchestration must not duplicate mapping lookup logic.

Request construction (load template + build `RuntimeExecutionRequest`) is currently
duplicated between Preview and Export. This is acceptable while they remain separate
business use cases. If a future consumer appears (CLI, Batch Export, Scheduler), consider
extracting `RuntimeExecutionRequestFactory` or `RuntimeExecutionRequestBuilder`
(Application Layer only). Do not extract prematurely.

---

# Canonical Runtime Model Review (Mandatory)

Before implementing any new runtime model, perform an architecture review against the **Canonical Runtime Model Principle** defined in `docs/project-development-workflow.md`.

Specifically review whether introducing a new structure (for example, `RuntimeExecutionTree`) provides genuine execution value, or merely duplicates the existing `RuntimeTemplate` without representing a distinct execution stage.

Execution artifacts that materialize runtime occurrences and resolved values are permitted when they remain non-canonical generated outputs, similar to `compiled_schema_json`.

A new **canonical** runtime model should only be introduced if it adds new behavior or represents a distinct definition-stage responsibility, such as:

- immutable resolved values
- execution optimization
- scheduling
- dependency analysis
- execution state
- another clearly different runtime responsibility

Creating a new **canonical** model solely to copy `RuntimeTemplate` and append one or more fields is **not sufficient justification**.

If you determine that the proposed structure should become a second **canonical** runtime model and does **not** satisfy the Canonical Runtime Model Principle:

STOP.

Do not implement it.

Instead provide:

1. Why the new model is unnecessary.
2. The recommended alternative architecture.
3. The impact on the current phase.
4. The files that would change under the revised architecture.

Wait for approval before proceeding.

Only implement a new runtime model after confirming that it adds real architectural value rather than becoming another structural mirror of `RuntimeTemplate`.

---
Application Layer may contain reusable shared components
(e.g. TemplateCompileMappingResolver)

when:

- the logic is repository-aware,
- reused by multiple Application Services,
- independent from Runtime Engine,
- and does not represent a business use case itself.

Such shared components must never be moved into the Runtime Engine.

---
# Feature Evolution Principle

## Purpose

Prevent premature abstraction and keep the frontend architecture aligned with real business evolution.

Reusable components should emerge from proven usage patterns rather than speculation.

---

## Principle

Feature-specific implementations should remain inside their owning feature until there is sufficient evidence that the abstraction is stable.

Do **not** extract shared components simply because two implementations look similar.

Instead, apply the **Rule of Three**.

---

## Rule of Three

A reusable abstraction should normally be extracted only when **at least three independent use cases** exist.

Example:

❌ Too early

```
TemplateTable
MasterDataTable
```

Although both display tables, they may evolve differently.

Keep them inside their respective features.

---

✅ Appropriate extraction

```
TemplateTable
MasterDataTable
ExportHistoryTable
```

If all three require nearly identical behaviors:

- pagination
- sorting
- loading
- empty state
- row actions

then extract:

```
DataTable
```

as a shared presentation component.

---

## Extraction Criteria

Before extracting a shared component, verify:

- At least three concrete use cases exist, **or**
- There is strong architectural evidence that the abstraction is stable.

The extracted component should have:

- clear responsibility
- stable API
- no business knowledge
- no feature-specific naming
- no direct REST/API access

---

## Ownership Rules

Feature-specific components belong inside:

```
features/<feature-name>/
    components/
```

Examples:

```
TemplateForm
TemplateSchemaEditor
TemplateToolbar

MasterDataFieldEditor
MasterDataRecordTable

PreviewPanel
ExportToolbar
```

These should **not** be moved to shared components prematurely.

---

## Shared Components

Only generic presentation components belong under:

```
components/
```

Examples:

```
DataTable
ConfirmDialog
DeleteDialog
FormDialog
SearchBar
Pagination
StatusBadge
EmptyState
LoadingOverlay
XmlViewer
JsonEditor
SplitPanel
```

These components must remain:

- presentation-only
- business-agnostic
- reusable across features

---

## Architecture Review Requirement

Whenever a potential reusable component is discovered, classify it through the Architecture Feedback Loop.

Possible outcomes:

- Critical Architecture Issue
- Recommended Improvement
- Documentation Update
- Future Improvement
- Rejected Alternative

If extraction changes ownership boundaries or shared architecture, implementation must stop until the proposal is reviewed and approved.

---

## Rationale

Over-abstraction increases coupling and makes future feature evolution more difficult.

Waiting until an abstraction proves itself through real usage produces:

- simpler code
- clearer ownership
- lower maintenance cost
- more stable shared components

This principle complements the existing:

- Feature Isolation Principle
- Feature Public API Principle
- Cross-Feature Integration Principle
- Consumer-Owned Integration Components Principle
- Execution Session Principle
- Execution Screen Principle
- Backend Single Source of Truth Principle
- Dynamic Form Ownership Principle
- Feature Completeness Principle
- API Ownership Principle
- Editable vs Generated UI Principle

and ensures the frontend evolves from real product needs rather than speculative design.

---

## Frontend Implementation Status

| Phase | Scope | Status |
| ----- | ----- | ------ |
| 6.0 | UI/UX architecture (docs) | ✅ Approved |
| 6.1 | Frontend foundation (shell, auth, api client) | ✅ Approved |
| 6.2 | Template module (CRUD, metadata, schema editor) | ✅ Approved |
| 6.3 | Master Data module (types, fields, records) | ✅ Approved |
| 6.3.5 | Template ↔ Master Data integration (mapping picker) | ✅ Approved |
| 6.4 | XML Generation (preview/export orchestration) | ✅ Approved |
| 6.5+ | Export History, Saved Inputs, Dashboard, Settings | Pending |

Phase 6.2–6.4 validated the frozen frontend architecture across independent business modules
and cross-feature integration. Phase 6.4 completes the **MVP end-to-end business flow**:
Template → Master Data → XML Generation over public REST APIs only.

Remaining work is primarily infrastructure: Export History, Saved Inputs, Batch Export,
Versioning, Dashboard, Settings expansion.

Feature boundaries: Template (Metadata → Schema → CRUD → REST); Master Data (Types → Fields
→ Records → REST); XML Generation (orchestration → Preview/Export → REST). Nothing below
REST is visible to the UI.

---

## Phase 7 — Stabilization & Release (v1.0.0)

| Phase | Scope | Status |
| ----- | ----- | ------ |
| 7.0 | Release preparation mandate | ✅ In progress |
| 7.1 | Architecture cleanup review | ✅ Complete — no unnecessary refactors |
| 7.2 | Documentation freeze | ✅ Complete — see `docs/release/API-CONTRACT.md` |
| 7.3 | API contract verification | ✅ Postman + OpenAPI (dev) |
| 7.4 | Performance review | ✅ Documented — no optimization |
| 7.5 | Security review | ✅ Architecture review only |
| 7.6 | Docker deployment | ✅ `docker compose up --build` |
| 7.7 | CI/CD | ✅ `.github/workflows/ci.yml` |
| 7.8 | Release candidate | ✅ `docs/release/RELEASE-NOTES-v1.0.0-rc1.md` |
| 7.9 | Final release | Pending verification → tag `v1.0.0` |

Release artifacts: `docs/release/` (deployment guide, architecture summary, known limitations, technical debt summary).

Task execution details belong in `docs/release/phase-7.*.md`, not in this workflow document (Documentation Separation Principle).