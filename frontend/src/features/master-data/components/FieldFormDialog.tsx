import { zodResolver } from '@hookform/resolvers/zod';
import { useEffect } from 'react';
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
import type {
    MasterDataFieldDataType,
    MasterDataFieldDetail,
} from '@/features/master-data/types/master-data.types';

const schema = z.object({
    code: z.string().min(1, 'Code is required'),
    name: z.string().min(1, 'Name is required'),
    dataType: z.enum(['STRING', 'INTEGER', 'LONG', 'DECIMAL', 'BOOLEAN', 'DATE', 'DATETIME']),
    required: z.boolean(),
    displayOrder: z.number().min(1),
    description: z.string().optional(),
    defaultValue: z.string().optional(),
    unique: z.boolean(),
    searchable: z.boolean(),
});

type FormValues = z.infer<typeof schema>;

interface FieldFormDialogProps {
    open: boolean;
    mode: 'create' | 'edit';
    loading?: boolean;
    initial?: MasterDataFieldDetail | null;
    nextDisplayOrder?: number;
    onSubmit: (values: FormValues) => void;
    onClose: () => void;
}

export function FieldFormDialog({
    open,
    mode,
    loading,
    initial,
    nextDisplayOrder = 1,
    onSubmit,
    onClose,
}: FieldFormDialogProps) {
    const form = useForm<FormValues>({
        resolver: zodResolver(schema),
        defaultValues: {
            code: '',
            name: '',
            dataType: 'STRING',
            required: false,
            displayOrder: nextDisplayOrder,
            description: '',
            defaultValue: '',
            unique: false,
            searchable: false,
        },
    });

    useEffect(() => {
        if (!open) {
            return;
        }
        if (mode === 'edit' && initial) {
            form.reset({
                code: initial.code,
                name: initial.name,
                dataType: initial.dataType,
                required: initial.required,
                displayOrder: initial.displayOrder,
                description: initial.description ?? '',
                defaultValue: initial.defaultValue ?? '',
                unique: initial.unique,
                searchable: initial.searchable,
            });
        } else {
            form.reset({
                code: '',
                name: '',
                dataType: 'STRING',
                required: false,
                displayOrder: nextDisplayOrder,
                description: '',
                defaultValue: '',
                unique: false,
                searchable: false,
            });
        }
    }, [open, mode, initial, nextDisplayOrder, form]);

    return (
        <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
            <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
                <DialogHeader>
                    <DialogTitle>{mode === 'create' ? 'Create field' : 'Edit field'}</DialogTitle>
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
                                        <Input {...field} disabled={mode === 'edit'} />
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
                            name="dataType"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Data type</FormLabel>
                                    <FormControl>
                                        <Select {...field} value={field.value as MasterDataFieldDataType}>
                                            <option value="STRING">STRING</option>
                                            <option value="INTEGER">INTEGER</option>
                                            <option value="LONG">LONG</option>
                                            <option value="DECIMAL">DECIMAL</option>
                                            <option value="BOOLEAN">BOOLEAN</option>
                                            <option value="DATE">DATE</option>
                                            <option value="DATETIME">DATETIME</option>
                                        </Select>
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="displayOrder"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Display order</FormLabel>
                                    <FormControl>
                                        <Input
                                            type="number"
                                            min={1}
                                            value={field.value}
                                            onChange={(event) => field.onChange(Number(event.target.value))}
                                        />
                                    </FormControl>
                                    <FormMessage />
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="required"
                            render={({ field }) => (
                                <FormItem className="flex items-center gap-2">
                                    <FormControl>
                                        <input type="checkbox" checked={field.value} onChange={field.onChange} />
                                    </FormControl>
                                    <FormLabel className="!mt-0">Required</FormLabel>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="unique"
                            render={({ field }) => (
                                <FormItem className="flex items-center gap-2">
                                    <FormControl>
                                        <input type="checkbox" checked={field.value} onChange={field.onChange} />
                                    </FormControl>
                                    <FormLabel className="!mt-0">Unique</FormLabel>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="searchable"
                            render={({ field }) => (
                                <FormItem className="flex items-center gap-2">
                                    <FormControl>
                                        <input type="checkbox" checked={field.value} onChange={field.onChange} />
                                    </FormControl>
                                    <FormLabel className="!mt-0">Searchable</FormLabel>
                                </FormItem>
                            )}
                        />
                        <FormField
                            control={form.control}
                            name="defaultValue"
                            render={({ field }) => (
                                <FormItem>
                                    <FormLabel>Default value</FormLabel>
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
                        <DialogFooter>
                            <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
                                Cancel
                            </Button>
                            <Button type="submit" disabled={loading}>
                                {loading ? 'Saving…' : mode === 'create' ? 'Create' : 'Save'}
                            </Button>
                        </DialogFooter>
                    </form>
                </Form>
            </DialogContent>
        </Dialog>
    );
}

export type { FormValues as FieldFormValues };
