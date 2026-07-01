import { apiClient } from '@/api/client';
import { ApiClientError, type ApiResponse } from '@/types/api/common';
import type { SavedInput } from '@/features/saved-input/types/saved-input.types';

export async function fetchSavedInput(templateId: number): Promise<SavedInput | null> {
    try {
        const response = await apiClient.get<ApiResponse<SavedInput>>(`/saved-inputs/template/${templateId}`);
        return response.data.data;
    } catch (error) {
        if (error instanceof ApiClientError && error.status === 404) {
            return null;
        }
        throw error;
    }
}
