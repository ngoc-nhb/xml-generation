import { useMemo, useState } from 'react';
import { flushSync } from 'react-dom';

import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import { SchemaFieldEditor } from '@/features/templates/components/SchemaFieldEditor';
import { SchemaFieldTree } from '@/features/templates/components/SchemaFieldTree';
import { useUnsavedChangesBlocker } from '@/features/templates/hooks/useUnsavedChangesBlocker';
import type { DraftTemplateField, TemplateMapping, TemplateSchema } from '@/features/templates/types/template.types';
import {
    applyValueNodeDefaults,
    buildDraftFieldTree,
    createEmptyField,
    duplicateDraftFieldSubtree,
    findDuplicateFieldNames,
    normalizeAllDraftFieldMetadata,
    normalizeDraftFieldMetadata,
    normalizeDraftSchemaFields,
    removeDraftFieldAndDescendants,
    reorderDraftSiblingToIndex,
    serializeSchemaState,
    toApiFields,
    toDraftFields,
    toDraftFieldsFromImport,
} from '@/features/templates/utils/schemaTree';
import { toast } from '@/providers/ToastProvider';

interface SchemaEditorSavePayload {
    fields: ReturnType<typeof toApiFields>;
    mappings: TemplateMapping[];
}

interface SchemaEditorProps {
    initialSchema: TemplateSchema | null;
    importMode?: boolean;
    saving?: boolean;
    onSave: (schema: SchemaEditorSavePayload) => Promise<void>;
    onSaved?: () => void;
    onCancel: () => void;
}

function cloneSchema(
    schema: TemplateSchema | null,
    importMode = false,
): { fields: DraftTemplateField[]; mappings: TemplateMapping[] } {
    const fields = importMode
        ? normalizeDraftSchemaFields(
              normalizeAllDraftFieldMetadata(
                  toDraftFieldsFromImport(
                      (schema?.fields ?? []).map((field) => ({
                          ...field,
                          imported: true,
                      })),
                  ),
              ),
          )
        : normalizeDraftSchemaFields(normalizeAllDraftFieldMetadata(toDraftFields(schema?.fields ?? [])));
    return {
        fields,
        mappings: schema?.mappings.map((mapping) => ({ ...mapping })) ?? [],
    };
}

export function SchemaEditor({ initialSchema, importMode, saving, onSave, onSaved, onCancel }: SchemaEditorProps) {
    const [draft, setDraft] = useState(() => cloneSchema(initialSchema, importMode));
    const [selectedClientId, setSelectedClientId] = useState<string | null>(draft.fields[0]?.clientId ?? null);
    const [deleteTargetClientId, setDeleteTargetClientId] = useState<string | null>(null);
    const [showDiscardDialog, setShowDiscardDialog] = useState(false);
    const [baselineSerialized, setBaselineSerialized] = useState(() => serializeSchemaState(draft.fields, draft.mappings));
    const [allowNavigation, setAllowNavigation] = useState(false);
    const [mappingValidationErrors, setMappingValidationErrors] = useState<Record<string, string>>({});

    const isDirty = serializeSchemaState(draft.fields, draft.mappings) !== baselineSerialized;
    const blocker = useUnsavedChangesBlocker({ when: isDirty && !allowNavigation });
    const discardDialogOpen = showDiscardDialog || blocker.state === 'blocked';

    const tree = useMemo(() => buildDraftFieldTree(draft.fields), [draft.fields]);
    const selectedField = draft.fields.find((field) => field.clientId === selectedClientId) ?? null;
    const selectedFieldMapping = selectedField
        ? draft.mappings.find((mapping) => mapping.fieldName === selectedField.fieldName) ?? null
        : null;

    function upsertFieldMapping(fieldName: string, masterDataFieldId: number | null) {
        setMappingValidationErrors((current) => {
            if (!(fieldName in current)) {
                return current;
            }
            const next = { ...current };
            delete next[fieldName];
            return next;
        });

        setDraft((current) => {
            const existingIndex = current.mappings.findIndex((mapping) => mapping.fieldName === fieldName);
            if (masterDataFieldId === null) {
                if (existingIndex < 0) {
                    return current;
                }
                return {
                    ...current,
                    mappings: current.mappings.filter((mapping) => mapping.fieldName !== fieldName),
                };
            }

            if (existingIndex >= 0) {
                const nextMappings = current.mappings.map((mapping, index) =>
                    index === existingIndex ? { ...mapping, masterDataFieldId } : mapping,
                );
                return { ...current, mappings: nextMappings };
            }

            return {
                ...current,
                mappings: [...current.mappings, { fieldName, masterDataFieldId }],
            };
        });
    }

    function handleMasterDataMappingChange(masterDataFieldId: number | null) {
        if (!selectedField) {
            return;
        }
        upsertFieldMapping(selectedField.fieldName, masterDataFieldId);
    }

    function updateFields(nextFields: DraftTemplateField[]) {
        setDraft((current) => ({ ...current, fields: normalizeDraftSchemaFields(nextFields) }));
    }

    function handleAddRoot() {
        const fieldName = `Field${draft.fields.length + 1}`;
        const created = normalizeDraftFieldMetadata({
            ...createEmptyField(null, null, draft.fields.filter((field) => !field.parentClientId).length + 1),
            fieldName,
            xmlName: fieldName,
            displayName: fieldName,
        });
        updateFields([...draft.fields, created]);
        setSelectedClientId(created.clientId);
    }

    function handleAddChild(parentClientId: string) {
        const parent = draft.fields.find((field) => field.clientId === parentClientId);
        if (!parent) {
            return;
        }

        const siblings = draft.fields.filter((field) => field.parentClientId === parentClientId);
        const fieldName = `${parent.fieldName}Child${siblings.length + 1}`;
        const created = applyValueNodeDefaults({
            ...createEmptyField(parentClientId, parent.fieldName, siblings.length + 1),
            fieldName,
            xmlName: fieldName,
            displayName: fieldName,
        });
        updateFields([...draft.fields, created]);
        setSelectedClientId(created.clientId);
    }

    function handleDuplicateField(clientId: string) {
        const { fields: nextFields, mappings: nextMappings, duplicatedRootClientId } = duplicateDraftFieldSubtree(
            draft.fields,
            clientId,
            draft.mappings,
        );
        setDraft({ fields: nextFields, mappings: nextMappings });
        if (duplicatedRootClientId) {
            setSelectedClientId(duplicatedRootClientId);
        }
    }

    function handleReorder(clientId: string, newIndex: number) {
        updateFields(reorderDraftSiblingToIndex(draft.fields, clientId, newIndex));
    }

    function handleDeleteField(clientId: string) {
        const target = draft.fields.find((field) => field.clientId === clientId);
        if (!target) {
            return;
        }

        const nextFields = removeDraftFieldAndDescendants(draft.fields, clientId);
        const remainingWithSameName = nextFields.filter((field) => field.fieldName === target.fieldName);
        const nextMappings =
            remainingWithSameName.length > 0
                ? draft.mappings
                : draft.mappings.filter((mapping) => mapping.fieldName !== target.fieldName);
        setDraft({ fields: normalizeDraftSchemaFields(nextFields), mappings: nextMappings });
        if (selectedClientId === clientId) {
            setSelectedClientId(nextFields[0]?.clientId ?? null);
        }
        setDeleteTargetClientId(null);
    }

    function handleFieldChange(field: DraftTemplateField) {
        const previous = selectedField;
        if (!previous) {
            return;
        }

        let nextFields = draft.fields.map((item) => {
            if (item.clientId === previous.clientId) {
                return normalizeDraftFieldMetadata(field);
            }
            if (item.parentClientId === previous.clientId) {
                return { ...item, parentFieldName: field.fieldName };
            }
            return item;
        });

        nextFields = normalizeDraftSchemaFields(nextFields);

        const fieldsWithPreviousName = draft.fields.filter((item) => item.fieldName === previous.fieldName);
        const nextMappings =
            fieldsWithPreviousName.length === 1 && previous.fieldName !== field.fieldName
                ? draft.mappings.map((mapping) =>
                      mapping.fieldName === previous.fieldName ? { ...mapping, fieldName: field.fieldName } : mapping,
                  )
                : draft.mappings;

        setDraft({ fields: nextFields, mappings: nextMappings });
        setSelectedClientId(field.clientId);
    }

    async function handleSave() {
        const normalizedFields = normalizeDraftSchemaFields(normalizeAllDraftFieldMetadata(draft.fields));
        const duplicates = findDuplicateFieldNames(normalizedFields);
        if (duplicates.length > 0) {
            toast.error(`Duplicate field names: ${duplicates.join(', ')}`);
            return;
        }

        const nextMappingErrors: Record<string, string> = {};
        for (const field of normalizedFields) {
            if (field.sourceType !== 'MASTER_DATA') {
                continue;
            }
            const mapping = draft.mappings.find((item) => item.fieldName === field.fieldName);
            if (!mapping?.masterDataFieldId) {
                nextMappingErrors[field.fieldName] = 'Please select a Master Data field.';
            }
        }

        if (Object.keys(nextMappingErrors).length > 0) {
            setMappingValidationErrors(nextMappingErrors);
            toast.error('Please select a Master Data field for all master data fields.');
            return;
        }

        setMappingValidationErrors({});

        const payload = { fields: toApiFields(normalizedFields), mappings: draft.mappings };
        const savedSerialized = serializeSchemaState(normalizedFields, draft.mappings);

        setAllowNavigation(true);
        try {
            await onSave(payload);
            flushSync(() => {
                setDraft((current) => ({ ...current, fields: normalizedFields }));
                setBaselineSerialized(savedSerialized);
            });
            onSaved?.();
        } catch {
            setAllowNavigation(false);
        }
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
                    selectedClientId={selectedClientId}
                    onSelect={setSelectedClientId}
                    onAddRoot={handleAddRoot}
                    onAddChild={handleAddChild}
                    onReorder={(clientId, newIndex) => handleReorder(clientId, newIndex)}
                    onDuplicate={handleDuplicateField}
                    onDelete={setDeleteTargetClientId}
                />
                <SchemaFieldEditor
                    field={selectedField}
                    parentOptions={draft.fields}
                    masterDataFieldId={selectedFieldMapping?.masterDataFieldId ?? null}
                    masterDataMappingError={
                        selectedField ? mappingValidationErrors[selectedField.fieldName] ?? null : null
                    }
                    onChange={handleFieldChange}
                    onMasterDataFieldIdChange={handleMasterDataMappingChange}
                />
            </div>

            <div className="flex gap-3 border-t border-border pt-4">
                <Button onClick={() => void handleSave()} disabled={saving}>
                    {saving ? 'Saving schema…' : 'Save schema'}
                </Button>
                <Button variant="outline" onClick={() => (isDirty ? setShowDiscardDialog(true) : onCancel())} disabled={saving}>
                    Cancel
                </Button>
            </div>

            <ConfirmDialog
                open={deleteTargetClientId !== null}
                title="Delete field"
                description="Delete this field and all of its descendants?"
                confirmLabel="Delete field"
                destructive
                onConfirm={() => deleteTargetClientId && handleDeleteField(deleteTargetClientId)}
                onCancel={() => setDeleteTargetClientId(null)}
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
