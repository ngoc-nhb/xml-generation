import { useMemo } from 'react';

import { Select } from '@/components/ui/select';
import { useMasterDataFieldsForType, useMasterDataRecordList } from '@/features/master-data';
import { formatMasterDataRecordLabel } from '@/features/xml-generation/utils/masterDataRecordLabel';

interface MasterDataTypeRecordPickerProps {
    typeId: number;
    value: number | null;
    onChange: (recordId: number, recordLabel: string) => void;
    disabled?: boolean;
}

export function MasterDataTypeRecordPicker({
    typeId,
    value,
    onChange,
    disabled = false,
}: MasterDataTypeRecordPickerProps) {
    const { data: fieldsData, isLoading: fieldsLoading } = useMasterDataFieldsForType(typeId);
    const { data: recordsData, isLoading: recordsLoading } = useMasterDataRecordList({
        typeId,
        page: 1,
        pageSize: 500,
    });

    const fields = useMemo(
        () => (fieldsData?.items ?? []).slice().sort((left, right) => left.displayOrder - right.displayOrder),
        [fieldsData?.items],
    );
    const records = recordsData?.items ?? [];
    const isLoading = fieldsLoading || recordsLoading;

    return (
        <Select
            value={value?.toString() ?? ''}
            disabled={disabled || isLoading}
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
