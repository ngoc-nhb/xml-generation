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

    return (
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
    );
}
