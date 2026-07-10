import { LoadingSpinner } from '@/components/loading-spinner';
import { MasterDataTypeRecordPicker } from '@/features/xml-generation/components/MasterDataTypeRecordPicker';
import { useResolvedMasterDataTypes } from '@/features/xml-generation/hooks/useResolvedMasterDataTypes';
import type { TemplateField, TemplateMapping } from '@/features/templates/types/template.types';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';
import {
    countGroupOccurrences,
    findSelectionEntry,
    upsertSelectionEntry,
} from '@/features/xml-generation/utils/masterDataOccurrences';

interface TemplateMappedMasterDataSelectorProps {
    fields: TemplateField[];
    mappings: TemplateMapping[];
    /** Current input form data — occurrence counts for repeatable groups are read from here, not from the schema, so the section count always tracks add/remove/duplicate. */
    formData: Record<string, unknown>;
    selections: SelectedMasterDataEntry[];
    onChange: (selections: SelectedMasterDataEntry[]) => void;
}

export function TemplateMappedMasterDataSelector({
    fields,
    mappings,
    formData,
    selections,
    onChange,
}: TemplateMappedMasterDataSelectorProps) {
    const { requiredTypeContexts, isLoading } = useResolvedMasterDataTypes(mappings, fields);

    if (requiredTypeContexts.length === 0) {
        return (
            <p className="text-sm text-muted-foreground">No master data mappings are defined for this template.</p>
        );
    }

    if (isLoading) {
        return <LoadingSpinner label="Loading master data requirements…" />;
    }

    function updateSelection(
        typeId: number,
        typeCode: string,
        typeName: string,
        groupFieldName: string | null,
        occurrenceIndex: number,
        recordId: number,
        recordLabel: string,
    ) {
        onChange(
            upsertSelectionEntry(selections, {
                typeId,
                typeCode,
                typeName,
                recordId,
                recordLabel,
                groupFieldName,
                occurrenceIndex,
            }),
        );
    }

    return (
        <ul className="space-y-4">
            {requiredTypeContexts.map(({ type, groupFieldName }) => {
                const occurrenceCount = countGroupOccurrences(formData, groupFieldName);
                if (occurrenceCount === 0) {
                    return null;
                }

                return Array.from({ length: occurrenceCount }, (_, occurrenceIndex) => {
                    const entry = findSelectionEntry(selections, type.id, groupFieldName, occurrenceIndex);
                    const label =
                        groupFieldName === null
                            ? type.name
                            : `${groupFieldName}[${occurrenceIndex}] — ${type.name}`;

                    return (
                        <li key={`${type.id}-${groupFieldName ?? 'root'}-${occurrenceIndex}`} className="space-y-0.5">
                            <p className="text-sm font-medium leading-tight text-foreground">{label}</p>
                            <MasterDataTypeRecordPicker
                                typeId={type.id}
                                value={entry && entry.recordId > 0 ? entry.recordId : null}
                                onChange={(recordId, recordLabel) =>
                                    updateSelection(
                                        type.id,
                                        type.code,
                                        type.name,
                                        groupFieldName,
                                        occurrenceIndex,
                                        recordId,
                                        recordLabel,
                                    )
                                }
                            />
                        </li>
                    );
                });
            })}
        </ul>
    );
}
