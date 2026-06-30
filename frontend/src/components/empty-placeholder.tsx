import type { ReactNode } from 'react';
import { Inbox } from 'lucide-react';

import { cn } from '@/utils/cn';

interface EmptyPlaceholderProps {
    title: string;
    description?: string;
    action?: ReactNode;
    className?: string;
}

export function EmptyPlaceholder({ title, description, action, className }: EmptyPlaceholderProps) {
    return (
        <div
            className={cn(
                'flex flex-col items-center justify-center gap-3 rounded-lg border border-dashed border-border bg-card px-6 py-16 text-center',
                className,
            )}
        >
            <Inbox className="h-10 w-10 text-muted-foreground" aria-hidden />
            <div>
                <h3 className="text-base font-medium text-foreground">{title}</h3>
                {description ? <p className="mt-1 text-sm text-muted-foreground">{description}</p> : null}
            </div>
            {action}
        </div>
    );
}
