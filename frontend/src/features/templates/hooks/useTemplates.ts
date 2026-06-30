import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import * as templatesApi from '@/features/templates/api/templates.api';
import { templateQueryKeys } from '@/features/templates/hooks/queryKeys';
import type {
    CreateTemplateRequest,
    TemplateListParams,
    UpdateTemplateRequest,
    UpdateTemplateSchemaRequest,
} from '@/features/templates/types/template.types';

export function useTemplateList(params: TemplateListParams) {
    return useQuery({
        queryKey: templateQueryKeys.list({
            page: params.page ?? 1,
            pageSize: params.pageSize ?? 20,
            keyword: params.keyword ?? '',
        }),
        queryFn: () => templatesApi.fetchTemplates(params),
    });
}

export function useTemplateDetail(id: number | undefined) {
    return useQuery({
        queryKey: templateQueryKeys.detail(id ?? 0),
        queryFn: () => templatesApi.fetchTemplate(id!),
        enabled: id !== undefined && !Number.isNaN(id),
    });
}

export function useCreateTemplate() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: CreateTemplateRequest) => templatesApi.createTemplate(request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: templateQueryKeys.lists() });
        },
    });
}

export function useUpdateTemplate(id: number) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: UpdateTemplateRequest) => templatesApi.updateTemplate(id, request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: templateQueryKeys.detail(id) });
            void queryClient.invalidateQueries({ queryKey: templateQueryKeys.lists() });
        },
    });
}

export function useUpdateTemplateSchema(id: number) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: UpdateTemplateSchemaRequest) => templatesApi.updateTemplateSchema(id, request),
        onSuccess: (schema) => {
            queryClient.setQueryData(
                templateQueryKeys.detail(id),
                (current: Awaited<ReturnType<typeof templatesApi.fetchTemplate>> | undefined) => {
                    if (!current) {
                        return current;
                    }
                    return {
                        ...current,
                        schema,
                    };
                },
            );
        },
    });
}

export function useDeleteTemplate() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: number) => templatesApi.deleteTemplate(id),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: templateQueryKeys.lists() });
        },
    });
}
