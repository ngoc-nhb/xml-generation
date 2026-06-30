import type { MasterDataFieldListItem } from '@/features/master-data/types/master-data.types';

export function buildEmptyRecordData(fields: MasterDataFieldListItem[]): Record<string, unknown> {
    const data: Record<string, unknown> = {};
    for (const field of fields) {
        if (field.defaultValue !== null && field.defaultValue !== '') {
            data[field.code] = coerceDefaultValue(field.defaultValue, field.dataType);
        } else {
            data[field.code] = getEmptyValueForType(field.dataType);
        }
    }
    return data;
}

export function normalizeRecordData(
    input: Record<string, unknown>,
    fields: MasterDataFieldListItem[],
): Record<string, unknown> {
    const normalized: Record<string, unknown> = {};
    for (const field of fields) {
        const raw = input[field.code];
        if (raw === '' || raw === null || raw === undefined) {
            normalized[field.code] = field.required ? getEmptyValueForType(field.dataType) : null;
            continue;
        }
        normalized[field.code] = coerceInputValue(raw, field.dataType);
    }
    return normalized;
}

function getEmptyValueForType(dataType: MasterDataFieldListItem['dataType']): unknown {
    switch (dataType) {
        case 'BOOLEAN':
            return false;
        case 'INTEGER':
        case 'LONG':
        case 'DECIMAL':
            return '';
        default:
            return '';
    }
}

function coerceDefaultValue(value: string, dataType: MasterDataFieldListItem['dataType']): unknown {
    return coerceInputValue(value, dataType);
}

function coerceInputValue(raw: unknown, dataType: MasterDataFieldListItem['dataType']): unknown {
    if (raw === null || raw === undefined || raw === '') {
        return raw;
    }
    switch (dataType) {
        case 'INTEGER':
            return Number.parseInt(String(raw), 10);
        case 'LONG':
        case 'DECIMAL':
            return Number(raw);
        case 'BOOLEAN':
            return raw === true || raw === 'true';
        default:
            return String(raw);
    }
}
