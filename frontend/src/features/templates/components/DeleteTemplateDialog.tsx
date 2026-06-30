import { ConfirmDialog } from '@/features/templates/components/ConfirmDialog';
import type { TemplateListItem } from '@/features/templates/types/template.types';

interface DeleteTemplateDialogProps {
    template: TemplateListItem | null;
    loading?: boolean;
    onConfirm: () => void;
    onCancel: () => void;
}

export function DeleteTemplateDialog({ template, loading, onConfirm, onCancel }: DeleteTemplateDialogProps) {
    return (
        <ConfirmDialog
            open={template !== null}
            title="Delete template"
            description={
                template
                    ? `Delete "${template.name}" (${template.code})? This action cannot be undone.`
                    : ''
            }
            confirmLabel="Delete"
            destructive
            loading={loading}
            onConfirm={onConfirm}
            onCancel={onCancel}
        />
    );
}
