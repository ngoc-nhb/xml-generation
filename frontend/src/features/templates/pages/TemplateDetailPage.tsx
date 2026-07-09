import { Link, useNavigate, useParams } from 'react-router-dom';

import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { TemplateDetailView } from '@/features/templates/components/TemplateDetailView';
import { TemplatePageHeader } from '@/features/templates/components/TemplatePageHeader';
import { useTemplateDetail } from '@/features/templates/hooks/useTemplates';
import { useSetPageMeta } from '@/providers/PageMetaProvider';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';

export function TemplateDetailPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const templateId = Number(id);
    const { data, isLoading, isError, error, refetch } = useTemplateDetail(templateId);

    useSetPageMeta(data ? { title: data.name, description: `Template ${data.code}` } : null);

    if (Number.isNaN(templateId)) {
        return <FullPageError title="Invalid template" description="The template ID is not valid." />;
    }

    if (isLoading) {
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
                backTo="/templates"
                backLabel="Back to templates"
                actions={
                    <>
                        <Button asChild variant="outline">
                            <Link to={`/templates/${data.id}/edit`}>Edit metadata</Link>
                        </Button>
                        <Button asChild>
                            <Link to={`/templates/${data.id}/schema`}>Edit schema</Link>
                        </Button>
                    </>
                }
            />
            <TemplateDetailView template={data} />
            <div>
                <Button variant="ghost" onClick={() => navigate('/templates')}>
                    Back to list
                </Button>
            </div>
        </div>
    );
}
