import { Plus } from 'lucide-react';
import { useEffect, useMemo, useRef, useState, type MouseEvent } from 'react';

import { Button } from '@/components/ui/button';
import { CollapsibleSection } from '@/components/collapsible-section';
import { Input } from '@/components/ui/input';
import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import type { FieldTreeNode, TemplateField } from '@/features/templates/types/template.types';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import { BulkEditDialog } from '@/features/xml-generation/components/BulkEditDialog';
import {
    buildInputGroupOpenState,
    childGroupKey,
    collectInputGroupKeys,
    collectInstanceGroupKeys,
    isInputGroupContainer,
    isInputRepeatableGroup,
    itemCollapseKey,
    setRepeatableItemOpenState,
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
import { logRuntimeCheckpoint, summarizeNestedScheduleInfo } from '@/features/xml-generation/utils/runtimeInvestigation';

interface DynamicInputFormProps {
    fields: TemplateField[];
    value: FormObject;
    onChange: (value: FormObject) => void;
    groupOpenState: Record<string, boolean>;
    onGroupOpenChange: (groupKey: string, open: boolean) => void;
    onGroupOpenStateReplace?: (next: Record<string, boolean>) => void;
    onRepeatableItemsRemove?: (groupFieldName: string, removedIndices: number[]) => void;
    onRepeatableItemsDuplicate?: (
        groupFieldName: string,
        selectedIndices: number[],
        copies: number,
    ) => void;
}

export function DynamicInputForm({
    fields,
    value,
    onChange,
    groupOpenState,
    onGroupOpenChange,
    onGroupOpenStateReplace,
    onRepeatableItemsRemove,
    onRepeatableItemsDuplicate,
}: DynamicInputFormProps) {
    const tree = useMemo(() => buildFieldTree(fields), [fields]);
    const inputFields = useMemo(() => listInputFields(fields), [fields]);

    useEffect(() => {
        logRuntimeCheckpoint('5_DynamicInputForm_props', {
            fieldsCount: fields.length,
            value,
            nestedSummary: summarizeNestedScheduleInfo(value),
        });
    }, [fields, value]);

    if (inputFields.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">
                This template has no editable data fields.
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
                            onGroupOpenStateReplace={onGroupOpenStateReplace}
                            onRepeatableItemsRemove={onRepeatableItemsRemove}
                            onRepeatableItemsDuplicate={onRepeatableItemsDuplicate}
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
                        onGroupOpenStateReplace={onGroupOpenStateReplace}
                        onRepeatableItemsRemove={onRepeatableItemsRemove}
                        onRepeatableItemsDuplicate={onRepeatableItemsDuplicate}
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
    onGroupOpenStateReplace?: (next: Record<string, boolean>) => void;
    onChange: (value: FormValue) => void;
    onRepeatableItemsRemove?: (groupFieldName: string, removedIndices: number[]) => void;
    onRepeatableItemsDuplicate?: (
        groupFieldName: string,
        selectedIndices: number[],
        copies: number,
    ) => void;
}

function FormFieldNode(props: FormFieldNodeProps) {
    const { node, fields } = props;
    if (isInputGroupContainer(node, fields)) {
        if (isInputRepeatableGroup(node)) {
            return <RepeatableGroupForm {...props} />;
        }
        return <SingleGroupForm {...props} />;
    }

    if (!isFormInputField(node.field)) {
        return null;
    }

    return (
        <InputFieldRow
            field={node.field}
            value={(props.value ?? '') as FormScalar}
            onChange={(next) => props.onChange(next)}
        />
    );
}

function deepCloneItem(item: FormObject): FormObject {
    return structuredClone(item);
}

function RepeatableGroupForm({
    node,
    fields,
    groupKey,
    value,
    groupOpenState,
    onGroupOpenChange,
    onGroupOpenStateReplace,
    onChange,
    onRepeatableItemsRemove,
    onRepeatableItemsDuplicate,
}: FormFieldNodeProps) {
    const items = normalizeRepeatableItems(node, value);
    const label = node.field.displayName || node.field.fieldName;
    const occurrenceHint = formatOccurrenceHint(node.field.occurrenceRule);
    const minOne = node.field.occurrenceRule === 'ONE_OR_MORE';
    const editableFields = useMemo(
        () =>
            node.children
                .map((child) => child.field)
                .filter((field) => isFormInputField(field) && field.sourceType !== 'MASTER_DATA'),
        [node.children],
    );

    const [selectionMode, setSelectionMode] = useState(false);
    const [selectedIndices, setSelectedIndices] = useState<number[]>([]);
    const [copies, setCopies] = useState(1);
    const [bulkEditOpen, setBulkEditOpen] = useState(false);
    const [showRemoveDialog, setShowRemoveDialog] = useState(false);
    const selectionAnchorRef = useRef<number | null>(null);
    const selectAllRef = useRef<HTMLInputElement>(null);

    const allSelected = items.length > 0 && selectedIndices.length === items.length;
    const someSelected = selectedIndices.length > 0 && !allSelected;
    const remainingAfterRemove = items.length - selectedIndices.length;
    const canRemoveSelected =
        selectedIndices.length > 0 && (!minOne || remainingAfterRemove >= 1);

    useEffect(() => {
        if (node.field.fieldName !== 'ScheduleInfo' && node.field.fieldName !== 'GameCategory') {
            return;
        }
        logRuntimeCheckpoint(`6_repeatable_${node.field.fieldName}_componentData`, {
            groupKey,
            parentPath: groupKey,
            fieldName: node.field.fieldName,
            rawValue: value,
            normalizedItems: items,
            itemCount: items.length,
        });
    }, [groupKey, items, node.field.fieldName, value]);

    useEffect(() => {
        setSelectedIndices((current) => current.filter((index) => index < items.length));
    }, [items.length]);

    useEffect(() => {
        if (selectAllRef.current) {
            selectAllRef.current.indeterminate = someSelected;
        }
    }, [someSelected]);

    function updateItems(nextItems: FormObject[]) {
        onChange(nextItems);
    }

    function updateItem(index: number, nextItem: FormObject) {
        updateItems(items.map((item, itemIndex) => (itemIndex === index ? nextItem : item)));
    }

    function addItem() {
        updateItems([...items, createRepeatableItemDefault(node)]);
    }

    function toggleSelected(index: number, checked: boolean) {
        setSelectedIndices((current) => {
            if (checked) {
                return current.includes(index) ? current : [...current, index].sort((a, b) => a - b);
            }
            return current.filter((item) => item !== index);
        });
    }

    function handleItemCheckboxClick(index: number, event: MouseEvent<HTMLInputElement>) {
        event.preventDefault();
        if (event.shiftKey && selectionAnchorRef.current != null) {
            const start = Math.min(selectionAnchorRef.current, index);
            const end = Math.max(selectionAnchorRef.current, index);
            const range = Array.from({ length: end - start + 1 }, (_, offset) => start + offset);
            setSelectedIndices(range);
            return;
        }
        const nextChecked = !selectedIndices.includes(index);
        toggleSelected(index, nextChecked);
        selectionAnchorRef.current = index;
    }

    function handleSelectAllChange(checked: boolean) {
        if (checked) {
            setSelectedIndices(items.map((_, index) => index));
            selectionAnchorRef.current = items.length > 0 ? 0 : null;
            return;
        }
        setSelectedIndices([]);
    }

    function duplicateSelected() {
        if (selectedIndices.length === 0 || copies < 1) {
            return;
        }
        const ordered = [...selectedIndices].sort((a, b) => a - b);
        const insertAt = ordered[ordered.length - 1]! + 1;
        const clones: FormObject[] = [];
        for (let copy = 0; copy < copies; copy += 1) {
            for (const sourceIndex of ordered) {
                clones.push(deepCloneItem(items[sourceIndex]!));
            }
        }
        updateItems([...items.slice(0, insertAt), ...clones, ...items.slice(insertAt)]);
        onRepeatableItemsDuplicate?.(node.field.fieldName, ordered, copies);
        setSelectedIndices([]);
        setSelectionMode(false);
        selectionAnchorRef.current = null;
    }

    function confirmRemoveSelected() {
        if (!canRemoveSelected) {
            return;
        }
        const removed = new Set(selectedIndices);
        const orderedRemoved = [...selectedIndices].sort((a, b) => a - b);
        updateItems(items.filter((_, index) => !removed.has(index)));
        onRepeatableItemsRemove?.(node.field.fieldName, orderedRemoved);
        setSelectedIndices([]);
        setShowRemoveDialog(false);
        selectionAnchorRef.current = null;
    }

    function expandAllItems() {
        onGroupOpenStateReplace?.(setRepeatableItemOpenState(groupOpenState, groupKey, items.length, true));
    }

    function collapseAllItems() {
        onGroupOpenStateReplace?.(setRepeatableItemOpenState(groupOpenState, groupKey, items.length, false));
    }

    function applyBulkEdit(updates: Array<{ index: number; patch: FormObject }>) {
        const nextItems = items.map((item) => ({ ...item }));
        for (const update of updates) {
            nextItems[update.index] = { ...nextItems[update.index], ...update.patch };
        }
        updateItems(nextItems);
        setSelectedIndices([]);
        setSelectionMode(false);
        selectionAnchorRef.current = null;
    }

    return (
        <>
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
                <div className="flex flex-wrap items-center gap-2">
                    <Button type="button" variant="outline" size="sm" onClick={expandAllItems}>
                        Expand All
                    </Button>
                    <Button type="button" variant="outline" size="sm" onClick={collapseAllItems}>
                        Collapse All
                    </Button>
                    <Button
                        type="button"
                        variant={selectionMode ? 'default' : 'outline'}
                        size="sm"
                        onClick={() => {
                            setSelectionMode((current) => !current);
                            setSelectedIndices([]);
                            selectionAnchorRef.current = null;
                        }}
                    >
                        Selection Mode
                    </Button>
                    {selectionMode ? (
                        <>
                            <label className="flex items-center gap-2 text-sm text-foreground">
                                <input
                                    ref={selectAllRef}
                                    type="checkbox"
                                    checked={allSelected}
                                    onChange={(event) => handleSelectAllChange(event.target.checked)}
                                />
                                Select All
                            </label>
                            <label className="flex items-center gap-2 text-sm text-muted-foreground">
                                Copies
                                <Input
                                    type="number"
                                    min={1}
                                    className="h-8 w-16"
                                    value={copies}
                                    onChange={(event) =>
                                        setCopies(Math.max(1, Number.parseInt(event.target.value, 10) || 1))
                                    }
                                />
                            </label>
                            <Button
                                type="button"
                                size="sm"
                                variant="outline"
                                disabled={selectedIndices.length === 0 || editableFields.length === 0}
                                onClick={() => setBulkEditOpen(true)}
                            >
                                Bulk Edit
                            </Button>
                            <Button
                                type="button"
                                size="sm"
                                disabled={selectedIndices.length === 0}
                                onClick={duplicateSelected}
                            >
                                Duplicate
                            </Button>
                            <Button
                                type="button"
                                size="sm"
                                variant="destructive"
                                disabled={!canRemoveSelected}
                                onClick={() => setShowRemoveDialog(true)}
                            >
                                Remove
                            </Button>
                        </>
                    ) : null}
                </div>

                {items.map((item, index) => {
                    const itemKey = itemCollapseKey(groupKey, index);
                    return (
                        <div
                            key={`${node.field.fieldName}-${index}`}
                            className="flex items-start gap-2 rounded-md border border-border bg-muted/20 p-4"
                        >
                            {selectionMode ? (
                                <input
                                    type="checkbox"
                                    className="mt-1"
                                    checked={selectedIndices.includes(index)}
                                    onClick={(event) => handleItemCheckboxClick(index, event)}
                                    onChange={() => {
                                        /* selection handled in onClick for Shift+Click support */
                                    }}
                                    aria-label={`Select ${label} #${index + 1}`}
                                />
                            ) : null}
                            <div className="min-w-0 flex-1">
                                <CollapsibleSection
                                    title={`${label} #${index + 1}`}
                                    open={groupOpenState[itemKey] ?? true}
                                    onOpenChange={(open) => onGroupOpenChange(itemKey, open)}
                                    headerClassName="text-sm font-medium"
                                    contentClassName="space-y-3"
                                >
                                    <div className="space-y-3">
                                        {node.children.map((child) => {
                                            const childKey = childGroupKey(itemKey, child.field.fieldName);
                                            return (
                                                <FormFieldNode
                                                    key={child.field.fieldName}
                                                    node={child}
                                                    fields={fields}
                                                    groupKey={childKey}
                                                    value={item[child.field.fieldName]}
                                                    groupOpenState={groupOpenState}
                                                    onGroupOpenChange={onGroupOpenChange}
                                                    onGroupOpenStateReplace={onGroupOpenStateReplace}
                                                    onRepeatableItemsRemove={onRepeatableItemsRemove}
                                                    onRepeatableItemsDuplicate={onRepeatableItemsDuplicate}
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
                                </CollapsibleSection>
                            </div>
                        </div>
                    );
                })}

                <Button type="button" variant="outline" size="sm" onClick={addItem}>
                    <Plus className="h-4 w-4" />
                    Add {label}
                </Button>
            </CollapsibleSection>

            <BulkEditDialog
                open={bulkEditOpen}
                label={label}
                fields={editableFields}
                selectedIndices={selectedIndices}
                items={items}
                onClose={() => setBulkEditOpen(false)}
                onApply={applyBulkEdit}
            />

            <ConfirmDialog
                open={showRemoveDialog}
                title="Delete selected nodes?"
                description={`${selectedIndices.length} node${selectedIndices.length === 1 ? '' : 's'} will be removed.\n\nThis action cannot be undone.`}
                cancelLabel="Cancel"
                confirmLabel="Delete"
                destructive
                onCancel={() => setShowRemoveDialog(false)}
                onConfirm={confirmRemoveSelected}
            />
        </>
    );
}

function SingleGroupForm({
    node,
    fields,
    groupKey,
    value,
    groupOpenState,
    onGroupOpenChange,
    onGroupOpenStateReplace,
    onChange,
    onRepeatableItemsRemove,
    onRepeatableItemsDuplicate,
}: FormFieldNodeProps) {
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
                const childKey = childGroupKey(groupKey, child.field.fieldName);
                return (
                    <FormFieldNode
                        key={child.field.fieldName}
                        node={child}
                        fields={fields}
                        groupKey={childKey}
                        value={objectValue[child.field.fieldName]}
                        groupOpenState={groupOpenState}
                        onGroupOpenChange={onGroupOpenChange}
                        onGroupOpenStateReplace={onGroupOpenStateReplace}
                        onRepeatableItemsRemove={onRepeatableItemsRemove}
                        onRepeatableItemsDuplicate={onRepeatableItemsDuplicate}
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
                {field.sourceType === 'MASTER_DATA' ? (
                    <span className="ml-1 text-xs font-normal text-muted-foreground">(master data)</span>
                ) : null}
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

export {
    buildInputGroupOpenState,
    collectInputGroupKeys,
    collectInstanceGroupKeys,
};
