import { useMemo } from 'react';
import { X } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { useMasterDataFieldsForType, useMasterDataRecordList, useMasterDataTypeList } from '@/features/master-data';
import { formatMasterDataRecordLabel } from '@/features/xml-generation/utils/masterDataRecordLabel';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';

interface MasterDataSelectorProps {
    selections: SelectedMasterDataEntry[];
    onChange: (selections: SelectedMasterDataEntry[]) => void;
    allowedTypeIds?: number[];
    hideAddControl?: boolean;
    hideRemoveControl?: boolean;
    hideHeader?: boolean;
}

export function MasterDataSelector({
    selections,
    onChange,
    allowedTypeIds,
    hideAddControl = false,
    hideRemoveControl = false,
    hideHeader = false,
}: MasterDataSelectorProps) {
    const { data: typesData, isLoading: typesLoading } = useMasterDataTypeList({
        page: 1,
        pageSize: 100,
    });

    const types = useMemo(() => typesData?.items ?? [], [typesData?.items]);
    const visibleSelections = useMemo(() => {
        if (!allowedTypeIds || allowedTypeIds.length === 0) {
            return selections;
        }
        return selections.filter((entry) => allowedTypeIds.includes(entry.typeId));
    }, [allowedTypeIds, selections]);
    const availableTypes = types.filter(
        (type) =>
            (!allowedTypeIds || allowedTypeIds.includes(type.id)) &&
            !visibleSelections.some((entry) => entry.typeId === type.id),
    );

    function addType(typeId: number) {
        const type = types.find((item) => item.id === typeId);
        if (!type) {
            return;
        }
        onChange([
            ...selections,
            {
                typeId: type.id,
                typeCode: type.code,
                typeName: type.name,
                recordId: 0,
                recordLabel: '',
                groupFieldName: null,
                occurrenceIndex: 0,
            },
        ]);
    }

    function removeType(typeId: number) {
        onChange(selections.filter((entry) => entry.typeId !== typeId));
    }

    function updateRecord(typeId: number, recordId: number, recordLabel: string) {
        onChange(
            selections.map((entry) =>
                entry.typeId === typeId ? { ...entry, recordId, recordLabel } : entry,
            ),
        );
    }

    return (
        <div className="space-y-3">
            <div className="flex items-center justify-between gap-3">
                {hideHeader ? null : <p className="text-sm font-medium text-foreground">Master data selection</p>}
                {!hideAddControl && availableTypes.length > 0 ? (
                    <Select
                        value=""
                        disabled={typesLoading}
                        onChange={(event) => {
                            const next = event.target.value ? Number(event.target.value) : null;
                            if (next) {
                                addType(next);
                            }
                        }}
                    >
                        <option value="">Add master data type…</option>
                        {availableTypes.map((type) => (
                            <option key={type.id} value={type.id}>
                                {type.code} — {type.name}
                            </option>
                        ))}
                    </Select>
                ) : null}
            </div>
            {visibleSelections.length === 0 ? (
                <p className="text-sm text-muted-foreground">
                    {hideAddControl
                        ? 'No master data types are required for this template.'
                        : 'Optionally add master data types used by template mappings.'}
                </p>
            ) : (
                <ul className="space-y-3">
                    {visibleSelections.map((entry) => (
                        <li key={entry.typeId} className="rounded-md border border-border p-3">
                            <div className="mb-2 flex items-start justify-between gap-2">
                                <div>
                                    <p className="text-sm font-medium text-foreground">{entry.typeName}</p>
                                </div>
                                {!hideRemoveControl ? (
                                    <Button type="button" variant="ghost" size="sm" onClick={() => removeType(entry.typeId)}>
                                        <X className="h-4 w-4" />
                                    </Button>
                                ) : null}
                            </div>
                            <MasterDataRecordPicker
                                typeId={entry.typeId}
                                value={entry.recordId || null}
                                onChange={(recordId, recordLabel) => updateRecord(entry.typeId, recordId, recordLabel)}
                            />
                        </li>
                    ))}
                </ul>
            )}
        </div>
    );
}

function MasterDataRecordPicker({
    typeId,
    value,
    onChange,
}: {
    typeId: number;
    value: number | null;
    onChange: (recordId: number, recordLabel: string) => void;
}) {
    const { data: fieldsData, isLoading: fieldsLoading } = useMasterDataFieldsForType(typeId);
    const { data, isLoading: recordsLoading } = useMasterDataRecordList({ typeId, page: 1, pageSize: 500 });

    const fields = useMemo(
        () => (fieldsData?.items ?? []).slice().sort((left, right) => left.displayOrder - right.displayOrder),
        [fieldsData?.items],
    );
    const records = data?.items ?? [];
    const isLoading = fieldsLoading || recordsLoading;

    return (
        <Select
            value={value?.toString() ?? ''}
            disabled={isLoading}
            onChange={(event) => {
                const recordId = Number(event.target.value);
                const record = records.find((item) => item.id === recordId);
                const label = record ? formatMasterDataRecordLabel(fields, record.data) : '';
                onChange(recordId, label);
            }}
        >
            <option value="">{isLoading ? 'Loading records…' : 'Select a record…'}</option>
            {records.map((record) => (
                <option key={record.id} value={record.id}>
                    {formatMasterDataRecordLabel(fields, record.data)}
                </option>
            ))}
        </Select>
    );
}

function asRecordIdRef(value: unknown): { id: number } | undefined {
    if (
        value !== null &&
        typeof value === 'object' &&
        !Array.isArray(value) &&
        typeof (value as { id?: unknown }).id === 'number'
    ) {
        return value as { id: number };
    }
    return undefined;
}

/**
 * Builds the request-ready `selectedMasterData`: template-level selections (`groupFieldName === null`)
 * as a flat `{ typeCode: { id } }`, and per-occurrence selections as `{ groupFieldName: [{ typeCode: { id } }, ...] }`
 * aligned by index with `inputData[groupFieldName]` — the array shape `ValueResolutionServiceImpl` already expects.
 */
export function toSelectedMasterDataPayload(selections: SelectedMasterDataEntry[]): Record<string, unknown> {
    const payload: Record<string, unknown> = {};
    const groupedEntries = new Map<string, SelectedMasterDataEntry[]>();

    for (const entry of selections) {
        if (entry.groupFieldName === null) {
            if (entry.recordId > 0) {
                payload[entry.typeCode] = { id: entry.recordId };
            }
            continue;
        }
        const list = groupedEntries.get(entry.groupFieldName) ?? [];
        list.push(entry);
        groupedEntries.set(entry.groupFieldName, list);
    }

    for (const [groupFieldName, entries] of groupedEntries) {
        const maxIndex = entries.reduce((max, entry) => Math.max(max, entry.occurrenceIndex), -1);
        const occurrences: Record<string, unknown>[] = Array.from({ length: maxIndex + 1 }, () => ({}));
        for (const entry of entries) {
            if (entry.recordId > 0) {
                occurrences[entry.occurrenceIndex][entry.typeCode] = { id: entry.recordId };
            }
        }
        payload[groupFieldName] = occurrences;
    }

    return payload;
}

/** Restores `recordId`/`recordLabel` onto each (typeId, groupFieldName, occurrenceIndex) entry from a previously saved payload. */
export function applySavedMasterDataSelections(
    base: SelectedMasterDataEntry[],
    saved: Record<string, unknown> | null | undefined,
): SelectedMasterDataEntry[] {
    if (!saved) {
        return base;
    }
    return base.map((entry) => {
        let savedRef: unknown;
        if (entry.groupFieldName === null) {
            savedRef = saved[entry.typeCode];
        } else {
            const groupSaved = saved[entry.groupFieldName];
            const occurrence = Array.isArray(groupSaved) ? groupSaved[entry.occurrenceIndex] : undefined;
            savedRef =
                occurrence !== null && typeof occurrence === 'object' && !Array.isArray(occurrence)
                    ? (occurrence as Record<string, unknown>)[entry.typeCode]
                    : undefined;
        }
        const savedEntry = asRecordIdRef(savedRef);
        if (savedEntry && savedEntry.id > 0) {
            return {
                ...entry,
                recordId: savedEntry.id,
                recordLabel: entry.recordLabel || `Record #${savedEntry.id}`,
            };
        }
        return entry;
    });
}
