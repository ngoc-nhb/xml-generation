export type TemplateStatus = 'ACTIVE' | 'INACTIVE';

export type TemplateFieldNodeType = 'GROUP' | 'ELEMENT' | 'ATTRIBUTE';

export type TemplateFieldValueType =
    | 'STRING'
    | 'INTEGER'
    | 'LONG'
    | 'DECIMAL'
    | 'BOOLEAN'
    | 'DATE'
    | 'DATETIME';

export type TemplateFieldSourceType = 'INPUT' | 'MASTER_DATA' | 'STATIC';

export type TemplateFieldOccurrenceRule = 'ONE_OR_MORE' | 'ZERO_OR_MORE' | 'ZERO_OR_ONE';

export type TemplateFieldEmptyHandling = 'REQUIRED' | 'OMIT_IF_EMPTY' | 'EMPTY_TAG_IF_EMPTY' | 'ZERO_IF_EMPTY';

export interface TemplateListItem {
    id: number;
    code: string;
    name: string;
    description: string | null;
    status: TemplateStatus;
    createdAt: string;
    updatedAt: string;
}

export interface TemplateField {
    fieldName: string;
    parentFieldName: string | null;
    xmlName: string;
    displayName: string | null;
    nodeType: TemplateFieldNodeType;
    valueType: TemplateFieldValueType | null;
    sourceType: TemplateFieldSourceType | null;
    occurrenceRule: TemplateFieldOccurrenceRule | null;
    emptyHandling: TemplateFieldEmptyHandling;
    requiredWhenParentExists: boolean | null;
    triggerActivation: boolean | null;
    defaultValue: string | null;
    staticValue: string | null;
    xmlPath: string | null;
    namespace: string | null;
    displayOrder: number;
    description: string | null;
}

/** Editor-only stable identity; stripped before API save. */
export interface DraftTemplateField extends TemplateField {
    clientId: string;
    parentClientId: string | null;
    /** True when the field originated from XML import. */
    imported?: boolean;
}

export interface TemplateImportDraft {
    suggestedCode: string;
    suggestedName: string;
    sourceFileName: string;
    fields: Array<TemplateField & { imported: boolean }>;
}

export interface TemplateMapping {
    fieldName: string;
    masterDataFieldId: number | null;
}

export interface TemplateSchema {
    version: number | null;
    fields: TemplateField[];
    mappings: TemplateMapping[];
}

export interface TemplateDetail {
    id: number;
    code: string;
    name: string;
    description: string | null;
    status: TemplateStatus;
    createdAt: string;
    updatedAt: string;
    schema: TemplateSchema | null;
}

export interface CreateTemplateRequest {
    code: string;
    name: string;
    description?: string | null;
    schema?: TemplateSchema | null;
}

export interface UpdateTemplateRequest {
    name: string;
    description?: string | null;
    status: TemplateStatus;
}

export interface UpdateTemplateSchemaRequest {
    version: number | null;
    fields: TemplateField[];
    mappings: TemplateMapping[];
}

export interface TemplateListParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
    status?: TemplateStatus;
}

export interface CreateTemplateResponse {
    id: number;
}

export interface FieldTreeNode {
    field: TemplateField;
    children: FieldTreeNode[];
}

export interface DraftFieldTreeNode {
    field: DraftTemplateField;
    children: DraftFieldTreeNode[];
}
