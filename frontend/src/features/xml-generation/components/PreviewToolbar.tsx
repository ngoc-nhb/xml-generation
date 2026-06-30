import { Eye, FileOutput } from 'lucide-react';

import { Button } from '@/components/ui/button';

interface PreviewToolbarProps {
    disabled?: boolean;
    loading?: boolean;
    onPreview: () => void;
}

export function PreviewToolbar({ disabled, loading, onPreview }: PreviewToolbarProps) {
    return (
        <Button onClick={onPreview} disabled={disabled || loading}>
            <Eye className="h-4 w-4" />
            {loading ? 'Previewing…' : 'Preview'}
        </Button>
    );
}

interface ExportToolbarProps {
    disabled?: boolean;
    loading?: boolean;
    onExport: () => void;
}

export function ExportToolbar({ disabled, loading, onExport }: ExportToolbarProps) {
    return (
        <Button variant="secondary" onClick={onExport} disabled={disabled || loading}>
            <FileOutput className="h-4 w-4" />
            {loading ? 'Exporting…' : 'Export'}
        </Button>
    );
}
