import type {
    MasterDataFieldListItem,
    MasterDataTypeListItem,
} from '@/features/master-data/types/master-data.types';

export interface MasterDataFieldGroup {
    typeId: number;
    typeName: string;
    typeCode: string;
    fields: MasterDataFieldListItem[];
}

/**
 * Groups master data fields under their types.
 * Types keep API creation order; fields are ordered by displayOrder within each type.
 */
export function buildMasterDataFieldGroups(
    types: MasterDataTypeListItem[],
    fields: MasterDataFieldListItem[],
): MasterDataFieldGroup[] {
    const fieldsByTypeId = new Map<number, MasterDataFieldListItem[]>();

    for (const field of fields) {
        const siblings = fieldsByTypeId.get(field.typeId) ?? [];
        siblings.push(field);
        fieldsByTypeId.set(field.typeId, siblings);
    }

    return types
        .map((type) => ({
            typeId: type.id,
            typeName: type.name,
            typeCode: type.code,
            fields: (fieldsByTypeId.get(type.id) ?? []).slice().sort((left, right) => left.displayOrder - right.displayOrder),
        }))
        .filter((group) => group.fields.length > 0);
}

function normalizeSearchText(value: string): string {
    return value.trim().toLowerCase();
}

export function filterMasterDataFieldGroups(
    groups: MasterDataFieldGroup[],
    searchQuery: string,
): MasterDataFieldGroup[] {
    const query = normalizeSearchText(searchQuery);
    if (!query) {
        return groups;
    }

    return groups
        .map((group) => {
            const typeMatches = normalizeSearchText(group.typeName).includes(query);
            const fields = typeMatches
                ? group.fields
                : group.fields.filter((field) => normalizeSearchText(field.name).includes(query));

            if (fields.length === 0) {
                return null;
            }

            return { ...group, fields };
        })
        .filter((group): group is MasterDataFieldGroup => group !== null);
}
