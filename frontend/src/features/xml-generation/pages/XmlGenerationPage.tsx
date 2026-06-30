import { ExecutionPanel } from '@/features/xml-generation/components/ExecutionPanel';

export function XmlGenerationPage() {
    return (
        <div className="space-y-6">
            <div className="border-b border-border pb-6">
                <h1 className="text-2xl font-semibold text-foreground">XML Generation</h1>
                <p className="text-sm text-muted-foreground">
                    Select a template, provide input JSON and master data, then preview or export XML.
                </p>
            </div>
            <ExecutionPanel />
        </div>
    );
}
