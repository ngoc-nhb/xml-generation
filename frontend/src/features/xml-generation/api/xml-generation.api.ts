import { apiClient } from '@/api/client';
import { ApiClientError, type ApiError, type ApiResponse } from '@/types/api/common';
import type {
    ExecutionRequestBody,
    ExportResult,
    PreviewResult,
} from '@/features/xml-generation/types/xml-generation.types';

interface XmlResponse {
    xml: string;
}

function toValidationResult(errors: ApiError[] | undefined): PreviewResult {
    return {
        kind: 'validation',
        errors: errors && errors.length > 0 ? errors : [{ code: 'VALIDATION_FAILED' }],
    };
}

async function postExecution<T extends PreviewResult | ExportResult>(
    url: string,
    body: ExecutionRequestBody,
): Promise<T> {
    try {
        const response = await apiClient.post<ApiResponse<XmlResponse>>(url, body);
        const payload = response.data;
        if (payload.success === false) {
            return toValidationResult(payload.errors) as T;
        }
        return { kind: 'success', xml: payload.data.xml } as T;
    } catch (error) {
        if (error instanceof ApiClientError) {
            if (error.status === 200 || error.errors.length > 0) {
                return toValidationResult(error.errors) as T;
            }
        }
        throw error;
    }
}

export async function previewXml(templateId: number, body: ExecutionRequestBody): Promise<PreviewResult> {
    return postExecution(`/templates/${templateId}/preview`, body);
}

export async function exportXml(templateId: number, body: ExecutionRequestBody): Promise<ExportResult> {
    return postExecution(`/templates/${templateId}/export`, body);
}
