import { Loader2 } from 'lucide-react';

import { cn } from '@/utils/cn';

interface LoadingSpinnerProps {
    label?: string;
    className?: string;
}

export function LoadingSpinner({ label = 'Loading…', className }: LoadingSpinnerProps) {
    return (
        <div className={cn('flex flex-col items-center justify-center gap-3 py-12 text-muted-foreground', className)}>
            <Loader2 className="h-8 w-8 animate-spin text-primary" aria-hidden />
            <p className="text-sm">{label}</p>
        </div>
    );
}
