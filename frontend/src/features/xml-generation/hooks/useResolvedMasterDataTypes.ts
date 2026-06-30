import { useMemo } from 'react';
import { useQueries } from '@tanstack/react-query';

import { fetchMasterDataField } from '@/features/master-data/api/fields.api';
import { masterDataQueryKeys } from '@/features/master-data/hooks/queryKeys';
import { useMasterDataTypeList } from '@/features/master-data/hooks/useMasterDataTypes';
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

    const requiredTypeIds = useMemo(() => {
        const ids = new Set<number>();
        for (const query of fieldQueries) {
            if (query.data?.typeId) {
                ids.add(query.data.typeId);
            }
        }
        return [...ids];
    }, [fieldQueries]);

    const typesQuery = useMasterDataTypeList({ page: 1, pageSize: 200 });
    const isLoading = fieldQueries.some((query) => query.isLoading) || typesQuery.isLoading;

    const requiredTypes = useMemo(() => {
        const types = typesQuery.data?.items ?? [];
        return types.filter((type) => requiredTypeIds.includes(type.id));
    }, [requiredTypeIds, typesQuery.data?.items]);

    const emptySelections = useMemo((): SelectedMasterDataEntry[] => {
        return requiredTypes.map((type) => ({
            typeId: type.id,
            typeCode: type.code,
            typeName: type.name,
            recordId: 0,
            recordLabel: '',
        }));
    }, [requiredTypes]);

    return {
        mappedFieldIds,
        requiredTypeIds,
        requiredTypes,
        emptySelections,
        isLoading,
    };
}
