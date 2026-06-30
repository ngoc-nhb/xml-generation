import { Badge } from '@/components/ui/badge';
import type { TemplateStatus } from '@/features/templates/types/template.types';

interface TemplateStatusBadgeProps {
    status: TemplateStatus;
}

export function TemplateStatusBadge({ status }: TemplateStatusBadgeProps) {
    return (
        <Badge variant={status === 'ACTIVE' ? 'success' : 'secondary'}>
            {status === 'ACTIVE' ? 'Active' : 'Inactive'}
        </Badge>
    );
}
