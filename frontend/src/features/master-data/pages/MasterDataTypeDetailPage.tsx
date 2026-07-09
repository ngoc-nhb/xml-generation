import { Link, useParams } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { MasterDataPageHeader } from '@/features/master-data/components/MasterDataPageHeader';
import { MasterDataStatusBadge } from '@/features/master-data/components/MasterDataStatusBadge';
import { MasterDataWorkflowSteps } from '@/features/master-data/components/MasterDataWorkflowSteps';
import { WorkflowContinuationLinkBanner } from '@/features/master-data/components/WorkflowContinuationBanner';
import { useMasterDataFieldsForType } from '@/features/master-data/hooks/useMasterDataFields';
import { useMasterDataRecordList } from '@/features/master-data/hooks/useMasterDataRecords';
import { useMasterDataTypeDetail } from '@/features/master-data/hooks/useMasterDataTypes';
import { useSetPageMeta } from '@/providers/PageMetaProvider';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';

export function MasterDataTypeDetailPage() {
    const { typeId } = useParams();
    const id = Number(typeId);
    const { data, isLoading, isError, error, refetch } = useMasterDataTypeDetail(id);
    const fieldsQuery = useMasterDataFieldsForType(id);
    const recordsQuery = useMasterDataRecordList({ typeId: id, page: 1, pageSize: 1 });

    useSetPageMeta(
        data
            ? {
                  title: data.name,
                  description: `Master data type ${data.code}. Continue the workflow to add data fields and records.`,
              }
            : null,
    );

    if (Number.isNaN(id)) {
        return <FullPageError title="Invalid type" description="The master data type ID is not valid." />;
    }

    if (isLoading || fieldsQuery.isLoading || recordsQuery.isLoading) {
        return <LoadingSpinner label="Loading type…" />;
    }

    if (isError || !data) {
        return (
            <FullPageError
                title="Unable to load type"
                description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                onRetry={() => void refetch()}
            />
        );
    }

    const hasFields = (fieldsQuery.data?.items.length ?? 0) > 0;
    const hasRecords = (recordsQuery.data?.meta.totalRecords ?? 0) > 0;

    return (
        <div className="space-y-8">
            <MasterDataPageHeader
                backTo="/master-data"
                backLabel="Back to types"
                actions={
                    <>
                        <Button asChild variant="outline">
                            <Link to={`/master-data/types/${data.id}/edit`}>Edit metadata</Link>
                        </Button>
                        <Button asChild variant={hasFields ? 'outline' : 'default'}>
                            <Link to={`/master-data/types/${data.id}/fields`}>Create Data Field</Link>
                        </Button>
                        <Button asChild variant={hasFields ? 'default' : 'secondary'}>
                            <Link
                                to={`/master-data/types/${data.id}/records`}
                                className={!hasFields ? 'pointer-events-none opacity-50' : undefined}
                                tabIndex={hasFields ? 0 : -1}
                                aria-disabled={!hasFields}
                            >
                                Create Records
                            </Link>
                        </Button>
                    </>
                }
            />
            <MasterDataWorkflowSteps
                activeStep={hasFields ? (hasRecords ? 'records' : 'dataFile') : 'dataFile'}
                typeId={data.id}
                hasTypes
                hasFields={hasFields}
                hasRecords={hasRecords}
            />
            {!hasFields ? (
                <WorkflowContinuationLinkBanner
                    message="Master Type created successfully. Continue by creating a Data Field."
                    actionLabel="Create Data Field"
                    actionTo={`/master-data/types/${data.id}/fields`}
                />
            ) : !hasRecords ? (
                <WorkflowContinuationLinkBanner
                    message="Data Field is ready. Next, create Records."
                    actionLabel="Create Records"
                    actionTo={`/master-data/types/${data.id}/records`}
                />
            ) : null}
            <Card>
                <CardHeader>
                    <CardTitle>Type metadata</CardTitle>
                </CardHeader>
                <CardContent className="grid gap-4 sm:grid-cols-2">
                    <div>
                        <p className="text-xs uppercase text-muted-foreground">Code</p>
                        <p className="text-sm">{data.code}</p>
                    </div>
                    <div>
                        <p className="text-xs uppercase text-muted-foreground">Status</p>
                        <MasterDataStatusBadge status={data.status} />
                    </div>
                    <div className="sm:col-span-2">
                        <p className="text-xs uppercase text-muted-foreground">Name</p>
                        <p className="text-sm">{data.name}</p>
                    </div>
                </CardContent>
            </Card>
        </div>
    );
}
