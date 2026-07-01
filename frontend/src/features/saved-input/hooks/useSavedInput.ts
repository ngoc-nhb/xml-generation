import { useQuery } from '@tanstack/react-query';

import { fetchSavedInput } from '@/features/saved-input/api/saved-input.api';

export const savedInputQueryKeys = {
    byTemplate: (templateId: number) => ['saved-input', templateId] as const,
};

export function useSavedInput(templateId: number | null) {
    return useQuery({
        queryKey: savedInputQueryKeys.byTemplate(templateId ?? 0),
        queryFn: () => fetchSavedInput(templateId!),
        enabled: templateId !== null,
        retry: false,
    });
}
