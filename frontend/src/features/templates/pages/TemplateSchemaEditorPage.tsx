import { useNavigate, useParams } from 'react-router-dom';

import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { SchemaEditor } from '@/features/templates/components/SchemaEditor';
import { TemplatePageHeader } from '@/features/templates/components/TemplatePageHeader';
import { useTemplateDetail, useUpdateTemplateSchema } from '@/features/templates/hooks/useTemplates';
import type { TemplateField, TemplateMapping } from '@/features/templates/types/template.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { useSetPageMeta } from '@/providers/PageMetaProvider';
import { toast } from '@/providers/ToastProvider';

export function TemplateSchemaEditorPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const templateId = Number(id);
    const { data, isLoading, isError, error, refetch } = useTemplateDetail(templateId);
    const updateSchemaMutation = useUpdateTemplateSchema(templateId);

    useSetPageMeta(
        data
            ? {
                  title: 'Template Schema',
                  description: `Edit fields and mappings for ${data.code}.`,
              }
            : null,
    );

    async function handleSave(schema: { fields: TemplateField[]; mappings: TemplateMapping[] }) {
        try {
            await updateSchemaMutation.mutateAsync({
                version: data?.schema?.version ?? null,
                fields: schema.fields,
                mappings: schema.mappings,
            });
            toast.success('Schema saved');
        } catch (saveError) {
            const message =
                saveError instanceof ApiClientError ? getPrimaryErrorMessage(saveError.errors) : 'Failed to save schema';
            toast.error(message);
            throw saveError;
        }
    }

    if (Number.isNaN(templateId)) {
        return <FullPageError title="Invalid template" description="The template ID is not valid." />;
    }

    if (isLoading) {
        return <LoadingSpinner label="Loading schema…" />;
    }

    if (isError || !data) {
        return (
            <FullPageError
                title="Unable to load schema"
                description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                onRetry={() => void refetch()}
            />
        );
    }

    return (
        <div className="space-y-6">
            <TemplatePageHeader
                backTo={`/templates/${templateId}`}
                backLabel="Back to detail"
            />
            <SchemaEditor
                initialSchema={data.schema}
                saving={updateSchemaMutation.isPending}
                onSave={(schema) => handleSave(schema)}
                onSaved={() => navigate(`/templates/${templateId}`)}
                onCancel={() => navigate(`/templates/${templateId}`)}
            />
        </div>
    );
}
