import type { ReactNode } from 'react';

import { PageToolbar } from '@/components/page-toolbar';

interface TemplatePageHeaderProps {
    backTo?: string;
    backLabel?: string;
    actions?: ReactNode;
}

export function TemplatePageHeader({ backTo, backLabel, actions }: TemplatePageHeaderProps) {
    return <PageToolbar backTo={backTo} backLabel={backLabel} actions={actions} />;
}
