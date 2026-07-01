import { ChevronLeft, ChevronRight } from 'lucide-react';

import { Button } from '@/components/ui/button';
import type { PageMeta } from '@/types/api/common';

interface WorkspacePaginationProps {
    meta: PageMeta;
    onPageChange: (page: number) => void;
}

export function WorkspacePagination({ meta, onPageChange }: WorkspacePaginationProps) {
    return (
        <div className="flex items-center justify-between gap-4">
            <p className="text-sm text-muted-foreground">
                Page {meta.page} of {meta.totalPages} · {meta.totalRecords} workspaces
            </p>
            <div className="flex items-center gap-2">
                <Button
                    variant="outline"
                    size="sm"
                    disabled={meta.page <= 1}
                    onClick={() => onPageChange(meta.page - 1)}
                >
                    <ChevronLeft className="h-4 w-4" />
                    Previous
                </Button>
                <Button
                    variant="outline"
                    size="sm"
                    disabled={meta.page >= meta.totalPages}
                    onClick={() => onPageChange(meta.page + 1)}
                >
                    Next
                    <ChevronRight className="h-4 w-4" />
                </Button>
            </div>
        </div>
    );
}
