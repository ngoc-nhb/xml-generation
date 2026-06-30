import { deleteData, getData, getPaginatedData, postData, putData } from '@/api/client';
import type { PageMeta } from '@/types/api/common';
import type {
    CreateTemplateRequest,
    CreateTemplateResponse,
    TemplateDetail,
    TemplateListItem,
    TemplateListParams,
    TemplateSchema,
    UpdateTemplateRequest,
    UpdateTemplateSchemaRequest,
} from '@/features/templates/types/template.types';

export async function fetchTemplates(
    params: TemplateListParams,
): Promise<{ items: TemplateListItem[]; meta: PageMeta }> {
    const { data, meta } = await getPaginatedData<TemplateListItem[]>('/templates', {
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 20,
        keyword: params.keyword || undefined,
        status: params.status,
    });
    return { items: data, meta };
}

export async function fetchTemplate(id: number): Promise<TemplateDetail> {
    return getData<TemplateDetail>(`/templates/${id}`);
}

export async function createTemplate(request: CreateTemplateRequest): Promise<CreateTemplateResponse> {
    return postData<CreateTemplateResponse, CreateTemplateRequest>('/templates', request);
}

export async function updateTemplate(id: number, request: UpdateTemplateRequest): Promise<TemplateDetail> {
    await putData(`/templates/${id}`, request);
    return fetchTemplate(id);
}

export async function updateTemplateSchema(
    id: number,
    request: UpdateTemplateSchemaRequest,
): Promise<TemplateSchema | null> {
    return putData<TemplateSchema | null, UpdateTemplateSchemaRequest>(`/templates/${id}/schema`, request);
}

export async function deleteTemplate(id: number): Promise<void> {
    await deleteData(`/templates/${id}`);
}
