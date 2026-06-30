import { useMemo, useState } from 'react';

import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import { SchemaFieldEditor } from '@/features/templates/components/SchemaFieldEditor';
import { SchemaFieldTree } from '@/features/templates/components/SchemaFieldTree';
import { SchemaMappingEditor } from '@/features/templates/components/SchemaMappingEditor';
import { useUnsavedChangesBlocker } from '@/features/templates/hooks/useUnsavedChangesBlocker';
import type { TemplateField, TemplateMapping, TemplateSchema } from '@/features/templates/types/template.types';

interface SchemaEditorSavePayload {
    fields: TemplateField[];
    mappings: TemplateMapping[];
}
import {
    buildFieldTree,
    createEmptyField,
    normalizeSchemaFields,
    removeFieldAndDescendants,
    reorderSibling,
} from '@/features/templates/utils/schemaTree';

interface SchemaEditorProps {
    initialSchema: TemplateSchema | null;
    saving?: boolean;
    onSave: (schema: SchemaEditorSavePayload) => void;
    onCancel: () => void;
}

function cloneSchema(schema: TemplateSchema | null): { fields: TemplateField[]; mappings: TemplateMapping[] } {
    return {
        fields: schema?.fields.map((field) => ({ ...field })) ?? [],
        mappings: schema?.mappings.map((mapping) => ({ ...mapping })) ?? [],
    };
}

function serializeDraft(fields: TemplateField[], mappings: TemplateMapping[]): string {
    return JSON.stringify({ fields, mappings });
}

export function SchemaEditor({ initialSchema, saving, onSave, onCancel }: SchemaEditorProps) {
    const [draft, setDraft] = useState(() => cloneSchema(initialSchema));
    const [selectedFieldName, setSelectedFieldName] = useState<string | null>(draft.fields[0]?.fieldName ?? null);
    const [deleteTarget, setDeleteTarget] = useState<string | null>(null);
    const [showDiscardDialog, setShowDiscardDialog] = useState(false);

    const initialSerialized = useMemo(() => serializeDraft(initialSchema?.fields ?? [], initialSchema?.mappings ?? []), [initialSchema]);
    const isDirty = serializeDraft(draft.fields, draft.mappings) !== initialSerialized;

    const blocker = useUnsavedChangesBlocker({ when: isDirty });
    const discardDialogOpen = showDiscardDialog || blocker.state === 'blocked';

    const tree = useMemo(() => buildFieldTree(draft.fields), [draft.fields]);
    const selectedField = draft.fields.find((field) => field.fieldName === selectedFieldName) ?? null;

    function updateFields(nextFields: TemplateField[]) {
        setDraft((current) => ({ ...current, fields: normalizeSchemaFields(nextFields) }));
    }

    function updateMappings(nextMappings: TemplateMapping[]) {
        setDraft((current) => ({ ...current, mappings: nextMappings }));
    }

    function handleAddRoot() {
        const nextField = createEmptyField(null, draft.fields.filter((field) => !field.parentFieldName).length + 1);
        const fieldName = `Field${draft.fields.length + 1}`;
        const created = { ...nextField, fieldName, xmlName: fieldName, displayName: fieldName };
        updateFields([...draft.fields, created]);
        setSelectedFieldName(created.fieldName);
    }

    function handleAddChild(parentFieldName: string) {
        const siblings = draft.fields.filter((field) => field.parentFieldName === parentFieldName);
        const fieldName = `${parentFieldName}Child${siblings.length + 1}`;
        const created = {
            ...createEmptyField(parentFieldName, siblings.length + 1),
            fieldName,
            xmlName: fieldName,
            displayName: fieldName,
        };
        updateFields([...draft.fields, created]);
        setSelectedFieldName(created.fieldName);
    }

    function handleDeleteField(fieldName: string) {
        const nextFields = removeFieldAndDescendants(draft.fields, fieldName);
        const nextMappings = draft.mappings.filter((mapping) => mapping.fieldName !== fieldName);
        setDraft({ fields: normalizeSchemaFields(nextFields), mappings: nextMappings });
        if (selectedFieldName === fieldName) {
            setSelectedFieldName(nextFields[0]?.fieldName ?? null);
        }
        setDeleteTarget(null);
    }

    function handleFieldChange(field: TemplateField) {
        const previousName = selectedFieldName;
        if (!previousName) {
            return;
        }

        let nextFields = draft.fields.map((item) => {
            if (item.fieldName === previousName) {
                return field;
            }
            if (previousName !== field.fieldName && item.parentFieldName === previousName) {
                return { ...item, parentFieldName: field.fieldName };
            }
            return item;
        });

        nextFields = normalizeSchemaFields(nextFields);

        const nextMappings =
            previousName !== field.fieldName
                ? draft.mappings.map((mapping) =>
                      mapping.fieldName === previousName ? { ...mapping, fieldName: field.fieldName } : mapping,
                  )
                : draft.mappings;

        setDraft({ fields: nextFields, mappings: nextMappings });
        setSelectedFieldName(field.fieldName);
    }

    function handleSave() {
        onSave({ fields: normalizeSchemaFields(draft.fields), mappings: draft.mappings });
    }

    function handleConfirmDiscard() {
        setShowDiscardDialog(false);
        if (blocker.state === 'blocked') {
            blocker.proceed?.();
            return;
        }
        onCancel();
    }

    return (
        <div className="space-y-6">
            <div className="grid gap-6 xl:grid-cols-[minmax(0,1fr)_minmax(0,1.2fr)]">
                <SchemaFieldTree
                    nodes={tree}
                    selectedFieldName={selectedFieldName}
                    onSelect={setSelectedFieldName}
                    onAddRoot={handleAddRoot}
                    onAddChild={handleAddChild}
                    onMove={(fieldName, direction) => updateFields(reorderSibling(draft.fields, fieldName, direction))}
                    onDelete={setDeleteTarget}
                />
                <SchemaFieldEditor field={selectedField} parentOptions={draft.fields} onChange={handleFieldChange} />
            </div>

            <SchemaMappingEditor fields={draft.fields} mappings={draft.mappings} onChange={updateMappings} />

            <div className="flex gap-3 border-t border-border pt-4">
                <Button onClick={handleSave} disabled={saving}>
                    {saving ? 'Saving schema…' : 'Save schema'}
                </Button>
                <Button variant="outline" onClick={() => (isDirty ? setShowDiscardDialog(true) : onCancel())} disabled={saving}>
                    Cancel
                </Button>
            </div>

            <ConfirmDialog
                open={deleteTarget !== null}
                title="Delete field"
                description="Delete this field and all of its descendants?"
                confirmLabel="Delete field"
                destructive
                onConfirm={() => deleteTarget && handleDeleteField(deleteTarget)}
                onCancel={() => setDeleteTarget(null)}
            />

            <ConfirmDialog
                open={discardDialogOpen}
                title="Discard unsaved changes?"
                description="Your schema changes will be lost if you leave without saving."
                confirmLabel="Discard changes"
                destructive
                onConfirm={handleConfirmDiscard}
                onCancel={() => {
                    setShowDiscardDialog(false);
                    blocker.reset?.();
                }}
            />
        </div>
    );
}
