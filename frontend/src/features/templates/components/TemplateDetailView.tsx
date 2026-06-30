import type { ReactNode } from 'react';
import { Link } from 'react-router-dom';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { TemplateStatusBadge } from '@/features/templates/components/TemplateStatusBadge';
import type { TemplateDetail } from '@/features/templates/types/template.types';
import { buildFieldTree } from '@/features/templates/utils/schemaTree';
import { formatDateTime } from '@/utils/formatDate';

interface TemplateDetailViewProps {
    template: TemplateDetail;
}

export function TemplateDetailView({ template }: TemplateDetailViewProps) {
    const fieldCount = template.schema?.fields.length ?? 0;
    const mappingCount = template.schema?.mappings.length ?? 0;
    const tree = buildFieldTree(template.schema?.fields ?? []);

    return (
        <div className="space-y-6">
            <Card>
                <CardHeader>
                    <CardTitle>Metadata</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-4 sm:grid-cols-2">
                    <DetailItem label="Code" value={template.code} />
                    <DetailItem label="Name" value={template.name} />
                    <DetailItem label="Status" value={<TemplateStatusBadge status={template.status} />} />
                    <DetailItem label="Created" value={formatDateTime(template.createdAt)} />
                    <DetailItem label="Updated" value={formatDateTime(template.updatedAt)} />
                    <DetailItem label="Description" value={template.description || '—'} className="sm:col-span-2" />
                </CardContent>
            </Card>

            <Card>
                <CardHeader className="flex flex-row items-center justify-between">
                    <CardTitle>Schema summary</CardTitle>
                    <Button asChild variant="outline" size="sm">
                        <Link to={`/templates/${template.id}/schema`}>Edit schema</Link>
                    </Button>
                </CardHeader>
                <CardContent className="space-y-4">
                    <p className="text-sm text-muted-foreground">
                        {fieldCount} fields · {mappingCount} mappings
                    </p>
                    {fieldCount === 0 ? (
                        <p className="text-sm text-muted-foreground">No schema defined yet.</p>
                    ) : (
                        <ul className="space-y-1 text-sm">
                            {tree.map((node) => (
                                <SchemaTreePreview key={node.field.fieldName} node={node} depth={0} />
                            ))}
                        </ul>
                    )}
                </CardContent>
            </Card>
        </div>
    );
}

function DetailItem({
    label,
    value,
    className,
}: {
    label: string;
    value: ReactNode;
    className?: string;
}) {
    return (
        <div className={className}>
            <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">{label}</p>
            <div className="mt-1 text-sm text-foreground">{value}</div>
        </div>
    );
}

function SchemaTreePreview({
    node,
    depth,
}: {
    node: ReturnType<typeof buildFieldTree>[number];
    depth: number;
}) {
    return (
        <li>
            <div style={{ paddingLeft: `${depth * 16}px` }} className="font-mono text-xs">
                {node.field.fieldName} ({node.field.nodeType})
            </div>
            <ul>
                {node.children.map((child) => (
                    <SchemaTreePreview key={child.field.fieldName} node={child} depth={depth + 1} />
                ))}
            </ul>
        </li>
    );
}
