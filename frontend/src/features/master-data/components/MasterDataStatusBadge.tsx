import { Badge } from '@/components/ui/badge';
import type { MasterDataTypeStatus } from '@/features/master-data/types/master-data.types';

export function MasterDataStatusBadge({ status }: { status: MasterDataTypeStatus }) {
    return (
        <Badge variant={status === 'ACTIVE' ? 'success' : 'secondary'}>
            {status === 'ACTIVE' ? 'Active' : 'Inactive'}
        </Badge>
    );
}
