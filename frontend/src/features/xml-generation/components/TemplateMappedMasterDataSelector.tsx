import { LoadingSpinner } from '@/components/loading-spinner';
import { MasterDataTypeRecordPicker } from '@/features/xml-generation/components/MasterDataTypeRecordPicker';
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
    const { mappedFieldIds, orderedRequiredTypes, isLoading } = useResolvedMasterDataTypes(mappings);

    if (mappedFieldIds.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">No master data mappings are defined for this template.</p>
        );
    }

    if (isLoading) {
        return <LoadingSpinner label="Loading master data requirements…" />;
    }

    function updateSelection(typeId: number, recordId: number, recordLabel: string) {
        onChange(
            selections.map((entry) =>
                entry.typeId === typeId ? { ...entry, recordId, recordLabel } : entry,
            ),
        );
    }

    if (orderedRequiredTypes.length === 0) {
        return <p className="text-sm text-muted-foreground">No master data types are required for this template.</p>;
    }

    return (
        <ul className="space-y-4">
            {orderedRequiredTypes.map((type) => {
                const entry =
                    selections.find((selection) => selection.typeId === type.id) ??
                    ({
                        typeId: type.id,
                        typeCode: type.code,
                        typeName: type.name,
                        recordId: 0,
                        recordLabel: '',
                    } satisfies SelectedMasterDataEntry);

                return (
                    <li key={type.id} className="space-y-0.5">
                        <p className="text-sm font-medium leading-tight text-foreground">{type.name}</p>
                        <MasterDataTypeRecordPicker
                            typeId={type.id}
                            value={entry.recordId > 0 ? entry.recordId : null}
                            onChange={(recordId, recordLabel) => updateSelection(type.id, recordId, recordLabel)}
                        />
                    </li>
                );
            })}
        </ul>
    );
}
