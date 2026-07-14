export type WorkspaceStatus = 'ACTIVE' | 'INACTIVE';

export type WorkspaceType = 'GLOBAL' | 'PERSONAL';

export type WorkspaceRole = 'WORKSPACE_ADMIN' | 'WORKSPACE_USER';

export type WorkspacePermissionCode = 'IMPORT_TEMPLATE' | 'MANAGE_MASTER_DATA';

export const WORKSPACE_PERMISSION_OPTIONS: {
    code: WorkspacePermissionCode;
    label: string;
}[] = [
    { code: 'IMPORT_TEMPLATE', label: 'Can Import XML Templates' },
    { code: 'MANAGE_MASTER_DATA', label: 'Can Manage Master Data' },
];

export interface WorkspaceSummary {
    id: number;
    code: string;
    name: string;
    status: WorkspaceStatus;
    type: WorkspaceType;
    myRole?: WorkspaceRole | null;
    myPermissions?: WorkspacePermissionCode[] | null;
}

export interface WorkspaceListItem extends WorkspaceSummary {
    description: string | null;
    createdAt: string;
}

export interface WorkspaceDetail extends WorkspaceSummary {
    description: string | null;
    createdById: number;
    createdAt: string;
    updatedAt: string;
}

export interface WorkspaceMember {
    userId: number;
    username: string;
    role: WorkspaceRole;
    permissions: WorkspacePermissionCode[];
}

export interface CreateWorkspaceRequest {
    code: string;
    name: string;
    description?: string | null;
    status: WorkspaceStatus;
}

export interface CreatePersonalWorkspaceRequest {
    name?: string | null;
}

export interface UpdateWorkspaceRequest {
    name: string;
    description?: string | null;
    status: WorkspaceStatus;
}

export interface CreateWorkspaceResponse {
    id: number;
    code: string;
    name: string;
    description: string | null;
    status: WorkspaceStatus;
    type: WorkspaceType;
}

export interface WorkspaceListParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
}
