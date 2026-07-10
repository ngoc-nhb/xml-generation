import type { FieldTreeNode, TemplateField } from '@/features/templates/types/template.types';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import {
    isRepeatableOccurrence,
    isSchemaContainerField,
    unwrapRootInputScope,
} from '@/features/xml-generation/utils/inputFormSchema';

export interface TemplateCompileMapping {
    fieldName: string;
    masterDataTypeCode: string;
    masterDataFieldName: string;
}

function asObject(value: unknown): Record<string, unknown> {
    if (value !== null && typeof value === 'object' && !Array.isArray(value)) {
        return value as Record<string, unknown>;
    }
    return {};
}

function normalizeToArray(value: unknown): Record<string, unknown>[] {
    if (Array.isArray(value)) {
        return value.filter((item) => typeof item === 'object' && item !== null && !Array.isArray(item)) as Record<
            string,
            unknown
        >[];
    }
    if (value !== null && typeof value === 'object') {
        return [value as Record<string, unknown>];
    }
    return [];
}

function setTypeField(
    scope: Record<string, unknown>,
    typeCode: string,
    fieldName: string,
    value: unknown,
): void {
    const existing = scope[typeCode];
    const typeObject =
        existing !== null && typeof existing === 'object' && !Array.isArray(existing)
            ? { ...(existing as Record<string, unknown>) }
            : {};
    typeObject[fieldName] = value;
    scope[typeCode] = typeObject;
}

function processMasterDataFields(
    node: FieldTreeNode,
    inputScope: Record<string, unknown>,
    masterScope: Record<string, unknown>,
    mappingByFieldName: Map<string, TemplateCompileMapping>,
): void {
    const { field, children } = node;

    if (field.nodeType !== 'GROUP') {
        if (field.sourceType !== 'MASTER_DATA') {
            return;
        }
        const mapping = mappingByFieldName.get(field.fieldName);
        if (!mapping) {
            return;
        }
        const raw = inputScope[field.fieldName];
        if (raw === undefined || raw === null) {
            return;
        }
        setTypeField(masterScope, mapping.masterDataTypeCode, mapping.masterDataFieldName, raw);
        return;
    }

    const groupInput = inputScope[field.fieldName];

    if (isRepeatableOccurrence(field.occurrenceRule)) {
        const occurrences = normalizeToArray(groupInput);
        const masterOccurrences: Record<string, unknown>[] = [];

        for (const occurrenceInput of occurrences) {
            const occurrenceScope: Record<string, unknown> = {};
            for (const child of children) {
                processMasterDataFields(child, occurrenceInput, occurrenceScope, mappingByFieldName);
            }
            masterOccurrences.push(occurrenceScope);
        }

        if (masterOccurrences.some((occurrence) => Object.keys(occurrence).length > 0)) {
            masterScope[field.fieldName] = masterOccurrences;
        }
        return;
    }

    const groupInputObject = asObject(groupInput);
    for (const child of children) {
        processMasterDataFields(child, groupInputObject, masterScope, mappingByFieldName);
    }
}

/**
 * Builds expanded selectedMasterData from imported input values and template mappings.
 *
 * Repeatable groups receive per-occurrence master data scopes keyed by the group field name,
 * matching ValueResolutionServiceImpl.
 */
export function buildImportedSelectedMasterData(
    fields: TemplateField[],
    compileMappings: TemplateCompileMapping[],
    importedInputData: Record<string, unknown>,
): Record<string, unknown> {
    if (compileMappings.length === 0) {
        return {};
    }

    const mappingByFieldName = new Map(compileMappings.map((mapping) => [mapping.fieldName, mapping]));
    const tree = buildFieldTree(fields);
    const inputScope = unwrapRootInputScope(fields, importedInputData);
    const result: Record<string, unknown> = {};

    for (const root of tree) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                processMasterDataFields(child, inputScope, result, mappingByFieldName);
            }
            continue;
        }
        processMasterDataFields(root, inputScope, result, mappingByFieldName);
    }

    return result;
}

export function applyPickerSelectionsToMasterData(
    base: Record<string, unknown>,
    selections: SelectedMasterDataEntry[],
): Record<string, unknown> {
    const result = { ...base };
    for (const entry of selections) {
        if (entry.recordId > 0) {
            result[entry.typeCode] = { id: entry.recordId };
        }
    }
    return result;
}
