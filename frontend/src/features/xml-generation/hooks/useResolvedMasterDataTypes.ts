import { useMemo } from 'react';
import { useQueries } from '@tanstack/react-query';

import { fetchMasterDataField } from '@/features/master-data/api/fields.api';
import { masterDataQueryKeys } from '@/features/master-data/hooks/queryKeys';
import { useMasterDataTypeList } from '@/features/master-data/hooks/useMasterDataTypes';
import type { MasterDataTypeListItem } from '@/features/master-data/types/master-data.types';
import type { TemplateField, TemplateMapping } from '@/features/templates/types/template.types';
import { findOwningRepeatableGroupFieldName } from '@/features/templates/utils/schemaTree';
import type { MasterDataTypeContext } from '@/features/xml-generation/utils/masterDataOccurrences';
import type { TemplateCompileMapping } from '@/features/xml-generation/utils/importedMasterDataBuilder';

export function useResolvedMasterDataTypes(mappings: TemplateMapping[], fields: TemplateField[] = []) {
    const mappedFieldIds = useMemo(
        () => [...new Set(mappings.map((mapping) => mapping.masterDataFieldId).filter((id): id is number => id !== null))],
        [mappings],
    );

    const fieldQueries = useQueries({
        queries: mappedFieldIds.map((id) => ({
            queryKey: masterDataQueryKeys.fieldDetail(id),
            queryFn: () => fetchMasterDataField(id),
            enabled: id > 0,
        })),
    });

    const typesQuery = useMasterDataTypeList({ page: 1, pageSize: 500 });
    const isLoading = fieldQueries.some((query) => query.isLoading) || typesQuery.isLoading;

    const fieldIdToTypeId = useMemo(() => {
        const map = new Map<number, number>();
        for (const query of fieldQueries) {
            if (query.data) {
                map.set(query.data.id, query.data.typeId);
            }
        }
        return map;
    }, [fieldQueries]);

/**
     * Master Data contexts required by mappings, ordered by first appearance in the template
     * mapping list. A context is one (type, owning repeatable group) pair — the same type
     * mapped inside two different repeatable groups (or once at template level and once in a
     * group) yields separate contexts, each with its own occurrence-scoped selections.
     * `groupFieldName` is `null` when the mapped field lives outside any repeatable group —
     * the pre-existing single-selection case.
     */
    const requiredTypeContexts = useMemo((): MasterDataTypeContext[] => {
        const types = typesQuery.data?.items ?? [];
        const typeById = new Map(types.map((type) => [type.id, type]));
        const fieldByName = new Map(fields.map((field) => [field.fieldName, field]));
        const result: MasterDataTypeContext[] = [];
        const seenContextKeys = new Set<string>();

        for (const mapping of mappings) {
            if (!mapping.masterDataFieldId) {
                continue;
            }
            // A mapping row can outlive its field's MASTER_DATA sourceType (e.g. the field was
            // mapped, then switched back to INPUT) — such stale mappings must not spawn a section.
            if (fieldByName.get(mapping.fieldName)?.sourceType !== 'MASTER_DATA') {
                continue;
            }
            const typeId = fieldIdToTypeId.get(mapping.masterDataFieldId);
            if (!typeId) {
                continue;
            }
            const groupFieldName = findOwningRepeatableGroupFieldName(fields, mapping.fieldName);
            const contextKey = `${typeId}:${groupFieldName ?? ''}`;
            if (seenContextKeys.has(contextKey)) {
                continue;
            }
            const type = typeById.get(typeId);
            if (!type) {
                continue;
            }
            result.push({ type, groupFieldName });
            seenContextKeys.add(contextKey);
        }

        return result;
    }, [fieldIdToTypeId, fields, mappings, typesQuery.data?.items]);

    const orderedRequiredTypes = useMemo(
        (): MasterDataTypeListItem[] => requiredTypeContexts.map((context) => context.type),
        [requiredTypeContexts],
    );

    const requiredTypeIds = useMemo(
        () => orderedRequiredTypes.map((type) => type.id),
        [orderedRequiredTypes],
    );

    const compileMappings = useMemo((): TemplateCompileMapping[] => {
        const types = typesQuery.data?.items ?? [];
        const typeById = new Map(types.map((type) => [type.id, type]));
        const fieldById = new Map<number, (typeof fieldQueries)[number]['data']>();

        mappedFieldIds.forEach((fieldId, index) => {
            const field = fieldQueries[index]?.data;
            if (field) {
                fieldById.set(fieldId, field);
            }
        });

        const result: TemplateCompileMapping[] = [];
        for (const mapping of mappings) {
            if (!mapping.masterDataFieldId) {
                continue;
            }
            const field = fieldById.get(mapping.masterDataFieldId);
            if (!field) {
                continue;
            }
            const type = typeById.get(field.typeId);
            if (!type) {
                continue;
            }
            result.push({
                fieldName: mapping.fieldName,
                masterDataTypeCode: type.code,
                masterDataFieldName: field.code,
            });
        }
        return result;
    }, [fieldQueries, mappedFieldIds, mappings, typesQuery.data?.items]);

    return {
        mappedFieldIds,
        requiredTypeIds,
        orderedRequiredTypes,
        requiredTypeContexts,
        /** @deprecated Use orderedRequiredTypes */
        requiredTypes: orderedRequiredTypes,
        compileMappings,
        isLoading,
    };
}
