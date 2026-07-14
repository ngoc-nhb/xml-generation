import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import * as workspaceApi from '@/features/workspace/api/workspace.api';
import { workspaceQueryKeys } from '@/features/workspace/hooks/queryKeys';
import type {
    CreatePersonalWorkspaceRequest,
    CreateWorkspaceRequest,
    UpdateWorkspaceRequest,
    WorkspaceListParams,
    WorkspacePermissionCode,
} from '@/features/workspace/types/workspace.types';
import { useAuth } from '@/providers/AuthProvider';

const SWITCHER_LIST_PAGE_SIZE = 100;

export function useWorkspaceList() {
    const { isAuthenticated } = useAuth();

    return useQuery({
        queryKey: workspaceQueryKeys.list(1, SWITCHER_LIST_PAGE_SIZE),
        queryFn: () => workspaceApi.fetchWorkspaces(1, SWITCHER_LIST_PAGE_SIZE),
        select: (result) => result.items,
        enabled: isAuthenticated,
    });
}

export function useWorkspaceManagementList(params: WorkspaceListParams) {
    const page = params.page ?? 1;
    const pageSize = params.pageSize ?? 20;
    const keyword = params.keyword ?? '';

    return useQuery({
        queryKey: workspaceQueryKeys.managementList({ page, pageSize, keyword }),
        queryFn: () => workspaceApi.fetchWorkspaceList({ page, pageSize, keyword }),
    });
}

export function useWorkspaceDetail(id: number | undefined) {
    return useQuery({
        queryKey: workspaceQueryKeys.detail(id ?? 0),
        queryFn: () => workspaceApi.fetchWorkspace(id!),
        enabled: id !== undefined && !Number.isNaN(id),
    });
}

export function useCreateWorkspace() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: CreateWorkspaceRequest) => workspaceApi.createWorkspace(request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.all });
        },
    });
}

export function useCreatePersonalWorkspace() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request?: CreatePersonalWorkspaceRequest) =>
            workspaceApi.createPersonalWorkspace(request ?? {}),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.all });
        },
    });
}

export function useUpdateWorkspace(id: number) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (request: UpdateWorkspaceRequest) => workspaceApi.updateWorkspace(id, request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.detail(id) });
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.all });
        },
    });
}

export function useDeleteWorkspace() {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: (id: number) => workspaceApi.deleteWorkspace(id),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.all });
        },
    });
}

export function useWorkspaceMembers(workspaceId: number | undefined) {
    return useQuery({
        queryKey: workspaceQueryKeys.members(workspaceId ?? 0),
        queryFn: () => workspaceApi.fetchWorkspaceMembers(workspaceId!),
        enabled: workspaceId !== undefined && !Number.isNaN(workspaceId),
    });
}

export function useUpdateWorkspaceMemberPermissions(workspaceId: number) {
    const queryClient = useQueryClient();

    return useMutation({
        mutationFn: ({ userId, permissions }: { userId: number; permissions: WorkspacePermissionCode[] }) =>
            workspaceApi.updateWorkspaceMemberPermissions(workspaceId, userId, permissions),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.members(workspaceId) });
            void queryClient.invalidateQueries({ queryKey: workspaceQueryKeys.all });
        },
    });
}
