import { Badge } from '@/components/ui/badge';
import type { WorkspaceStatus } from '@/features/workspace/types/workspace.types';

interface WorkspaceStatusBadgeProps {
    status: WorkspaceStatus;
}

export function WorkspaceStatusBadge({ status }: WorkspaceStatusBadgeProps) {
    return (
        <Badge variant={status === 'ACTIVE' ? 'success' : 'secondary'}>
            {status === 'ACTIVE' ? 'Active' : 'Inactive'}
        </Badge>
    );
}
