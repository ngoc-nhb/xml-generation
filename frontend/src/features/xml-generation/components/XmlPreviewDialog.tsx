import { Copy } from 'lucide-react';

import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { PreviewErrorPanel } from '@/features/xml-generation/components/PreviewErrorPanel';
import type { ApiError } from '@/types/api/common';
import { toast } from '@/providers/ToastProvider';

interface XmlPreviewDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    xml: string | null;
    validationErrors: ApiError[];
    loading?: boolean;
}

export function XmlPreviewDialog({
    open,
    onOpenChange,
    xml,
    validationErrors,
    loading = false,
}: XmlPreviewDialogProps) {
    const hasErrors = validationErrors.length > 0;

    async function handleCopy() {
        if (!xml) {
            return;
        }
        try {
            await navigator.clipboard.writeText(xml);
            toast.success('XML copied to clipboard');
        } catch {
            toast.error('Failed to copy XML');
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="flex h-[90vh] max-h-[90vh] w-[90vw] max-w-[90vw] flex-col gap-0 p-0 sm:rounded-lg">
                <DialogHeader className="shrink-0 border-b border-border px-6 py-4 pr-12">
                    <DialogTitle>{hasErrors && !xml ? 'Validation errors' : 'XML Preview'}</DialogTitle>
                </DialogHeader>
                <div className="min-h-0 flex-1 overflow-y-auto px-6 py-4">
                    {loading ? <LoadingSpinner label="Generating XML…" /> : null}
                    {hasErrors ? <div className="mb-4"><PreviewErrorPanel errors={validationErrors} /></div> : null}
                    {xml ? (
                        <pre className="overflow-x-auto whitespace-pre-wrap break-words rounded-md border border-border bg-card p-4 font-mono text-xs leading-relaxed text-foreground">
                            {xml}
                        </pre>
                    ) : !loading && !hasErrors ? (
                        <p className="text-sm text-muted-foreground">No XML was generated.</p>
                    ) : null}
                </div>
                <DialogFooter className="shrink-0 border-t border-border px-6 py-4">
                    <Button type="button" variant="outline" onClick={() => void handleCopy()} disabled={!xml}>
                        <Copy className="h-4 w-4" />
                        Copy XML
                    </Button>
                    <Button type="button" onClick={() => onOpenChange(false)}>
                        Close
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
