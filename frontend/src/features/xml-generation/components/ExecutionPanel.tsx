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
    applySavedMasterDataSelections,
    toSelectedMasterDataPayload,
} from '@/features/xml-generation/components/MasterDataSelector';
import { ExportToolbar, PreviewToolbar } from '@/features/xml-generation/components/PreviewToolbar';
import { TemplateMappedMasterDataSelector } from '@/features/xml-generation/components/TemplateMappedMasterDataSelector';
import { TemplateSelector } from '@/features/xml-generation/components/TemplateSelector';
import { XmlPreviewDialog } from '@/features/xml-generation/components/XmlPreviewDialog';
import { useResolvedMasterDataTypes } from '@/features/xml-generation/hooks/useResolvedMasterDataTypes';
import { useExportXml, usePreviewXml } from '@/features/xml-generation/hooks/useXmlGeneration';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import {
    buildDefaultFormData,
    countInputFields,
    formDataToInputData,
    mergeSavedInputIntoFormData,
    serializeFormState,
    type FormObject,
} from '@/features/xml-generation/utils/inputFormSchema';
import {
    resolveXmlDownloadFilename,
} from '@/features/xml-generation/utils/downloadXml';
import { EMPTY_JSON } from '@/features/xml-generation/utils/jsonEditor';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';
import { useQueryClient } from '@tanstack/react-query';

type InputMode = 'form' | 'json';
type InitMode = 'empty' | 'sample';

function hasSampleInputData(sample: Record<string, unknown> | null | undefined): boolean {
    return sample != null && typeof sample === 'object' && Object.keys(sample).length > 0;
}

export function ExecutionPanel() {
    const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<TemplateListItem | null>(null);
    const [inputMode, setInputMode] = useState<InputMode>('form');
    const [initMode, setInitMode] = useState<InitMode>('empty');
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
    const [masterDataSidebarCollapsed, setMasterDataSidebarCollapsed] = useState(false);
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
    const { emptySelections, isLoading: masterTypesLoading } = useResolvedMasterDataTypes(schemaMappings);

    const groupKeys = useMemo(() => {
        if (schemaFields.length === 0) {
            return [];
        }
        return collectInputGroupKeys(buildFieldTree(schemaFields), schemaFields);
    }, [schemaFields]);

    useEffect(() => {
        setGroupOpenState(buildInputGroupOpenState(groupKeys, true));
    }, [selectedTemplateId, groupKeys]);

    const defaultFormData = useMemo(
        () => (schemaFields.length > 0 ? buildDefaultFormData(schemaFields) : {}),
        [schemaFields],
    );

    const hasSampleData = useMemo(
        () => hasSampleInputData(templateDetail?.sampleInputJson),
        [templateDetail?.sampleInputJson],
    );

    const emptyInitFormData = useMemo(() => {
        if (schemaFields.length === 0) {
            return {};
        }
        if (!savedInputQuery.data) {
            return defaultFormData;
        }
        return mergeSavedInputIntoFormData(defaultFormData, savedInputQuery.data.inputData);
    }, [schemaFields, defaultFormData, savedInputQuery.data]);

    const sampleInitFormData = useMemo(() => {
        if (schemaFields.length === 0 || !hasSampleData || !templateDetail?.sampleInputJson) {
            return defaultFormData;
        }
        return mergeSavedInputIntoFormData(defaultFormData, templateDetail.sampleInputJson);
    }, [schemaFields, defaultFormData, hasSampleData, templateDetail?.sampleInputJson]);

    const initialFormData = useMemo(() => {
        if (initMode === 'sample' && hasSampleData) {
            return sampleInitFormData;
        }
        return emptyInitFormData;
    }, [initMode, hasSampleData, sampleInitFormData, emptyInitFormData]);

    const initialMasterDataSelections = useMemo(() => {
        if (masterTypesLoading) {
            return emptySelections;
        }
        return applySavedMasterDataSelections(emptySelections, savedInputQuery.data?.selectedMasterData ?? null);
    }, [emptySelections, masterTypesLoading, savedInputQuery.data]);

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

    useEffect(() => {
        if (!savedInputQuery.data || selectedTemplateId === null) {
            return;
        }
        if (templateDetailLoading || masterTypesLoading || savedInputQuery.isLoading) {
            return;
        }
        if (restoredToastTemplateId.current === selectedTemplateId) {
            return;
        }
        restoredToastTemplateId.current = selectedTemplateId;
        toast.success('Loaded previous input.');
    }, [
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
        setInitMode('empty');
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

    function handleInitModeChange(mode: InitMode) {
        if (mode === initMode) {
            return;
        }
        setInitMode(mode);
        setFormDataOverride(null);
        setInputJsonOverride(null);
        setJsonError(null);
    }

    function handleGroupOpenChange(groupKey: string, open: boolean) {
        setGroupOpenState((current) => ({ ...current, [groupKey]: open }));
    }

    function expandAllGroups() {
        setGroupOpenState(buildInputGroupOpenState(groupKeys, true));
    }

    function collapseAllGroups() {
        setGroupOpenState(buildInputGroupOpenState(groupKeys, false));
    }

    const masterDataSidebar = selectedTemplateId ? (
        <TemplateMappedMasterDataSelector
            mappings={schemaMappings}
            selections={masterDataSelections}
            onChange={setMasterDataOverride}
        />
    ) : (
        <p className="text-sm text-muted-foreground">Select a template to load master data selectors.</p>
    );

    return (
        <div className="flex h-[calc(100vh-7rem)] min-h-[32rem] flex-col">
            <div className="sticky top-0 z-10 shrink-0 space-y-4 border-b border-border bg-background pb-4">
                <div className="flex flex-wrap items-start justify-between gap-4">
                    <div>
                        <h1 className="text-2xl font-semibold text-foreground">XML Generation</h1>
                        <p className="text-sm text-muted-foreground">
                            Select a template, provide input JSON and master data, then preview or export XML.
                        </p>
                    </div>
                    <div className="flex flex-wrap gap-2">
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
                </div>
                <div className="max-w-md">
                    <TemplateSelector value={selectedTemplateId} onChange={handleTemplateChange} />
                </div>
                {selectedTemplateId && hasSampleData ? (
                    <div className="space-y-2">
                        <p className="text-sm font-medium text-foreground">Initialization</p>
                        <div className="flex flex-wrap gap-2">
                            <Button
                                type="button"
                                size="sm"
                                variant={initMode === 'empty' ? 'default' : 'outline'}
                                onClick={() => handleInitModeChange('empty')}
                            >
                                New Empty Input
                            </Button>
                            <Button
                                type="button"
                                size="sm"
                                variant={initMode === 'sample' ? 'default' : 'outline'}
                                onClick={() => handleInitModeChange('sample')}
                            >
                                Load Sample Data
                            </Button>
                        </div>
                    </div>
                ) : null}
            </div>

            <div className="flex min-h-0 flex-1 flex-col py-4">
                <ResizableSidebarLayout
                    sidebarCollapsed={masterDataSidebarCollapsed}
                    onSidebarCollapsedChange={setMasterDataSidebarCollapsed}
                    sidebar={masterDataSidebar}
                >
                    <div className="flex min-h-0 flex-1 flex-col pl-4">
                        <div className="mb-3 flex shrink-0 flex-wrap items-center justify-between gap-3">
                            <p className="text-sm font-medium text-foreground">Input data</p>
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
                        <div className="min-h-0 flex-1 overflow-y-auto rounded-md border border-border p-4">
                            {templateDetailLoading || masterTypesLoading || savedInputQuery.isLoading ? (
                                <LoadingSpinner label="Loading template schema…" />
                            ) : !selectedTemplateId ? (
                                <p className="text-sm text-muted-foreground">Select a template to generate the input form.</p>
                            ) : inputMode === 'form' ? (
                                schemaReady && inputFieldCount > 0 ? (
                                    <DynamicInputForm
                                        key={`${selectedTemplateId}-${initMode}`}
                                        fields={schemaFields}
                                        value={formData}
                                        groupOpenState={groupOpenState}
                                        onGroupOpenChange={handleGroupOpenChange}
                                        onChange={(next) => setFormDataOverride(next)}
                                    />
                                ) : (
                                    <p className="text-sm text-muted-foreground">
                                        This template has no INPUT fields. Switch to JSON for manual input or use master
                                        data mappings.
                                    </p>
                                )
                            ) : (
                                <JsonInputEditor
                                    key={`${selectedTemplateId}-${initMode}`}
                                    value={inputJson}
                                    onChange={(next) => setInputJsonOverride(next)}
                                    onValidationChange={setJsonError}
                                />
                            )}
                        </div>
                    </div>
                </ResizableSidebarLayout>
            </div>

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
