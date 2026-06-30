import { useState } from 'react';

import type { ApiError } from '@/types/api/common';
import type { TemplateListItem } from '@/features/templates';
import { JsonInputEditor, parseInputJson } from '@/features/xml-generation/components/JsonInputEditor';
import { MasterDataSelector, toSelectedMasterDataPayload } from '@/features/xml-generation/components/MasterDataSelector';
import { PreviewPanel } from '@/features/xml-generation/components/PreviewPanel';
import { ExportToolbar, PreviewToolbar } from '@/features/xml-generation/components/PreviewToolbar';
import { TemplateSelector } from '@/features/xml-generation/components/TemplateSelector';
import { useExportXml, usePreviewXml } from '@/features/xml-generation/hooks/useXmlGeneration';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import { EMPTY_JSON } from '@/features/xml-generation/utils/jsonEditor';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function ExecutionPanel() {
    const [selectedTemplateId, setSelectedTemplateId] = useState<number | null>(null);
    const [selectedTemplate, setSelectedTemplate] = useState<TemplateListItem | null>(null);
    const [inputJson, setInputJson] = useState(EMPTY_JSON);
    const [jsonError, setJsonError] = useState<string | null>(null);
    const [masterDataSelections, setMasterDataSelections] = useState<SelectedMasterDataEntry[]>([]);
    const [outputXml, setOutputXml] = useState<string | null>(null);
    const [validationErrors, setValidationErrors] = useState<ApiError[]>([]);
    const [outputSource, setOutputSource] = useState<'preview' | 'export' | null>(null);

    const previewMutation = usePreviewXml();
    const exportMutation = useExportXml();

    const executionDisabled = selectedTemplateId === null || jsonError !== null;

    function buildRequestBody() {
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

    function handleTemplateChange(templateId: number | null, template: TemplateListItem | null) {
        setSelectedTemplateId(templateId);
        setSelectedTemplate(template);
        setOutputXml(null);
        setValidationErrors([]);
        setOutputSource(null);
        setMasterDataSelections([]);
    }

    return (
        <div className="space-y-6">
            <section className="grid gap-6 lg:grid-cols-2">
                <TemplateSelector value={selectedTemplateId} onChange={handleTemplateChange} />
                <MasterDataSelector selections={masterDataSelections} onChange={setMasterDataSelections} />
            </section>

            <section className="grid gap-6 lg:grid-cols-2">
                <JsonInputEditor value={inputJson} onChange={setInputJson} onValidationChange={setJsonError} />
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
                    </p>
                ) : null}
            </section>
        </div>
    );
}
