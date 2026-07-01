import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { Badge } from '@/components/ui/badge';
import { Select } from '@/components/ui/select';
import { Textarea } from '@/components/ui/select';
import { SchemaHelpTooltip } from '@/features/templates/components/SchemaHelpTooltip';
import type { DraftTemplateField } from '@/features/templates/types/template.types';
import { normalizeDraftFieldMetadata, applyValueNodeDefaults } from '@/features/templates/utils/schemaTree';

interface SchemaFieldEditorProps {
    field: DraftTemplateField | null;
    parentOptions: DraftTemplateField[];
    onChange: (field: DraftTemplateField) => void;
}

export function SchemaFieldEditor({ field, parentOptions, onChange }: SchemaFieldEditorProps) {
    if (!field) {
        return (
            <div className="rounded-md border border-dashed border-border p-6 text-sm text-muted-foreground">
                Select a field from the tree to edit its metadata.
            </div>
        );
    }

    const currentField = field;

    function update<K extends keyof DraftTemplateField>(key: K, value: DraftTemplateField[K]) {
        onChange({ ...currentField, [key]: value });
    }

    function handleParentChange(parentClientId: string) {
        const parent = parentOptions.find((option) => option.clientId === parentClientId);
        onChange({
            ...currentField,
            parentClientId: parentClientId || null,
            parentFieldName: parent?.fieldName ?? null,
        });
    }

    return (
        <div className="space-y-4 rounded-md border border-border p-4">
            <div className="flex items-center gap-2">
                <h2 className="text-sm font-semibold text-foreground">Field details</h2>
                {currentField.imported ? <Badge variant="secondary">Imported</Badge> : null}
            </div>
            <FieldInput
                label="Field name"
                value={currentField.fieldName}
                onChange={(value) =>
                    onChange(
                        normalizeDraftFieldMetadata({
                            ...currentField,
                            fieldName: value,
                            xmlName: value,
                            displayName: value,
                        }),
                    )
                }
            />
            <div className="space-y-2">
                <Label htmlFor="parentFieldName">Parent field</Label>
                <Select id="parentFieldName" value={currentField.parentClientId ?? ''} onChange={(event) => handleParentChange(event.target.value)}>
                    <option value="">None (root)</option>
                    {parentOptions
                        .filter((option) => option.clientId !== currentField.clientId)
                        .map((option) => (
                            <option key={option.clientId} value={option.clientId}>
                                {option.fieldName}
                            </option>
                        ))}
                </Select>
            </div>
            <FieldInput label="XML name" value={currentField.xmlName} onChange={(value) => update('xmlName', value)} />
            <FieldInput label="Display name" value={currentField.displayName ?? ''} onChange={(value) => update('displayName', value)} />
            <div className="space-y-2">
                <Label htmlFor="nodeType">
                    <SchemaHelpTooltip label="Node type">
                        <div className="space-y-2">
                            <p>
                                <strong>GROUP</strong> — Container for child nodes. Does not generate a value itself.
                            </p>
                            <p>
                                <strong>ELEMENT</strong> — Generates an XML element containing a value.
                            </p>
                            <p>
                                <strong>ATTRIBUTE</strong> — Generates an XML attribute on its parent element.
                            </p>
                        </div>
                    </SchemaHelpTooltip>
                </Label>
                <Select
                    id="nodeType"
                    value={currentField.nodeType}
                    onChange={(event) => {
                        const nextNodeType = event.target.value as DraftTemplateField['nodeType'];
                        const nextField = { ...currentField, nodeType: nextNodeType };
                        if (nextNodeType === 'GROUP') {
                            onChange(normalizeDraftFieldMetadata(nextField));
                            return;
                        }
                        if (currentField.nodeType === 'GROUP') {
                            onChange(applyValueNodeDefaults(nextField));
                            return;
                        }
                        onChange(normalizeDraftFieldMetadata(nextField));
                    }}
                >
                    <option value="GROUP">GROUP</option>
                    <option value="ELEMENT">ELEMENT</option>
                    <option value="ATTRIBUTE">ATTRIBUTE</option>
                </Select>
            </div>
            <div className="space-y-2">
                <Label htmlFor="emptyHandling">
                    <SchemaHelpTooltip label="Empty handling">
                        <div className="space-y-2">
                            <p>
                                <strong>REQUIRED</strong> — Missing value causes a validation error.
                            </p>
                            <p>
                                <strong>ZERO_IF_EMPTY</strong> — Missing value becomes <code>0</code>.
                            </p>
                            <p>
                                <strong>EMPTY_TAG_IF_EMPTY</strong> — Generates an empty XML tag.
                            </p>
                            <p>
                                <strong>OMIT_IF_EMPTY</strong> — Omits the XML element when empty.
                            </p>
                        </div>
                    </SchemaHelpTooltip>
                </Label>
                <Select
                    id="emptyHandling"
                    value={currentField.emptyHandling}
                    onChange={(event) => update('emptyHandling', event.target.value as DraftTemplateField['emptyHandling'])}
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
                    value={currentField.nodeType === 'GROUP' ? '' : (currentField.sourceType ?? '')}
                    disabled={currentField.nodeType === 'GROUP'}
                    onChange={(event) => {
                        const nextSourceType = (event.target.value || null) as DraftTemplateField['sourceType'];
                        if (!nextSourceType && currentField.nodeType !== 'GROUP') {
                            onChange(normalizeDraftFieldMetadata({ ...currentField, nodeType: 'GROUP' }));
                            return;
                        }
                        onChange(
                            normalizeDraftFieldMetadata({
                                ...currentField,
                                sourceType: nextSourceType,
                            }),
                        );
                    }}
                >
                    {currentField.nodeType !== 'GROUP' ? <option value="">None (container)</option> : null}
                    <option value="INPUT">INPUT</option>
                    <option value="MASTER_DATA">MASTER_DATA</option>
                    <option value="STATIC">STATIC</option>
                </Select>
            </div>
            <div className="space-y-2">
                <Label htmlFor="valueType">Value type</Label>
                <Select
                    id="valueType"
                    value={currentField.nodeType === 'GROUP' ? '' : (currentField.valueType ?? '')}
                    disabled={currentField.nodeType === 'GROUP'}
                    onChange={(event) => update('valueType', (event.target.value || null) as DraftTemplateField['valueType'])}
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
                <Label htmlFor="occurrenceRule">
                    <SchemaHelpTooltip label="Occurrence rule">
                        <div className="space-y-2">
                            <p>
                                <strong>ONE_OR_MORE</strong> — Must appear at least once.
                            </p>
                            <p>
                                <strong>ZERO_OR_MORE</strong> — May repeat any number of times.
                            </p>
                            <p>
                                <strong>ZERO_OR_ONE</strong> — May appear once or not at all.
                            </p>
                            <p>
                                <strong>None</strong> — Used for value nodes that do not repeat.
                            </p>
                        </div>
                    </SchemaHelpTooltip>
                </Label>
                <Select
                    id="occurrenceRule"
                    value={currentField.occurrenceRule ?? ''}
                    onChange={(event) => update('occurrenceRule', (event.target.value || null) as DraftTemplateField['occurrenceRule'])}
                >
                    <option value="">None</option>
                    <option value="ONE_OR_MORE">ONE_OR_MORE</option>
                    <option value="ZERO_OR_MORE">ZERO_OR_MORE</option>
                    <option value="ZERO_OR_ONE">ZERO_OR_ONE</option>
                </Select>
            </div>
            <FieldInput label="Static value" value={currentField.staticValue ?? ''} onChange={(value) => update('staticValue', value || null)} />
            <FieldInput label="Default value" value={currentField.defaultValue ?? ''} onChange={(value) => update('defaultValue', value || null)} />
            <div className="space-y-2">
                <Label htmlFor="description">Description</Label>
                <Textarea id="description" value={currentField.description ?? ''} onChange={(event) => update('description', event.target.value || null)} />
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
