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
    return fields.map(({ clientId: _clientId, parentClientId: _parentClientId, imported: _imported, ...field }) => {
        void _clientId;
        void _parentClientId;
        void _imported;
        return field;
    });
}

export function toDraftFieldsFromImport(
    fields: Array<TemplateField & { imported?: boolean }>,
): DraftTemplateField[] {
    const drafts: DraftTemplateField[] = fields.map((field) => ({
        ...field,
        clientId: createClientId(),
        parentClientId: null,
        imported: field.imported ?? true,
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
        const parentCandidates = byName.get(draft.parentFieldName) ?? [];
        const parent = parentCandidates[0];
        if (parent) {
            draft.parentClientId = parent.clientId;
        }
    }

    return drafts;
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

    return reorderDraftSiblingToIndex(fields, clientId, swapIndex);
}

export function reorderDraftSiblingToIndex(
    fields: DraftTemplateField[],
    clientId: string,
    newIndex: number,
): DraftTemplateField[] {
    const target = fields.find((field) => field.clientId === clientId);
    if (!target) {
        return fields;
    }

    const siblings = getDraftSiblingFields(fields, target);
    const oldIndex = siblings.findIndex((field) => field.clientId === clientId);
    if (oldIndex < 0 || oldIndex === newIndex || newIndex < 0 || newIndex >= siblings.length) {
        return fields;
    }

    const reordered = [...siblings];
    const [moved] = reordered.splice(oldIndex, 1);
    reordered.splice(newIndex, 0, moved);

    const orderMap = new Map(reordered.map((field, orderIndex) => [field.clientId, orderIndex + 1]));

    return normalizeDraftSchemaFields(
        fields.map((field) => {
            const nextOrder = orderMap.get(field.clientId);
            return nextOrder ? { ...field, displayOrder: nextOrder } : field;
        }),
    );
}

function generateUniqueFieldName(baseName: string, existingNames: Set<string>): string {
    const normalizedBase = baseName.trim() || 'Field';
    let candidate = `${normalizedBase}_copy`;
    let counter = 2;
    while (existingNames.has(candidate)) {
        candidate = `${normalizedBase}_copy${counter}`;
        counter += 1;
    }
    return candidate;
}

function collectDraftSubtree(fields: DraftTemplateField[], rootClientId: string): DraftTemplateField[] {
    const collected: DraftTemplateField[] = [];

    function walk(clientId: string) {
        const node = fields.find((field) => field.clientId === clientId);
        if (!node) {
            return;
        }
        collected.push(node);
        const children = fields
            .filter((field) => field.parentClientId === clientId)
            .sort((a, b) => a.displayOrder - b.displayOrder);
        for (const child of children) {
            walk(child.clientId);
        }
    }

    walk(rootClientId);
    return collected;
}

export function duplicateDraftFieldSubtree(
    fields: DraftTemplateField[],
    clientId: string,
    mappings: TemplateMapping[],
): { fields: DraftTemplateField[]; mappings: TemplateMapping[]; duplicatedRootClientId: string | null } {
    const target = fields.find((field) => field.clientId === clientId);
    if (!target) {
        return { fields, mappings, duplicatedRootClientId: null };
    }

    const subtree = collectDraftSubtree(fields, clientId);
    const existingNames = new Set(fields.map((field) => field.fieldName));
    const clientIdMap = new Map<string, string>();
    const fieldNameMap = new Map<string, string>();

    for (const field of subtree) {
        clientIdMap.set(field.clientId, createClientId());
        const nextName = generateUniqueFieldName(field.fieldName, existingNames);
        fieldNameMap.set(field.fieldName, nextName);
        existingNames.add(nextName);
    }

    const clonedFields = subtree.map((field) => {
        const originalParent = field.parentClientId
            ? fields.find((item) => item.clientId === field.parentClientId)
            : null;

        return normalizeDraftFieldMetadata({
            ...field,
            clientId: clientIdMap.get(field.clientId)!,
            fieldName: fieldNameMap.get(field.fieldName)!,
            xmlName: fieldNameMap.get(field.fieldName)!,
            displayName: fieldNameMap.get(field.fieldName)!,
            parentClientId: field.clientId === clientId ? target.parentClientId : clientIdMap.get(field.parentClientId!) ?? null,
            parentFieldName:
                field.clientId === clientId
                    ? target.parentFieldName
                    : originalParent
                      ? fieldNameMap.get(originalParent.fieldName) ?? null
                      : null,
        });
    });

    const shiftedFields = fields.map((field) => {
        if (
            field.parentClientId === target.parentClientId &&
            field.displayOrder > target.displayOrder &&
            !subtree.some((item) => item.clientId === field.clientId)
        ) {
            return { ...field, displayOrder: field.displayOrder + 1 };
        }
        return field;
    });

    const rootCloneClientId = clientIdMap.get(clientId)!;
    const nextFields = normalizeDraftSchemaFields([
        ...shiftedFields,
        ...clonedFields.map((field) =>
            field.clientId === rootCloneClientId ? { ...field, displayOrder: target.displayOrder + 1 } : field,
        ),
    ]);

    const nextMappings = [...mappings];
    for (const field of subtree) {
        const mapping = mappings.find((item) => item.fieldName === field.fieldName);
        if (mapping) {
            nextMappings.push({
                ...mapping,
                fieldName: fieldNameMap.get(field.fieldName)!,
            });
        }
    }

    return { fields: nextFields, mappings: nextMappings, duplicatedRootClientId: rootCloneClientId };
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
        xmlName: field.xmlName || field.fieldName,
        displayName: field.displayName ?? field.xmlName ?? field.fieldName,
    };

    if (withNames.nodeType === 'GROUP') {
        return {
            ...withNames,
            sourceType: null,
            valueType: null,
            staticValue: null,
            // Imported fields carry the occurrenceRule inferred by the backend XML parser
            // (including `null` for containers observed exactly once) — that inference is
            // the source of truth and must not be overwritten. The `ONE_OR_MORE` default
            // below only applies to fields manually created in the Schema Editor.
            occurrenceRule: withNames.imported ? withNames.occurrenceRule : (withNames.occurrenceRule ?? 'ONE_OR_MORE'),
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
