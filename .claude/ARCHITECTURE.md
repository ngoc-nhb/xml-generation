# Architecture Notes

Generated from a full codebase scan (2026-07-01). Complements the root `CLAUDE.md`
(rules/conventions — authoritative) with concrete pointers into the current code.

## Build/Test Commands

- Build: `./gradlew build`
- Run: `./gradlew bootRun`
- Single test class: `./gradlew test --tests "com.company.xmlgen.<package>.<ClassName>"`
- Stack: Spring Boot 3.3.5, Java 21 toolchain, Spring Web/Validation/Data JPA/Security/Actuator,
  springdoc-openapi, Flyway (`flyway-core` + `flyway-database-postgresql`), JJWT 0.12.6,
  PostgreSQL driver. Tests: `spring-boot-starter-test`, `spring-boot-testcontainers`,
  `spring-security-test`, Testcontainers (junit-jupiter, postgresql).
- Backend run requires `SPRING_DATASOURCE_URL/USERNAME/PASSWORD` and `JWT_SECRET` env vars
  (README §4.2) and a local Postgres on port 5433.
- Frontend (React 19 + Vite, separate `frontend/` dir): `npm install && npm run dev`;
  `npm run lint` / `npm run build`.

## Architecture Overview

Feature-based layered architecture: `Controller → Service → Repository → Database`,
formalized in `docs/adr/ADR-001-service-layer-boundary.md`. Controllers are thin and wrap
results in `common.api.ApiResponse`; service public interfaces use request/response DTOs,
never JPA entities or Spring Data types; entities live in `{module}.entity`.

`engine/` (`src/main/java/com/company/xmlgen/engine/`) is a reserved/placeholder package
(only `package-info.java`). The actual XML generation engine lives in `xmlgeneration.domain`
and `xmlgeneration.service` (`ExecutionTreeXmlWriter`, `RuntimeExecutionPlanner`,
`RuntimeExecutionOrchestrator`), operating only on domain types (`ExecutionNode`,
`ExecutionPlan`, `RuntimeExecutionNode/Tree`) — never DTOs, per the CLAUDE.md rule that DTOs
must never enter the XML Engine.

## Feature Modules

- **authentication** — login/JWT. `AuthenticationController`, `AuthenticationService(Impl)`,
  `TokenProvider`/`JwtTokenProvider`, `UserRepository`, `UserEntity`, `AuthenticatedUser`.
- **common** — shared API envelope/pagination/base entity: `ApiResponse`, `ApiError`,
  `PageMeta`, `PageResult`, `BaseEntity`.
- **config** — app-wide Spring config (Jackson, OpenAPI, persistence, security, web).
- **engine** — reserved/empty package; real engine logic is under `xmlgeneration`.
- **exception** — `GlobalExceptionHandler`, `ApplicationException`/`BusinessException`
  hierarchy, `ErrorCode`, `ErrorResponseWriter`, `FieldViolation`.
- **exporthistory** — scaffolding only (`package-info.java` per layer); not yet implemented,
  intentionally deferred (not dead code).
- **infrastructure** — CORS/security. `CorsConfig`/`CorsProperties`, `JwtAuthenticationFilter`,
  `SecurityConfig`, `RestAccessDeniedHandler`, `RestAuthenticationEntryPoint`.
- **masterdata** — metadata (types/fields) + JSONB records. Controllers:
  `MasterDataTypeController`, `MasterDataFieldController`, `MasterDataRecordController`.
  Services: `MasterDataTypeServiceImpl`, `MasterDataFieldServiceImpl`,
  `MasterDataRecordServiceImpl`, `MasterDataValidationServiceImpl`. Validation rules
  (Convention 6): `RequiredValidationRule`, `DataTypeValidationRule`, `UniqueValidationRule`,
  `ReferenceValidationRule` (each a `ValidationRule` bean with `priority()`). Entities:
  `MasterDataTypeEntity`, `MasterDataFieldEntity`, `MasterDataRecordEntity`.
- **savedinput** — persisted user input drafts. `SavedInputController`,
  `SavedInputServiceImpl`, `SavedInputRepository`, `SavedInputEntity`.
- **template** — template/schema authoring, compilation, XML import. Controller:
  `TemplateController`. Services: `TemplateServiceImpl`, `TemplateSchemaCompilerImpl`,
  `TemplateSchemaParserImpl`, `TemplateCompilationOrchestratorImpl`,
  `TemplateCompileMappingResolverImpl`, `RuntimeLoaderImpl`; `importing` sub-package handles
  XML-to-template drafting (`XmlImportParserImpl`, `TemplateDraftBuilderImpl`). Entities:
  `TemplateEntity`, `TemplateFieldEntity`, `TemplateMappingEntity` + enums. Domain
  (engine-facing, no DTOs): `RuntimeTemplate`, `RuntimeField`, `TemplateCompileContext/Mapping`.
- **workspace** — multi-tenant workspace context/ownership. `WorkspaceController`,
  `WorkspaceServiceImpl`, `WorkspaceOwnershipGuard`, `WorkspaceContextResolver`,
  `WorkspaceContextFilter`, entities `WorkspaceEntity`/`WorkspaceMemberEntity`. Governed by
  `docs/adr/ADR-003-workspace-ownership.md`.
- **xmlgeneration** — runtime engine: preview/export of XML. Controllers: `ExportController`,
  `PreviewController`. Orchestration: `RuntimeExecutionPlannerImpl`,
  `RuntimeValidationServiceImpl` (`HierarchyValidationRule`, `NodeTypeValidationRule`,
  `OccurrenceValidationRule`, `EmptyHandlingValidationRule`), `SelectedMasterDataLoaderImpl`,
  `ValueResolutionServiceImpl`, `ResolvedValueValidationServiceImpl`,
  `RuntimeExecutionOrchestratorImpl`, `ExecutionTreeXmlWriter`, `XMLGenerationServiceImpl`,
  `ExportServiceImpl`, `PreviewServiceImpl`.

## Database

- PostgreSQL 15+ (16 recommended), Spring Data JPA + Flyway.
- Migrations in `src/main/resources/db/migration/`, `V{n}__description.sql` (V1–V17), e.g.
  `V1__baseline.sql`, `V6__create_master_data_types_table.sql`,
  `V9__create_master_data_records_table.sql`, `V12__create_template_metadata_tables.sql`,
  `V13__create_workspace_tables.sql`, `V17__create_saved_inputs_table.sql`.
- `MasterDataRecordEntity` stores runtime data as JSONB (`data_json`), per Convention 4 /
  ADR-002 — no entity-per-type tables.

## Key ADRs

- **ADR-001** (`docs/adr/ADR-001-service-layer-boundary.md`) — Controller→Service→Repository→DB;
  thin controllers wrapping `ApiResponse`; service interfaces must not leak JPA/Spring Data
  types; `@Transactional` only at service level.
- **ADR-002** (`docs/adr/ADR-002-metadata-driven-architecture.md`) — Three-layer model:
  **Metadata** (`MasterDataType`/`Field`, `Template`/`TemplateField`/`TemplateMapping`) →
  **Schema** (resolved shape, computed not stored) → **Runtime**
  (`MasterDataRecord.data_json` JSONB + Mapping/XML Engine). No business logic may hardcode
  field names. "Single Save Principle": Template schema saves are atomic (fields + mappings +
  compilation in one transaction, rollback on compile failure, no standalone Mapping CRUD API).
  `ValidationRule` priority list: Required=100, DataType=200, Unique=300, Reference=400.
- **ADR-003** (`docs/adr/ADR-003-workspace-ownership.md`) — multi-tenant resource ownership
  model for the `workspace` module.

## Request Flow: XML Generation (Preview/Export)

1. `xmlgeneration.controller.PreviewController` / `ExportController` receive
   `PreviewRequest`/`ExportRequest` DTOs.
2. `PreviewServiceImpl`/`ExportServiceImpl` load the compiled template via
   `template.service.RuntimeLoaderImpl` → `RuntimeTemplate` (domain type).
3. `RuntimeExecutionPlannerImpl` builds an `ExecutionPlan`/`RuntimeExecutionTree` (domain
   types, no DTOs) from the `RuntimeTemplate`.
4. `RuntimeValidationServiceImpl` traverses the tree applying `HierarchyValidationRule`,
   `NodeTypeValidationRule`, `OccurrenceValidationRule`, `EmptyHandlingValidationRule`.
5. `SelectedMasterDataLoaderImpl` fetches master data records (via `masterdata` repositories);
   `ValueResolutionServiceImpl` + `ResolvedValueValidationServiceImpl` resolve values into the
   tree.
6. `RuntimeExecutionOrchestratorImpl` drives the pipeline; `ExecutionTreeXmlWriter` serializes
   the final `RuntimeExecutionTree` to XML — the infrastructure-independent "engine" step,
   operating purely on domain objects.
7. Result is mapped back to `PreviewResponse`/`ExportResponse` DTOs at the service boundary.

**Engine independence caveat**: `ExecutionTreeXmlWriter` and `RuntimeExecutionNode.field()`
reference `template.entity` enums (`TemplateFieldNodeType`, `TemplateFieldEmptyHandling`) —
persistence-layer enum types, not full JPA entities. ADR-001 §4 allows this ("Response DTOs
may reference stable business enums... not tightly coupled to persistence behavior"), but
it's worth revisiting once the dedicated `engine` package is actually built out.
