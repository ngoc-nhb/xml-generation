/**
 * XML Generation feature public API.
 *
 * Orchestrates Preview and Export REST endpoints only.
 * No runtime engine types exposed.
 */

export { XmlGenerationPage } from '@/features/xml-generation/pages/XmlGenerationPage';

export { useExportXml, usePreviewXml } from '@/features/xml-generation/hooks/useXmlGeneration';

export type {
    ExecutionRequestBody,
    ExportResult,
    PreviewResult,
    SelectedMasterDataEntry,
} from '@/features/xml-generation/types/xml-generation.types';
