import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select } from '@/components/ui/select';
import type { MasterDataFieldListItem } from '@/features/master-data/types/master-data.types';

interface DynamicRecordFormProps {
    fields: MasterDataFieldListItem[];
    values: Record<string, unknown>;
    onChange: (values: Record<string, unknown>) => void;
}

export function DynamicRecordForm({ fields, values, onChange }: DynamicRecordFormProps) {
    function updateField(code: string, value: unknown) {
        onChange({ ...values, [code]: value });
    }

    if (fields.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">
                Define fields for this master data type before creating records.
            </p>
        );
    }

    return (
        <div className="space-y-4">
            {fields
                .slice()
                .sort((a, b) => a.displayOrder - b.displayOrder)
                .map((field) => (
                    <div key={field.id} className="space-y-2">
                        <Label htmlFor={field.code}>
                            {field.name}
                            {field.required ? ' *' : ''}
                        </Label>
                        {field.dataType === 'BOOLEAN' ? (
                            <Select
                                id={field.code}
                                value={String(values[field.code] ?? false)}
                                onChange={(event) => updateField(field.code, event.target.value === 'true')}
                            >
                                <option value="false">False</option>
                                <option value="true">True</option>
                            </Select>
                        ) : (
                            <Input
                                id={field.code}
                                type={getInputType(field.dataType)}
                                value={String(values[field.code] ?? '')}
                                onChange={(event) => updateField(field.code, event.target.value)}
                            />
                        )}
                        {field.description ? (
                            <p className="text-xs text-muted-foreground">{field.description}</p>
                        ) : null}
                    </div>
                ))}
        </div>
    );
}

function getInputType(dataType: MasterDataFieldListItem['dataType']): string {
    switch (dataType) {
        case 'INTEGER':
        case 'LONG':
        case 'DECIMAL':
            return 'number';
        case 'DATE':
            return 'date';
        case 'DATETIME':
            return 'datetime-local';
        default:
            return 'text';
    }
}
