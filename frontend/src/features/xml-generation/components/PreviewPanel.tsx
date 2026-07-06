import { LoadingSpinner } from '@/components/loading-spinner';
import { PreviewErrorPanel } from '@/features/xml-generation/components/PreviewErrorPanel';
import { XmlViewer } from '@/features/xml-generation/components/XmlViewer';
import type { ApiError } from '@/types/api/common';

interface PreviewPanelProps {
    xml: string | null;
    validationErrors: ApiError[];
    loading?: boolean;
    source?: 'preview' | 'export' | null;
}

export function PreviewPanel({ xml, validationErrors, loading, source }: PreviewPanelProps) {
    return (
        <div className="space-y-3">
            {source ? (
                <p className="text-xs uppercase tracking-wide text-muted-foreground">
                    {source === 'preview' ? 'Preview result' : 'Export result'}
                </p>
            ) : null}
            {loading ? <LoadingSpinner label="Generating XML…" /> : null}
            <PreviewErrorPanel errors={validationErrors} />
            <XmlViewer xml={loading ? null : xml} />
        </div>
    );
}
