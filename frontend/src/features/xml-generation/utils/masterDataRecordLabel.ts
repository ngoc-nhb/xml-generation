import type { MasterDataFieldListItem } from '@/features/master-data/types/master-data.types';

/**
 * Formats a master data record label by concatenating field values in displayOrder.
 * Values are joined with " - " (temporary display rule for XML Generation).
 */
export function formatMasterDataRecordLabel(
    fields: MasterDataFieldListItem[],
    data: Record<string, unknown>,
): string {
    const orderedFields = fields.slice().sort((left, right) => left.displayOrder - right.displayOrder);
    const values = orderedFields
        .map((field) => data[field.code])
        .filter((value) => value !== null && value !== undefined && value !== '')
        .map(String);

    return values.length > 0 ? values.join(' - ') : 'Record';
}
