import { useRef } from 'react';
import { useNavigate } from 'react-router-dom';
import { Upload } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogDescription,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { useImportTemplateXml } from '@/features/templates/hooks/useTemplates';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

interface TemplateImportDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
}

export function TemplateImportDialog({ open, onOpenChange }: TemplateImportDialogProps) {
    const navigate = useNavigate();
    const fileInputRef = useRef<HTMLInputElement>(null);
    const importMutation = useImportTemplateXml();

    async function handleFileSelected(file: File | undefined) {
        if (!file) {
            return;
        }

        try {
            const draft = await importMutation.mutateAsync(file);
            onOpenChange(false);
            navigate('/templates/import', { state: { draft } });
        } catch (error) {
            const message =
                error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Failed to import XML';
            toast.error(message);
        } finally {
            if (fileInputRef.current) {
                fileInputRef.current.value = '';
            }
        }
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent>
                <DialogHeader>
                    <DialogTitle>Import XML</DialogTitle>
                    <DialogDescription>
                        Upload a sample XML file to generate an editable template draft. Nothing is saved until you
                        review and confirm.
                    </DialogDescription>
                </DialogHeader>
                <div className="rounded-md border border-dashed border-border p-6 text-center">
                    <Upload className="mx-auto mb-3 h-8 w-8 text-muted-foreground" aria-hidden />
                    <p className="mb-4 text-sm text-muted-foreground">Choose an XML file from your computer.</p>
                    <input
                        ref={fileInputRef}
                        type="file"
                        accept=".xml,application/xml,text/xml"
                        className="hidden"
                        onChange={(event) => void handleFileSelected(event.target.files?.[0])}
                    />
                    <Button
                        type="button"
                        variant="secondary"
                        disabled={importMutation.isPending}
                        onClick={() => fileInputRef.current?.click()}
                    >
                        {importMutation.isPending ? 'Importing…' : 'Choose XML file'}
                    </Button>
                </div>
                <DialogFooter>
                    <Button type="button" variant="outline" onClick={() => onOpenChange(false)} disabled={importMutation.isPending}>
                        Cancel
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
