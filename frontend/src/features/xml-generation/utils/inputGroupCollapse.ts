import type { FieldTreeNode, TemplateField } from '@/features/templates/types/template.types';
import { isRepeatableOccurrence, isSchemaContainerField } from '@/features/xml-generation/utils/inputFormSchema';

/** Collects stable keys for every collapsible group node in the input form tree. */
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

export function buildInputGroupOpenState(keys: string[], open: boolean): Record<string, boolean> {
    return Object.fromEntries(keys.map((key) => [key, open]));
}

export function isInputGroupContainer(node: FieldTreeNode, fields: TemplateField[]): boolean {
    return isSchemaContainerField(node.field, fields) || node.children.length > 0;
}

export function isInputRepeatableGroup(node: FieldTreeNode): boolean {
    return isRepeatableOccurrence(node.field.occurrenceRule);
}
