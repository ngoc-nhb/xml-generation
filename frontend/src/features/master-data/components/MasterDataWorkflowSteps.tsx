import type { ReactNode } from 'react';
import { Check, ChevronRight, Circle } from 'lucide-react';
import { Link } from 'react-router-dom';

import { cn } from '@/utils/cn';

export type MasterDataWorkflowStep = 'type' | 'dataFile' | 'records';

interface MasterDataWorkflowStepsProps {
    activeStep: MasterDataWorkflowStep;
    typeId?: number;
    hasTypes: boolean;
    hasFields: boolean;
    hasRecords: boolean;
    action?: ReactNode;
}

interface StepDefinition {
    id: MasterDataWorkflowStep;
    label: string;
    href?: string;
    enabled: boolean;
}

function resolveStatus(
    step: MasterDataWorkflowStep,
    activeStep: MasterDataWorkflowStep,
    completed: Record<MasterDataWorkflowStep, boolean>,
): 'completed' | 'current' | 'upcoming' {
    if (completed[step]) {
        return 'completed';
    }
    if (activeStep === step) {
        return 'current';
    }
    return 'upcoming';
}

export function MasterDataWorkflowSteps({
    activeStep,
    typeId,
    hasTypes,
    hasFields,
    hasRecords,
    action,
}: MasterDataWorkflowStepsProps) {
    const completed: Record<MasterDataWorkflowStep, boolean> = {
        type: hasTypes,
        dataFile: hasFields,
        records: hasRecords,
    };

    const steps: StepDefinition[] = [
        { id: 'type', label: 'Master Type', href: '/master-data', enabled: true },
        {
            id: 'dataFile',
            label: 'Data Field',
            href: typeId ? `/master-data/types/${typeId}/fields` : undefined,
            enabled: hasTypes,
        },
        {
            id: 'records',
            label: 'Records',
            href: typeId && hasFields ? `/master-data/types/${typeId}/records` : undefined,
            enabled: hasFields,
        },
    ];

    return (
        <nav aria-label="Master data workflow" className="rounded-lg border border-border bg-card p-4">
            <div className="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-2">
                <ol className="flex flex-col gap-3 sm:flex-row sm:items-center sm:gap-2 sm:flex-1 sm:justify-start">
                    {steps.map((step, index) => {
                    const status = resolveStatus(step.id, activeStep, completed);
                    const isDisabled = !step.enabled || status === 'upcoming';
                    const content = (
                        <span
                            className={cn(
                                'inline-flex items-center gap-2 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                                status === 'current' && 'bg-primary text-primary-foreground',
                                status === 'completed' && 'text-foreground',
                                status === 'upcoming' && 'text-muted-foreground',
                                isDisabled && status !== 'current' && 'opacity-60',
                            )}
                        >
                            {status === 'completed' ? (
                                <Check className="h-4 w-4 shrink-0 text-emerald-600" aria-hidden />
                            ) : status === 'current' ? (
                                <span className="flex h-4 w-4 shrink-0 items-center justify-center rounded-full bg-primary-foreground/20 text-[10px] font-bold">
                                    ▶
                                </span>
                            ) : (
                                <Circle className="h-4 w-4 shrink-0 opacity-50" aria-hidden />
                            )}
                            {step.label}
                        </span>
                    );

                    return (
                        <li key={step.id} className="flex items-center gap-2">
                            {step.href && step.enabled ? (
                                <Link to={step.href} className="hover:opacity-90">
                                    {content}
                                </Link>
                            ) : (
                                content
                            )}
                            {index < steps.length - 1 ? (
                                <ChevronRight className="hidden h-4 w-4 shrink-0 text-muted-foreground sm:block" aria-hidden />
                            ) : null}
                        </li>
                    );
                })}
                </ol>
                {action ? <div className="flex justify-center sm:flex-1">{action}</div> : null}
                {action ? <div className="hidden sm:block sm:flex-1" aria-hidden /> : null}
            </div>
        </nav>
    );
}
