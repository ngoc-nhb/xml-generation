import { zodResolver } from '@hookform/resolvers/zod';
import { useState } from 'react';
import { useForm } from 'react-hook-form';
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
import { WorkspaceAssignmentField } from '@/features/user-management/components/WorkspaceAssignmentField';
import type {
    SystemRole,
    WorkspaceMembershipAssignment,
} from '@/features/user-management/types/user-management.types';

const schema = z.object({
    username: z.string().min(1, 'Username is required'),
    password: z.string().min(1, 'Password is required'),
    role: z.enum(['ADMIN', 'USER']),
});

type FormValues = z.infer<typeof schema>;

export interface CreateUserSubmitValues extends FormValues {
    memberships: WorkspaceMembershipAssignment[];
}

interface CreateUserDialogProps {
    open: boolean;
    loading?: boolean;
    onSubmit: (values: CreateUserSubmitValues) => void;
    onClose: () => void;
}

export function CreateUserDialog({ open, loading, onSubmit, onClose }: CreateUserDialogProps) {
    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            username: '',
            password: '',
            role: 'USER',
        },
    });
    const [memberships, setMemberships] = useState<WorkspaceMembershipAssignment[]>([]);

    function handleClose() {
        form.reset();
        setMemberships([]);
        onClose();
    }

    return (
        <Dialog
            open={open}
            onOpenChange={(next) => {
                if (!next) {
                    handleClose();
                }
            }}
        >
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create User</DialogTitle>
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
                            name="password"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Password</FormLabel>
                                    <FormControl>
                                        <Input {...field} type="password" autoComplete="new-password" />
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
                                Assign organization workspaces and permissions. Personal workspaces are
                                created by the user later.
                            </p>
                            <WorkspaceAssignmentField value={memberships} onChange={setMemberships} />
                        </div>
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={handleClose} disabled={loading}>
                                Cancel
                            </Button>
                            <Button type="submit" disabled={loading}>
                                {loading ? 'Creating…' : 'Create'}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}
