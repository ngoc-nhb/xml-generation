import { useNavigate, useParams } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { WorkspaceEditForm } from '@/features/workspace/components/WorkspaceForm';
import { WorkspacePageHeader } from '@/features/workspace/components/WorkspacePageHeader';
import { useUpdateWorkspace, useWorkspaceDetail } from '@/features/workspace/hooks/useWorkspaces';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function WorkspaceEditPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const workspaceId = Number(id);
    const { data, isLoading, isError, error, refetch } = useWorkspaceDetail(
        Number.isNaN(workspaceId) ? undefined : workspaceId,
    );
    const updateMutation = useUpdateWorkspace(workspaceId);

    async function handleSubmit(values: { name: string; description?: string; status: 'ACTIVE' | 'INACTIVE' }) {
        try {
            await updateMutation.mutateAsync({
                name: values.name,
                description: values.description || null,
                status: values.status,
            });
            toast.success('Workspace updated');
            navigate('/workspaces', { replace: true });
        } catch (updateError) {
            const message =
                updateError instanceof ApiClientError
                    ? getPrimaryErrorMessage(updateError.errors)
                    : 'Failed to update workspace';
            toast.error(message);
        }
    }

    if (Number.isNaN(workspaceId)) {
        return <FullPageError title="Invalid workspace" description="The workspace id in the URL is not valid." />;
    }

    return (
        <div className="space-y-6">
            <WorkspacePageHeader
                title="Edit Workspace"
                description="Update workspace metadata and status."
                backTo="/workspaces"
                backLabel="Back to workspaces"
            />
            {isLoading ? <LoadingSpinner label="Loading workspace…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load workspace"
                    description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                    onRetry={() => void refetch()}
                />
            ) : null}
            {!isLoading && !isError && data ? (
                <Card>
                    <CardHeader>
                        <CardTitle>{data.name}</CardTitle>
                    </CardHeader>
                    <CardContent>
                        <WorkspaceEditForm
                            code={data.code}
                            initialValues={{
                                name: data.name,
                                description: data.description ?? '',
                                status: data.status,
                            }}
                            loading={updateMutation.isPending}
                            onSubmit={(values) => void handleSubmit(values)}
                            onCancel={() => navigate('/workspaces')}
                        />
                    </CardContent>
                </Card>
            ) : null}
        </div>
    );
}
