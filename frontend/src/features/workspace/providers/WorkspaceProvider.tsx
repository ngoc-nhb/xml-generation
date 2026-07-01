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
import { LoadingSpinner } from '@/components/loading-spinner';
import { useWorkspaceList } from '@/features/workspace/hooks/useWorkspaces';
import type { WorkspaceSummary } from '@/features/workspace/types/workspace.types';
import { useAuth } from '@/providers/AuthProvider';
import { toast } from '@/providers/ToastProvider';
import { clearStoredWorkspaceId, getStoredWorkspaceId, setStoredWorkspaceId } from '@/utils/storage';

/** Default workspace from backend migration — used only to bootstrap workspace list loading. */
const BOOTSTRAP_WORKSPACE_ID = 1;

interface WorkspaceContextValue {
    currentWorkspace: WorkspaceSummary | null;
    workspaces: WorkspaceSummary[];
    isLoading: boolean;
    switchWorkspace: (workspaceId: number) => void;
    refreshWorkspaces: () => Promise<void>;
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

function ensureBootstrapWorkspaceId(): number {
    const stored = getStoredWorkspaceId();
    if (stored !== null) {
        return stored;
    }
    setStoredWorkspaceId(BOOTSTRAP_WORKSPACE_ID);
    return BOOTSTRAP_WORKSPACE_ID;
}

export function WorkspaceProvider({ children }: { children: ReactNode }) {
    const { isAuthenticated, user } = useAuth();
    const navigate = useNavigate();
    const queryClient = useQueryClient();
    const { data: workspaces = [], isLoading, refetch } = useWorkspaceList();
    const [workspaceRevision, setWorkspaceRevision] = useState(0);

    if (isAuthenticated) {
        ensureBootstrapWorkspaceId();
    }

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
            navigate(user?.isAdmin ? '/templates' : '/xml-generation', { replace: true });
        },
        [workspaces, queryClient, navigate, user?.isAdmin],
    );

    const handleWorkspaceError = useCallback(
        (code: string) => {
            if (!isAuthenticated) {
                return;
            }

            switch (code) {
                case 'WORKSPACE_REQUIRED':
                    navigate('/workspace-required', { replace: true });
                    break;
                case 'INVALID_WORKSPACE':
                case 'WORKSPACE_NOT_FOUND':
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
        }),
        [currentWorkspace, workspaces, isLoading, switchWorkspace, refreshWorkspaces],
    );

    if (!isAuthenticated) {
        return <>{children}</>;
    }

    if (isLoading || currentWorkspace === null) {
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
