import { useMemo, useState } from 'react';
import { useNavigate, useSearchParams } from 'react-router-dom';
import { Plus } from 'lucide-react';

import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/features/master-data/components/ConfirmDialog';
import { MasterDataPageHeader } from '@/features/master-data/components/MasterDataPageHeader';
import { MasterDataPagination } from '@/features/master-data/components/MasterDataPagination';
import { MasterDataWorkflowSteps } from '@/features/master-data/components/MasterDataWorkflowSteps';
import { SearchToolbar } from '@/features/master-data/components/SearchToolbar';
import { TypeCreateDialog } from '@/features/master-data/components/TypeForm';
import { TypeListTable } from '@/features/master-data/components/TypeListTable';
import { useCreateMasterDataType, useDeleteMasterDataType, useMasterDataTypeList } from '@/features/master-data/hooks/useMasterDataTypes';
import type { MasterDataTypeListItem } from '@/features/master-data/types/master-data.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function MasterDataTypeListPage() {
    const navigate = useNavigate();
    const [searchParams, setSearchParams] = useSearchParams();
    const [keywordInput, setKeywordInput] = useState(searchParams.get('keyword') ?? '');
    const [createOpen, setCreateOpen] = useState(false);
    const [deleteTarget, setDeleteTarget] = useState<MasterDataTypeListItem | null>(null);

    const page = Number(searchParams.get('page') ?? '1');
    const keyword = searchParams.get('keyword') ?? '';

    const listParams = useMemo(
        () => ({ page: Number.isNaN(page) ? 1 : page, pageSize: 20, keyword }),
        [page, keyword],
    );

    const { data, isLoading, isError, error, refetch } = useMasterDataTypeList(listParams);
    const createMutation = useCreateMasterDataType();
    const deleteMutation = useDeleteMasterDataType();

    const hasTypes = (data?.items.length ?? 0) > 0;

    function applySearch() {
        const next = new URLSearchParams(searchParams);
        if (keywordInput.trim()) next.set('keyword', keywordInput.trim());
        else next.delete('keyword');
        next.set('page', '1');
        setSearchParams(next);
    }

    function handlePageChange(nextPage: number) {
        const next = new URLSearchParams(searchParams);
        next.set('page', String(nextPage));
        setSearchParams(next);
    }

    async function handleCreate(values: { code: string; name: string; description?: string; status: 'ACTIVE' | 'INACTIVE' }) {
        try {
            const response = await createMutation.mutateAsync({
                code: values.code,
                name: values.name,
                description: values.description || null,
                status: values.status,
            });
            toast.success('Master Type created successfully.');
            setCreateOpen(false);
            navigate(`/master-data/types/${response.id}`);
        } catch (createError) {
            toast.error(createError instanceof ApiClientError ? getPrimaryErrorMessage(createError.errors) : 'Create failed');
        }
    }

    async function handleDeleteConfirm() {
        if (!deleteTarget) return;
        try {
            await deleteMutation.mutateAsync(deleteTarget.id);
            toast.success('Master data type deleted');
            setDeleteTarget(null);
        } catch (deleteError) {
            toast.error(deleteError instanceof ApiClientError ? getPrimaryErrorMessage(deleteError.errors) : 'Delete failed');
        }
    }

    return (
        <div className="space-y-8">
            <MasterDataPageHeader
                actions={
                    <Button onClick={() => setCreateOpen(true)}>
                        <Plus className="h-4 w-4" />
                        Create Master Type
                    </Button>
                }
            />
            <MasterDataWorkflowSteps
                activeStep="type"
                hasTypes={hasTypes}
                hasFields={false}
                hasRecords={false}
            />
            <SearchToolbar
                value={keywordInput}
                placeholder="Search by type name"
                onChange={setKeywordInput}
                onSearch={applySearch}
            />
            {isLoading ? <LoadingSpinner label="Loading master data types…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load types"
                    description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                    onRetry={() => void refetch()}
                />
            ) : null}
            {!isLoading && !isError && data ? (
                data.items.length === 0 ? (
                    <EmptyPlaceholder
                        title="No Master Types found"
                        description="Create a Master Type to begin managing reusable data."
                        action={
                            <Button onClick={() => setCreateOpen(true)}>
                                <Plus className="h-4 w-4" />
                                Create Master Type
                            </Button>
                        }
                    />
                ) : (
                    <div className="space-y-4">
                        <TypeListTable items={data.items} onDelete={setDeleteTarget} />
                        <MasterDataPagination meta={data.meta} onPageChange={handlePageChange} />
                    </div>
                )
            ) : null}
            <TypeCreateDialog
                open={createOpen}
                loading={createMutation.isPending}
                onSubmit={(values) => void handleCreate(values)}
                onClose={() => setCreateOpen(false)}
            />
            <ConfirmDialog
                open={deleteTarget !== null}
                title="Delete master data type"
                description={deleteTarget ? `Delete "${deleteTarget.name}" (${deleteTarget.code})?` : ''}
                confirmLabel="Delete"
                destructive
                loading={deleteMutation.isPending}
                onConfirm={() => void handleDeleteConfirm()}
                onCancel={() => setDeleteTarget(null)}
            />
        </div>
    );
}
