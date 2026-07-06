import { useMemo } from 'react';
import { useQueries } from '@tanstack/react-query';

import { fetchMasterDataField } from '@/features/master-data/api/fields.api';
import { masterDataQueryKeys } from '@/features/master-data/hooks/queryKeys';
import { useMasterDataTypeList } from '@/features/master-data/hooks/useMasterDataTypes';
import type { MasterDataTypeListItem } from '@/features/master-data/types/master-data.types';
import type { TemplateMapping } from '@/features/templates/types/template.types';
import type { SelectedMasterDataEntry } from '@/features/xml-generation/types/xml-generation.types';

export function useResolvedMasterDataTypes(mappings: TemplateMapping[]) {
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

    /** Types required by mappings, ordered by first appearance in the template mapping list. */
    const orderedRequiredTypes = useMemo((): MasterDataTypeListItem[] => {
        const types = typesQuery.data?.items ?? [];
        const typeById = new Map(types.map((type) => [type.id, type]));
        const result: MasterDataTypeListItem[] = [];
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
            result.push(type);
            seenTypeIds.add(typeId);
        }

        return result;
    }, [fieldIdToTypeId, mappings, typesQuery.data?.items]);

    const requiredTypeIds = useMemo(
        () => orderedRequiredTypes.map((type) => type.id),
        [orderedRequiredTypes],
    );

    const emptySelections = useMemo((): SelectedMasterDataEntry[] => {
        return orderedRequiredTypes.map((type) => ({
            typeId: type.id,
            typeCode: type.code,
            typeName: type.name,
            recordId: 0,
            recordLabel: '',
        }));
    }, [orderedRequiredTypes]);

    return {
        mappedFieldIds,
        requiredTypeIds,
        orderedRequiredTypes,
        /** @deprecated Use orderedRequiredTypes */
        requiredTypes: orderedRequiredTypes,
        emptySelections,
        isLoading,
    };
}
