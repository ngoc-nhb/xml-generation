import { useMemo, useState } from 'react';
import { Link, useParams, useSearchParams } from 'react-router-dom';
import { Plus } from 'lucide-react';

import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/features/master-data/components/ConfirmDialog';
import { MasterDataPageHeader } from '@/features/master-data/components/MasterDataPageHeader';
import { MasterDataTypeContext } from '@/features/master-data/components/MasterDataTypeContext';
import { MasterDataPagination } from '@/features/master-data/components/MasterDataPagination';
import { MasterDataWorkflowSteps } from '@/features/master-data/components/MasterDataWorkflowSteps';
import { RecordFormDialog } from '@/features/master-data/components/RecordFormDialog';
import { RecordListTable } from '@/features/master-data/components/RecordListTable';
import { SearchToolbar } from '@/features/master-data/components/SearchToolbar';
import { useMasterDataFieldsForType } from '@/features/master-data/hooks/useMasterDataFields';
import {
    useCreateMasterDataRecord,
    useDeleteMasterDataRecord,
    useMasterDataRecordList,
    useUpdateMasterDataRecord,
} from '@/features/master-data/hooks/useMasterDataRecords';
import { useMasterDataTypeDetail } from '@/features/master-data/hooks/useMasterDataTypes';
import { useSetPageMeta } from '@/providers/PageMetaProvider';
import type { MasterDataRecordListItem } from '@/features/master-data/types/master-data.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function MasterDataRecordListPage() {
    const { typeId } = useParams();
    const id = Number(typeId);
    const [searchParams, setSearchParams] = useSearchParams();
    const [keywordInput, setKeywordInput] = useState(searchParams.get('keyword') ?? '');
    const [dialogMode, setDialogMode] = useState<'create' | 'edit' | null>(null);
    const [editRecord, setEditRecord] = useState<MasterDataRecordListItem | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<MasterDataRecordListItem | null>(null);

    const page = Number(searchParams.get('page') ?? '1');
    const keyword = searchParams.get('keyword') ?? '';

    const typeQuery = useMasterDataTypeDetail(id);
    const fieldsQuery = useMasterDataFieldsForType(id);
    const listParams = useMemo(
        () => ({ typeId: id, page: Number.isNaN(page) ? 1 : page, pageSize: 20, keyword }),
        [id, page, keyword],
    );
    const { data, isLoading, isError, error, refetch } = useMasterDataRecordList(listParams);
    const createMutation = useCreateMasterDataRecord();
    const updateMutation = useUpdateMasterDataRecord(editRecord?.id ?? 0);
    const deleteMutation = useDeleteMasterDataRecord();

    const fields = fieldsQuery.data?.items ?? [];
    const hasFields = fields.length > 0;
    const hasRecords = (data?.meta.totalRecords ?? 0) > 0;

    useSetPageMeta(
        typeQuery.data
            ? {
                  title: `${typeQuery.data.name} — Records`,
                  description: `Create and manage records for master data type ${typeQuery.data.code}.`,
              }
            : null,
    );

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

    async function handleSubmit(recordData: Record<string, unknown>) {
        try {
            if (dialogMode === 'create') {
                await createMutation.mutateAsync({ typeId: id, data: recordData });
                toast.success('Record created');
            } else if (editRecord) {
                await updateMutation.mutateAsync({ data: recordData });
                toast.success('Record updated');
            }
            setDialogMode(null);
            setEditRecord(null);
        } catch (submitError) {
            toast.error(submitError instanceof ApiClientError ? getPrimaryErrorMessage(submitError.errors) : 'Save failed');
        }
    }

    async function handleDeleteConfirm() {
        if (!deleteTarget) return;
        try {
            await deleteMutation.mutateAsync(deleteTarget.id);
            toast.success('Record deleted');
            setDeleteTarget(null);
        } catch (deleteError) {
            toast.error(deleteError instanceof ApiClientError ? getPrimaryErrorMessage(deleteError.errors) : 'Delete failed');
        }
    }

    if (Number.isNaN(id)) {
        return <FullPageError title="Invalid type" description="The master data type ID is not valid." />;
    }

    if (typeQuery.isLoading || fieldsQuery.isLoading) {
        return <LoadingSpinner label="Loading records…" />;
    }

    if (typeQuery.isError || !typeQuery.data) {
        return (
            <FullPageError
                title="Unable to load type"
                description={
                    typeQuery.error instanceof ApiClientError
                        ? getPrimaryErrorMessage(typeQuery.error.errors)
                        : 'Request failed'
                }
                onRetry={() => void typeQuery.refetch()}
            />
        );
    }

    return (
        <div className="space-y-8">
            <MasterDataPageHeader
                backTo={`/master-data/types/${id}`}
                backLabel="Back to type detail"
                actions={
                    <Button onClick={() => setDialogMode('create')} disabled={!hasFields} variant={hasFields ? 'default' : 'secondary'}>
                        <Plus className="h-4 w-4" />
                        Create Record
                    </Button>
                }
            />
            <MasterDataTypeContext name={typeQuery.data.name} code={typeQuery.data.code} />
            <MasterDataWorkflowSteps
                activeStep="records"
                typeId={id}
                hasTypes
                hasFields={hasFields}
                hasRecords={hasRecords}
            />
            {!hasFields ? (
                <EmptyPlaceholder
                    title="No data fields available"
                    description={`Create a data field for ${typeQuery.data.name} before adding records.`}
                    action={
                        <Button asChild>
                            <Link to={`/master-data/types/${id}/fields`}>Create Data Field</Link>
                        </Button>
                    }
                />
            ) : (
                <>
                    <SearchToolbar
                        value={keywordInput}
                        placeholder="Search records"
                        onChange={setKeywordInput}
                        onSearch={applySearch}
                    />
                    {isLoading ? <LoadingSpinner label="Loading records…" /> : null}
                    {isError ? (
                        <FullPageError
                            title="Unable to load records"
                            description={
                                error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'
                            }
                            onRetry={() => void refetch()}
                        />
                    ) : null}
                    {!isLoading && !isError && data ? (
                        data.items.length === 0 ? (
                            <EmptyPlaceholder
                                title="No Records found"
                                description={`Create records to populate ${typeQuery.data.name}.`}
                                action={
                                    <Button onClick={() => setDialogMode('create')}>
                                        <Plus className="h-4 w-4" />
                                        Create Record
                                    </Button>
                                }
                            />
                        ) : (
                            <div className="space-y-4">
                                <RecordListTable
                                    items={data.items}
                                    fields={fields}
                                    onEdit={(item) => {
                                        setEditRecord(item);
                                        setDialogMode('edit');
                                    }}
                                    onDelete={setDeleteTarget}
                                />
                                <MasterDataPagination meta={data.meta} onPageChange={handlePageChange} />
                            </div>
                        )
                    ) : null}
                </>
            )}
            <RecordFormDialog
                open={dialogMode !== null}
                mode={dialogMode ?? 'create'}
                loading={createMutation.isPending || updateMutation.isPending}
                typeName={typeQuery.data.name}
                typeCode={typeQuery.data.code}
                fields={fields}
                initialData={editRecord?.data}
                onSubmit={(recordData) => void handleSubmit(recordData)}
                onClose={() => {
                    setDialogMode(null);
                    setEditRecord(null);
                }}
            />
            <ConfirmDialog
                open={deleteTarget !== null}
                title="Delete record"
                description={deleteTarget ? `Delete record #${deleteTarget.id}?` : ''}
                confirmLabel="Delete"
                destructive
                loading={deleteMutation.isPending}
                onConfirm={() => void handleDeleteConfirm()}
                onCancel={() => setDeleteTarget(null)}
            />
        </div>
    );
}
