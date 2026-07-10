import type { MasterDataTypeListItem } from '@/features/master-data/types/master-data.types';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';

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
    return selections
        .filter((entry) => !(entry.groupFieldName === groupFieldName && entry.occurrenceIndex === removedIndex))
        .map((entry) =>
            entry.groupFieldName === groupFieldName && entry.occurrenceIndex > removedIndex
                ? { ...entry, occurrenceIndex: entry.occurrenceIndex - 1 }
                : entry,
        );
}
