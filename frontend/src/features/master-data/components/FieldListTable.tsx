import { Pencil, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import type { MasterDataFieldListItem } from '@/features/master-data/types/master-data.types';

interface FieldListTableProps {
    items: MasterDataFieldListItem[];
    onEdit: (item: MasterDataFieldListItem) => void;
    onDelete: (item: MasterDataFieldListItem) => void;
}

export function FieldListTable({ items, onEdit, onDelete }: FieldListTableProps) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Code</TableHead>
                    <TableHead>Name</TableHead>
                    <TableHead>Type</TableHead>
                    <TableHead>Required</TableHead>
                    <TableHead>Order</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-mono text-xs">{item.code}</TableCell>
                        <TableCell>{item.name}</TableCell>
                        <TableCell>{item.dataType}</TableCell>
                        <TableCell>{item.required ? 'Yes' : 'No'}</TableCell>
                        <TableCell>{item.displayOrder}</TableCell>
                        <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                                <Button variant="outline" size="sm" onClick={() => onEdit(item)}>
                                    <Pencil className="h-4 w-4" />
                                    Edit
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
