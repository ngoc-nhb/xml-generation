import type { ReactNode } from 'react';

import { PageToolbar } from '@/components/page-toolbar';

interface MasterDataPageHeaderProps {
    backTo?: string;
    backLabel?: string;
    actions?: ReactNode;
}

export function MasterDataPageHeader({ backTo, backLabel, actions }: MasterDataPageHeaderProps) {
    return <PageToolbar backTo={backTo} backLabel={backLabel} actions={actions} />;
}
