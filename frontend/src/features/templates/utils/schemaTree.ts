import type {
    DraftFieldTreeNode,
    DraftTemplateField,
    FieldTreeNode,
    TemplateField,
    TemplateMapping,
} from '@/features/templates/types/template.types';

function createClientId(): string {
    return crypto.randomUUID();
}

export function buildFieldTree(fields: TemplateField[]): FieldTreeNode[] {
    const byParent = new Map<string | null, TemplateField[]>();

    for (const field of fields) {
        const parentKey = field.parentFieldName ?? null;
        const siblings = byParent.get(parentKey) ?? [];
        siblings.push(field);
        byParent.set(parentKey, siblings);
    }

    for (const siblings of byParent.values()) {
        siblings.sort((a, b) => a.displayOrder - b.displayOrder);
    }

    function build(parentFieldName: string | null): FieldTreeNode[] {
        const siblings = byParent.get(parentFieldName) ?? [];
        return siblings.map((field) => ({
            field,
            children: build(field.fieldName),
        }));
    }

    return build(null);
}

export function buildDraftFieldTree(fields: DraftTemplateField[]): DraftFieldTreeNode[] {
    const byParent = new Map<string | null, DraftTemplateField[]>();

    for (const field of fields) {
        const parentKey = field.parentClientId ?? null;
        const siblings = byParent.get(parentKey) ?? [];
        siblings.push(field);
        byParent.set(parentKey, siblings);
    }

    for (const siblings of byParent.values()) {
        siblings.sort((a, b) => a.displayOrder - b.displayOrder);
    }

    function build(parentClientId: string | null): DraftFieldTreeNode[] {
        const siblings = byParent.get(parentClientId) ?? [];
        return siblings.map((field) => ({
            field,
            children: build(field.clientId),
        }));
    }

    return build(null);
}

export function toDraftFields(fields: TemplateField[]): DraftTemplateField[] {
    const drafts: DraftTemplateField[] = fields.map((field) => ({
        ...field,
        clientId: createClientId(),
        parentClientId: null,
    }));

    const byName = new Map<string, DraftTemplateField[]>();
    for (const draft of drafts) {
        const siblings = byName.get(draft.fieldName) ?? [];
        siblings.push(draft);
        byName.set(draft.fieldName, siblings);
    }

    for (const draft of drafts) {
        if (!draft.parentFieldName) {
            continue;
        }
        draft.parentClientId = byName.get(draft.parentFieldName)?.[0]?.clientId ?? null;
    }

    return drafts;
}

export function toApiFields(fields: DraftTemplateField[]): TemplateField[] {
    return fields.map(({ clientId: _clientId, parentClientId: _parentClientId, ...field }) => {
        void _clientId;
        void _parentClientId;
        return field;
    });
}

/** Canonical serialized form for dirty-checking and post-save baseline. */
export function serializeSchemaState(fields: DraftTemplateField[], mappings: TemplateMapping[]): string {
    return JSON.stringify({
        fields: toApiFields(normalizeDraftSchemaFields(fields)),
        mappings,
    });
}

export function findDuplicateFieldNames(fields: DraftTemplateField[]): string[] {
    const seen = new Set<string>();
    const duplicates = new Set<string>();

    for (const field of fields) {
        if (seen.has(field.fieldName)) {
            duplicates.add(field.fieldName);
        }
        seen.add(field.fieldName);
    }

    return [...duplicates];
}

export function flattenFieldTree(nodes: FieldTreeNode[]): TemplateField[] {
    const result: TemplateField[] = [];

    function walk(nodeList: FieldTreeNode[]) {
        for (const node of nodeList) {
            result.push(node.field);
            walk(node.children);
        }
    }

    walk(nodes);
    return result;
}

export function getSiblingFields(fields: TemplateField[], field: TemplateField): TemplateField[] {
    return fields
        .filter((item) => (item.parentFieldName ?? null) === (field.parentFieldName ?? null))
        .sort((a, b) => a.displayOrder - b.displayOrder);
}

export function getDraftSiblingFields(fields: DraftTemplateField[], field: DraftTemplateField): DraftTemplateField[] {
    return fields
        .filter((item) => (item.parentClientId ?? null) === (field.parentClientId ?? null))
        .sort((a, b) => a.displayOrder - b.displayOrder);
}

export function reorderSibling(
    fields: TemplateField[],
    fieldName: string,
    direction: 'up' | 'down',
): TemplateField[] {
    const target = fields.find((field) => field.fieldName === fieldName);
    if (!target) {
        return fields;
    }

    const siblings = getSiblingFields(fields, target);
    const index = siblings.findIndex((field) => field.fieldName === fieldName);
    const swapIndex = direction === 'up' ? index - 1 : index + 1;
    if (swapIndex < 0 || swapIndex >= siblings.length) {
        return fields;
    }

    const reordered = [...siblings];
    [reordered[index], reordered[swapIndex]] = [reordered[swapIndex], reordered[index]];

    const orderMap = new Map(reordered.map((field, orderIndex) => [field.fieldName, orderIndex + 1]));

    return fields.map((field) => {
        const nextOrder = orderMap.get(field.fieldName);
        return nextOrder ? { ...field, displayOrder: nextOrder } : field;
    });
}

export function reorderDraftSibling(
    fields: DraftTemplateField[],
    clientId: string,
    direction: 'up' | 'down',
): DraftTemplateField[] {
    const target = fields.find((field) => field.clientId === clientId);
    if (!target) {
        return fields;
    }

    const siblings = getDraftSiblingFields(fields, target);
    const index = siblings.findIndex((field) => field.clientId === clientId);
    const swapIndex = direction === 'up' ? index - 1 : index + 1;
    if (swapIndex < 0 || swapIndex >= siblings.length) {
        return fields;
    }

    const reordered = [...siblings];
    [reordered[index], reordered[swapIndex]] = [reordered[swapIndex], reordered[index]];

    const orderMap = new Map(reordered.map((field, orderIndex) => [field.clientId, orderIndex + 1]));

    return fields.map((field) => {
        const nextOrder = orderMap.get(field.clientId);
        return nextOrder ? { ...field, displayOrder: nextOrder } : field;
    });
}

export function removeFieldAndDescendants(fields: TemplateField[], fieldName: string): TemplateField[] {
    const toRemove = new Set<string>();

    function collect(name: string) {
        toRemove.add(name);
        for (const field of fields) {
            if (field.parentFieldName === name) {
                collect(field.fieldName);
            }
        }
    }

    collect(fieldName);
    return fields.filter((field) => !toRemove.has(field.fieldName));
}

export function removeDraftFieldAndDescendants(fields: DraftTemplateField[], clientId: string): DraftTemplateField[] {
    const toRemove = new Set<string>();

    function collect(id: string) {
        toRemove.add(id);
        for (const field of fields) {
            if (field.parentClientId === id) {
                collect(field.clientId);
            }
        }
    }

    collect(clientId);
    return fields.filter((field) => !toRemove.has(field.clientId));
}

export function createEmptyField(
    parentClientId: string | null,
    parentFieldName: string | null,
    displayOrder: number,
): DraftTemplateField {
    const isRootContainer = parentClientId === null;

    return {
        clientId: createClientId(),
        parentClientId,
        fieldName: '',
        parentFieldName,
        xmlName: '',
        displayName: '',
        nodeType: isRootContainer ? 'GROUP' : 'ELEMENT',
        valueType: isRootContainer ? null : 'STRING',
        sourceType: isRootContainer ? null : 'INPUT',
        occurrenceRule: isRootContainer ? 'ONE_OR_MORE' : null,
        emptyHandling: 'REQUIRED',
        requiredWhenParentExists: false,
        triggerActivation: null,
        defaultValue: null,
        staticValue: null,
        xmlPath: null,
        namespace: null,
        displayOrder,
        description: null,
    };
}

export function normalizeDraftFieldMetadata(field: DraftTemplateField): DraftTemplateField {
    const withNames: DraftTemplateField = {
        ...field,
        xmlName: field.fieldName,
        displayName: field.fieldName,
    };

    if (withNames.nodeType === 'GROUP') {
        return {
            ...withNames,
            sourceType: null,
            valueType: null,
            staticValue: null,
            occurrenceRule: withNames.occurrenceRule ?? 'ONE_OR_MORE',
        };
    }

    return withNames;
}

/** Apply INPUT defaults when a field becomes a value node (e.g. GROUP → ELEMENT). */
export function applyValueNodeDefaults(field: DraftTemplateField): DraftTemplateField {
    if (field.nodeType === 'GROUP') {
        return normalizeDraftFieldMetadata(field);
    }

    return normalizeDraftFieldMetadata({
        ...field,
        sourceType: field.sourceType ?? 'INPUT',
        valueType: field.valueType ?? 'STRING',
    });
}

export function normalizeAllDraftFieldMetadata(fields: DraftTemplateField[]): DraftTemplateField[] {
    const parentClientIdsWithChildren = new Set(
        fields.map((field) => field.parentClientId).filter((clientId): clientId is string => clientId != null),
    );

    return fields.map((field) => {
        const hasChildren = parentClientIdsWithChildren.has(field.clientId);

        if (hasChildren && field.nodeType !== 'GROUP') {
            return normalizeDraftFieldMetadata({ ...field, nodeType: 'GROUP' });
        }

        if (field.nodeType === 'GROUP') {
            return normalizeDraftFieldMetadata(field);
        }

        if (field.sourceType == null) {
            return applyValueNodeDefaults(field);
        }

        return normalizeDraftFieldMetadata(field);
    });
}

export function normalizeSchemaFields(fields: TemplateField[]): TemplateField[] {
    const grouped = new Map<string | null, TemplateField[]>();

    for (const field of fields) {
        const key = field.parentFieldName ?? null;
        const siblings = grouped.get(key) ?? [];
        siblings.push(field);
        grouped.set(key, siblings);
    }

    const normalized: TemplateField[] = [];

    for (const siblings of grouped.values()) {
        siblings
            .sort((a, b) => a.displayOrder - b.displayOrder)
            .forEach((field, index) => {
                normalized.push({ ...field, displayOrder: index + 1 });
            });
    }

    return normalized;
}

export function normalizeDraftSchemaFields(fields: DraftTemplateField[]): DraftTemplateField[] {
    const grouped = new Map<string | null, DraftTemplateField[]>();

    for (const field of fields) {
        const key = field.parentClientId ?? null;
        const siblings = grouped.get(key) ?? [];
        siblings.push(field);
        grouped.set(key, siblings);
    }

    const normalized: DraftTemplateField[] = [];

    for (const siblings of grouped.values()) {
        siblings
            .sort((a, b) => a.displayOrder - b.displayOrder)
            .forEach((field, index) => {
                normalized.push({ ...field, displayOrder: index + 1 });
            });
    }

    return normalized;
}
