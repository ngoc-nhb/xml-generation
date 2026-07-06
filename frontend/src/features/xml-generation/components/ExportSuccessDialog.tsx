import { Download } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
    Dialog,
    DialogContent,
    DialogFooter,
    DialogHeader,
    DialogTitle,
} from '@/components/ui/dialog';
import { downloadXml } from '@/features/xml-generation/utils/downloadXml';

interface ExportSuccessDialogProps {
    open: boolean;
    onOpenChange: (open: boolean) => void;
    filename: string;
    xml: string;
}

export function ExportSuccessDialog({ open, onOpenChange, filename, xml }: ExportSuccessDialogProps) {
    function handleDownload() {
        downloadXml(xml, filename);
    }

    return (
        <Dialog open={open} onOpenChange={onOpenChange}>
            <DialogContent className="sm:max-w-md">
                <DialogHeader>
                    <DialogTitle>Export successful</DialogTitle>
                </DialogHeader>
                <div className="space-y-2 text-sm">
                    <p className="text-muted-foreground">Your XML file is ready.</p>
                    <div>
                        <p className="font-medium text-foreground">File</p>
                        <p className="font-mono text-foreground">{filename}</p>
                    </div>
                </div>
                <DialogFooter>
                    <Button type="button" variant="outline" onClick={() => onOpenChange(false)}>
                        Close
                    </Button>
                    <Button type="button" onClick={handleDownload}>
                        <Download className="h-4 w-4" />
                        Download file
                    </Button>
                </DialogFooter>
            </DialogContent>
        </Dialog>
    );
}
