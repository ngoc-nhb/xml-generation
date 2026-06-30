import { useMutation } from '@tanstack/react-query';

import * as xmlGenerationApi from '@/features/xml-generation/api/xml-generation.api';
import type { ExecutionRequestBody } from '@/features/xml-generation/types/xml-generation.types';

export function usePreviewXml() {
    return useMutation({
        mutationFn: ({ templateId, body }: { templateId: number; body: ExecutionRequestBody }) =>
            xmlGenerationApi.previewXml(templateId, body),
    });
}

export function useExportXml() {
    return useMutation({
        mutationFn: ({ templateId, body }: { templateId: number; body: ExecutionRequestBody }) =>
            xmlGenerationApi.exportXml(templateId, body),
    });
}
