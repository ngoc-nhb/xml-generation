import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect, useState } from 'react';
import { useForm } from 'react-hook-form';
import { useQuery } from '@tanstack/react-query';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Select } from '@/components/ui/select';
import { fetchUserWorkspaces } from '@/features/user-management/api/users.api';
import { WorkspaceAssignmentField } from '@/features/user-management/components/WorkspaceAssignmentField';
import type {
    SystemRole,
    UserListItem,
    WorkspaceMembershipAssignment,
} from '@/features/user-management/types/user-management.types';

const schema = z.object({
    username: z.string().min(1, 'Username is required'),
    role: z.enum(['ADMIN', 'USER']),
});

type FormValues = z.infer<typeof schema>;

export interface EditUserSubmitValues extends FormValues {
    memberships: WorkspaceMembershipAssignment[];
}

interface EditUserDialogProps {
    open: boolean;
    user: UserListItem | null;
    loading?: boolean;
    onSubmit: (values: EditUserSubmitValues) => void;
    onClose: () => void;
}

export function EditUserDialog({ open, user, loading, onSubmit, onClose }: EditUserDialogProps) {
    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: { username: '', role: 'USER' },
    });
    const [memberships, setMemberships] = useState<WorkspaceMembershipAssignment[]>([]);

    const membershipsQuery = useQuery({
        queryKey: ['users', user?.id, 'workspaces'],
        queryFn: () => fetchUserWorkspaces(user!.id),
        enabled: open && user !== null,
    });

    useEffect(() => {
        if (open && user) {
            form.reset({
                username: user.username,
                role: user.role,
            });
        }
    }, [open, user, form]);

    useEffect(() => {
        if (open && membershipsQuery.data) {
            setMemberships(
                membershipsQuery.data
                    .filter((item) => item.type === 'GLOBAL')
                    .map((item) => ({
                        workspaceId: item.workspaceId,
                        permissions: item.permissions,
                    })),
            );
        }
    }, [open, membershipsQuery.data]);

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                if (!next) {
                    onClose();
                }
            }}
        >
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Edit User</DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form
                        className="space-y-4"
                        onSubmit={form.handleSubmit((values) => onSubmit({ ...values, memberships }))}
                    >
                        <FormField
                            control={form.control}
                            name="username"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Username</FormLabel>
                                    <FormControl>
                                        <Input {...field} autoComplete="off" />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="role"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Role</FormLabel>
                                    <FormControl>
                                        <Select {...field} value={field.value as SystemRole}>
                                            <option value="USER">USER</option>
                                            <option value="ADMIN">ADMIN</option>
                                        </Select>
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <div className="space-y-1">
                            <p className="text-sm font-medium text-foreground">Global workspaces</p>
                            <p className="text-xs text-muted-foreground">
                                Personal workspaces are managed by the user and are not listed here.
                            </p>
                            {membershipsQuery.isLoading ? (
                                <p className="text-sm text-muted-foreground">Loading memberships…</p>
                            ) : (
                                <WorkspaceAssignmentField value={memberships} onChange={setMemberships} />
                            )}
                        </div>
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
                                Cancel
                            </Button>
                            <Button type="submit" disabled={loading}>
                                {loading ? 'Saving…' : 'Save'}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
