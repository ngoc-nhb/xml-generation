import type { MasterDataTypeListItem } from '@/features/master-data/types/master-data.types';
import type { TemplateField } from '@/features/templates/types/template.types';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import { isSchemaContainerField, type FormObject } from '@/features/xml-generation/utils/inputFormSchema';

/** A Master Data type required by the template schema, scoped to the repeatable group that owns it (if any). */
export interface MasterDataTypeContext {
    type: MasterDataTypeListItem;
    groupFieldName: string | null;
}

/**
 * The repeatable group may sit at any depth of the form data tree (e.g.
 * `GameSchedule.GameCategory`), so a top-level key lookup is not enough.
 * Field names are unique within a template schema, so the first key match found
 * while walking nested objects is the group.
 */
function findGroupValue(scope: unknown, groupFieldName: string): unknown {
    if (scope === null || typeof scope !== 'object') {
        return undefined;
    }
    if (Array.isArray(scope)) {
        for (const item of scope) {
            const found = findGroupValue(item, groupFieldName);
            if (found !== undefined) {
                return found;
            }
        }
        return undefined;
    }
    const record = scope as Record<string, unknown>;
    if (groupFieldName in record) {
        return record[groupFieldName];
    }
    for (const value of Object.values(record)) {
        const found = findGroupValue(value, groupFieldName);
        if (found !== undefined) {
            return found;
        }
    }
    return undefined;
}

/**
 * Number of Master Data sections `groupFieldName` currently needs, derived from the live
 * input form data — not the schema — so it tracks add/remove/duplicate of group instances
 * automatically. `null` (template-level, not inside a repeatable group) always needs exactly 1.
 */
export function countGroupOccurrences(
    formData: Record<string, unknown>,
    groupFieldName: string | null,
): number {
    if (groupFieldName === null) {
        return 1;
    }
    const raw = findGroupValue(formData, groupFieldName);
    return Array.isArray(raw) ? raw.length : 0;
}

/** Expands type contexts into one default (unselected) entry per required occurrence. */
export function buildDefaultSelections(
    contexts: MasterDataTypeContext[],
    formData: Record<string, unknown>,
): SelectedMasterDataEntry[] {
    const result: SelectedMasterDataEntry[] = [];
    for (const { type, groupFieldName } of contexts) {
        const count = countGroupOccurrences(formData, groupFieldName);
        for (let occurrenceIndex = 0; occurrenceIndex < count; occurrenceIndex += 1) {
            result.push({
                typeId: type.id,
                typeCode: type.code,
                typeName: type.name,
                recordId: 0,
                recordLabel: '',
                groupFieldName,
                occurrenceIndex,
            });
        }
    }
    return result;
}

export function findSelectionEntry(
    selections: SelectedMasterDataEntry[],
    typeId: number,
    groupFieldName: string | null,
    occurrenceIndex: number,
): SelectedMasterDataEntry | undefined {
    return selections.find(
        (entry) =>
            entry.typeId === typeId &&
            entry.groupFieldName === groupFieldName &&
            entry.occurrenceIndex === occurrenceIndex,
    );
}

/** Replaces the entry matching (typeId, groupFieldName, occurrenceIndex); appends if absent. Every other entry is left untouched, so updating one occurrence never affects another. */
export function upsertSelectionEntry(
    selections: SelectedMasterDataEntry[],
    entry: SelectedMasterDataEntry,
): SelectedMasterDataEntry[] {
    const index = selections.findIndex(
        (item) =>
            item.typeId === entry.typeId &&
            item.groupFieldName === entry.groupFieldName &&
            item.occurrenceIndex === entry.occurrenceIndex,
    );
    if (index === -1) {
        return [...selections, entry];
    }
    return selections.map((item, itemIndex) => (itemIndex === index ? entry : item));
}

/**
 * Repeated-group selections are keyed by array index, but `formData` array indices shift
 * whenever an occurrence is removed from the middle. Call this alongside the corresponding
 * `formData` update so remaining occurrences keep pointing at their own selection instead of
 * silently inheriting whatever used to sit at the same index:
 * - the removed occurrence's own entries are dropped
 * - every later occurrence's entries shift down by one
 * - entries for every other group / unaffected occurrence are left untouched
 */
export function removeGroupOccurrenceAndReindex(
    selections: SelectedMasterDataEntry[],
    groupFieldName: string,
    removedIndex: number,
): SelectedMasterDataEntry[] {
    return removeGroupOccurrencesAndReindex(selections, groupFieldName, [removedIndex]);
}

/** Remove multiple occurrences at once, then compact remaining indices. */
export function removeGroupOccurrencesAndReindex(
    selections: SelectedMasterDataEntry[],
    groupFieldName: string,
    removedIndices: number[],
): SelectedMasterDataEntry[] {
    if (removedIndices.length === 0) {
        return selections;
    }
    const removed = new Set(removedIndices);
    return selections
        .filter(
            (entry) =>
                !(entry.groupFieldName === groupFieldName && removed.has(entry.occurrenceIndex)),
        )
        .map((entry) => {
            if (entry.groupFieldName !== groupFieldName) {
                return entry;
            }
            let shift = 0;
            for (const index of removed) {
                if (index < entry.occurrenceIndex) {
                    shift += 1;
                }
            }
            return shift === 0 ? entry : { ...entry, occurrenceIndex: entry.occurrenceIndex - shift };
        });
}

/**
 * After duplicating selected occurrences (inserted after the last selected index),
 * copy Master Data bindings for each new occurrence and shift later indices.
 *
 * Example: selected [1,2,4], copies=2 → insert at 5:
 * new indices 5,6 get copies of 1,2,4 then 7,8 get another round.
 */
export function duplicateGroupOccurrencesAndReindex(
    selections: SelectedMasterDataEntry[],
    groupFieldName: string,
    selectedIndices: number[],
    copies: number,
): SelectedMasterDataEntry[] {
    if (selectedIndices.length === 0 || copies <= 0) {
        return selections;
    }

    const ordered = [...selectedIndices].sort((a, b) => a - b);
    const insertAt = ordered[ordered.length - 1]! + 1;
    const insertedCount = ordered.length * copies;

    const shifted = selections.map((entry) =>
        entry.groupFieldName === groupFieldName && entry.occurrenceIndex >= insertAt
            ? { ...entry, occurrenceIndex: entry.occurrenceIndex + insertedCount }
            : entry,
    );

    const created: SelectedMasterDataEntry[] = [];
    for (let copy = 0; copy < copies; copy += 1) {
        ordered.forEach((sourceIndex, offset) => {
            const newIndex = insertAt + copy * ordered.length + offset;
            for (const entry of selections) {
                if (entry.groupFieldName === groupFieldName && entry.occurrenceIndex === sourceIndex) {
                    created.push({ ...entry, occurrenceIndex: newIndex });
                }
            }
        });
    }

    return [...shifted, ...created];
}

function isRepeatableRule(rule: TemplateField['occurrenceRule']): boolean {
    return rule === 'ONE_OR_MORE' || rule === 'ZERO_OR_MORE';
}

/** Writes `value` at the schema path of `fieldName`, cloning only the touched branch so React state updates correctly. Returns null when the path cannot be resolved. */
function setFieldValueAtPath(
    fields: TemplateField[],
    formData: FormObject,
    fieldName: string,
    groupFieldName: string | null,
    occurrenceIndex: number,
    value: unknown,
): FormObject | null {
    const byName = new Map(fields.map((field) => [field.fieldName, field]));
    const field = byName.get(fieldName);
    if (!field) {
        return null;
    }

    const ancestors: TemplateField[] = [];
    let current: TemplateField | undefined = field;
    while (current?.parentFieldName) {
        const parent = byName.get(current.parentFieldName);
        if (!parent) {
            return null;
        }
        ancestors.unshift(parent);
        current = parent;
    }

    // resolveInitialFormData unwraps a single root container, so drop it from the path too.
    const roots = fields.filter((item) => !item.parentFieldName);
    if (
        ancestors.length > 0 &&
        roots.length === 1 &&
        isSchemaContainerField(roots[0], fields) &&
        ancestors[0].fieldName === roots[0].fieldName
    ) {
        ancestors.shift();
    }

    const result: FormObject = { ...formData };
    let scope: FormObject = result;
    for (const ancestor of ancestors) {
        const key = ancestor.fieldName;
        if (isRepeatableRule(ancestor.occurrenceRule)) {
            // Only the occurrence the user picked for may be written; a repeatable ancestor
            // other than the selection's own group would make the target occurrence ambiguous.
            if (ancestor.fieldName !== groupFieldName) {
                return null;
            }
            const items = scope[key];
            if (!Array.isArray(items) || occurrenceIndex >= items.length) {
                return null;
            }
            const nextItems = [...items];
            nextItems[occurrenceIndex] = { ...nextItems[occurrenceIndex] };
            scope[key] = nextItems;
            scope = nextItems[occurrenceIndex];
            continue;
        }

        const child = scope[key];
        const nextChild: FormObject =
            child !== null && typeof child === 'object' && !Array.isArray(child) ? { ...child } : {};
        scope[key] = nextChild;
        scope = nextChild;
    }

    scope[field.fieldName] = value === null || value === undefined ? '' : (value as FormObject[string]);
    return result;
}

/**
 * Applies a picked Master Data record's values onto the input form data, so the mapped
 * MASTER_DATA fields (e.g. SeasonID/SeasonName) update live in the Input Data panel the
 * moment a record is selected — scoped to the one group occurrence the pick belongs to.
 */
export function applyMasterDataRecordToFormData(
    fields: TemplateField[],
    formData: FormObject,
    mappings: Array<{ fieldName: string; masterDataFieldName: string }>,
    entry: Pick<SelectedMasterDataEntry, 'groupFieldName' | 'occurrenceIndex'>,
    recordData: Record<string, unknown>,
): FormObject {
    let next = formData;
    for (const mapping of mappings) {
        const value = recordData[mapping.masterDataFieldName];
        const updated = setFieldValueAtPath(
            fields,
            next,
            mapping.fieldName,
            entry.groupFieldName,
            entry.occurrenceIndex,
            value,
        );
        if (updated) {
            next = updated;
        }
    }
    return next;
}
