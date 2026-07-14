import type { FieldTreeNode, TemplateField } from '@/features/templates/types/template.types';
import {
    isRepeatableOccurrence,
    isSchemaContainerField,
    normalizeRepeatableItems,
    type FormObject,
    type FormValue,
} from '@/features/xml-generation/utils/inputFormSchema';

export function itemCollapseKey(groupKey: string, index: number): string {
    return `${groupKey}[${index}]`;
}

export function childGroupKey(parentKey: string, fieldName: string): string {
    return parentKey ? `${parentKey}.${fieldName}` : fieldName;
}

/** Collects schema-level group keys (no occurrence indices). Used for template init. */
export function collectInputGroupKeys(nodes: FieldTreeNode[], fields: TemplateField[]): string[] {
    const keys: string[] = [];

    function walk(node: FieldTreeNode, parentKey: string) {
        const isContainer = isSchemaContainerField(node.field, fields) || node.children.length > 0;
        if (!isContainer) {
            return;
        }

        const key = parentKey ? `${parentKey}.${node.field.fieldName}` : node.field.fieldName;
        keys.push(key);

        for (const child of node.children) {
            walk(child, key);
        }
    }

    for (const root of nodes) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                walk(child, '');
            }
            continue;
        }
        walk(root, '');
    }

    return keys;
}

/**
 * Collects collapse keys including per-occurrence item keys from live form data.
 * Example: `ScheduleInfo`, `ScheduleInfo[0]`, `ScheduleInfo[0].Nested`, `ScheduleInfo[1]`.
 */
export function collectInstanceGroupKeys(
    nodes: FieldTreeNode[],
    fields: TemplateField[],
    formData: FormObject,
): string[] {
    const keys: string[] = [];

    function walk(node: FieldTreeNode, parentKey: string, value: FormValue | undefined) {
        const isContainer = isSchemaContainerField(node.field, fields) || node.children.length > 0;
        if (!isContainer) {
            return;
        }

        const key = childGroupKey(parentKey, node.field.fieldName);
        keys.push(key);

        if (isRepeatableOccurrence(node.field.occurrenceRule)) {
            const items = normalizeRepeatableItems(node, value);
            items.forEach((item, index) => {
                const itemKey = itemCollapseKey(key, index);
                keys.push(itemKey);
                for (const child of node.children) {
                    walk(child, itemKey, item[child.field.fieldName]);
                }
            });
            return;
        }

        const objectValue =
            typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as FormObject) : {};
        for (const child of node.children) {
            walk(child, key, objectValue[child.field.fieldName]);
        }
    }

    for (const root of nodes) {
        if (isSchemaContainerField(root.field, fields)) {
            for (const child of root.children) {
                walk(child, '', formData[child.field.fieldName]);
            }
            continue;
        }
        walk(root, '', formData[root.field.fieldName]);
    }

    return keys;
}

export function buildInputGroupOpenState(keys: string[], open: boolean): Record<string, boolean> {
    return Object.fromEntries(keys.map((key) => [key, open]));
}

/** Expand/collapse only item nodes belonging to one repeatable group. */
export function setRepeatableItemOpenState(
    current: Record<string, boolean>,
    groupKey: string,
    itemCount: number,
    open: boolean,
): Record<string, boolean> {
    const next = { ...current };
    for (let index = 0; index < itemCount; index += 1) {
        next[itemCollapseKey(groupKey, index)] = open;
    }
    return next;
}

export function isInputGroupContainer(node: FieldTreeNode, fields: TemplateField[]): boolean {
    return isSchemaContainerField(node.field, fields) || node.children.length > 0;
}

export function isInputRepeatableGroup(node: FieldTreeNode): boolean {
    return isRepeatableOccurrence(node.field.occurrenceRule);
}
