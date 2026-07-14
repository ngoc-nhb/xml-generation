import {
    createContext,
    useCallback,
    useContext,
    useEffect,
    useMemo,
    useState,
    type ReactNode,
} from 'react';
import { useNavigate } from 'react-router-dom';
import { useQueryClient } from '@tanstack/react-query';

import { isWorkspaceErrorCode, setWorkspaceErrorHandler } from '@/api/workspaceErrors';
import { Button } from '@/components/ui/button';
import { LoadingSpinner } from '@/components/loading-spinner';
import {
    useCreatePersonalWorkspace,
    useWorkspaceList,
} from '@/features/workspace/hooks/useWorkspaces';
import type { WorkspacePermissionCode, WorkspaceSummary } from '@/features/workspace/types/workspace.types';
import { useAuth } from '@/providers/AuthProvider';
import { toast } from '@/providers/ToastProvider';
import { clearStoredWorkspaceId, getStoredWorkspaceId, setStoredWorkspaceId } from '@/utils/storage';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';

interface WorkspaceContextValue {
    currentWorkspace: WorkspaceSummary | null;
    workspaces: WorkspaceSummary[];
    isLoading: boolean;
    switchWorkspace: (workspaceId: number) => void;
    refreshWorkspaces: () => Promise<void>;
    hasPermission: (permission: WorkspacePermissionCode) => boolean;
    createPersonalWorkspace: (name?: string) => Promise<void>;
    isCreatingPersonalWorkspace: boolean;
}

const WorkspaceContext = createContext<WorkspaceContextValue | undefined>(undefined);

function findActiveWorkspace(
    workspaces: WorkspaceSummary[],
    preferredId: number | null,
): WorkspaceSummary | null {
    const active = workspaces.filter((workspace) => workspace.status === 'ACTIVE');
    if (active.length === 0) {
        return null;
    }
    if (preferredId !== null) {
        const match = active.find((workspace) => workspace.id === preferredId);
        if (match) {
            return match;
        }
    }
    return active[0] ?? null;
}

export function WorkspaceProvider({ children }: { children: ReactNode }) {
    const { isAuthenticated, user } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { data: workspaces = [], isLoading, refetch } = useWorkspaceList();
    const createPersonalMutation = useCreatePersonalWorkspace();
    const [workspaceRevision, setWorkspaceRevision] = useState(0);

    const currentWorkspace = useMemo(() => {
        void workspaceRevision;
        if (!isAuthenticated || isLoading) {
            return null;
        }

        const storedId = getStoredWorkspaceId();
        const resolved = findActiveWorkspace(workspaces, storedId);
        if (resolved && resolved.id !== storedId) {
            setStoredWorkspaceId(resolved.id);
        }
        if (!resolved) {
            clearStoredWorkspaceId();
        }
        return resolved;
    }, [isAuthenticated, isLoading, workspaces, workspaceRevision]);

    const refreshWorkspaces = useCallback(async () => {
        await refetch();
        setWorkspaceRevision((revision) => revision + 1);
    }, [refetch]);

    const switchWorkspace = useCallback(
        (workspaceId: number) => {
            const target = workspaces.find((workspace) => workspace.id === workspaceId);
            if (!target) {
                return;
            }
            if (target.status === 'INACTIVE') {
                toast.error('This workspace is inactive. Please choose another workspace.');
                return;
            }
            if (target.id === getStoredWorkspaceId()) {
                return;
            }

            setStoredWorkspaceId(target.id);
            setWorkspaceRevision((revision) => revision + 1);
            queryClient.clear();
            navigate('/dashboard', { replace: true });
        },
        [workspaces, queryClient, navigate],
    );

    const hasPermission = useCallback(
        (permission: WorkspacePermissionCode) => {
            if (user?.isAdmin) {
                return true;
            }
            return Boolean(currentWorkspace?.myPermissions?.includes(permission));
        },
        [currentWorkspace, user?.isAdmin],
    );

    const createPersonalWorkspace = useCallback(
        async (name?: string) => {
            try {
                const created = await createPersonalMutation.mutateAsync(name ? { name } : {});
                setStoredWorkspaceId(created.id);
                await refreshWorkspaces();
                toast.success('Personal workspace created');
                navigate('/dashboard', { replace: true });
            } catch (error) {
                const message =
                    error instanceof ApiClientError
                        ? getPrimaryErrorMessage(error.errors)
                        : 'Failed to create personal workspace';
                toast.error(message);
                throw error;
            }
        },
        [createPersonalMutation, refreshWorkspaces, navigate],
    );

    const handleWorkspaceError = useCallback(
        (code: string) => {
            if (!isAuthenticated) {
                return;
            }

            switch (code) {
                case 'WORKSPACE_REQUIRED':
                    navigate('/dashboard', { replace: true });
                    clearStoredWorkspaceId();
                    void refreshWorkspaces();
                    break;
                case 'INVALID_WORKSPACE':
                case 'WORKSPACE_NOT_FOUND':
                    clearStoredWorkspaceId();
                    void refreshWorkspaces();
                    break;
                case 'WORKSPACE_ACCESS_DENIED':
                    toast.error('You do not have access to this workspace.');
                    clearStoredWorkspaceId();
                    void refreshWorkspaces();
                    break;
                case 'WORKSPACE_INACTIVE':
                    toast.error('This workspace is inactive. Please choose another workspace.');
                    clearStoredWorkspaceId();
                    void refreshWorkspaces();
                    break;
                default:
                    break;
            }
        },
        [isAuthenticated, navigate, refreshWorkspaces],
    );

    useEffect(() => {
        setWorkspaceErrorHandler(handleWorkspaceError);
        return () => setWorkspaceErrorHandler(null);
    }, [handleWorkspaceError]);

    const value = useMemo<WorkspaceContextValue>(
        () => ({
            currentWorkspace,
            workspaces,
            isLoading,
            switchWorkspace,
            refreshWorkspaces,
            hasPermission,
            createPersonalWorkspace,
            isCreatingPersonalWorkspace: createPersonalMutation.isPending,
        }),
        [
            currentWorkspace,
            workspaces,
            isLoading,
            switchWorkspace,
            refreshWorkspaces,
            hasPermission,
            createPersonalWorkspace,
            createPersonalMutation.isPending,
        ],
    );

    if (!isAuthenticated) {
        return <>{children}</>;
    }

    if (isLoading) {
        return <LoadingSpinner label="Loading workspace…" />;
    }

    return <WorkspaceContext.Provider value={value}>{children}</WorkspaceContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useWorkspace(): WorkspaceContextValue {
    const context = useContext(WorkspaceContext);
    if (!context) {
        throw new Error('useWorkspace must be used within WorkspaceProvider');
    }
    return context;
}

// eslint-disable-next-line react-refresh/only-export-components
export { isWorkspaceErrorCode };

export function NoWorkspaceEmptyState() {
    const { createPersonalWorkspace, isCreatingPersonalWorkspace } = useWorkspace();

    return (
        <div className="flex min-h-[50vh] flex-col items-center justify-center gap-4 text-center">
            <div className="space-y-2">
                <h2 className="text-xl font-semibold text-foreground">No Workspace Available</h2>
                <p className="max-w-md text-sm text-muted-foreground">
                    You are not assigned to any global workspace yet. Create a personal workspace to get
                    started, or ask an administrator to assign you to one.
                </p>
            </div>
            <Button
                disabled={isCreatingPersonalWorkspace}
                onClick={() => void createPersonalWorkspace()}
            >
                {isCreatingPersonalWorkspace ? 'Creating…' : 'Create Personal Workspace'}
            </Button>
        </div>
    );
}
