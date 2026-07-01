import { Link } from 'react-router-dom';
import { Pencil, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { WorkspaceStatusBadge } from '@/features/workspace/components/WorkspaceStatusBadge';
import type { WorkspaceListItem } from '@/features/workspace/types/workspace.types';
import { formatDateTime } from '@/utils/formatDate';

interface WorkspaceListTableProps {
    items: WorkspaceListItem[];
    onDelete: (item: WorkspaceListItem) => void;
}

export function WorkspaceListTable({ items, onDelete }: WorkspaceListTableProps) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Name</TableHead>
                    <TableHead>Code</TableHead>
                    <TableHead>Description</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead>Created At</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-medium">{item.name}</TableCell>
                        <TableCell>{item.code}</TableCell>
                        <TableCell className="max-w-xs truncate text-muted-foreground">
                            {item.description?.trim() ? item.description : '—'}
                        </TableCell>
                        <TableCell>
                            <WorkspaceStatusBadge status={item.status} />
                        </TableCell>
                        <TableCell>{formatDateTime(item.createdAt)}</TableCell>
                        <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                                <Button asChild variant="outline" size="sm">
                                    <Link to={`/workspaces/${item.id}/edit`}>
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
