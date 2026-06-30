import { Plus } from 'lucide-react';
import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import type { FieldTreeNode, TemplateField } from '@/features/templates/types/template.types';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import {
    createRepeatableItemDefault,
    formatOccurrenceHint,
    isFormInputField,
    isRepeatableOccurrence,
    isSchemaContainerField,
    listInputFields,
    normalizeRepeatableItems,
    type FormObject,
    type FormScalar,
    type FormValue,
} from '@/features/xml-generation/utils/inputFormSchema';

interface DynamicInputFormProps {
    fields: TemplateField[];
    value: FormObject;
    onChange: (value: FormObject) => void;
}

export function DynamicInputForm({ fields, value, onChange }: DynamicInputFormProps) {
    const tree = useMemo(() => buildFieldTree(fields), [fields]);
    const inputFields = useMemo(() => listInputFields(fields), [fields]);

    if (inputFields.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">
                This template has no input fields. Use master data or static values in the schema instead.
            </p>
        );
    }

    function updateRootField(fieldName: string, nextValue: FormValue) {
        onChange({ ...value, [fieldName]: nextValue });
    }

    return (
        <div className="max-h-[520px] space-y-4 overflow-y-auto rounded-md border border-border p-4">
            {tree.map((root) => {
                if (isSchemaContainerField(root.field, fields)) {
                    return root.children.map((child) => (
                        <FormFieldNode
                            key={child.field.fieldName}
                            node={child}
                            fields={fields}
                            value={value[child.field.fieldName]}
                            onChange={(next) => updateRootField(child.field.fieldName, next)}
                        />
                    ));
                }

                return (
                    <FormFieldNode
                        key={root.field.fieldName}
                        node={root}
                        fields={fields}
                        value={value[root.field.fieldName]}
                        onChange={(next) => updateRootField(root.field.fieldName, next)}
                    />
                );
            })}
        </div>
    );
}

function FormFieldNode({
    node,
    fields,
    value,
    onChange,
}: {
    node: FieldTreeNode;
    fields: TemplateField[];
    value: FormValue | undefined;
    onChange: (value: FormValue) => void;
}) {
    const isContainer = isSchemaContainerField(node.field, fields) || node.children.length > 0;

    if (isContainer) {
        if (isRepeatableOccurrence(node.field.occurrenceRule)) {
            return <RepeatableGroupForm node={node} fields={fields} value={value} onChange={onChange} />;
        }
        return <SingleGroupForm node={node} fields={fields} value={value} onChange={onChange} />;
    }

    if (!isFormInputField(node.field)) {
        return null;
    }

    return (
        <InputFieldRow
            field={node.field}
            value={(value ?? '') as FormScalar}
            onChange={(next) => onChange(next)}
        />
    );
}

function RepeatableGroupForm({
    node,
    fields,
    value,
    onChange,
}: {
    node: FieldTreeNode;
    fields: TemplateField[];
    value: FormValue | undefined;
    onChange: (value: FormValue) => void;
}) {
    const items = normalizeRepeatableItems(node, value);
    const label = node.field.displayName || node.field.fieldName;
    const occurrenceHint = formatOccurrenceHint(node.field.occurrenceRule);
    const minOne = node.field.occurrenceRule === 'ONE_OR_MORE';

    function updateItems(nextItems: FormObject[]) {
        onChange(nextItems);
    }

    function updateItem(index: number, nextItem: FormObject) {
        updateItems(items.map((item, itemIndex) => (itemIndex === index ? nextItem : item)));
    }

    function addItem() {
        updateItems([...items, createRepeatableItemDefault(node)]);
    }

    function removeItem(index: number) {
        if (minOne && items.length <= 1) {
            return;
        }
        updateItems(items.filter((_, itemIndex) => itemIndex !== index));
    }

    return (
        <div className="space-y-3">
            <div className="flex flex-wrap items-center gap-2">
                <h3 className="text-sm font-semibold text-foreground">
                    {label}
                    {occurrenceHint ? <span className="ml-1 font-normal text-muted-foreground">{occurrenceHint}</span> : null}
                </h3>
            </div>

            {items.map((item, index) => (
                <div key={`${node.field.fieldName}-${index}`} className="space-y-3 rounded-md border border-border bg-muted/20 p-4">
                    <div className="flex items-center justify-between gap-3 border-b border-border pb-2">
                        <p className="text-sm font-medium text-foreground">
                            {label} #{index + 1}
                        </p>
                        <Button
                            type="button"
                            variant="outline"
                            size="sm"
                            disabled={minOne && items.length <= 1}
                            onClick={() => removeItem(index)}
                        >
                            Remove
                        </Button>
                    </div>
                    <div className="space-y-3">
                        {node.children.map((child) => (
                            <FormFieldNode
                                key={child.field.fieldName}
                                node={child}
                                fields={fields}
                                value={item[child.field.fieldName]}
                                onChange={(next) =>
                                    updateItem(index, {
                                        ...item,
                                        [child.field.fieldName]: next,
                                    })
                                }
                            />
                        ))}
                    </div>
                </div>
            ))}

            <Button type="button" variant="outline" size="sm" onClick={addItem}>
                <Plus className="h-4 w-4" />
                Add {label}
            </Button>
        </div>
    );
}

function SingleGroupForm({
    node,
    fields,
    value,
    onChange,
}: {
    node: FieldTreeNode;
    fields: TemplateField[];
    value: FormValue | undefined;
    onChange: (value: FormValue) => void;
}) {
    const objectValue =
        typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as FormObject) : {};
    const label = node.field.displayName || node.field.fieldName;

    function updateChild(fieldName: string, nextValue: FormValue) {
        onChange({ ...objectValue, [fieldName]: nextValue });
    }

    return (
        <div className="space-y-3 rounded-md border border-border/70 p-3">
            <p className="text-sm font-medium text-foreground">{label}</p>
            <div className="space-y-3 pl-2">
                {node.children.map((child) => (
                    <FormFieldNode
                        key={child.field.fieldName}
                        node={child}
                        fields={fields}
                        value={objectValue[child.field.fieldName]}
                        onChange={(next) => updateChild(child.field.fieldName, next)}
                    />
                ))}
            </div>
        </div>
    );
}

function InputFieldRow({
    field,
    value,
    onChange,
}: {
    field: TemplateField;
    value: FormScalar;
    onChange: (value: FormScalar) => void;
}) {
    return (
        <div className="grid gap-2 sm:grid-cols-[minmax(0,40%)_minmax(0,1fr)] sm:items-center">
            <label className="font-mono text-sm font-medium text-foreground">
                {field.fieldName}
                {field.emptyHandling === 'REQUIRED' ? <span className="ml-1 text-destructive">*</span> : null}
            </label>
            <FieldInput field={field} value={value} onChange={onChange} />
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
