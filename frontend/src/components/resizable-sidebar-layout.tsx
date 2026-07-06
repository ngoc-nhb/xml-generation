import { useCallback, useEffect, useRef, useState, type ReactNode } from 'react';
import { ChevronDown, ChevronRight } from 'lucide-react';

import { cn } from '@/utils/cn';

const DEFAULT_LEFT_PERCENT = 25;
const MIN_LEFT_PX = 220;
const MAX_LEFT_PERCENT = 35;
const STORAGE_KEY = 'xmlgen-master-data-sidebar-percent';

interface ResizableSidebarLayoutProps {
    sidebarTitle?: string;
    sidebarCollapsed: boolean;
    onSidebarCollapsedChange: (collapsed: boolean) => void;
    sidebar: ReactNode;
    children: ReactNode;
    className?: string;
}

function readStoredPercent(): number {
    try {
        const saved = sessionStorage.getItem(STORAGE_KEY);
        if (!saved) {
            return DEFAULT_LEFT_PERCENT;
        }
        const parsed = Number.parseFloat(saved);
        return Number.isFinite(parsed) ? parsed : DEFAULT_LEFT_PERCENT;
    } catch {
        return DEFAULT_LEFT_PERCENT;
    }
}

export function ResizableSidebarLayout({
    sidebarTitle = 'Master Data',
    sidebarCollapsed,
    onSidebarCollapsedChange,
    sidebar,
    children,
    className,
}: ResizableSidebarLayoutProps) {
    const containerRef = useRef<HTMLDivElement>(null);
    const [leftPercent, setLeftPercent] = useState(readStoredPercent);
    const [isDragging, setIsDragging] = useState(false);

    const clampPercent = useCallback((nextPercent: number, containerWidth: number) => {
        const minPercent = (MIN_LEFT_PX / containerWidth) * 100;
        return Math.min(MAX_LEFT_PERCENT, Math.max(minPercent, nextPercent));
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
            const nextPercent = clampPercent(((event.clientX - rect.left) / rect.width) * 100, rect.width);
            setLeftPercent(nextPercent);
            sessionStorage.setItem(STORAGE_KEY, String(nextPercent));
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
    }, [clampPercent, isDragging]);

    return (
        <div ref={containerRef} className={cn('flex min-h-0 flex-1', className)}>
            {sidebarCollapsed ? (
                <div className="flex w-10 shrink-0 flex-col border-r border-border bg-card">
                    <button
                        type="button"
                        className="flex h-full min-h-[8rem] flex-col items-center gap-2 px-2 py-3 text-xs font-medium text-foreground hover:bg-muted/50"
                        aria-expanded={false}
                        title={`Expand ${sidebarTitle}`}
                        onClick={() => onSidebarCollapsedChange(false)}
                    >
                        <ChevronRight className="h-4 w-4 shrink-0" />
                        <span className="[writing-mode:vertical-rl] rotate-180">{sidebarTitle}</span>
                    </button>
                </div>
            ) : (
                <>
                    <aside
                        className="flex min-h-0 shrink-0 flex-col border-r border-border bg-card"
                        style={{ width: `${leftPercent}%` }}
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
                    <div
                        role="separator"
                        aria-orientation="vertical"
                        aria-label="Resize master data sidebar"
                        className={cn(
                            'w-1 shrink-0 cursor-col-resize bg-border transition-colors hover:bg-primary/40',
                            isDragging && 'bg-primary/60',
                        )}
                        onMouseDown={() => setIsDragging(true)}
                    />
                </>
            )}
            <div className="flex min-h-0 min-w-0 flex-1 flex-col">{children}</div>
        </div>
    );
}
