import { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Plus, Search } from 'lucide-react';

import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { DeleteWorkspaceDialog } from '@/features/workspace/components/DeleteWorkspaceDialog';
import { WorkspaceListTable } from '@/features/workspace/components/WorkspaceListTable';
import { WorkspacePageHeader } from '@/features/workspace/components/WorkspacePageHeader';
import { WorkspacePagination } from '@/features/workspace/components/WorkspacePagination';
import { useDeleteWorkspace, useWorkspaceManagementList } from '@/features/workspace/hooks/useWorkspaces';
import type { WorkspaceListItem } from '@/features/workspace/types/workspace.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function WorkspaceListPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [keywordInput, setKeywordInput] = useState(searchParams.get('keyword') ?? '');
    const [deleteTarget, setDeleteTarget] = useState<WorkspaceListItem | null>(null);

    const page = Number(searchParams.get('page') ?? '1');
    const keyword = searchParams.get('keyword') ?? '';

    const listParams = useMemo(
        () => ({
            page: Number.isNaN(page) ? 1 : page,
            pageSize: 20,
            keyword,
        }),
        [page, keyword],
    );

    const { data, isLoading, isError, error, refetch, isFetching } = useWorkspaceManagementList(listParams);
    const deleteMutation = useDeleteWorkspace();

    function applySearch() {
        const next = new URLSearchParams(searchParams);
        if (keywordInput.trim()) {
            next.set('keyword', keywordInput.trim());
        } else {
            next.delete('keyword');
        }
        next.set('page', '1');
        setSearchParams(next);
    }

    function handlePageChange(nextPage: number) {
        const next = new URLSearchParams(searchParams);
        next.set('page', String(nextPage));
        setSearchParams(next);
    }

    async function handleDeleteConfirm() {
        if (!deleteTarget) {
            return;
        }
        try {
            await deleteMutation.mutateAsync(deleteTarget.id);
            toast.success('Workspace deleted');
            setDeleteTarget(null);
        } catch (deleteError) {
            const message =
                deleteError instanceof ApiClientError
                    ? getPrimaryErrorMessage(deleteError.errors)
                    : 'Failed to delete workspace';
            toast.error(message);
        }
    }

    return (
        <div className="space-y-6">
            <WorkspacePageHeader
                actions={
                    <Button asChild>
                        <Link to="/workspaces/new">
                            <Plus className="h-4 w-4" />
                            New Workspace
                        </Link>
                    </Button>
                }
            />

            <div className="flex flex-col gap-3 sm:flex-row">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        className="pl-9"
                        placeholder="Search by name or code"
                        value={keywordInput}
                        onChange={(event) => setKeywordInput(event.target.value)}
                        onKeyDown={(event) => {
                            if (event.key === 'Enter') {
                                applySearch();
                            }
                        }}
                    />
                </div>
                <Button variant="secondary" onClick={applySearch}>
                    Search
                </Button>
            </div>

            {isLoading ? <LoadingSpinner label="Loading workspaces…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load workspaces"
                    description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                    onRetry={() => void refetch()}
                />
            ) : null}

            {!isLoading && !isError && data ? (
                data.items.length === 0 ? (
                    <EmptyPlaceholder
                        title="No workspaces found"
                        description={keyword ? 'Try a different search term.' : 'Create your first workspace.'}
                        action={
                            <Button asChild>
                                <Link to="/workspaces/new">New Workspace</Link>
                            </Button>
                        }
                    />
                ) : (
                    <div className="space-y-4">
                        {isFetching ? <p className="text-sm text-muted-foreground">Refreshing…</p> : null}
                        <WorkspaceListTable items={data.items} onDelete={setDeleteTarget} />
                        <WorkspacePagination meta={data.meta} onPageChange={handlePageChange} />
                    </div>
                )
            ) : null}

            <DeleteWorkspaceDialog
                workspace={deleteTarget}
                loading={deleteMutation.isPending}
                onConfirm={() => void handleDeleteConfirm()}
                onCancel={() => setDeleteTarget(null)}
            />
        </div>
    );
}
