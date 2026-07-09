import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { ChevronDown, ChevronLeft, ChevronRight } from 'lucide-react';

import { cn } from '@/utils/cn';

const DEFAULT_PANEL_PERCENT = 28;
const MIN_PANEL_PX = 220;
const MAX_PANEL_PERCENT = 35;

interface ResizableSidebarLayoutProps {
    sidebarTitle?: string;
    sidebarCollapsed: boolean;
    onSidebarCollapsedChange: (collapsed: boolean) => void;
    sidebar: ReactNode;
    children: ReactNode;
    className?: string;
    /** Panel placement relative to the primary content area. */
    side?: 'left' | 'right';
    storageKey?: string;
}

function readStoredPercent(storageKey: string): number {
    try {
        const saved = sessionStorage.getItem(storageKey);
        if (!saved) {
            return DEFAULT_PANEL_PERCENT;
        }
        const parsed = Number.parseFloat(saved);
        return Number.isFinite(parsed) ? parsed : DEFAULT_PANEL_PERCENT;
    } catch {
        return DEFAULT_PANEL_PERCENT;
    }
}

export function ResizableSidebarLayout({
    sidebarTitle = 'Panel',
    sidebarCollapsed,
    onSidebarCollapsedChange,
    sidebar,
    children,
    className,
    side = 'left',
    storageKey = 'xmlgen-sidebar-percent',
}: ResizableSidebarLayoutProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const [panelPercent, setPanelPercent] = useState(() => readStoredPercent(storageKey));
    const [isDragging, setIsDragging] = useState(false);
    const isRight = side === 'right';

    const clampPercent = useCallback((nextPercent: number, containerWidth: number) => {
        const minPercent = (MIN_PANEL_PX / containerWidth) * 100;
        return Math.min(MAX_PANEL_PERCENT, Math.max(minPercent, nextPercent));
    }, []);

    useEffect(() => {
        if (!isDragging) {
            return;
        }

        function handleMouseMove(event: MouseEvent) {
            const container = containerRef.current;
            if (!container) {
                return;
            }
            const rect = container.getBoundingClientRect();
            const offsetX = isRight ? rect.right - event.clientX : event.clientX - rect.left;
            const nextPercent = clampPercent((offsetX / rect.width) * 100, rect.width);
            setPanelPercent(nextPercent);
            sessionStorage.setItem(storageKey, String(nextPercent));
        }

        function handleMouseUp() {
            setIsDragging(false);
        }

        document.addEventListener('mousemove', handleMouseMove);
        document.addEventListener('mouseup', handleMouseUp);
        document.body.style.cursor = 'col-resize';
        document.body.style.userSelect = 'none';

        return () => {
            document.removeEventListener('mousemove', handleMouseMove);
            document.removeEventListener('mouseup', handleMouseUp);
            document.body.style.cursor = '';
            document.body.style.userSelect = '';
        };
    }, [clampPercent, isDragging, isRight, storageKey]);

    const collapsedRail = (
        <div className={cn('flex w-10 shrink-0 flex-col bg-card', isRight ? 'border-l' : 'border-r', 'border-border')}>
            <button
                type="button"
                className="flex h-full min-h-[8rem] flex-col items-center gap-2 px-2 py-3 text-xs font-medium text-foreground hover:bg-muted/50"
                aria-expanded={false}
                title={`Expand ${sidebarTitle}`}
                onClick={() => onSidebarCollapsedChange(false)}
            >
                {isRight ? <ChevronLeft className="h-4 w-4 shrink-0" /> : <ChevronRight className="h-4 w-4 shrink-0" />}
                <span className="[writing-mode:vertical-rl] rotate-180">{sidebarTitle}</span>
            </button>
        </div>
    );

    const expandedPanel = (
        <>
            {!isRight ? (
                <aside
                    className="flex min-h-0 shrink-0 flex-col border-r border-border bg-card"
                    style={{ width: `${panelPercent}%` }}
                >
                    <button
                        type="button"
                        className="flex shrink-0 items-center gap-2 border-b border-border px-3 py-2 text-left text-sm font-medium text-foreground hover:bg-muted/50"
                        aria-expanded
                        onClick={() => onSidebarCollapsedChange(true)}
                    >
                        <ChevronDown className="h-4 w-4 shrink-0 opacity-70" />
                        {sidebarTitle}
                    </button>
                    <div className="min-h-0 flex-1 overflow-y-auto p-3">{sidebar}</div>
                </aside>
            ) : null}
            {!isRight ? (
                <div
                    role="separator"
                    aria-orientation="vertical"
                    aria-label={`Resize ${sidebarTitle}`}
                    className={cn(
                        'w-1 shrink-0 cursor-col-resize bg-border transition-colors hover:bg-primary/40',
                        isDragging && 'bg-primary/60',
                    )}
                    onMouseDown={() => setIsDragging(true)}
                />
            ) : null}
            <div className="flex min-h-0 min-w-0 flex-1 flex-col">{children}</div>
            {isRight ? (
                <div
                    role="separator"
                    aria-orientation="vertical"
                    aria-label={`Resize ${sidebarTitle}`}
                    className={cn(
                        'w-1 shrink-0 cursor-col-resize bg-border transition-colors hover:bg-primary/40',
                        isDragging && 'bg-primary/60',
                    )}
                    onMouseDown={() => setIsDragging(true)}
                />
            ) : null}
            {isRight ? (
                <aside
                    className="sticky top-0 flex max-h-full min-h-0 shrink-0 flex-col self-start border-l border-border bg-card"
                    style={{ width: `${panelPercent}%` }}
                >
                    <button
                        type="button"
                        className="flex shrink-0 items-center gap-2 border-b border-border px-3 py-2 text-left text-sm font-medium text-foreground hover:bg-muted/50"
                        aria-expanded
                        onClick={() => onSidebarCollapsedChange(true)}
                    >
                        <ChevronDown className="h-4 w-4 shrink-0 opacity-70" />
                        {sidebarTitle}
                    </button>
                    <div className="min-h-0 flex-1 overflow-y-auto p-3">{sidebar}</div>
                </aside>
            ) : null}
        </>
    );

    return (
        <div ref={containerRef} className={cn('flex min-h-0 flex-1', className)}>
            {sidebarCollapsed ? (
                <>
                    {isRight ? <div className="flex min-h-0 min-w-0 flex-1 flex-col">{children}</div> : null}
                    {collapsedRail}
                    {!isRight ? <div className="flex min-h-0 min-w-0 flex-1 flex-col">{children}</div> : null}
                </>
            ) : (
                expandedPanel
            )}
        </div>
    );
}
