import { deleteData, getData, getPaginatedData, postData, putData } from '@/api/client';
import type { PageMeta } from '@/types/api/common';
import type {
    CreateWorkspaceRequest,
    CreateWorkspaceResponse,
    UpdateWorkspaceRequest,
    WorkspaceDetail,
    WorkspaceListItem,
    WorkspaceListParams,
    WorkspaceSummary,
} from '@/features/workspace/types/workspace.types';

export async function fetchWorkspaces(
    page = 1,
    pageSize = 20,
): Promise<{ items: WorkspaceSummary[]; meta: PageMeta }> {
    const { data, meta } = await getPaginatedData<WorkspaceSummary[]>('/workspaces', {
        page,
        pageSize,
    });
    return { items: data, meta };
}

async function enrichWorkspaceListItems(items: WorkspaceSummary[]): Promise<WorkspaceListItem[]> {
    return Promise.all(
        items.map(async (item) => {
            const detail = await fetchWorkspace(item.id);
            return {
                ...item,
                description: detail.description,
                createdAt: detail.createdAt,
            };
        }),
    );
}

export async function fetchWorkspaceList(
    params: WorkspaceListParams,
): Promise<{ items: WorkspaceListItem[]; meta: PageMeta }> {
    const page = params.page ?? 1;
    const pageSize = params.pageSize ?? 20;
    const keyword = params.keyword?.trim().toLowerCase() ?? '';

    if (keyword) {
        const { items } = await fetchWorkspaces(1, 100);
        const filtered = items.filter(
            (workspace) =>
                workspace.name.toLowerCase().includes(keyword) ||
                workspace.code.toLowerCase().includes(keyword),
        );
        const start = (page - 1) * pageSize;
        const pageItems = filtered.slice(start, start + pageSize);
        return {
            items: await enrichWorkspaceListItems(pageItems),
            meta: {
                page,
                pageSize,
                totalRecords: filtered.length,
                totalPages: Math.max(1, Math.ceil(filtered.length / pageSize)),
            },
        };
    }

    const { items, meta } = await fetchWorkspaces(page, pageSize);
    return {
        items: await enrichWorkspaceListItems(items),
        meta,
    };
}

export async function fetchWorkspace(id: number): Promise<WorkspaceDetail> {
    return getData<WorkspaceDetail>(`/workspaces/${id}`);
}

export async function createWorkspace(request: CreateWorkspaceRequest): Promise<CreateWorkspaceResponse> {
    return postData<CreateWorkspaceResponse, CreateWorkspaceRequest>('/workspaces', request);
}

export async function updateWorkspace(id: number, request: UpdateWorkspaceRequest): Promise<WorkspaceDetail> {
    await putData(`/workspaces/${id}`, request);
    return fetchWorkspace(id);
}

export async function deleteWorkspace(id: number): Promise<void> {
    await deleteData(`/workspaces/${id}`);
}
