import { useMemo, useState } from 'react';

import { Input } from '@/components/ui/input';
import { Select } from '@/components/ui/select';
import { Button } from '@/components/ui/button';
import {
    useMasterDataFieldDetail,
    useMasterDataFieldPickerOptions,
    useMasterDataTypeDetail,
    type MasterDataFieldOption,
} from '@/features/master-data';
import { useDebouncedValue } from '@/features/templates/hooks/useDebouncedValue';

interface MasterDataFieldPickerProps {
    value: number | null;
    onChange: (masterDataFieldId: number | null) => void;
}

function buildOptionLabel(typeCode: string, fieldCode: string, fieldName: string): string {
    return `${typeCode} / ${fieldCode} — ${fieldName}`;
}

export function MasterDataFieldPicker({ value, onChange }: MasterDataFieldPickerProps) {
    const [keyword, setKeyword] = useState('');
    const debouncedKeyword = useDebouncedValue(keyword, 300);

    const { options, isLoading, isFetching } = useMasterDataFieldPickerOptions({
        keyword: debouncedKeyword,
        page: 1,
        pageSize: 50,
    });

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
        <div className="min-w-[280px] space-y-2">
            <Input
                placeholder="Search master data fields…"
                value={keyword}
                onChange={(event) => setKeyword(event.target.value)}
            />
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
                    <option value="">No mapping</option>
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
            {isFetching ? <p className="text-xs text-muted-foreground">Searching…</p> : null}
        </div>
    );
}
