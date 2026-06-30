import type { MasterDataFieldListItem, MasterDataFieldOption } from '@/features/master-data/types/master-data.types';

export function toMasterDataFieldOption(field: MasterDataFieldListItem): MasterDataFieldOption {
    return {
        id: field.id,
        typeId: field.typeId,
        typeCode: field.typeCode,
        typeName: field.typeName,
        code: field.code,
        name: field.name,
        label: `${field.typeCode} / ${field.code} — ${field.name}`,
    };
}

export function toMasterDataFieldOptions(fields: MasterDataFieldListItem[]): MasterDataFieldOption[] {
    return fields.map(toMasterDataFieldOption);
}
