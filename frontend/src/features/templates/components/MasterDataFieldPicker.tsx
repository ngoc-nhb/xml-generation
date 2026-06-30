import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import {
    useAllMasterDataFieldPickerOptions,
    useMasterDataFieldDetail,
    useMasterDataTypeDetail,
    type MasterDataFieldOption,
} from '@/features/master-data';

interface MasterDataFieldPickerProps {
    value: number | null;
    onChange: (masterDataFieldId: number | null) => void;
}

function buildOptionLabel(typeCode: string, fieldCode: string, fieldName: string): string {
    return `${typeCode} / ${fieldCode} — ${fieldName}`;
}

export function MasterDataFieldPicker({ value, onChange }: MasterDataFieldPickerProps) {
    const { options, isLoading } = useAllMasterDataFieldPickerOptions();

    const { data: selectedField } = useMasterDataFieldDetail(value ?? undefined);
    const { data: selectedType } = useMasterDataTypeDetail(selectedField?.typeId);

    const displayOptions = useMemo((): MasterDataFieldOption[] => {
        const merged = [...options];

        if (value && selectedField && selectedType && !merged.some((option) => option.id === value)) {
            merged.unshift({
                id: selectedField.id,
                typeId: selectedField.typeId,
                typeCode: selectedType.code,
                typeName: selectedType.name,
                code: selectedField.code,
                name: selectedField.name,
                label: buildOptionLabel(selectedType.code, selectedField.code, selectedField.name),
            });
        }

        return merged;
    }, [options, value, selectedField, selectedType]);

    return (
        <div className="min-w-[280px]">
            <div className="flex gap-2">
                <Select
                    className="flex-1"
                    value={value?.toString() ?? ''}
                    disabled={isLoading}
                    onChange={(event) => {
                        const nextValue = event.target.value;
                        onChange(nextValue ? Number(nextValue) : null);
                    }}
                >
                    <option value="">{isLoading ? 'Loading fields…' : 'No mapping'}</option>
                    {displayOptions.map((option) => (
                        <option key={option.id} value={option.id}>
                            {option.label}
                        </option>
                    ))}
                </Select>
                {value ? (
                    <Button type="button" variant="outline" size="sm" onClick={() => onChange(null)}>
                        Clear
                    </Button>
                ) : null}
            </div>
        </div>
    );
}
