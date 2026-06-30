import { Link } from 'react-router-dom';
import { Pencil, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { TemplateStatusBadge } from '@/features/templates/components/TemplateStatusBadge';
import type { TemplateListItem } from '@/features/templates/types/template.types';
import { formatDateTime } from '@/utils/formatDate';

interface TemplateListTableProps {
    items: TemplateListItem[];
    onDelete: (item: TemplateListItem) => void;
}

export function TemplateListTable({ items, onDelete }: TemplateListTableProps) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Code</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Updated</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-medium">
                            <Link to={`/templates/${item.id}`} className="text-primary hover:underline">
                                {item.code}
                            </Link>
                        </TableCell>
                        <TableCell>{item.name}</TableCell>
                        <TableCell>
                            <TemplateStatusBadge status={item.status} />
                        </TableCell>
                        <TableCell>{formatDateTime(item.updatedAt)}</TableCell>
                        <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                                <Button asChild variant="outline" size="sm">
                                    <Link to={`/templates/${item.id}/edit`}>
                                        <Pencil className="h-4 w-4" />
                                        Edit
                                    </Link>
                                </Button>
                                <Button variant="destructive" size="sm" onClick={() => onDelete(item)}>
                                    <Trash2 className="h-4 w-4" />
                                    Delete
                                </Button>
                            </div>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
