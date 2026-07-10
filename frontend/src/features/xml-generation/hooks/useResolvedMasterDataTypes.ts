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
     * Types required by mappings, ordered by first appearance in the template mapping list,
     * each paired with the repeatable group field name that owns it (or `null` when the
     * mapped field lives outside any repeatable group — the pre-existing single-selection case).
     * The owning group is derived from the *first* mapping seen for that type, since every
     * field mapped to the same Master Data type is expected to belong to the same group
     * instance (e.g. SeasonID and SeasonName both live under GameCategory).
     */
    const requiredTypeContexts = useMemo((): MasterDataTypeContext[] => {
        const types = typesQuery.data?.items ?? [];
        const typeById = new Map(types.map((type) => [type.id, type]));
        const result: MasterDataTypeContext[] = [];
        const seenTypeIds = new Set<number>();

        for (const mapping of mappings) {
            if (!mapping.masterDataFieldId) {
                continue;
            }
            const typeId = fieldIdToTypeId.get(mapping.masterDataFieldId);
            if (!typeId || seenTypeIds.has(typeId)) {
                continue;
            }
            const type = typeById.get(typeId);
            if (!type) {
                continue;
            }
            result.push({ type, groupFieldName: findOwningRepeatableGroupFieldName(fields, mapping.fieldName) });
            seenTypeIds.add(typeId);
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
