import { getData, getPaginatedData, postData, putData } from '@/api/client';
import type { PageMeta } from '@/types/api/common';
import type {
    CreateUserRequest,
    ResetPasswordRequest,
    UpdateUserRequest,
    UserDetail,
    UserListItem,
    UserListParams,
} from '@/features/user-management/types/user-management.types';

export async function fetchUsers(
    params: UserListParams,
): Promise<{ items: UserListItem[]; meta: PageMeta }> {
    const { data, meta } = await getPaginatedData<UserListItem[]>('/users', {
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 20,
    });
    return { items: data, meta };
}

export async function fetchUser(id: number): Promise<UserDetail> {
    return getData<UserDetail>(`/users/${id}`);
}

export async function createUser(request: CreateUserRequest): Promise<{ id: number; username: string; role: string }> {
    return postData<{ id: number; username: string; role: string }, CreateUserRequest>('/users', request);
}

export async function updateUser(id: number, request: UpdateUserRequest): Promise<void> {
    await putData(`/users/${id}`, request);
}

export async function resetUserPassword(id: number, request: ResetPasswordRequest): Promise<void> {
    await putData(`/users/${id}/password`, request);
}
