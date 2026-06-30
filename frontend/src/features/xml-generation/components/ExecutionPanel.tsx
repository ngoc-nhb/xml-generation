import { useMemo, useState } from 'react';

import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import type { ApiError } from '@/types/api/common';
import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import { useTemplateDetail, type TemplateListItem } from '@/features/templates';
import { DynamicInputForm } from '@/features/xml-generation/components/DynamicInputForm';
import { JsonInputEditor, parseInputJson } from '@/features/xml-generation/components/JsonInputEditor';
import { toSelectedMasterDataPayload } from '@/features/xml-generation/components/MasterDataSelector';
import { PreviewPanel } from '@/features/xml-generation/components/PreviewPanel';
import { ExportToolbar, PreviewToolbar } from '@/features/xml-generation/components/PreviewToolbar';
import { TemplateMappedMasterDataSelector } from '@/features/xml-generation/components/TemplateMappedMasterDataSelector';
import { TemplateSelector } from '@/features/xml-generation/components/TemplateSelector';
import { useResolvedMasterDataTypes } from '@/features/xml-generation/hooks/useResolvedMasterDataTypes';
import { useExportXml, usePreviewXml } from '@/features/xml-generation/hooks/useXmlGeneration';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import {
    buildDefaultFlatFormData,
    countInputFields,
    flatFormDataToInputData,
    serializeFlatFormState,
    type FormScalar,
} from '@/features/xml-generation/utils/inputFormSchema';
import { EMPTY_JSON } from '@/features/xml-generation/utils/jsonEditor';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

type InputMode = 'form' | 'json';

export function ExecutionPanel() {
    const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<TemplateListItem | null>(null);
    const [inputMode, setInputMode] = useState<InputMode>('form');
    const [formDataOverride, setFormDataOverride] = useState<Record<string, FormScalar> | null>(null);
    const [masterDataOverride, setMasterDataOverride] = useState<SelectedMasterDataEntry[] | null>(null);
    const [inputJsonOverride, setInputJsonOverride] = useState<string | null>(null);
    const [jsonError, setJsonError] = useState<string | null>(null);
    const [outputXml, setOutputXml] = useState<string | null>(null);
    const [validationErrors, setValidationErrors] = useState<ApiError[]>([]);
    const [outputSource, setOutputSource] = useState<'preview' | 'export' | null>(null);
    const [pendingTemplate, setPendingTemplate] = useState<{
        templateId: number | null;
        template: TemplateListItem | null;
    } | null>(null);
    const [showSwitchDialog, setShowSwitchDialog] = useState(false);

    const { data: templateDetail, isLoading: templateDetailLoading } = useTemplateDetail(selectedTemplateId ?? undefined);
    const schemaFields = useMemo(() => templateDetail?.schema?.fields ?? [], [templateDetail?.schema?.fields]);
    const schemaMappings = useMemo(() => templateDetail?.schema?.mappings ?? [], [templateDetail?.schema?.mappings]);
    const inputFieldCount = useMemo(() => countInputFields(schemaFields), [schemaFields]);
    const { emptySelections, isLoading: masterTypesLoading } = useResolvedMasterDataTypes(schemaMappings);

    const defaultFormData = useMemo(
        () => (schemaFields.length > 0 ? buildDefaultFlatFormData(schemaFields) : {}),
        [schemaFields],
    );
    const defaultInputJson = useMemo(
        () =>
            schemaFields.length > 0
                ? JSON.stringify(flatFormDataToInputData(schemaFields, defaultFormData), null, 2)
                : EMPTY_JSON,
        [schemaFields, defaultFormData],
    );
    const formBaseline = useMemo(
        () => (schemaFields.length > 0 ? serializeFlatFormState(schemaFields, defaultFormData) : ''),
        [schemaFields, defaultFormData],
    );
    const masterDataBaseline = useMemo(() => JSON.stringify(emptySelections), [emptySelections]);

    const formData = formDataOverride ?? defaultFormData;
    const masterDataSelections = masterDataOverride ?? emptySelections;
    const inputJson = inputJsonOverride ?? defaultInputJson;

    const previewMutation = usePreviewXml();
    const exportMutation = useExportXml();

    const isFormDirty =
        schemaFields.length > 0 && serializeFlatFormState(schemaFields, formData) !== formBaseline;
    const isMasterDirty = JSON.stringify(masterDataSelections) !== masterDataBaseline;
    const isDirty = isFormDirty || isMasterDirty;

    const schemaReady = Boolean(selectedTemplateId && templateDetail?.schema && !masterTypesLoading);
    const executionDisabled =
        selectedTemplateId === null ||
        templateDetailLoading ||
        masterTypesLoading ||
        (inputMode === 'json' && jsonError !== null);

    function buildRequestBody() {
        if (!templateDetail?.schema?.fields) {
            return null;
        }

        if (inputMode === 'form') {
            return {
                inputData: flatFormDataToInputData(templateDetail.schema.fields, formData),
                selectedMasterData: toSelectedMasterDataPayload(masterDataSelections),
            };
        }

        const inputData = parseInputJson(inputJson);
        if (!inputData) {
            setJsonError('Invalid JSON syntax.');
            return null;
        }

        return {
            inputData,
            selectedMasterData: toSelectedMasterDataPayload(masterDataSelections),
        };
    }

    async function handlePreview() {
        if (!selectedTemplateId) {
            return;
        }
        const body = buildRequestBody();
        if (!body) {
            return;
        }
        setValidationErrors([]);
        try {
            const result = await previewMutation.mutateAsync({ templateId: selectedTemplateId, body });
            if (result.kind === 'success') {
                setOutputXml(result.xml);
                setOutputSource('preview');
                setValidationErrors([]);
                toast.success('Preview generated');
            } else {
                setValidationErrors(result.errors);
                setOutputSource('preview');
            }
        } catch (error) {
            toast.error(error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Preview failed');
        }
    }

    async function handleExport() {
        if (!selectedTemplateId) {
            return;
        }
        const body = buildRequestBody();
        if (!body) {
            return;
        }
        setValidationErrors([]);
        try {
            const result = await exportMutation.mutateAsync({ templateId: selectedTemplateId, body });
            if (result.kind === 'success') {
                setOutputXml(result.xml);
                setOutputSource('export');
                setValidationErrors([]);
                toast.success('Export completed');
            } else {
                setValidationErrors(result.errors);
                setOutputSource('export');
            }
        } catch (error) {
            toast.error(error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Export failed');
        }
    }

    function resetInputOverrides() {
        setFormDataOverride(null);
        setMasterDataOverride(null);
        setInputJsonOverride(null);
        setJsonError(null);
    }

    function applyTemplateSelection(templateId: number | null, template: TemplateListItem | null) {
        resetInputOverrides();
        setSelectedTemplateId(templateId);
        setSelectedTemplate(template);
        setOutputXml(null);
        setValidationErrors([]);
        setOutputSource(null);
        setInputMode('form');
    }

    function handleTemplateChange(templateId: number | null, template: TemplateListItem | null) {
        if (isDirty && templateId !== selectedTemplateId) {
            setPendingTemplate({ templateId, template });
            setShowSwitchDialog(true);
            return;
        }
        applyTemplateSelection(templateId, template);
    }

    function handleInputModeChange(mode: InputMode) {
        if (mode === 'json' && schemaFields.length > 0) {
            setInputJsonOverride(JSON.stringify(flatFormDataToInputData(schemaFields, formData), null, 2));
            setJsonError(null);
        }
        setInputMode(mode);
    }

    return (
        <div className="space-y-6">
            <section className="grid gap-6 lg:grid-cols-2">
                <TemplateSelector value={selectedTemplateId} onChange={handleTemplateChange} />
                {selectedTemplateId ? (
                    <TemplateMappedMasterDataSelector
                        mappings={schemaMappings}
                        selections={masterDataSelections}
                        onChange={setMasterDataOverride}
                    />
                ) : (
                    <div className="rounded-md border border-dashed border-border p-4 text-sm text-muted-foreground">
                        Select a template to load required master data selectors.
                    </div>
                )}
            </section>

            <section className="grid gap-6 lg:grid-cols-2">
                <div className="flex h-full flex-col space-y-3">
                    <div className="flex items-center justify-between gap-3">
                        <p className="text-sm font-medium text-foreground">Input data</p>
                        <div className="flex gap-2">
                            <Button
                                type="button"
                                size="sm"
                                variant={inputMode === 'form' ? 'default' : 'outline'}
                                onClick={() => handleInputModeChange('form')}
                            >
                                Form
                            </Button>
                            <Button
                                type="button"
                                size="sm"
                                variant={inputMode === 'json' ? 'default' : 'outline'}
                                onClick={() => handleInputModeChange('json')}
                            >
                                JSON
                            </Button>
                        </div>
                    </div>
                    {templateDetailLoading || masterTypesLoading ? (
                        <LoadingSpinner label="Loading template schema…" />
                    ) : !selectedTemplateId ? (
                        <p className="text-sm text-muted-foreground">Select a template to generate the input form.</p>
                    ) : inputMode === 'form' ? (
                        schemaReady && inputFieldCount > 0 ? (
                            <DynamicInputForm
                                key={selectedTemplateId}
                                fields={schemaFields}
                                value={formData}
                                onChange={(next) => setFormDataOverride(next)}
                            />
                        ) : (
                            <p className="text-sm text-muted-foreground">
                                This template has no INPUT fields. Switch to JSON for manual input or use master data mappings.
                            </p>
                        )
                    ) : (
                        <JsonInputEditor
                            key={selectedTemplateId}
                            value={inputJson}
                            onChange={(next) => setInputJsonOverride(next)}
                            onValidationChange={setJsonError}
                        />
                    )}
                </div>
                <PreviewPanel
                    xml={outputXml}
                    validationErrors={validationErrors}
                    loading={previewMutation.isPending || exportMutation.isPending}
                    source={outputSource}
                />
            </section>

            <section className="flex flex-wrap gap-3 border-t border-border pt-4">
                <PreviewToolbar
                    disabled={executionDisabled}
                    loading={previewMutation.isPending}
                    onPreview={() => void handlePreview()}
                />
                <ExportToolbar
                    disabled={executionDisabled}
                    loading={exportMutation.isPending}
                    onExport={() => void handleExport()}
                />
                {selectedTemplate ? (
                    <p className="self-center text-sm text-muted-foreground">
                        Target: {selectedTemplate.code} — {selectedTemplate.name}
                        {inputFieldCount > 0 ? ` · ${inputFieldCount} input fields` : null}
                    </p>
                ) : null}
            </section>

            <ConfirmDialog
                open={showSwitchDialog}
                title="Switch template?"
                description="You have unsaved input changes. Switching templates will reset the form and master data selections."
                confirmLabel="Switch template"
                destructive
                onConfirm={() => {
                    setShowSwitchDialog(false);
                    if (pendingTemplate) {
                        applyTemplateSelection(pendingTemplate.templateId, pendingTemplate.template);
                    }
                    setPendingTemplate(null);
                }}
                onCancel={() => {
                    setShowSwitchDialog(false);
                    setPendingTemplate(null);
                }}
            />
        </div>
    );
}
