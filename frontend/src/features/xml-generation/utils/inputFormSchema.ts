import type { FieldTreeNode, TemplateField, TemplateFieldOccurrenceRule } from '@/features/templates/types/template.types';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';

export type FormScalar = string | number | boolean | null;
export interface FormObject {
    [key: string]: FormScalar | FormObject | FormObject[];
}
export type FormArray = FormObject[];
export type FormValue = FormScalar | FormObject | FormArray;

function isRepeatable(rule: TemplateFieldOccurrenceRule | null | undefined): boolean {
    return rule === 'ZERO_OR_MORE' || rule === 'ONE_OR_MORE';
}

export function isRepeatableOccurrence(rule: TemplateFieldOccurrenceRule | null | undefined): boolean {
    return isRepeatable(rule);
}

export function formatOccurrenceHint(rule: TemplateFieldOccurrenceRule | null | undefined): string | null {
    if (rule === 'ZERO_OR_MORE') {
        return '(0..*)';
    }
    if (rule === 'ONE_OR_MORE') {
        return '(1..*)';
    }
    return null;
}

export function normalizeRepeatableItems(
    node: FieldTreeNode,
    raw: FormValue | undefined,
): FormObject[] {
    const items = Array.isArray(raw) ? raw.filter((item) => typeof item === 'object' && item !== null && !Array.isArray(item)) : [];
    if (node.field.occurrenceRule === 'ONE_OR_MORE' && items.length === 0) {
        return [createRepeatableItemDefault(node)];
    }
    return items as FormObject[];
}

function coerceDefaultValue(raw: string, valueType: TemplateField['valueType']): FormScalar {
    switch (valueType) {
        case 'INTEGER':
        case 'LONG': {
            const parsed = Number.parseInt(raw, 10);
            return Number.isNaN(parsed) ? '' : parsed;
        }
        case 'DECIMAL': {
            const parsed = Number.parseFloat(raw);
            return Number.isNaN(parsed) ? '' : parsed;
        }
        case 'BOOLEAN':
            return raw === 'true' || raw === '1';
        default:
            return raw;
    }
}

function scalarDefault(field: TemplateField): FormScalar {
    if (field.defaultValue !== null && field.defaultValue !== '') {
        return coerceDefaultValue(field.defaultValue, field.valueType);
    }
    if (field.valueType === 'BOOLEAN') {
        return false;
    }
    if (field.valueType === 'INTEGER' || field.valueType === 'LONG' || field.valueType === 'DECIMAL') {
        return '';
    }
    return '';
}

function coerceOutputScalar(value: FormScalar, valueType: TemplateField['valueType']): unknown {
    if (value === null || value === '') {
        return value === null ? null : '';
    }
    switch (valueType) {
        case 'INTEGER':
        case 'LONG': {
            const parsed = typeof value === 'number' ? value : Number.parseInt(String(value), 10);
            return Number.isNaN(parsed) ? '' : parsed;
        }
        case 'DECIMAL': {
            const parsed = typeof value === 'number' ? value : Number.parseFloat(String(value));
            return Number.isNaN(parsed) ? '' : parsed;
        }
        case 'BOOLEAN':
            return value === true || value === 'true' || value === 1 || value === '1';
        default:
            return String(value);
    }
}

function isScalarDataField(field: TemplateField): boolean {
    return (
        (field.nodeType === 'ELEMENT' || field.nodeType === 'ATTRIBUTE') &&
        (field.sourceType === 'INPUT' || field.sourceType === 'MASTER_DATA')
    );
}

export function isFormInputField(field: TemplateField): boolean {
    return isScalarDataField(field);
}

function fieldHasChildren(field: TemplateField, fields: TemplateField[]): boolean {
    return fields.some((item) => item.parentFieldName === field.fieldName);
}

/** Container nodes hold child fields and do not take input values themselves. */
export function isSchemaContainerField(field: TemplateField, fields: TemplateField[]): boolean {
    return field.nodeType === 'GROUP' || fieldHasChildren(field, fields);
}

function isContainerNode(node: FieldTreeNode): boolean {
    return node.field.nodeType === 'GROUP' || node.children.length > 0;
}

export function buildNodeDefault(node: FieldTreeNode): FormValue | undefined {
    const { field, children } = node;

    if (isContainerNode(node)) {
        if (isRepeatable(field.occurrenceRule)) {
            if (field.occurrenceRule === 'ONE_OR_MORE') {
                return [createRepeatableItemDefault(node)];
            }
            return [];
        }
        const object: FormObject = {};
        for (const child of children) {
            const childDefault = buildNodeDefault(child);
            if (childDefault !== undefined) {
                object[child.field.fieldName] = childDefault;
            }
        }
        return object;
    }

    if (!isScalarDataField(field)) {
        return undefined;
    }

    return scalarDefault(field);
}

export function createRepeatableItemDefault(node: FieldTreeNode): FormObject {
    const object: FormObject = {};
    for (const child of node.children) {
        const childDefault = buildNodeDefault(child);
        if (childDefault !== undefined) {
            object[child.field.fieldName] = childDefault;
        }
    }
    return object;
}

export function buildDefaultFormData(fields: TemplateField[]): FormObject {
    const tree = buildFieldTree(fields);
    const result: FormObject = {};

    for (const root of tree) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                const value = buildNodeDefault(child);
                if (value !== undefined) {
                    result[child.field.fieldName] = value;
                }
            }
            continue;
        }

        const value = buildNodeDefault(root);
        if (value !== undefined) {
            result[root.field.fieldName] = value;
        }
    }

    return result;
}

function mergeFormValue(base: FormValue | undefined, saved: unknown): FormValue | undefined {
    if (saved === undefined) {
        return base;
    }
    if (Array.isArray(saved)) {
        return saved as FormArray;
    }
    if (saved !== null && typeof saved === 'object') {
        const baseObject = base && typeof base === 'object' && !Array.isArray(base) ? (base as FormObject) : {};
        const savedObject = saved as Record<string, unknown>;
        const merged: FormObject = { ...baseObject };
        for (const [key, value] of Object.entries(savedObject)) {
            const next = mergeFormValue(baseObject[key], value);
            if (next !== undefined) {
                merged[key] = next;
            }
        }
        return merged;
    }
    return saved as FormScalar;
}

/**
 * Accepts both canonical input ({@code CommentReport: {...}}) and wrapped root input
 * ({@code Football: { CommentReport: {...} }}) when the template has a single root container.
 */
export function unwrapRootInputScope(
    fields: TemplateField[],
    inputData: Record<string, unknown>,
): Record<string, unknown> {
    const tree = buildFieldTree(fields);
    if (tree.length !== 1) {
        return inputData;
    }

    const root = tree[0];
    if (!isSchemaContainerField(root.field, fields)) {
        return inputData;
    }

    if (root.children.some((child) => child.field.fieldName in inputData)) {
        return inputData;
    }

    const wrapped = inputData[root.field.fieldName];
    if (wrapped !== null && typeof wrapped === 'object' && !Array.isArray(wrapped)) {
        return wrapped as Record<string, unknown>;
    }

    return inputData;
}

function normalizeRepeatableWriteItems(value: FormValue | undefined): FormObject[] {
    if (Array.isArray(value)) {
        return value.filter((item) => typeof item === 'object' && item !== null && !Array.isArray(item)) as FormObject[];
    }
    if (value !== null && typeof value === 'object') {
        return [value as FormObject];
    }
    return [];
}

function readScalarValue(raw: unknown): FormScalar {
    if (raw === null || raw === undefined) {
        return '';
    }
    if (typeof raw === 'boolean' || typeof raw === 'number') {
        return raw;
    }
    return String(raw);
}

function readNodeInput(node: FieldTreeNode, value: unknown): FormValue | undefined {
    const { field, children } = node;

    if (isContainerNode(node)) {
        if (isRepeatable(field.occurrenceRule)) {
            const items = normalizeRepeatableWriteItems(value as FormValue | undefined);
            // ONE_OR_MORE requires at least one occurrence, and the form renders a default
            // item when the array is empty — materialize that item into state too, so what
            // the user sees is exactly what preview/export serializes.
            if (field.occurrenceRule === 'ONE_OR_MORE' && items.length === 0) {
                return [createRepeatableItemDefault(node)];
            }
            return items.map((item) => readGroupObject(children, item));
        }

        if (value === null || typeof value !== 'object' || Array.isArray(value)) {
            return {};
        }
        return readGroupObject(children, value as Record<string, unknown>);
    }

    if (!isScalarDataField(field)) {
        return undefined;
    }

    return readScalarValue(value);
}

function readGroupObject(children: FieldTreeNode[], value: Record<string, unknown>): FormObject {
    const result: FormObject = {};
    for (const child of children) {
        const childValue = readNodeInput(child, value[child.field.fieldName]);
        if (childValue !== undefined) {
            result[child.field.fieldName] = childValue;
        }
    }
    return result;
}

/** Converts runtime {@code inputData} into editable form state using the template schema. */
export function inputDataToFormData(fields: TemplateField[], inputData: Record<string, unknown>): FormObject {
    const tree = buildFieldTree(fields);
    const inputScope = unwrapRootInputScope(fields, inputData);
    const result: FormObject = {};

    for (const root of tree) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                const value = readNodeInput(child, inputScope[child.field.fieldName]);
                if (value !== undefined) {
                    result[child.field.fieldName] = value;
                }
            }
            continue;
        }

        const value = readNodeInput(root, inputScope[root.field.fieldName]);
        if (value !== undefined) {
            result[root.field.fieldName] = value;
        }
    }

    return result;
}

/**
 * Resolves the initial form state using initialization priority:
 * Saved Input → Imported Base Data → Empty Template defaults.
 */
export function resolveInitialFormData(
    fields: TemplateField[],
    options: {
        savedInputData?: Record<string, unknown> | null;
        importedBaseData?: Record<string, unknown> | null;
    },
): FormObject {
    if (options.savedInputData) {
        return inputDataToFormData(fields, options.savedInputData);
    }
    if (options.importedBaseData && Object.keys(options.importedBaseData).length > 0) {
        return inputDataToFormData(fields, options.importedBaseData);
    }
    return buildDefaultFormData(fields);
}

/** Saved input values override template defaults; missing keys keep defaults. */
export function mergeSavedInputIntoFormData(
    defaults: FormObject,
    savedInputData: Record<string, unknown>,
): FormObject {
    const merged: FormObject = { ...defaults };
    for (const [key, value] of Object.entries(savedInputData)) {
        const next = mergeFormValue(defaults[key], value);
        if (next !== undefined) {
            merged[key] = next;
        }
    }
    return merged;
}

function writeNodeOutput(node: FieldTreeNode, value: FormValue | undefined): unknown {
    const { field, children } = node;

    if (isContainerNode(node)) {
        if (isRepeatable(field.occurrenceRule)) {
            const items = normalizeRepeatableWriteItems(value);
            return items.map((item) => writeGroupObject(children, item));
        }

        if (!value || typeof value !== 'object' || Array.isArray(value)) {
            return {};
        }
        return writeGroupObject(children, value);
    }

    if (!isScalarDataField(field)) {
        return undefined;
    }

    return coerceOutputScalar((value ?? '') as FormScalar, field.valueType);
}

function writeGroupObject(children: FieldTreeNode[], value: FormObject): Record<string, unknown> {
    const result: Record<string, unknown> = {};
    for (const child of children) {
        const childValue = value[child.field.fieldName];
        const output = writeNodeOutput(child, childValue);
        if (output !== undefined) {
            result[child.field.fieldName] = output;
        }
    }
    return result;
}

export function formDataToInputData(fields: TemplateField[], formData: FormObject): Record<string, unknown> {
    const tree = buildFieldTree(fields);
    const result: Record<string, unknown> = {};

    for (const root of tree) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                const output = writeNodeOutput(child, formData[child.field.fieldName]);
                if (output !== undefined) {
                    result[child.field.fieldName] = output;
                }
            }
            continue;
        }

        const output = writeNodeOutput(root, formData[root.field.fieldName]);
        if (output !== undefined) {
            result[root.field.fieldName] = output;
        }
    }

    return result;
}

export function serializeFormState(fields: TemplateField[], formData: FormObject): string {
    return JSON.stringify(formDataToInputData(fields, formData));
}

export function countInputFields(fields: TemplateField[]): number {
    return listInputFields(fields).length;
}

export function listInputFields(fields: TemplateField[]): TemplateField[] {
    return fields.filter(isScalarDataField).sort((a, b) => a.displayOrder - b.displayOrder);
}

export function buildDefaultFlatFormData(fields: TemplateField[]): Record<string, FormScalar> {
    const result: Record<string, FormScalar> = {};
    for (const field of listInputFields(fields)) {
        result[field.fieldName] = scalarDefault(field);
    }
    return result;
}

function assignInputValueByPath(
    result: Record<string, unknown>,
    field: TemplateField,
    fields: TemplateField[],
    byName: Map<string, TemplateField>,
    value: unknown,
): void {
    const ancestors: TemplateField[] = [];
    let current: TemplateField | undefined = field;
    while (current?.parentFieldName) {
        const parent = byName.get(current.parentFieldName);
        if (!parent) {
            break;
        }
        ancestors.unshift(parent);
        current = parent;
    }

    const roots = fields.filter((item) => !item.parentFieldName);
    if (
        ancestors.length > 0 &&
        roots.length === 1 &&
        isSchemaContainerField(roots[0], fields) &&
        ancestors[0].fieldName === roots[0].fieldName
    ) {
        ancestors.shift();
    }

    let scope: Record<string, unknown> = result;
    for (const ancestor of ancestors) {
        const key = ancestor.fieldName;
        if (isRepeatable(ancestor.occurrenceRule)) {
            if (!Array.isArray(scope[key])) {
                scope[key] = [{}];
            }
            const items = scope[key] as Record<string, unknown>[];
            if (items.length === 0) {
                items.push({});
            }
            scope = items[0] as Record<string, unknown>;
            continue;
        }

        if (typeof scope[key] !== 'object' || scope[key] === null || Array.isArray(scope[key])) {
            scope[key] = {};
        }
        scope = scope[key] as Record<string, unknown>;
    }

    scope[field.fieldName] = value;
}

export function flatFormDataToInputData(
    fields: TemplateField[],
    flat: Record<string, FormScalar>,
): Record<string, unknown> {
    const result: Record<string, unknown> = {};
    const byName = new Map(fields.map((field) => [field.fieldName, field]));

    for (const field of listInputFields(fields)) {
        const raw = flat[field.fieldName] ?? '';
        assignInputValueByPath(result, field, fields, byName, coerceOutputScalar(raw, field.valueType));
    }

    return result;
}

export function serializeFlatFormState(fields: TemplateField[], flat: Record<string, FormScalar>): string {
    return JSON.stringify(flatFormDataToInputData(fields, flat));
}
