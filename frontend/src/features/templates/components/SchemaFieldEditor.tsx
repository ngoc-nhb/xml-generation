import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Select } from '@/components/ui/select';
import { Textarea } from '@/components/ui/select';
import type { TemplateField } from '@/features/templates/types/template.types';

interface SchemaFieldEditorProps {
    field: TemplateField | null;
    parentOptions: TemplateField[];
    onChange: (field: TemplateField) => void;
}

export function SchemaFieldEditor({ field, parentOptions, onChange }: SchemaFieldEditorProps) {
    if (!field) {
        return (
            <div className="rounded-md border border-dashed border-border p-6 text-sm text-muted-foreground">
                Select a field from the tree to edit its metadata.
            </div>
        );
    }

    function update<K extends keyof TemplateField>(key: K, value: TemplateField[K]) {
        onChange({ ...field, [key]: value } as TemplateField);
    }

    return (
        <div className="space-y-4 rounded-md border border-border p-4">
            <h2 className="text-sm font-semibold text-foreground">Field details</h2>
            <FieldInput label="Field name" value={field.fieldName} onChange={(value) => update('fieldName', value)} />
            <div className="space-y-2">
                <Label htmlFor="parentFieldName">Parent field</Label>
                <Select
                    id="parentFieldName"
                    value={field.parentFieldName ?? ''}
                    onChange={(event) => update('parentFieldName', event.target.value || null)}
                >
                    <option value="">None (root)</option>
                    {parentOptions
                        .filter((option) => option.fieldName !== field.fieldName)
                        .map((option) => (
                            <option key={option.fieldName} value={option.fieldName}>
                                {option.fieldName}
                            </option>
                        ))}
                </Select>
            </div>
            <FieldInput label="XML name" value={field.xmlName} onChange={(value) => update('xmlName', value)} />
            <FieldInput label="Display name" value={field.displayName ?? ''} onChange={(value) => update('displayName', value)} />
            <div className="space-y-2">
                <Label htmlFor="nodeType">Node type</Label>
                <Select id="nodeType" value={field.nodeType} onChange={(event) => update('nodeType', event.target.value as TemplateField['nodeType'])}>
                    <option value="GROUP">GROUP</option>
                    <option value="ELEMENT">ELEMENT</option>
                    <option value="ATTRIBUTE">ATTRIBUTE</option>
                </Select>
            </div>
            <div className="space-y-2">
                <Label htmlFor="emptyHandling">Empty handling</Label>
                <Select
                    id="emptyHandling"
                    value={field.emptyHandling}
                    onChange={(event) => update('emptyHandling', event.target.value as TemplateField['emptyHandling'])}
                >
                    <option value="REQUIRED">REQUIRED</option>
                    <option value="OMIT_IF_EMPTY">OMIT_IF_EMPTY</option>
                    <option value="EMPTY_TAG_IF_EMPTY">EMPTY_TAG_IF_EMPTY</option>
                    <option value="ZERO_IF_EMPTY">ZERO_IF_EMPTY</option>
                </Select>
            </div>
            <div className="space-y-2">
                <Label htmlFor="sourceType">Source type</Label>
                <Select
                    id="sourceType"
                    value={field.sourceType ?? ''}
                    onChange={(event) => update('sourceType', (event.target.value || null) as TemplateField['sourceType'])}
                >
                    <option value="">None</option>
                    <option value="INPUT">INPUT</option>
                    <option value="MASTER_DATA">MASTER_DATA</option>
                    <option value="STATIC">STATIC</option>
                </Select>
            </div>
            <div className="space-y-2">
                <Label htmlFor="valueType">Value type</Label>
                <Select
                    id="valueType"
                    value={field.valueType ?? ''}
                    onChange={(event) => update('valueType', (event.target.value || null) as TemplateField['valueType'])}
                >
                    <option value="">None</option>
                    <option value="STRING">STRING</option>
                    <option value="INTEGER">INTEGER</option>
                    <option value="LONG">LONG</option>
                    <option value="DECIMAL">DECIMAL</option>
                    <option value="BOOLEAN">BOOLEAN</option>
                    <option value="DATE">DATE</option>
                    <option value="DATETIME">DATETIME</option>
                </Select>
            </div>
            <div className="space-y-2">
                <Label htmlFor="occurrenceRule">Occurrence rule</Label>
                <Select
                    id="occurrenceRule"
                    value={field.occurrenceRule ?? ''}
                    onChange={(event) => update('occurrenceRule', (event.target.value || null) as TemplateField['occurrenceRule'])}
                >
                    <option value="">None</option>
                    <option value="ONE_OR_MORE">ONE_OR_MORE</option>
                    <option value="ZERO_OR_MORE">ZERO_OR_MORE</option>
                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                </Select>
            </div>
            <FieldInput label="Static value" value={field.staticValue ?? ''} onChange={(value) => update('staticValue', value || null)} />
            <FieldInput label="Default value" value={field.defaultValue ?? ''} onChange={(value) => update('defaultValue', value || null)} />
            <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea id="description" value={field.description ?? ''} onChange={(event) => update('description', event.target.value || null)} />
            </div>
        </div>
    );
}

function FieldInput({
    label,
    value,
    onChange,
}: {
    label: string;
    value: string;
    onChange: (value: string) => void;
}) {
    return (
        <div className="space-y-2">
            <Label>{label}</Label>
            <Input value={value} onChange={(event) => onChange(event.target.value)} />
        </div>
    );
}
