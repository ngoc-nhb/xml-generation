import { Badge } from '@/components/ui/badge';
import type { SystemRole } from '@/features/user-management/types/user-management.types';

interface UserRoleBadgeProps {
    role: SystemRole;
}

export function UserRoleBadge({ role }: UserRoleBadgeProps) {
    return (
        <Badge variant={role === 'ADMIN' ? 'default' : 'secondary'}>
            {role}
        </Badge>
    );
}
