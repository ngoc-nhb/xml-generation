import { Link } from 'react-router-dom';
import { Pencil, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { MasterDataStatusBadge } from '@/features/master-data/components/MasterDataStatusBadge';
import type { MasterDataTypeListItem } from '@/features/master-data/types/master-data.types';

interface TypeListTableProps {
    items: MasterDataTypeListItem[];
    onDelete: (item: MasterDataTypeListItem) => void;
}

export function TypeListTable({ items, onDelete }: TypeListTableProps) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Code</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Status</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-medium">
                            <Link to={`/master-data/types/${item.id}`} className="text-primary hover:underline">
                                {item.code}
                            </Link>
                        </TableCell>
                        <TableCell>{item.name}</TableCell>
                        <TableCell>
                            <MasterDataStatusBadge status={item.status} />
                        </TableCell>
                        <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                                <Button asChild variant="outline" size="sm">
                                    <Link to={`/master-data/types/${item.id}/edit`}>
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
