export const userManagementQueryKeys = {
    all: ['user-management'] as const,
    users: () => [...userManagementQueryKeys.all, 'users'] as const,
    userList: (params: { page: number; pageSize: number }) =>
        [...userManagementQueryKeys.users(), 'list', params] as const,
    userDetail: (id: number) => [...userManagementQueryKeys.users(), 'detail', id] as const,
};
