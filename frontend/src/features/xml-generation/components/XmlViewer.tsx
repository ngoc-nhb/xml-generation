interface XmlViewerProps {
    xml: string | null;
    emptyMessage?: string;
}

export function XmlViewer({ xml, emptyMessage = 'Run Preview or Export to generate XML.' }: XmlViewerProps) {
    if (!xml) {
        return (
            <div className="flex h-full min-h-[320px] items-center justify-center rounded-md border border-dashed border-border bg-muted/30 p-6 text-sm text-muted-foreground">
                {emptyMessage}
            </div>
        );
    }

    return (
        <pre className="h-full min-h-[320px] overflow-auto rounded-md border border-border bg-card p-4 font-mono text-xs leading-relaxed text-foreground">
            {xml}
        </pre>
    );
}
