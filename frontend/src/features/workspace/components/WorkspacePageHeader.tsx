import type { ReactNode } from 'react';

import { PageToolbar } from '@/components/page-toolbar';

interface WorkspacePageHeaderProps {
    backTo?: string;
    backLabel?: string;
    actions?: ReactNode;
}

export function WorkspacePageHeader({ backTo, backLabel, actions }: WorkspacePageHeaderProps) {
    return <PageToolbar backTo={backTo} backLabel={backLabel} actions={actions} />;
}
