import { zodResolver } from '@hookform/resolvers/zod';
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
import { Select, Textarea } from '@/components/ui/select';
import type { MasterDataTypeStatus } from '@/features/master-data/types/master-data.types';

const schema = z.object({
    code: z
        .string()
        .min(1, 'Code is required')
        .regex(/^[A-Z0-9_]+$/, 'Code must be uppercase letters, numbers, or underscores'),
    name: z.string().min(1, 'Name is required'),
    description: z.string().optional(),
    status: z.enum(['ACTIVE', 'INACTIVE']),
});

type FormValues = z.infer<typeof schema>;

interface TypeCreateDialogProps {
    open: boolean;
    loading?: boolean;
    onSubmit: (values: FormValues) => void;
    onClose: () => void;
}

export function TypeCreateDialog({ open, loading, onSubmit, onClose }: TypeCreateDialogProps) {
    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: { code: '', name: '', description: '', status: 'ACTIVE' },
    });

    return (
        <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Create master data type</DialogTitle>
                </DialogHeader>
                <Form {...form}>
                    <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
                        <FormField
                            control={form.control}
                            name="code"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Code</FormLabel>
                                    <FormControl>
                                        <Input {...field} onChange={(e) => field.onChange(e.target.value.toUpperCase())} />
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
                                        <Select {...field} value={field.value as MasterDataTypeStatus}>
                                            <option value="ACTIVE">Active</option>
                                            <option value="INACTIVE">Inactive</option>
                                        </Select>
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
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

const editSchema = z.object({
    name: z.string().min(1, 'Name is required'),
    description: z.string().optional(),
    status: z.enum(['ACTIVE', 'INACTIVE']),
});

type EditFormValues = z.infer<typeof editSchema>;

interface TypeEditFormProps {
    code: string;
    initialValues: EditFormValues;
    loading?: boolean;
    onSubmit: (values: EditFormValues) => void;
    onCancel: () => void;
}

export function TypeEditForm({ code, initialValues, loading, onSubmit, onCancel }: TypeEditFormProps) {
    const form = useForm<EditFormValues>({
        resolver: zodResolver(editSchema),
        defaultValues: initialValues,
    });

    return (
        <Form {...form}>
            <form className="space-y-4" onSubmit={form.handleSubmit(onSubmit)}>
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
                                <Select {...field} value={field.value as MasterDataTypeStatus}>
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
                        {loading ? 'Saving…' : 'Save changes'}
                    </Button>
                    <Button type="button" variant="outline" onClick={onCancel} disabled={loading}>
                        Cancel
                    </Button>
                </div>
            </form>
        </Form>
    );
}

export type { EditFormValues as TypeEditFormValues };
