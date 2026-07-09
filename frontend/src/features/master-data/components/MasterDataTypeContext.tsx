interface MasterDataTypeContextProps {
    name: string;
    code: string;
}

/** Shows which master data type the current screen belongs to. */
export function MasterDataTypeContext({ name, code }: MasterDataTypeContextProps) {
    return (
        <div className="rounded-lg border border-border bg-muted/30 px-4 py-3">
            <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Master Data Type</p>
            <p className="mt-1 text-base font-semibold text-foreground">{name}</p>
            <p className="text-sm text-muted-foreground">{code}</p>
        </div>
    );
}
