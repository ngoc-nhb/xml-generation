import { Link, useParams } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { MasterDataPageHeader } from '@/features/master-data/components/MasterDataPageHeader';
import { MasterDataStatusBadge } from '@/features/master-data/components/MasterDataStatusBadge';
import { useMasterDataTypeDetail } from '@/features/master-data/hooks/useMasterDataTypes';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';

export function MasterDataTypeDetailPage() {
    const { typeId } = useParams();
    const id = Number(typeId);
    const { data, isLoading, isError, error, refetch } = useMasterDataTypeDetail(id);

    if (Number.isNaN(id)) {
        return <FullPageError title="Invalid type" description="The master data type ID is not valid." />;
    }

    if (isLoading) {
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

    return (
        <div className="space-y-6">
            <MasterDataPageHeader
                title={data.name}
                description={`Master data type ${data.code}`}
                backTo="/master-data"
                backLabel="Back to types"
                actions={
                    <>
                        <Button asChild variant="outline">
                            <Link to={`/master-data/types/${data.id}/edit`}>Edit metadata</Link>
                        </Button>
                        <Button asChild>
                            <Link to={`/master-data/types/${data.id}/fields`}>Manage fields</Link>
                        </Button>
                        <Button asChild variant="secondary">
                            <Link to={`/master-data/types/${data.id}/records`}>Manage records</Link>
                        </Button>
                    </>
                }
            />
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
