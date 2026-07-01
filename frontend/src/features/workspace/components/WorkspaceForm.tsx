import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { Button } from '@/components/ui/button';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Select, Textarea } from '@/components/ui/select';
import type { WorkspaceStatus } from '@/features/workspace/types/workspace.types';

const createSchema = z.object({
    code: z
        .string()
        .min(1, 'Code is required')
        .max(100, 'Code must be at most 100 characters')
        .regex(/^[A-Z0-9_]+$/, 'Code must be uppercase letters, numbers, or underscores'),
    name: z.string().min(1, 'Name is required').max(255, 'Name must be at most 255 characters'),
    description: z.string().max(2000, 'Description is too long').optional(),
});

const editSchema = z.object({
    name: z.string().min(1, 'Name is required').max(255, 'Name must be at most 255 characters'),
    description: z.string().max(2000, 'Description is too long').optional(),
    status: z.enum(['ACTIVE', 'INACTIVE']),
});

export type CreateWorkspaceFormValues = z.infer<typeof createSchema>;
export type EditWorkspaceFormValues = z.infer<typeof editSchema>;

interface WorkspaceCreateFormProps {
    loading?: boolean;
    onSubmit: (values: CreateWorkspaceFormValues) => void;
    onCancel: () => void;
}

export function WorkspaceCreateForm({ loading, onSubmit, onCancel }: WorkspaceCreateFormProps) {
    const form = useForm<CreateWorkspaceFormValues>({
        resolver: zodResolver(createSchema),
        defaultValues: {
            code: '',
            name: '',
            description: '',
        },
    });

    return (
        <Form {...form}>
            <form className="space-y-6" onSubmit={form.handleSubmit(onSubmit)}>
                <FormField
                    control={form.control}
                    name="code"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Code</FormLabel>
                            <FormControl>
                                <Input
                                    {...field}
                                    placeholder="J_LEAGUE"
                                    onChange={(event) => field.onChange(event.target.value.toUpperCase())}
                                />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="name"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Name</FormLabel>
                            <FormControl>
                                <Input {...field} placeholder="J League" />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="description"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Description</FormLabel>
                            <FormControl>
                                <Textarea {...field} placeholder="Optional description" />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <div className="flex gap-3">
                    <Button type="submit" disabled={loading}>
                        {loading ? 'Creating…' : 'Create'}
                    </Button>
                    <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
                        Cancel
                    </Button>
                </div>
            </form>
        </Form>
    );
}

interface WorkspaceEditFormProps {
    initialValues: EditWorkspaceFormValues;
    code: string;
    loading?: boolean;
    onSubmit: (values: EditWorkspaceFormValues) => void;
    onCancel: () => void;
}

export function WorkspaceEditForm({ initialValues, code, loading, onSubmit, onCancel }: WorkspaceEditFormProps) {
    const form = useForm<EditWorkspaceFormValues>({
        resolver: zodResolver(editSchema),
        defaultValues: initialValues,
    });

    return (
        <Form {...form}>
            <form className="space-y-6" onSubmit={form.handleSubmit(onSubmit)}>
                <FormItem>
                    <FormLabel>Code</FormLabel>
                    <Input value={code} disabled />
                </FormItem>
                <FormField
                    control={form.control}
                    name="name"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Name</FormLabel>
                            <FormControl>
                                <Input {...field} />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="description"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Description</FormLabel>
                            <FormControl>
                                <Textarea {...field} />
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <FormField
                    control={form.control}
                    name="status"
                    render={({ field }) => (
                        <FormItem>
                            <FormLabel>Status</FormLabel>
                            <FormControl>
                                <Select {...field} value={field.value as WorkspaceStatus}>
                                    <option value="ACTIVE">Active</option>
                                    <option value="INACTIVE">Inactive</option>
                                </Select>
                            </FormControl>
                            <FormMessage />
                        </FormItem>
                    )}
                />
                <div className="flex gap-3">
                    <Button type="submit" disabled={loading}>
                        {loading ? 'Saving…' : 'Save'}
                    </Button>
                    <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
                        Cancel
                    </Button>
                </div>
            </form>
        </Form>
    );
}
