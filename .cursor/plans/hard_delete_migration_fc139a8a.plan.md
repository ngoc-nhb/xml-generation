---
name: Hard Delete Migration
overview: Migrate existing delete behavior from soft delete to physical deletion across templates and master data, removing soft-delete filters and cleaning obsolete schema columns while preserving API contracts.
todos:
  - id: code-cleanup
    content: Remove soft-delete fields, filters, and service behavior from master data and auth/template schema remnants.
    status: pending
  - id: fk-migration
    content: Add Flyway migration for hard-delete FK behavior and obsolete deleted_at column cleanup.
    status: pending
  - id: tests
    content: Update unit/integration tests and add hard-delete regression coverage.
    status: pending
  - id: verify
    content: Run compile/tests and manually verify delete-recreate flows.
    status: completed
isProject: false
---

# Hard Delete Migration Plan

## Important Note
MasterDataType duplicate checks currently use `code`, not `name`. The requested manual check says “same name”, but same-name creation already succeeds unless the code is duplicated. I will verify the meaningful regression as same `code` where the API supports it, and also confirm same-name creation is unaffected.

## Approach
- Use a new Flyway migration rather than editing existing migrations, so existing databases can migrate safely.
- Hard-delete through repositories for every delete API.
- Handle MasterDataType children before deleting the parent to avoid FK failures and orphan rows.
- Remove obsolete soft-delete filtering from queries and service logic.
- Keep API routes, response envelopes, and DTOs unchanged.

## Expected File Changes
- `src/main/java/com/company/xmlgen/masterdata/service/MasterDataTypeServiceImpl.java`
  - Replace recent soft delete with hard delete.
  - Delete child `MasterDataRecord` and `MasterDataField` rows for the type inside the transaction before deleting the type.
- `src/main/java/com/company/xmlgen/masterdata/service/MasterDataRecordServiceImpl.java`
  - Replace `deletedAt = now()` with `delete(entity)`.
  - Remove `.filter(record -> record.getDeletedAt() == null)` from detail/update/delete paths.
- `src/main/java/com/company/xmlgen/masterdata/repository/MasterDataRecordRepository.java`
  - Remove `deleted_at IS NULL` filters from list and duplicate lookup queries.
  - Add bulk delete support for records by `masterDataTypeId` if needed.
- `src/main/java/com/company/xmlgen/masterdata/repository/MasterDataFieldRepository.java`
  - Add bulk delete support for fields by `masterDataTypeId` if needed.
- `src/main/java/com/company/xmlgen/masterdata/entity/MasterDataTypeEntity.java`
  - Remove `deletedAt` mapping.
- `src/main/java/com/company/xmlgen/masterdata/entity/MasterDataRecordEntity.java`
  - Remove `deletedAt` mapping.
- `src/main/java/com/company/xmlgen/authentication/entity/UserEntity.java`
  - Remove obsolete `deletedAt` mapping because the schema cleanup removes the column.
- `src/main/java/com/company/xmlgen/authentication/service/AuthenticationServiceImpl.java`
  - Remove obsolete TODO referencing `deleted_at`.
- `src/main/resources/db/migration/V10__hard_delete_cleanup.sql`
  - Drop obsolete `deleted_at` columns from `templates`, `users`, `master_data_types`, and `master_data_records` if they exist.
  - Recreate relevant MasterData FK constraints for hard-delete behavior:
    - `master_data_records.master_data_type_id -> master_data_types.id ON DELETE CASCADE`
    - `master_data_fields.master_data_type_id -> master_data_types.id ON DELETE CASCADE`
    - `master_data_fields.master_data_reference_type_id -> master_data_types.id ON DELETE SET NULL`
- Tests:
  - `src/test/java/com/company/xmlgen/masterdata/service/MasterDataTypeServiceImplTest.java`
  - `src/test/java/com/company/xmlgen/masterdata/service/MasterDataRecordServiceImplTest.java`
  - `src/test/java/com/company/xmlgen/masterdata/service/MasterDataFieldServiceImplTest.java` if affected
  - `src/test/java/com/company/xmlgen/integration/TemplateDeleteIntegrationTest.java` or add similar regression coverage for master data delete/recreate

## Test Updates
- Update MasterDataType delete tests to expect hard delete and child cleanup.
- Update MasterDataRecord delete tests to expect physical delete and no soft-delete filtering behavior.
- Keep Template and MasterDataField hard-delete tests aligned with current behavior.
- Add regressions for:
  - delete then recreate same unique value/code
  - delete parent with child records/fields
  - delete non-existing resource
  - unique validation after hard delete

## Verification
- Run `./gradlew compileJava`.
- Run `./gradlew test`.
- Manually verify via API/curl after starting the app:
  - MasterDataType: create, delete, recreate with same code/name as applicable.
  - Template: create, delete, recreate same code.
  - MasterDataField: create, delete, recreate same field code/display order.
  - MasterDataRecord: create unique value, delete, recreate same unique value.

## Foreign Key Strategy
- Use database cascade for true ownership children of MasterDataType: fields and records.
- Use `ON DELETE SET NULL` for field reference pointers to another MasterDataType, because those are references from other schemas, not owned children.