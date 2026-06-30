import { Pencil, Trash2 } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import type { MasterDataFieldListItem, MasterDataRecordListItem } from '@/features/master-data/types/master-data.types';

interface RecordListTableProps {
    items: MasterDataRecordListItem[];
    fields: MasterDataFieldListItem[];
    onEdit: (item: MasterDataRecordListItem) => void;
    onDelete: (item: MasterDataRecordListItem) => void;
}

export function RecordListTable({ items, fields, onEdit, onDelete }: RecordListTableProps) {
    const columns = fields.slice().sort((a, b) => a.displayOrder - b.displayOrder).slice(0, 4);

    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>ID</TableHead>
                    {columns.map((field) => (
                        <TableHead key={field.id}>{field.name}</TableHead>
                    ))}
                    <TableHead className="text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell>{item.id}</TableCell>
                        {columns.map((field) => (
                            <TableCell key={field.id}>{formatCellValue(item.data[field.code])}</TableCell>
                        ))}
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

function formatCellValue(value: unknown): string {
    if (value === null || value === undefined) {
        return '—';
    }
    return String(value);
}
