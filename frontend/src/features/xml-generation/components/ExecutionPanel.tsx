import { useEffect, useMemo, useRef, useState } from 'react';

import { LoadingSpinner } from '@/components/loading-spinner';
import { ResizableSidebarLayout } from '@/components/resizable-sidebar-layout';
import { Button } from '@/components/ui/button';
import type { ApiError } from '@/types/api/common';
import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import { useTemplateDetail, type TemplateListItem } from '@/features/templates';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import { useSavedInput, savedInputQueryKeys } from '@/features/saved-input/hooks/useSavedInput';
import {
    DynamicInputForm,
    buildInputGroupOpenState,
    collectInputGroupKeys,
} from '@/features/xml-generation/components/DynamicInputForm';
import { ExportSuccessDialog } from '@/features/xml-generation/components/ExportSuccessDialog';
import { JsonInputEditor, parseInputJson } from '@/features/xml-generation/components/JsonInputEditor';
import {
    applyPickerSelectionsToMasterData,
    buildImportedSelectedMasterData,
} from '@/features/xml-generation/utils/importedMasterDataBuilder';
import { applySavedMasterDataSelections, toSelectedMasterDataPayload } from '@/features/xml-generation/components/MasterDataSelector';
import { ExportToolbar, PreviewToolbar } from '@/features/xml-generation/components/PreviewToolbar';
import { TemplateMappedMasterDataSelector } from '@/features/xml-generation/components/TemplateMappedMasterDataSelector';
import { TemplateSelector } from '@/features/xml-generation/components/TemplateSelector';
import { XmlPreviewDialog } from '@/features/xml-generation/components/XmlPreviewDialog';
import { useResolvedMasterDataTypes } from '@/features/xml-generation/hooks/useResolvedMasterDataTypes';
import { buildDefaultSelections, removeGroupOccurrenceAndReindex } from '@/features/xml-generation/utils/masterDataOccurrences';
import { useExportXml, usePreviewXml } from '@/features/xml-generation/hooks/useXmlGeneration';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import {
    countInputFields,
    formDataToInputData,
    resolveInitialFormData,
    serializeFormState,
    type FormObject,
} from '@/features/xml-generation/utils/inputFormSchema';
import {
    resolveXmlDownloadFilename,
} from '@/features/xml-generation/utils/downloadXml';
import { EMPTY_JSON } from '@/features/xml-generation/utils/jsonEditor';
import { logRuntimeCheckpoint, scheduleArrayLength, summarizeNestedScheduleInfo, summarizeSchemaHierarchy } from '@/features/xml-generation/utils/runtimeInvestigation';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';
import { useQueryClient } from '@tanstack/react-query';

type InputMode = 'form' | 'json';

function hasImportedBaseData(data: Record<string, unknown> | null | undefined): boolean {
    return data != null && typeof data === 'object' && Object.keys(data).length > 0;
}

export function ExecutionPanel() {
    const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<TemplateListItem | null>(null);
    const [inputMode, setInputMode] = useState<InputMode>('form');
    const [formDataOverride, setFormDataOverride] = useState<FormObject | null>(null);
    const [masterDataOverride, setMasterDataOverride] = useState<SelectedMasterDataEntry[] | null>(null);
    const [inputJsonOverride, setInputJsonOverride] = useState<string | null>(null);
    const [jsonError, setJsonError] = useState<string | null>(null);
    const [outputXml, setOutputXml] = useState<string | null>(null);
    const [validationErrors, setValidationErrors] = useState<ApiError[]>([]);
    const [pendingTemplate, setPendingTemplate] = useState<{
        templateId: number | null;
        template: TemplateListItem | null;
    } | null>(null);
    const [showSwitchDialog, setShowSwitchDialog] = useState(false);
    const [configPanelCollapsed, setConfigPanelCollapsed] = useState(false);
    const [previewDialogOpen, setPreviewDialogOpen] = useState(false);
    const [exportSuccessDialogOpen, setExportSuccessDialogOpen] = useState(false);
    const [exportedFilename, setExportedFilename] = useState('');
    const [groupOpenState, setGroupOpenState] = useState<Record<string, boolean>>({});

    const queryClient = useQueryClient();
    const restoredToastTemplateId = useRef<number | null>(null);

    const { data: templateDetail, isLoading: templateDetailLoading } = useTemplateDetail(selectedTemplateId ?? undefined);
    const savedInputQuery = useSavedInput(selectedTemplateId);
    const schemaFields = useMemo(() => templateDetail?.schema?.fields ?? [], [templateDetail?.schema?.fields]);
    const schemaMappings = useMemo(() => templateDetail?.schema?.mappings ?? [], [templateDetail?.schema?.mappings]);
    const inputFieldCount = useMemo(() => countInputFields(schemaFields), [schemaFields]);
    const {
        requiredTypeContexts,
        compileMappings,
        isLoading: masterTypesLoading,
    } = useResolvedMasterDataTypes(schemaMappings, schemaFields);

    const groupKeys = useMemo(() => {
        if (schemaFields.length === 0) {
            return [];
        }
        return collectInputGroupKeys(buildFieldTree(schemaFields), schemaFields);
    }, [schemaFields]);

    useEffect(() => {
        setGroupOpenState(buildInputGroupOpenState(groupKeys, true));
    }, [selectedTemplateId, groupKeys]);

    const hasImportedData = useMemo(
        () => hasImportedBaseData(templateDetail?.sampleInputJson),
        [templateDetail?.sampleInputJson],
    );

    const initialFormData = useMemo(() => {
        if (schemaFields.length === 0) {
            return {};
        }
        return resolveInitialFormData(schemaFields, {
            savedInputData: savedInputQuery.data?.inputData ?? null,
            importedBaseData: templateDetail?.sampleInputJson ?? null,
        });
    }, [schemaFields, savedInputQuery.data, templateDetail?.sampleInputJson]);

    const importedMasterData = useMemo(() => {
        if (!hasImportedData || !templateDetail?.sampleInputJson || schemaFields.length === 0) {
            return {};
        }
        return buildImportedSelectedMasterData(
            schemaFields,
            compileMappings,
            templateDetail.sampleInputJson,
        );
    }, [compileMappings, hasImportedData, schemaFields, templateDetail?.sampleInputJson]);

    const initialMasterDataSelections = useMemo(() => {
        const defaults = buildDefaultSelections(requiredTypeContexts, initialFormData);
        if (masterTypesLoading) {
            return defaults;
        }
        return applySavedMasterDataSelections(defaults, savedInputQuery.data?.selectedMasterData ?? null);
    }, [requiredTypeContexts, initialFormData, masterTypesLoading, savedInputQuery.data]);

    const defaultInputJson = useMemo(
        () =>
            schemaFields.length > 0
                ? JSON.stringify(formDataToInputData(schemaFields, initialFormData), null, 2)
                : EMPTY_JSON,
        [schemaFields, initialFormData],
    );
    const formBaseline = useMemo(
        () => (schemaFields.length > 0 ? serializeFormState(schemaFields, initialFormData) : ''),
        [schemaFields, initialFormData],
    );
    const masterDataBaseline = useMemo(
        () => JSON.stringify(initialMasterDataSelections),
        [initialMasterDataSelections],
    );

    const formData = formDataOverride ?? initialFormData;
    const masterDataSelections = masterDataOverride ?? initialMasterDataSelections;
    const inputJson = inputJsonOverride ?? defaultInputJson;

    const selectedMasterDataPayload = useMemo(() => {
        if (savedInputQuery.data?.selectedMasterData && !masterDataOverride) {
            return savedInputQuery.data.selectedMasterData;
        }
        if (Object.keys(importedMasterData).length > 0) {
            if (masterDataOverride) {
                return applyPickerSelectionsToMasterData(importedMasterData, masterDataSelections);
            }
            return importedMasterData;
        }
        return toSelectedMasterDataPayload(masterDataSelections);
    }, [
        importedMasterData,
        masterDataOverride,
        masterDataSelections,
        savedInputQuery.data?.selectedMasterData,
    ]);

    useEffect(() => {
        if (selectedTemplateId === null) {
            return;
        }
        if (templateDetailLoading || masterTypesLoading || savedInputQuery.isLoading) {
            return;
        }
        if (restoredToastTemplateId.current === selectedTemplateId) {
            return;
        }
        if (!savedInputQuery.data && !hasImportedData) {
            return;
        }
        restoredToastTemplateId.current = selectedTemplateId;
        toast.success(savedInputQuery.data ? 'Loaded previous input.' : 'Loaded imported data.');
    }, [
        hasImportedData,
        savedInputQuery.data,
        savedInputQuery.isLoading,
        selectedTemplateId,
        templateDetailLoading,
        masterTypesLoading,
    ]);

    const previewMutation = usePreviewXml();
    const exportMutation = useExportXml();

    const isFormDirty =
        schemaFields.length > 0 && serializeFormState(schemaFields, formData) !== formBaseline;
    const isMasterDirty = JSON.stringify(masterDataSelections) !== masterDataBaseline;
    const isDirty = isFormDirty || isMasterDirty;

    const schemaReady =
        Boolean(selectedTemplateId && templateDetail?.schema && !masterTypesLoading && !savedInputQuery.isLoading);
    const investigationLoggedTemplateId = useRef<number | null>(null);

    useEffect(() => {
        if (!schemaReady || selectedTemplateId === null) {
            return;
        }
        if (investigationLoggedTemplateId.current === selectedTemplateId) {
            return;
        }
        investigationLoggedTemplateId.current = selectedTemplateId;

        logRuntimeCheckpoint('0_templateSchema_hierarchy', summarizeSchemaHierarchy(schemaFields));
        logRuntimeCheckpoint('1_sampleInputJson_fromApi', {
            raw: templateDetail?.sampleInputJson ?? null,
            summary: summarizeNestedScheduleInfo(templateDetail?.sampleInputJson),
        });
        logRuntimeCheckpoint('2_savedInput_fromApi', savedInputQuery.data ?? null);
        logRuntimeCheckpoint('3_resolvedInitialFormData', {
            raw: initialFormData,
            summary: summarizeNestedScheduleInfo(
                formDataToInputData(schemaFields, initialFormData),
            ),
        });
        logRuntimeCheckpoint('4_reactState_beforeFirstRender', {
            formDataOverride,
            initialFormData,
            effectiveFormData: formData,
            formDataOverrideIsNull: formDataOverride === null,
            nestedSummaryFromEffectiveFormData: summarizeNestedScheduleInfo(
                formDataToInputData(schemaFields, formData),
            ),
        });
    }, [
        formData,
        formDataOverride,
        initialFormData,
        savedInputQuery.data,
        schemaReady,
        selectedTemplateId,
        templateDetail?.sampleInputJson,
    ]);

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
                inputData: formDataToInputData(templateDetail.schema.fields, formData),
                selectedMasterData: selectedMasterDataPayload,
            };
        }

        const inputData = parseInputJson(inputJson);
        if (!inputData) {
            setJsonError('Invalid JSON syntax.');
            return null;
        }

        return {
            inputData,
            selectedMasterData: selectedMasterDataPayload,
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
        logRuntimeCheckpoint('7_previewRequestPayload', {
            templateId: selectedTemplateId,
            body,
            nestedSummary: summarizeNestedScheduleInfo(body.inputData),
            scheduleArrayLengthInInputData: scheduleArrayLength(body.inputData),
        });
        setValidationErrors([]);
        try {
            const result = await previewMutation.mutateAsync({ templateId: selectedTemplateId, body });
            if (result.kind === 'success') {
                setOutputXml(result.xml);
                setValidationErrors([]);
                setPreviewDialogOpen(true);
                toast.success('Preview generated');
            } else {
                setOutputXml(null);
                setValidationErrors(result.errors);
                setPreviewDialogOpen(true);
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
                const filename = resolveXmlDownloadFilename(selectedTemplate?.name);
                setOutputXml(result.xml);
                setValidationErrors([]);
                setExportedFilename(filename);
                setExportSuccessDialogOpen(true);
                toast.success('Export successful');
                if (selectedTemplateId !== null) {
                    void queryClient.invalidateQueries({
                        queryKey: savedInputQueryKeys.byTemplate(selectedTemplateId),
                    });
                }
            } else {
                setOutputXml(null);
                setValidationErrors(result.errors);
                setPreviewDialogOpen(true);
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
        restoredToastTemplateId.current = null;
        setSelectedTemplateId(templateId);
        setSelectedTemplate(template);
        setOutputXml(null);
        setValidationErrors([]);
        setPreviewDialogOpen(false);
        setExportSuccessDialogOpen(false);
        setExportedFilename('');
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
            setInputJsonOverride(JSON.stringify(formDataToInputData(schemaFields, formData), null, 2));
            setJsonError(null);
        }
        setInputMode(mode);
    }

    function handleGroupOpenChange(groupKey: string, open: boolean) {
        setGroupOpenState((current) => ({ ...current, [groupKey]: open }));
    }

    /**
     * Removing a repeatable group occurrence shifts every later occurrence's array index down.
     * Master Data selections are keyed by that same index, so without this they'd end up
     * pointing at the wrong occurrence (or a stale, removed one) after the shift.
     */
    function handleRepeatableItemRemove(groupFieldName: string, removedIndex: number) {
        setMasterDataOverride(removeGroupOccurrenceAndReindex(masterDataSelections, groupFieldName, removedIndex));
    }

    function expandAllGroups() {
        setGroupOpenState(buildInputGroupOpenState(groupKeys, true));
    }

    function collapseAllGroups() {
        setGroupOpenState(buildInputGroupOpenState(groupKeys, false));
    }

    const masterDataSidebar = selectedTemplateId ? (
        <TemplateMappedMasterDataSelector
            fields={schemaFields}
            mappings={schemaMappings}
            formData={formData}
            selections={masterDataSelections}
            onChange={setMasterDataOverride}
        />
    ) : (
        <p className="text-sm text-muted-foreground">Select a template to load master data selectors.</p>
    );

    const configPanel = (
        <div className="space-y-6">
            <section className="space-y-2">
                <TemplateSelector value={selectedTemplateId} onChange={handleTemplateChange} />
            </section>

            <section className="space-y-2">
                <h2 className="text-sm font-medium text-foreground">Master Data</h2>
                {masterDataSidebar}
            </section>

            <section className="space-y-2 border-t border-border pt-4">
                <h2 className="text-sm font-medium text-foreground">Actions</h2>
                <div className="flex flex-col gap-2">
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
                </div>
            </section>
        </div>
    );

    return (
        <div className="flex min-h-0 flex-1 flex-col">
            <ResizableSidebarLayout
                side="right"
                storageKey="xmlgen-config-panel-percent"
                sidebarTitle="Configuration"
                sidebarCollapsed={configPanelCollapsed}
                onSidebarCollapsedChange={setConfigPanelCollapsed}
                sidebar={configPanel}
            >
                <div className="flex min-h-0 flex-1 flex-col pr-4">
                    <div className="mb-3 flex shrink-0 flex-wrap items-center justify-between gap-3">
                        <p className="text-sm font-medium text-foreground">Input Data</p>
                        <div className="flex flex-wrap items-center gap-2">
                            {inputMode === 'form' && groupKeys.length > 0 ? (
                                <>
                                    <Button type="button" size="sm" variant="outline" onClick={expandAllGroups}>
                                        Expand all
                                    </Button>
                                    <Button type="button" size="sm" variant="outline" onClick={collapseAllGroups}>
                                        Collapse all
                                    </Button>
                                </>
                            ) : null}
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
                    {validationErrors.length > 0 ? (
                        <div className="mb-3 shrink-0 rounded-md border border-destructive/30 bg-destructive/5 p-3">
                            <p className="text-sm font-medium text-destructive">Validation errors</p>
                            <ul className="mt-2 list-disc space-y-1 pl-5 text-sm text-destructive">
                                {validationErrors.map((item, index) => (
                                    <li key={`${item.code}-${index}`}>
                                        {item.field ? `${item.field}: ` : ''}
                                        {item.code}
                                    </li>
                                ))}
                            </ul>
                        </div>
                    ) : null}
                    {jsonError ? (
                        <div className="mb-3 shrink-0 rounded-md border border-destructive/30 bg-destructive/5 p-3 text-sm text-destructive">
                            {jsonError}
                        </div>
                    ) : null}
                    <div className="min-h-0 flex-1 overflow-y-auto rounded-md border border-border p-4">
                        {templateDetailLoading || masterTypesLoading || savedInputQuery.isLoading ? (
                            <LoadingSpinner label="Loading template schema…" />
                        ) : !selectedTemplateId ? (
                            <p className="text-sm text-muted-foreground">Select a template to generate the input form.</p>
                        ) : inputMode === 'form' ? (
                            schemaReady && inputFieldCount > 0 ? (
                                <DynamicInputForm
                                    key={selectedTemplateId}
                                    fields={schemaFields}
                                    value={formData}
                                    groupOpenState={groupOpenState}
                                    onGroupOpenChange={handleGroupOpenChange}
                                    onChange={(next) => setFormDataOverride(next)}
                                    onRepeatableItemRemove={handleRepeatableItemRemove}
                                />
                            ) : (
                                <p className="text-sm text-muted-foreground">
                                    This template has no editable data fields. Switch to JSON for manual input or use
                                    master data mappings.
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
                </div>
            </ResizableSidebarLayout>

            <XmlPreviewDialog
                open={previewDialogOpen}
                onOpenChange={setPreviewDialogOpen}
                xml={outputXml}
                validationErrors={validationErrors}
                loading={previewMutation.isPending}
            />

            <ExportSuccessDialog
                open={exportSuccessDialogOpen}
                onOpenChange={setExportSuccessDialogOpen}
                filename={exportedFilename}
                xml={outputXml ?? ''}
            />

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
