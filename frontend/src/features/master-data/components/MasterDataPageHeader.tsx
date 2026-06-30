import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

import { Button } from '@/components/ui/button';

interface MasterDataPageHeaderProps {
    title: string;
    description?: string;
    backTo?: string;
    backLabel?: string;
    actions?: ReactNode;
}

export function MasterDataPageHeader({
    title,
    description,
    backTo,
    backLabel = 'Back',
    actions,
}: MasterDataPageHeaderProps) {
    return (
        <div className="flex flex-col gap-4 border-b border-border pb-6 sm:flex-row sm:items-start sm:justify-between">
            <div className="space-y-2">
                {backTo ? (
                    <Button asChild variant="ghost" size="sm" className="h-auto px-0 text-muted-foreground">
                        <Link to={backTo}>{backLabel}</Link>
                    </Button>
                ) : null}
                <div>
                    <h1 className="text-2xl font-semibold text-foreground">{title}</h1>
                    {description ? <p className="text-sm text-muted-foreground">{description}</p> : null}
                </div>
            </div>
            {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
        </div>
    );
}
