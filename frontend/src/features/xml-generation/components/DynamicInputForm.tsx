import { useMemo } from 'react';

import { Input } from '@/components/ui/input';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import type { TemplateField } from '@/features/templates/types/template.types';
import {
    listInputFields,
    type FormScalar,
} from '@/features/xml-generation/utils/inputFormSchema';

interface DynamicInputFormProps {
    fields: TemplateField[];
    value: Record<string, FormScalar>;
    onChange: (value: Record<string, FormScalar>) => void;
}

export function DynamicInputForm({ fields, value, onChange }: DynamicInputFormProps) {
    const inputFields = useMemo(() => listInputFields(fields), [fields]);

    if (inputFields.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">
                This template has no input fields. Use master data or static values in the schema instead.
            </p>
        );
    }

    function updateField(fieldName: string, nextValue: FormScalar) {
        onChange({ ...value, [fieldName]: nextValue });
    }

    return (
        <div className="max-h-[520px] overflow-y-auto rounded-md border border-border">
            <Table>
                <TableHeader>
                    <TableRow>
                        <TableHead className="w-[40%]">Field name</TableHead>
                        <TableHead>Value</TableHead>
                    </TableRow>
                </TableHeader>
                <TableBody>
                    {inputFields.map((field) => (
                        <TableRow key={field.fieldName}>
                            <TableCell className="align-middle font-mono text-sm font-medium text-foreground">
                                {field.fieldName}
                                {field.emptyHandling === 'REQUIRED' ? (
                                    <span className="ml-1 text-destructive">*</span>
                                ) : null}
                            </TableCell>
                            <TableCell className="align-middle">
                                <FieldInput field={field} value={value[field.fieldName] ?? ''} onChange={(next) => updateField(field.fieldName, next)} />
                            </TableCell>
                        </TableRow>
                    ))}
                </TableBody>
            </Table>
        </div>
    );
}

function FieldInput({
    field,
    value,
    onChange,
}: {
    field: TemplateField;
    value: FormScalar;
    onChange: (value: FormScalar) => void;
}) {
    if (field.valueType === 'BOOLEAN') {
        return (
            <label className="flex items-center gap-2 text-sm">
                <input
                    type="checkbox"
                    checked={value === true}
                    onChange={(event) => onChange(event.target.checked)}
                />
                <span className="text-muted-foreground">{value === true ? 'true' : 'false'}</span>
            </label>
        );
    }

    const inputType =
        field.valueType === 'INTEGER' || field.valueType === 'LONG' || field.valueType === 'DECIMAL'
            ? 'number'
            : field.valueType === 'DATE'
              ? 'date'
              : field.valueType === 'DATETIME'
                ? 'datetime-local'
                : 'text';

    return (
        <Input
            type={inputType}
            step={field.valueType === 'DECIMAL' ? 'any' : field.valueType === 'INTEGER' ? '1' : undefined}
            value={value === null ? '' : String(value)}
            placeholder={field.displayName ?? field.xmlName}
            onChange={(event) => onChange(event.target.value)}
        />
    );
}
