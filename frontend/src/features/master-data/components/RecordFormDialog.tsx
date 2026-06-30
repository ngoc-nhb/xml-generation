import { useState } from 'react';

import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { DynamicRecordForm } from '@/features/master-data/components/DynamicRecordForm';
import type { MasterDataFieldListItem } from '@/features/master-data/types/master-data.types';
import { buildEmptyRecordData, normalizeRecordData } from '@/features/master-data/utils/recordForm';

interface RecordFormDialogProps {
    open: boolean;
    mode: 'create' | 'edit';
    loading?: boolean;
    fields: MasterDataFieldListItem[];
    initialData?: Record<string, unknown>;
    onSubmit: (data: Record<string, unknown>) => void;
    onClose: () => void;
}

export function RecordFormDialog(props: RecordFormDialogProps) {
    const { open, mode, onClose } = props;
    const formKey = `${mode}-${props.initialData ? JSON.stringify(Object.keys(props.initialData)) : 'new'}-${props.fields.length}`;

    return (
        <Dialog open={open} onOpenChange={(next) => !next && onClose()}>
            <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-lg">
                <DialogHeader>
                    <DialogTitle>{mode === 'create' ? 'Create record' : 'Edit record'}</DialogTitle>
                </DialogHeader>
                {open ? <RecordFormContent key={formKey} {...props} /> : null}
            </DialogContent>
        </Dialog>
    );
}

function RecordFormContent({
    mode,
    loading,
    fields,
    initialData,
    onSubmit,
    onClose,
}: RecordFormDialogProps) {
    const [values, setValues] = useState<Record<string, unknown>>(() =>
        mode === 'edit' && initialData ? initialData : buildEmptyRecordData(fields),
    );

    function handleSubmit(event: React.FormEvent) {
        event.preventDefault();
        onSubmit(normalizeRecordData(values, fields));
    }

    return (
        <form onSubmit={handleSubmit}>
            <DynamicRecordForm fields={fields} values={values} onChange={setValues} />
            <DialogFooter className="mt-6">
                <Button type="button" variant="outline" onClick={onClose} disabled={loading}>
                    Cancel
                </Button>
                <Button type="submit" disabled={loading || fields.length === 0}>
                    {loading ? 'Saving…' : mode === 'create' ? 'Create' : 'Save'}
                </Button>
            </DialogFooter>
        </form>
    );
}
