import { useMemo } from 'react';

import { Select } from '@/components/ui/select';
import { useTemplateList } from '@/features/templates';
import type { TemplateListItem } from '@/features/templates';

interface TemplateSelectorProps {
    value: number | null;
    onChange: (templateId: number | null, template: TemplateListItem | null) => void;
}

export function TemplateSelector({ value, onChange }: TemplateSelectorProps) {
    const { data, isLoading, isError } = useTemplateList({
        page: 1,
        pageSize: 100,
        status: 'ACTIVE',
    });

    const templates = useMemo(() => data?.items ?? [], [data?.items]);
    const selected = templates.find((item) => item.id === value) ?? null;

    return (
        <div className="space-y-3">
            <div className="space-y-2">
                <label htmlFor="template-select" className="text-sm font-medium text-foreground">
                    Template
                </label>
                <Select
                    id="template-select"
                    value={value?.toString() ?? ''}
                    disabled={isLoading || isError}
                    onChange={(event) => {
                        const nextId = event.target.value ? Number(event.target.value) : null;
                        const nextTemplate = templates.find((item) => item.id === nextId) ?? null;
                        onChange(nextId, nextTemplate);
                    }}
                >
                    <option value="">Select a template…</option>
                    {templates.map((template) => (
                        <option key={template.id} value={template.id}>
                            {template.code} — {template.name}
                        </option>
                    ))}
                </Select>
            </div>
            {selected ? (
                <div className="rounded-md border border-border bg-muted/40 p-3 text-sm">
                    <p className="font-medium text-foreground">{selected.name}</p>
                    <p className="text-muted-foreground">{selected.code}</p>
                    {selected.description ? (
                        <p className="mt-1 text-muted-foreground">{selected.description}</p>
                    ) : null}
                </div>
            ) : (
                <p className="text-sm text-muted-foreground">Choose an active template to begin.</p>
            )}
        </div>
    );
}
