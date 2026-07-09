import type { ReactNode } from 'react';

import { Button } from '@/components/ui/button';
import { Link } from 'react-router-dom';

interface WorkflowContinuationBannerProps {
    message: string;
    actionLabel: string;
    onAction: () => void;
}

export function WorkflowContinuationBanner({ message, actionLabel, onAction }: WorkflowContinuationBannerProps) {
    return (
        <div className="flex flex-col gap-3 rounded-lg border border-emerald-200 bg-emerald-50 p-4 sm:flex-row sm:items-center sm:justify-between dark:border-emerald-900/50 dark:bg-emerald-950/30">
            <p className="text-sm text-foreground">{message}</p>
            <Button onClick={onAction}>{actionLabel}</Button>
        </div>
    );
}

interface WorkflowContinuationLinkBannerProps {
    message: string;
    actionLabel: string;
    actionTo: string;
    actionNode?: ReactNode;
}

export function WorkflowContinuationLinkBanner({
    message,
    actionLabel,
    actionTo,
    actionNode,
}: WorkflowContinuationLinkBannerProps) {
    return (
        <div className="flex flex-col gap-3 rounded-lg border border-emerald-200 bg-emerald-50 p-4 sm:flex-row sm:items-center sm:justify-between dark:border-emerald-900/50 dark:bg-emerald-950/30">
            <p className="text-sm text-foreground">{message}</p>
            {actionNode ?? (
                <Button asChild>
                    <Link to={actionTo}>{actionLabel}</Link>
                </Button>
            )}
        </div>
    );
}
