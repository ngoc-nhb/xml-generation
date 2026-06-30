import { useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import { TemplateEditForm, type EditTemplateFormValues } from '@/features/templates/components/TemplateMetadataForm';
import { TemplatePageHeader } from '@/features/templates/components/TemplatePageHeader';
import { useTemplateDetail, useUpdateTemplate } from '@/features/templates/hooks/useTemplates';
import { useUnsavedChangesBlocker } from '@/features/templates/hooks/useUnsavedChangesBlocker';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function TemplateEditPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const templateId = Number(id);
    const { data, isLoading, isError, error, refetch } = useTemplateDetail(templateId);
    const updateMutation = useUpdateTemplate(templateId);
    const [isDirty, setIsDirty] = useState(false);
    const [showDiscardDialog, setShowDiscardDialog] = useState(false);

    const blocker = useUnsavedChangesBlocker({ when: isDirty });

    const initialValues = useMemo<EditTemplateFormValues | null>(() => {
        if (!data) {
            return null;
        }
        return {
            name: data.name,
            description: data.description ?? '',
            status: data.status,
        };
    }, [data]);

    async function handleSubmit(values: EditTemplateFormValues) {
        try {
            await updateMutation.mutateAsync({
                name: values.name,
                description: values.description || null,
                status: values.status,
            });
            toast.success('Template updated');
            setIsDirty(false);
            navigate(`/templates/${templateId}`);
        } catch (submitError) {
            const message =
                submitError instanceof ApiClientError ? getPrimaryErrorMessage(submitError.errors) : 'Failed to update template';
            toast.error(message);
        }
    }

    function handleCancel() {
        if (isDirty) {
            setShowDiscardDialog(true);
            return;
        }
        navigate(`/templates/${templateId}`);
    }

    if (Number.isNaN(templateId)) {
        return <FullPageError title="Invalid template" description="The template ID is not valid." />;
    }

    if (isLoading || !initialValues) {
        return <LoadingSpinner label="Loading template…" />;
    }

    if (isError || !data) {
        return (
            <FullPageError
                title="Unable to load template"
                description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                onRetry={() => void refetch()}
            />
        );
    }

    return (
        <div className="space-y-6">
            <TemplatePageHeader
                title="Edit template"
                description={`Update metadata for ${data.code}`}
                backTo={`/templates/${templateId}`}
                backLabel="Back to detail"
            />
            <Card>
                <CardHeader>
                    <CardTitle>Template metadata</CardTitle>
                </CardHeader>
                <CardContent>
                    <TemplateEditForm
                        initialValues={initialValues}
                        code={data.code}
                        loading={updateMutation.isPending}
                        onSubmit={(values) => void handleSubmit(values)}
                        onCancel={handleCancel}
                        onDirtyChange={setIsDirty}
                    />
                </CardContent>
            </Card>

            <ConfirmDialog
                open={showDiscardDialog || blocker.state === 'blocked'}
                title="Discard unsaved changes?"
                description="Your metadata changes will be lost if you leave without saving."
                confirmLabel="Discard changes"
                destructive
                onConfirm={() => {
                    setShowDiscardDialog(false);
                    if (blocker.state === 'blocked') {
                        blocker.proceed?.();
                        return;
                    }
                    navigate(`/templates/${templateId}`);
                }}
                onCancel={() => {
                    setShowDiscardDialog(false);
                    blocker.reset?.();
                }}
            />
        </div>
    );
}
