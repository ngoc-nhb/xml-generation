import type { ApiError } from '@/types/api/common';

const ERROR_MESSAGES: Record<string, string> = {
    MASTER_DATA_TYPE_NOT_FOUND: 'Master data type not found.',
    MASTER_DATA_RECORD_NOT_FOUND: 'Master data record not found.',
    MASTER_DATA_FIELD_NOT_FOUND: 'Master data field not found.',
    MASTER_DATA_TYPE_ALREADY_EXISTS: 'Master data type code already exists.',
    MASTER_DATA_IN_USE: 'Master data type is referenced by a template mapping.',
    MASTER_DATA_RECORD_IN_USE: 'Master data record cannot be deleted.',
    MASTER_DATA_SCHEMA_CONFLICT: 'Schema change conflicts with existing records.',
    MASTER_DATA_VERSION_CONFLICT: 'Schema version conflict. Refresh and try again.',
    MASTER_DATA_NOT_FOUND: 'Selected master data record was not found.',
    MASTER_DATA_TYPE_MISMATCH: 'Selected master data does not match the required type.',
    TEMPLATE_NOT_FOUND: 'Template not found.',
    TEMPLATE_NOT_COMPILED: 'Template is not compiled. Save the schema before preview or export.',
    TEMPLATE_CODE_ALREADY_EXISTS: 'Template code already exists.',
    TEMPLATE_IN_USE: 'Template cannot be deleted because it is referenced by other data.',
    TEMPLATE_FIELD_NAME_DUPLICATE: 'Duplicate field name in schema.',
    TEMPLATE_PARENT_FIELD_NOT_FOUND: 'Parent field does not exist.',
    TEMPLATE_INVALID_HIERARCHY: 'A field cannot be its own parent.',
    TEMPLATE_PARENT_CYCLE: 'Field hierarchy contains a cycle.',
    TEMPLATE_FIELD_NOT_FOUND: 'Referenced field was not found.',
    TEMPLATE_MAPPING_DUPLICATE: 'Duplicate mapping for the same field.',
    VALIDATION_FAILED: 'Request validation failed.',
    UNAUTHORIZED: 'Session expired. Please sign in again.',
    FORBIDDEN: 'You do not have permission to perform this action.',
    NETWORK_ERROR: 'Unable to connect to the server.',
    INTERNAL_SERVER_ERROR: 'An unexpected error occurred.',
    REQUEST_FAILED: 'Request failed.',
    WORKSPACE_REQUIRED: 'Workspace selection is required.',
    INVALID_WORKSPACE: 'The selected workspace is invalid.',
    WORKSPACE_INACTIVE: 'This workspace is inactive.',
    WORKSPACE_NOT_FOUND: 'Workspace not found.',
    WORKSPACE_CODE_ALREADY_EXISTS: 'Workspace code already exists.',
    WORKSPACE_IN_USE: 'Workspace cannot be deleted because it contains templates or master data.',
    XML_IMPORT_MALFORMED: 'The XML file is malformed.',
    XML_IMPORT_EMPTY: 'The XML file is empty.',
    XML_IMPORT_MULTIPLE_ROOTS: 'The XML file must contain exactly one root element.',
    XML_IMPORT_DUPLICATE_ATTRIBUTE: 'The XML file contains duplicate attributes.',
    XML_IMPORT_UNSUPPORTED_CONSTRUCT: 'The XML file contains an unsupported construct.',
};

export function getErrorMessage(error: ApiError): string {
    return ERROR_MESSAGES[error.code] ?? error.code;
}

export function getPrimaryErrorMessage(errors: ApiError[]): string {
    if (errors.length === 0) {
        return 'An unexpected error occurred.';
    }
    return getErrorMessage(errors[0]);
}
