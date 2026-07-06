import { useMemo, useState } from 'react';
import { useParams, useSearchParams } from 'react-router-dom';
import { Plus } from 'lucide-react';

import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { ConfirmDialog } from '@/features/master-data/components/ConfirmDialog';
import { FieldFormDialog, type FieldFormValues } from '@/features/master-data/components/FieldFormDialog';
import { FieldListTable } from '@/features/master-data/components/FieldListTable';
import { MasterDataPageHeader } from '@/features/master-data/components/MasterDataPageHeader';
import { MasterDataPagination } from '@/features/master-data/components/MasterDataPagination';
import { SearchToolbar } from '@/features/master-data/components/SearchToolbar';
import {
    useCreateMasterDataField,
    useDeleteMasterDataField,
    useMasterDataFieldDetail,
    useMasterDataFieldList,
    useUpdateMasterDataField,
} from '@/features/master-data/hooks/useMasterDataFields';
import { useMasterDataTypeDetail } from '@/features/master-data/hooks/useMasterDataTypes';
import type { MasterDataFieldListItem } from '@/features/master-data/types/master-data.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function MasterDataFieldListPage() {
    const { typeId } = useParams();
    const id = Number(typeId);
    const [searchParams, setSearchParams] = useSearchParams();
    const [keywordInput, setKeywordInput] = useState(searchParams.get('keyword') ?? '');
    const [dialogMode, setDialogMode] = useState<'create' | 'edit' | null>(null);
    const [editFieldId, setEditFieldId] = useState<number | null>(null);
    const [deleteTarget, setDeleteTarget] = useState<MasterDataFieldListItem | null>(null);

    const page = Number(searchParams.get('page') ?? '1');
    const keyword = searchParams.get('keyword') ?? '';

    const typeQuery = useMasterDataTypeDetail(id);
    const listParams = useMemo(
        () => ({ typeId: id, page: Number.isNaN(page) ? 1 : page, pageSize: 20, keyword }),
        [id, page, keyword],
    );
    const { data, isLoading, isError, error, refetch } = useMasterDataFieldList(listParams);
    const fieldDetailQuery = useMasterDataFieldDetail(editFieldId ?? undefined);
    const createMutation = useCreateMasterDataField();
    const updateMutation = useUpdateMasterDataField(editFieldId ?? 0);
    const deleteMutation = useDeleteMasterDataField();

    const nextDisplayOrder = (data?.items.length ?? 0) + 1;

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

    async function handleSubmit(values: FieldFormValues) {
        try {
            if (dialogMode === 'create') {
                await createMutation.mutateAsync({
                    typeId: id,
                    code: values.code,
                    name: values.name,
                    dataType: values.dataType,
                    required: values.required,
                    displayOrder: values.displayOrder,
                    description: values.description || null,
                    defaultValue: values.defaultValue || null,
                    unique: values.unique,
                    searchable: true,
                });
                toast.success('Field created');
            } else if (editFieldId) {
                await updateMutation.mutateAsync({
                    name: values.name,
                    dataType: values.dataType,
                    required: values.required,
                    displayOrder: values.displayOrder,
                    description: values.description || null,
                    defaultValue: values.defaultValue || null,
                    unique: values.unique,
                    searchable: true,
                });
                toast.success('Field updated');
            }
            setDialogMode(null);
            setEditFieldId(null);
        } catch (submitError) {
            toast.error(submitError instanceof ApiClientError ? getPrimaryErrorMessage(submitError.errors) : 'Save failed');
        }
    }

    async function handleDeleteConfirm() {
        if (!deleteTarget) return;
        try {
            await deleteMutation.mutateAsync(deleteTarget.id);
            toast.success('Field deleted');
            setDeleteTarget(null);
        } catch (deleteError) {
            toast.error(deleteError instanceof ApiClientError ? getPrimaryErrorMessage(deleteError.errors) : 'Delete failed');
        }
    }

    if (Number.isNaN(id)) {
        return <FullPageError title="Invalid type" description="The master data type ID is not valid." />;
    }

    if (typeQuery.isLoading) {
        return <LoadingSpinner label="Loading type…" />;
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
        <div className="space-y-6">
            <MasterDataPageHeader
                title="Fields"
                description={`Field definitions for ${typeQuery.data.code}`}
                backTo={`/master-data/types/${id}`}
                backLabel="Back to type detail"
                actions={
                    <Button onClick={() => setDialogMode('create')}>
                        <Plus className="h-4 w-4" />
                        Create field
                    </Button>
                }
            />
            <SearchToolbar
                value={keywordInput}
                placeholder="Search fields"
                onChange={setKeywordInput}
                onSearch={applySearch}
            />
            {isLoading ? <LoadingSpinner label="Loading fields…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load fields"
                    description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                    onRetry={() => void refetch()}
                />
            ) : null}
            {!isLoading && !isError && data ? (
                data.items.length === 0 ? (
                    <EmptyPlaceholder
                        title="No fields defined"
                        description="Create fields to define the record schema for this type."
                        action={
                            <Button onClick={() => setDialogMode('create')}>
                                <Plus className="h-4 w-4" />
                                Create field
                            </Button>
                        }
                    />
                ) : (
                    <div className="space-y-4">
                        <FieldListTable
                            items={data.items}
                            onEdit={(item) => {
                                setEditFieldId(item.id);
                                setDialogMode('edit');
                            }}
                            onDelete={setDeleteTarget}
                        />
                        <MasterDataPagination meta={data.meta} onPageChange={handlePageChange} />
                    </div>
                )
            ) : null}
            <FieldFormDialog
                open={dialogMode !== null}
                mode={dialogMode ?? 'create'}
                loading={createMutation.isPending || updateMutation.isPending}
                initial={fieldDetailQuery.data ?? null}
                nextDisplayOrder={nextDisplayOrder}
                onSubmit={(values) => void handleSubmit(values)}
                onClose={() => {
                    setDialogMode(null);
                    setEditFieldId(null);
                }}
            />
            <ConfirmDialog
                open={deleteTarget !== null}
                title="Delete field"
                description={deleteTarget ? `Delete field "${deleteTarget.name}" (${deleteTarget.code})?` : ''}
                confirmLabel="Delete"
                destructive
                loading={deleteMutation.isPending}
                onConfirm={() => void handleDeleteConfirm()}
                onCancel={() => setDeleteTarget(null)}
            />
        </div>
    );
}
