import { Plus } from 'lucide-react';
import { useMemo } from 'react';

import { Button } from '@/components/ui/button';
import { CollapsibleSection } from '@/components/collapsible-section';
import { Input } from '@/components/ui/input';
import type { FieldTreeNode, TemplateField } from '@/features/templates/types/template.types';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import {
    buildInputGroupOpenState,
    collectInputGroupKeys,
    isInputGroupContainer,
    isInputRepeatableGroup,
} from '@/features/xml-generation/utils/inputGroupCollapse';
import {
    createRepeatableItemDefault,
    formatOccurrenceHint,
    isFormInputField,
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
    groupOpenState: Record<string, boolean>;
    onGroupOpenChange: (groupKey: string, open: boolean) => void;
}

export function DynamicInputForm({
    fields,
    value,
    onChange,
    groupOpenState,
    onGroupOpenChange,
}: DynamicInputFormProps) {
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
        <div className="space-y-4">
            {tree.map((root) => {
                if (isInputGroupContainer(root, fields)) {
                    return root.children.map((child) => (
                        <FormFieldNode
                            key={child.field.fieldName}
                            node={child}
                            fields={fields}
                            groupKey={child.field.fieldName}
                            value={value[child.field.fieldName]}
                            groupOpenState={groupOpenState}
                            onGroupOpenChange={onGroupOpenChange}
                            onChange={(next) => updateRootField(child.field.fieldName, next)}
                        />
                    ));
                }

                return (
                    <FormFieldNode
                        key={root.field.fieldName}
                        node={root}
                        fields={fields}
                        groupKey={root.field.fieldName}
                        value={value[root.field.fieldName]}
                        groupOpenState={groupOpenState}
                        onGroupOpenChange={onGroupOpenChange}
                        onChange={(next) => updateRootField(root.field.fieldName, next)}
                    />
                );
            })}
        </div>
    );
}

interface FormFieldNodeProps {
    node: FieldTreeNode;
    fields: TemplateField[];
    groupKey: string;
    value: FormValue | undefined;
    groupOpenState: Record<string, boolean>;
    onGroupOpenChange: (groupKey: string, open: boolean) => void;
    onChange: (value: FormValue) => void;
}

function FormFieldNode({
    node,
    fields,
    groupKey,
    value,
    groupOpenState,
    onGroupOpenChange,
    onChange,
}: FormFieldNodeProps) {
    if (isInputGroupContainer(node, fields)) {
        if (isInputRepeatableGroup(node)) {
            return (
                <RepeatableGroupForm
                    node={node}
                    fields={fields}
                    groupKey={groupKey}
                    value={value}
                    groupOpenState={groupOpenState}
                    onGroupOpenChange={onGroupOpenChange}
                    onChange={onChange}
                />
            );
        }
        return (
            <SingleGroupForm
                node={node}
                fields={fields}
                groupKey={groupKey}
                value={value}
                groupOpenState={groupOpenState}
                onGroupOpenChange={onGroupOpenChange}
                onChange={onChange}
            />
        );
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
    groupKey,
    value,
    groupOpenState,
    onGroupOpenChange,
    onChange,
}: {
    node: FieldTreeNode;
    fields: TemplateField[];
    groupKey: string;
    value: FormValue | undefined;
    groupOpenState: Record<string, boolean>;
    onGroupOpenChange: (groupKey: string, open: boolean) => void;
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
        <CollapsibleSection
            title={
                <>
                    {label}
                    {occurrenceHint ? (
                        <span className="ml-1 font-normal text-muted-foreground">{occurrenceHint}</span>
                    ) : null}
                </>
            }
            open={groupOpenState[groupKey] ?? true}
            onOpenChange={(open) => onGroupOpenChange(groupKey, open)}
            headerClassName="text-sm font-semibold"
            contentClassName="space-y-3"
        >
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
                        {node.children.map((child) => {
                            const childKey = `${groupKey}.${child.field.fieldName}`;
                            return (
                                <FormFieldNode
                                    key={child.field.fieldName}
                                    node={child}
                                    fields={fields}
                                    groupKey={childKey}
                                    value={item[child.field.fieldName]}
                                    groupOpenState={groupOpenState}
                                    onGroupOpenChange={onGroupOpenChange}
                                    onChange={(next) =>
                                        updateItem(index, {
                                            ...item,
                                            [child.field.fieldName]: next,
                                        })
                                    }
                                />
                            );
                        })}
                    </div>
                </div>
            ))}

            <Button type="button" variant="outline" size="sm" onClick={addItem}>
                <Plus className="h-4 w-4" />
                Add {label}
            </Button>
        </CollapsibleSection>
    );
}

function SingleGroupForm({
    node,
    fields,
    groupKey,
    value,
    groupOpenState,
    onGroupOpenChange,
    onChange,
}: {
    node: FieldTreeNode;
    fields: TemplateField[];
    groupKey: string;
    value: FormValue | undefined;
    groupOpenState: Record<string, boolean>;
    onGroupOpenChange: (groupKey: string, open: boolean) => void;
    onChange: (value: FormValue) => void;
}) {
    const objectValue =
        typeof value === 'object' && value !== null && !Array.isArray(value) ? (value as FormObject) : {};
    const label = node.field.displayName || node.field.fieldName;

    function updateChild(fieldName: string, nextValue: FormValue) {
        onChange({ ...objectValue, [fieldName]: nextValue });
    }

    return (
        <CollapsibleSection
            title={label}
            open={groupOpenState[groupKey] ?? true}
            onOpenChange={(open) => onGroupOpenChange(groupKey, open)}
            contentClassName="space-y-3 pl-2"
        >
            {node.children.map((child) => {
                const childKey = `${groupKey}.${child.field.fieldName}`;
                return (
                    <FormFieldNode
                        key={child.field.fieldName}
                        node={child}
                        fields={fields}
                        groupKey={childKey}
                        value={objectValue[child.field.fieldName]}
                        groupOpenState={groupOpenState}
                        onGroupOpenChange={onGroupOpenChange}
                        onChange={(next) => updateChild(child.field.fieldName, next)}
                    />
                );
            })}
        </CollapsibleSection>
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
        <div className="grid grid-cols-1 gap-y-0.5 sm:grid-cols-[minmax(0,40%)_minmax(0,1fr)] sm:items-center sm:gap-x-3 sm:gap-y-0">
            <label className="font-mono text-sm font-medium leading-tight text-foreground">
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

export { buildInputGroupOpenState, collectInputGroupKeys };
