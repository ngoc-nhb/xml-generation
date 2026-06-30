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
        <div className="flex h-full flex-col space-y-3">
            <div className="flex items-center justify-between">
                <p className="text-sm font-medium text-foreground">XML output</p>
                {source ? (
                    <span className="text-xs uppercase tracking-wide text-muted-foreground">
                        {source === 'preview' ? 'Preview' : 'Export'}
                    </span>
                ) : null}
            </div>
            {loading ? <LoadingSpinner label="Generating XML…" /> : null}
            <PreviewErrorPanel errors={validationErrors} />
            <div className="flex-1">
                <XmlViewer xml={loading ? null : xml} />
            </div>
        </div>
    );
}
