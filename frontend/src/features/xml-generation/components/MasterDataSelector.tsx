import { useMemo } from 'react';
import { X } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { useMasterDataRecordList, useMasterDataTypeList } from '@/features/master-data';
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
                                    <p className="text-sm font-medium text-foreground">
                                        {entry.typeCode} — {entry.typeName}
                                    </p>
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
    const { data, isLoading } = useMasterDataRecordList({ typeId, page: 1, pageSize: 100 });

    const records = data?.items ?? [];

    return (
        <Select
            value={value?.toString() ?? ''}
            disabled={isLoading}
            onChange={(event) => {
                const recordId = Number(event.target.value);
                const record = records.find((item) => item.id === recordId);
                const label = record ? formatRecordLabel(record.data) : '';
                onChange(recordId, label);
            }}
        >
            <option value="">Select a record…</option>
            {records.map((record) => (
                <option key={record.id} value={record.id}>
                    {formatRecordLabel(record.data)}
                </option>
            ))}
        </Select>
    );
}

function formatRecordLabel(data: Record<string, unknown>): string {
    const values = Object.values(data)
        .filter((value) => value !== null && value !== undefined && value !== '')
        .slice(0, 3)
        .map(String);
    return values.length > 0 ? values.join(' · ') : 'Record';
}

export function toSelectedMasterDataPayload(
    selections: SelectedMasterDataEntry[],
): Record<string, { id: number }> {
    const payload: Record<string, { id: number }> = {};
    for (const entry of selections) {
        if (entry.recordId > 0) {
            payload[entry.typeCode] = { id: entry.recordId };
        }
    }
    return payload;
}
