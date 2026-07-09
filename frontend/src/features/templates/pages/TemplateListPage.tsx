import { useMemo, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { Plus, Search, Upload } from 'lucide-react';

import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { Input } from '@/components/ui/input';
import { DeleteTemplateDialog } from '@/features/templates/components/DeleteTemplateDialog';
import { TemplateImportDialog } from '@/features/templates/components/TemplateImportDialog';
import { TemplateListTable } from '@/features/templates/components/TemplateListTable';
import { TemplatePageHeader } from '@/features/templates/components/TemplatePageHeader';
import { TemplatePagination } from '@/features/templates/components/TemplatePagination';
import { useDeleteTemplate, useTemplateList } from '@/features/templates/hooks/useTemplates';
import type { TemplateListItem } from '@/features/templates/types/template.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function TemplateListPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [keywordInput, setKeywordInput] = useState(searchParams.get('keyword') ?? '');
    const [deleteTarget, setDeleteTarget] = useState<TemplateListItem | null>(null);
    const [importOpen, setImportOpen] = useState(false);

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

    const { data, isLoading, isError, error, refetch, isFetching } = useTemplateList(listParams);
    const deleteMutation = useDeleteTemplate();

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
            toast.success('Template deleted');
            setDeleteTarget(null);
        } catch (deleteError) {
            const message =
                deleteError instanceof ApiClientError
                    ? getPrimaryErrorMessage(deleteError.errors)
                    : 'Failed to delete template';
            toast.error(message);
        }
    }

    return (
        <div className="space-y-6">
            <TemplatePageHeader
                actions={
                    <div className="flex flex-wrap gap-2">
                        <Button variant="outline" onClick={() => setImportOpen(true)}>
                            <Upload className="h-4 w-4" />
                            Import XML
                        </Button>
                        <Button asChild>
                            <Link to="/templates/new">
                                <Plus className="h-4 w-4" />
                                Create template
                            </Link>
                        </Button>
                    </div>
                }
            />

            <div className="flex flex-col gap-3 sm:flex-row">
                <div className="relative flex-1">
                    <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                    <Input
                        className="pl-9"
                        placeholder="Search by template name"
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

            {isLoading ? <LoadingSpinner label="Loading templates…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load templates"
                    description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                    onRetry={() => void refetch()}
                />
            ) : null}

            {!isLoading && !isError && data ? (
                data.items.length === 0 ? (
                    <EmptyPlaceholder
                        title="No templates found"
                        description={keyword ? 'Try a different search term.' : 'Create your first template to get started.'}
                        action={
                            <Button asChild>
                                <Link to="/templates/new">Create template</Link>
                            </Button>
                        }
                    />
                ) : (
                    <div className="space-y-4">
                        {isFetching ? <p className="text-sm text-muted-foreground">Refreshing…</p> : null}
                        <TemplateListTable items={data.items} onDelete={setDeleteTarget} />
                        <TemplatePagination meta={data.meta} onPageChange={handlePageChange} />
                    </div>
                )
            ) : null}

            <DeleteTemplateDialog
                template={deleteTarget}
                loading={deleteMutation.isPending}
                onConfirm={() => void handleDeleteConfirm()}
                onCancel={() => setDeleteTarget(null)}
            />
            <TemplateImportDialog open={importOpen} onOpenChange={setImportOpen} />
        </div>
    );
}
