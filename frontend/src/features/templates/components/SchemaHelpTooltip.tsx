import { CircleHelp } from 'lucide-react';
import type { ReactNode } from 'react';

interface SchemaHelpTooltipProps {
    label: string;
    children: ReactNode;
}

export function SchemaHelpTooltip({ label, children }: SchemaHelpTooltipProps) {
    return (
        <span className="group/help relative inline-flex items-center gap-1.5">
            {label}
            <span
                className="inline-flex cursor-help text-muted-foreground"
                aria-label={`${label} help`}
                tabIndex={0}
            >
                <CircleHelp className="h-3.5 w-3.5" />
            </span>
            <span
                role="tooltip"
                className="pointer-events-none invisible absolute left-full top-1/2 z-50 ml-2 w-72 -translate-y-1/2 rounded-md border border-border bg-popover p-3 text-xs font-normal normal-case text-popover-foreground shadow-md group-hover/help:visible group-focus-within/help:visible"
            >
                {children}
            </span>
        </span>
    );
}
