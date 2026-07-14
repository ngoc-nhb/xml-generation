/**
 * Workspace feature public API.
 *
 * Cross-feature imports must use this module only.
 */

export { WorkspaceSwitcher } from '@/features/workspace/components/WorkspaceSwitcher';
export { WorkspaceRequiredPage } from '@/features/workspace/pages/WorkspaceRequiredPage';
export { WorkspaceListPage } from '@/features/workspace/pages/WorkspaceListPage';
export { WorkspaceCreatePage } from '@/features/workspace/pages/WorkspaceCreatePage';
export { WorkspaceEditPage } from '@/features/workspace/pages/WorkspaceEditPage';
export { WorkspacePermissionsPage } from '@/features/workspace/pages/WorkspacePermissionsPage';
export {
    WorkspaceProvider,
    useWorkspace,
    NoWorkspaceEmptyState,
} from '@/features/workspace/providers/WorkspaceProvider';
export { useWorkspaceList } from '@/features/workspace/hooks/useWorkspaces';

export type {
    WorkspaceDetail,
    WorkspaceListItem,
    WorkspaceSummary,
    WorkspaceStatus,
    WorkspaceType,
    WorkspacePermissionCode,
} from '@/features/workspace/types/workspace.types';
