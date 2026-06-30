import { useNavigate } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { TemplateCreateForm, type CreateTemplateFormValues } from '@/features/templates/components/TemplateMetadataForm';
import { TemplatePageHeader } from '@/features/templates/components/TemplatePageHeader';
import { useCreateTemplate } from '@/features/templates/hooks/useTemplates';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function TemplateCreatePage() {
    const navigate = useNavigate();
    const createMutation = useCreateTemplate();

    async function handleSubmit(values: CreateTemplateFormValues) {
        try {
            const response = await createMutation.mutateAsync({
                code: values.code,
                name: values.name,
                description: values.description || null,
                schema: null,
            });
            toast.success('Template created');
            navigate(`/templates/${response.id}/schema`, { replace: true });
        } catch (error) {
            const message = error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Failed to create template';
            toast.error(message);
        }
    }

    return (
        <div className="space-y-6">
            <TemplatePageHeader
                title="Create template"
                description="Define template metadata. Schema editing comes next."
                backTo="/templates"
                backLabel="Back to templates"
            />
            <Card>
                <CardHeader>
                    <CardTitle>Template metadata</CardTitle>
                </CardHeader>
                <CardContent>
                    <TemplateCreateForm
                        loading={createMutation.isPending}
                        onSubmit={(values) => void handleSubmit(values)}
                        onCancel={() => navigate('/templates')}
                    />
                </CardContent>
            </Card>
        </div>
    );
}
