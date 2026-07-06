import { ChevronDown, ChevronRight } from 'lucide-react';
import { useState, type ReactNode } from 'react';

import { cn } from '@/utils/cn';

interface CollapsibleSectionProps {
    title: ReactNode;
    open?: boolean;
    defaultOpen?: boolean;
    onOpenChange?: (open: boolean) => void;
    children: ReactNode;
    className?: string;
    headerClassName?: string;
    contentClassName?: string;
}

export function CollapsibleSection({
    title,
    open,
    defaultOpen = true,
    onOpenChange,
    children,
    className,
    headerClassName,
    contentClassName,
}: CollapsibleSectionProps) {
    const [internalOpen, setInternalOpen] = useState(defaultOpen);
    const isControlled = open !== undefined;
    const isOpen = isControlled ? open : internalOpen;
    const Icon = isOpen ? ChevronDown : ChevronRight;

    function setOpen(nextOpen: boolean) {
        if (!isControlled) {
            setInternalOpen(nextOpen);
        }
        onOpenChange?.(nextOpen);
    }

    return (
        <div className={className}>
            <button
                type="button"
                className={cn(
                    'flex w-full items-center gap-2 text-left text-sm font-medium text-foreground',
                    headerClassName,
                )}
                aria-expanded={isOpen}
                onClick={() => setOpen(!isOpen)}
            >
                <Icon className="h-4 w-4 shrink-0 opacity-70" />
                <span className="min-w-0 flex-1">{title}</span>
            </button>
            {isOpen ? <div className={cn('mt-3', contentClassName)}>{children}</div> : null}
        </div>
    );
}
