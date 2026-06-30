import { useMemo } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { TypeEditForm } from '@/features/master-data/components/TypeForm';
import { MasterDataPageHeader } from '@/features/master-data/components/MasterDataPageHeader';
import { useMasterDataTypeDetail, useUpdateMasterDataType } from '@/features/master-data/hooks/useMasterDataTypes';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function MasterDataTypeEditPage() {
    const navigate = useNavigate();
    const { typeId } = useParams();
    const id = Number(typeId);
    const { data, isLoading, isError, error, refetch } = useMasterDataTypeDetail(id);
    const updateMutation = useUpdateMasterDataType(id);

    const initialValues = useMemo(
        () =>
            data
                ? {
                      name: data.name,
                      description: '',
                      status: data.status,
                  }
                : null,
        [data],
    );

    async function handleSubmit(values: { name: string; description?: string; status: 'ACTIVE' | 'INACTIVE' }) {
        try {
            await updateMutation.mutateAsync({
                name: values.name,
                description: values.description || null,
                status: values.status,
            });
            toast.success('Type updated');
            navigate(`/master-data/types/${id}`);
        } catch (submitError) {
            toast.error(submitError instanceof ApiClientError ? getPrimaryErrorMessage(submitError.errors) : 'Update failed');
        }
    }

    if (Number.isNaN(id)) {
        return <FullPageError title="Invalid type" description="The master data type ID is not valid." />;
    }

    if (isLoading || !initialValues || !data) {
        return <LoadingSpinner label="Loading type…" />;
    }

    if (isError) {
        return (
            <FullPageError
                title="Unable to load type"
                description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                onRetry={() => void refetch()}
            />
        );
    }

    return (
        <div className="space-y-6">
            <MasterDataPageHeader
                title="Edit master data type"
                description={`Update metadata for ${data.code}`}
                backTo={`/master-data/types/${id}`}
                backLabel="Back to detail"
            />
            <Card>
                <CardHeader>
                    <CardTitle>Type metadata</CardTitle>
                </CardHeader>
                <CardContent>
                    <TypeEditForm
                        code={data.code}
                        initialValues={initialValues}
                        loading={updateMutation.isPending}
                        onSubmit={(values) => void handleSubmit(values)}
                        onCancel={() => navigate(`/master-data/types/${id}`)}
                    />
                </CardContent>
            </Card>
        </div>
    );
}
