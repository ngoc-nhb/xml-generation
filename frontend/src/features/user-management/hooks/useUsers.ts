import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import * as usersApi from '@/features/user-management/api/users.api';
import { userManagementQueryKeys } from '@/features/user-management/hooks/queryKeys';
import type {
    CreateUserRequest,
    ResetPasswordRequest,
    UpdateUserRequest,
    UserListParams,
} from '@/features/user-management/types/user-management.types';

export function useUserList(params: UserListParams) {
    return useQuery({
        queryKey: userManagementQueryKeys.userList({
            page: params.page ?? 1,
            pageSize: params.pageSize ?? 20,
        }),
        queryFn: () => usersApi.fetchUsers(params),
    });
}

export function useUserDetail(id: number | undefined) {
    return useQuery({
        queryKey: userManagementQueryKeys.userDetail(id ?? 0),
        queryFn: () => usersApi.fetchUser(id!),
        enabled: id !== undefined && !Number.isNaN(id),
    });
}

export function useCreateUser() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateUserRequest) => usersApi.createUser(request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: userManagementQueryKeys.users() });
        },
    });
}

export function useUpdateUser(id: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: UpdateUserRequest) => usersApi.updateUser(id, request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: userManagementQueryKeys.userDetail(id) });
            void queryClient.invalidateQueries({ queryKey: userManagementQueryKeys.users() });
        },
    });
}

export function useResetUserPassword(id: number) {
    return useMutation({
        mutationFn: (request: ResetPasswordRequest) => usersApi.resetUserPassword(id, request),
    });
}
