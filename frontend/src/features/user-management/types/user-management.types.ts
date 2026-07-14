export type SystemRole = 'ADMIN' | 'USER';

export interface UserListItem {
    id: number;
    username: string;
    role: SystemRole;
    createdAt: string;
    updatedAt: string;
}

export interface UserDetail extends UserListItem {}

export interface UserListParams {
    page?: number;
    pageSize?: number;
}

export interface CreateUserRequest {
    username: string;
    password: string;
    role: SystemRole;
}

export interface UpdateUserRequest {
    username: string;
    role: SystemRole;
}

export interface ResetPasswordRequest {
    password: string;
    confirmPassword: string;
}

export type WorkspaceMemberRole = 'WORKSPACE_ADMIN' | 'WORKSPACE_USER';

export type WorkspacePermissionCode = 'IMPORT_TEMPLATE' | 'MANAGE_MASTER_DATA';

export type WorkspaceType = 'GLOBAL' | 'PERSONAL';

export interface UserWorkspace {
    workspaceId: number;
    code: string;
    name: string;
    type: WorkspaceType;
    role: WorkspaceMemberRole;
    permissions: WorkspacePermissionCode[];
}

export interface WorkspaceMembershipAssignment {
    workspaceId: number;
    permissions: WorkspacePermissionCode[];
}
