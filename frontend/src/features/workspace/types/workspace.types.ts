export type WorkspaceStatus = 'ACTIVE' | 'INACTIVE';

export interface WorkspaceSummary {
    id: number;
    code: string;
    name: string;
    status: WorkspaceStatus;
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

export interface CreateWorkspaceRequest {
    code: string;
    name: string;
    description?: string | null;
    status: WorkspaceStatus;
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
}

export interface WorkspaceListParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
}
