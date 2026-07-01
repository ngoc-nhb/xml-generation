import { useMemo } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { zodResolver } from '@hookform/resolvers/zod';
import { useForm } from 'react-hook-form';
import { z } from 'zod';

import { FullPageError } from '@/components/full-page-error';
import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Form, FormControl, FormField, FormItem, FormLabel, FormMessage } from '@/components/ui/form';
import { Input } from '@/components/ui/input';
import { Textarea } from '@/components/ui/select';
import { SchemaEditor } from '@/features/templates/components/SchemaEditor';
import { TemplatePageHeader } from '@/features/templates/components/TemplatePageHeader';
import { useCreateTemplate } from '@/features/templates/hooks/useTemplates';
import type { TemplateField, TemplateImportDraft, TemplateMapping } from '@/features/templates/types/template.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

const metadataSchema = z.object({
    code: z
        .string()
        .min(1, 'Code is required')
        .max(100, 'Code must be at most 100 characters')
        .regex(/^[A-Z0-9_]+$/, 'Code must be uppercase letters, numbers, or underscores'),
    name: z.string().min(1, 'Name is required').max(255, 'Name must be at most 255 characters'),
    description: z.string().max(2000, 'Description is too long').optional(),
});

type MetadataFormValues = z.infer<typeof metadataSchema>;

interface ImportLocationState {
    draft?: TemplateImportDraft;
}

export function TemplateImportReviewPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const draft = (location.state as ImportLocationState | null)?.draft;
    const createMutation = useCreateTemplate();

    const form = useForm<MetadataFormValues>({
        resolver: zodResolver(metadataSchema),
        defaultValues: {
            code: draft?.suggestedCode ?? '',
            name: draft?.suggestedName ?? '',
            description: '',
        },
    });

    const initialSchema = useMemo(
        () =>
            draft
                ? {
                      version: null,
                      fields: draft.fields.map(({ imported: _imported, ...field }) => field),
                      mappings: [],
                  }
                : null,
        [draft],
    );

    if (!draft || !initialSchema) {
        return (
            <FullPageError
                title="No import draft"
                description="Upload an XML file from the template list to generate a draft."
                onRetry={() => navigate('/templates')}
            />
        );
    }

    async function handleSaveSchema(schema: { fields: TemplateField[]; mappings: TemplateMapping[] }) {
        const metadataValid = await form.trigger();
        if (!metadataValid) {
            toast.error('Fix template metadata before saving.');
            throw new Error('Metadata validation failed');
        }

        const metadata = form.getValues();
        try {
            const response = await createMutation.mutateAsync({
                code: metadata.code,
                name: metadata.name,
                description: metadata.description || null,
                schema: {
                    version: null,
                    fields: schema.fields,
                    mappings: schema.mappings,
                },
            });
            toast.success('Template created from XML import');
            navigate(`/templates/${response.id}/schema`, { replace: true });
        } catch (error) {
            if (error instanceof Error && error.message === 'Metadata validation failed') {
                throw error;
            }
            const message =
                error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Failed to save template';
            toast.error(message);
            throw error;
        }
    }

    return (
        <div className="space-y-6">
            <TemplatePageHeader
                title="Review imported template"
                description={`Draft generated from ${draft.sourceFileName}. Configure metadata, then review fields before saving.`}
                backTo="/templates"
                backLabel="Back to templates"
            />

            <Card>
                <CardHeader>
                    <CardTitle>Template metadata</CardTitle>
                </CardHeader>
                <CardContent>
                    <Form {...form}>
                        <form className="grid gap-4 md:grid-cols-2">
                            <FormField
                                control={form.control}
                                name="code"
                                render={({ field }) => (
                                    <FormItem>
                                        <FormLabel>Template code</FormLabel>
                                        <FormControl>
                                            <Input
                                                {...field}
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
                                        <FormLabel>Template name</FormLabel>
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
                                    <FormItem className="md:col-span-2">
                                        <FormLabel>Description</FormLabel>
                                        <FormControl>
                                            <Textarea {...field} placeholder="Optional description" />
                                        </FormControl>
                                        <FormMessage />
                                    </FormItem>
                                )}
                            />
                        </form>
                    </Form>
                </CardContent>
            </Card>

            <SchemaEditor
                initialSchema={initialSchema}
                importMode
                saving={createMutation.isPending}
                onSave={handleSaveSchema}
                onCancel={() => navigate('/templates')}
            />
        </div>
    );
}
