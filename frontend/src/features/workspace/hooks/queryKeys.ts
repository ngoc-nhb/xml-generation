import type { WorkspaceListParams } from '@/features/workspace/types/workspace.types';

export const workspaceQueryKeys = {
    all: ['workspaces'] as const,
    lists: () => [...workspaceQueryKeys.all, 'list'] as const,
    list: (page: number, pageSize: number) => [...workspaceQueryKeys.lists(), { page, pageSize }] as const,
    managementList: (params: Required<Pick<WorkspaceListParams, 'page' | 'pageSize'>> & { keyword: string }) =>
        [...workspaceQueryKeys.lists(), 'management', params] as const,
    details: () => [...workspaceQueryKeys.all, 'detail'] as const,
    detail: (id: number) => [...workspaceQueryKeys.details(), id] as const,
};
