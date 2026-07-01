import { useNavigate } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { WorkspaceCreateForm } from '@/features/workspace/components/WorkspaceForm';
import { WorkspacePageHeader } from '@/features/workspace/components/WorkspacePageHeader';
import { useCreateWorkspace } from '@/features/workspace/hooks/useWorkspaces';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function WorkspaceCreatePage() {
    const navigate = useNavigate();
    const createMutation = useCreateWorkspace();

    async function handleSubmit(values: { code: string; name: string; description?: string }) {
        try {
            await createMutation.mutateAsync({
                code: values.code,
                name: values.name,
                description: values.description || null,
                status: 'ACTIVE',
            });
            toast.success('Workspace created');
            navigate('/workspaces', { replace: true });
        } catch (error) {
            const message =
                error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Failed to create workspace';
            toast.error(message);
        }
    }

    return (
        <div className="space-y-6">
            <WorkspacePageHeader
                title="Create Workspace"
                description="Define a new workspace for templates and master data."
                backTo="/workspaces"
                backLabel="Back to workspaces"
            />
            <Card>
                <CardHeader>
                    <CardTitle>Workspace details</CardTitle>
                </CardHeader>
                <CardContent>
                    <WorkspaceCreateForm
                        loading={createMutation.isPending}
                        onSubmit={(values) => void handleSubmit(values)}
                        onCancel={() => navigate('/workspaces')}
                    />
                </CardContent>
            </Card>
        </div>
    );
}
