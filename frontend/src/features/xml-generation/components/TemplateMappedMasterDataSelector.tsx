import { LoadingSpinner } from '@/components/loading-spinner';
import { MasterDataSelector } from '@/features/xml-generation/components/MasterDataSelector';
import { useResolvedMasterDataTypes } from '@/features/xml-generation/hooks/useResolvedMasterDataTypes';
import type { TemplateMapping } from '@/features/templates/types/template.types';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';

interface TemplateMappedMasterDataSelectorProps {
    mappings: TemplateMapping[];
    selections: SelectedMasterDataEntry[];
    onChange: (selections: SelectedMasterDataEntry[]) => void;
}

export function TemplateMappedMasterDataSelector({
    mappings,
    selections,
    onChange,
}: TemplateMappedMasterDataSelectorProps) {
    const { mappedFieldIds, requiredTypeIds, isLoading } = useResolvedMasterDataTypes(mappings);

    if (mappedFieldIds.length === 0) {
        return (
            <div className="space-y-2 rounded-md border border-dashed border-border p-4 text-sm text-muted-foreground">
                <p className="font-medium text-foreground">Master data</p>
                <p>No master data mappings are defined for this template.</p>
            </div>
        );
    }

    if (isLoading) {
        return <LoadingSpinner label="Loading master data requirements…" />;
    }

    return (
        <div className="space-y-2">
            <p className="text-sm font-medium text-foreground">Master data (from template mappings)</p>
            <MasterDataSelector
                selections={selections}
                onChange={onChange}
                allowedTypeIds={requiredTypeIds}
                hideAddControl
                hideRemoveControl
                hideHeader
            />
        </div>
    );
}
