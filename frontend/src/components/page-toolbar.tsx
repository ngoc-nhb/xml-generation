import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

import { Button } from '@/components/ui/button';
import { cn } from '@/utils/cn';

interface PageToolbarProps {
    backTo?: string;
    backLabel?: string;
    actions?: ReactNode;
}

/** Back navigation and page actions below the global header. */
export function PageToolbar({ backTo, backLabel = 'Back', actions }: PageToolbarProps) {
    if (!backTo && !actions) {
        return null;
    }

    return (
        <div
            className={cn(
                'flex flex-col gap-4 sm:flex-row sm:items-center',
                actions && !backTo ? 'sm:justify-end' : 'sm:justify-between',
            )}
        >
            {backTo ? (
                <Button asChild variant="ghost" size="sm" className="h-auto w-fit px-0 text-muted-foreground">
                    <Link to={backTo}>{backLabel}</Link>
                </Button>
            ) : null}
            {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
        </div>
    );
}
