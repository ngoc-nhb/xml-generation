import { KeyRound, Pencil } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from '@/components/ui/table';
import { UserRoleBadge } from '@/features/user-management/components/UserRoleBadge';
import type { UserListItem } from '@/features/user-management/types/user-management.types';
import { formatDateTime } from '@/utils/formatDate';

interface UserListTableProps {
    items: UserListItem[];
    onEdit: (item: UserListItem) => void;
    onResetPassword: (item: UserListItem) => void;
}

export function UserListTable({ items, onEdit, onResetPassword }: UserListTableProps) {
    return (
        <Table>
            <TableHeader>
                <TableRow>
                    <TableHead>Username</TableHead>
                    <TableHead>Role</TableHead>
                    <TableHead>Created</TableHead>
                    <TableHead>Updated</TableHead>
                    <TableHead className="text-right">Actions</TableHead>
                </TableRow>
            </TableHeader>
            <TableBody>
                {items.map((item) => (
                    <TableRow key={item.id}>
                        <TableCell className="font-medium">{item.username}</TableCell>
                        <TableCell>
                            <UserRoleBadge role={item.role} />
                        </TableCell>
                        <TableCell>{formatDateTime(item.createdAt)}</TableCell>
                        <TableCell>{formatDateTime(item.updatedAt)}</TableCell>
                        <TableCell className="text-right">
                            <div className="flex justify-end gap-2">
                                <Button variant="outline" size="sm" onClick={() => onEdit(item)}>
                                    <Pencil className="h-4 w-4" />
                                    Edit
                                </Button>
                                <Button variant="outline" size="sm" onClick={() => onResetPassword(item)}>
                                    <KeyRound className="h-4 w-4" />
                                    Reset Password
                                </Button>
                            </div>
                        </TableCell>
                    </TableRow>
                ))}
            </TableBody>
        </Table>
    );
}
