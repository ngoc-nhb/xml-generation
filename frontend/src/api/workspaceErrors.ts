type WorkspaceErrorHandler = (code: string) => void;

let workspaceErrorHandler: WorkspaceErrorHandler | null = null;

export function setWorkspaceErrorHandler(handler: WorkspaceErrorHandler | null): void {
    workspaceErrorHandler = handler;
}

export function notifyWorkspaceError(code: string): void {
    workspaceErrorHandler?.(code);
}

const WORKSPACE_ERROR_CODES = new Set([
    'WORKSPACE_REQUIRED',
    'INVALID_WORKSPACE',
    'WORKSPACE_INACTIVE',
    'WORKSPACE_NOT_FOUND',
    'WORKSPACE_ACCESS_DENIED',
]);

export function isWorkspaceErrorCode(code: string): boolean {
    return WORKSPACE_ERROR_CODES.has(code);
}
