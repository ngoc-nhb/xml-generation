import { ConfirmDialog } from '@/features/workspace/components/ConfirmDialog';
import type { WorkspaceListItem } from '@/features/workspace/types/workspace.types';

interface DeleteWorkspaceDialogProps {
    workspace: WorkspaceListItem | null;
    loading?: boolean;
    onConfirm: () => void;
    onCancel: () => void;
}

export function DeleteWorkspaceDialog({ workspace, loading, onConfirm, onCancel }: DeleteWorkspaceDialogProps) {
    return (
        <ConfirmDialog
            open={workspace !== null}
            title="Delete Workspace?"
            description="This action cannot be undone."
            confirmLabel="Delete"
            destructive
            loading={loading}
            onConfirm={onConfirm}
            onCancel={onCancel}
        />
    );
}
