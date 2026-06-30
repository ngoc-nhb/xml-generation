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
