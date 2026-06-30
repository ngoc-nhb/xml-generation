import { useMemo } from 'react';
import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import * as fieldsApi from '@/features/master-data/api/fields.api';
import { masterDataQueryKeys } from '@/features/master-data/hooks/queryKeys';
import type {
    CreateMasterDataFieldRequest,
    MasterDataFieldListParams,
    UpdateMasterDataFieldRequest,
} from '@/features/master-data/types/master-data.types';
import { toMasterDataFieldOptions } from '@/features/master-data/utils/fieldPicker';

export function useMasterDataFieldList(params: MasterDataFieldListParams) {
    return useQuery({
        queryKey: masterDataQueryKeys.fieldList({
            typeId: params.typeId,
            page: params.page ?? 1,
            pageSize: params.pageSize ?? 20,
            keyword: params.keyword ?? '',
        }),
        queryFn: () => fieldsApi.fetchMasterDataFields(params),
    });
}

export function useMasterDataFieldDetail(id: number | undefined) {
    return useQuery({
        queryKey: masterDataQueryKeys.fieldDetail(id ?? 0),
        queryFn: () => fieldsApi.fetchMasterDataField(id!),
        enabled: id !== undefined && !Number.isNaN(id),
    });
}

/** Loads all fields for a type (large page size) — used by record forms. */
export function useMasterDataFieldsForType(typeId: number | undefined) {
    return useMasterDataFieldList({
        typeId,
        page: 1,
        pageSize: 200,
    });
}

/**
 * Public hook for future Template mapping picker integration.
 * Returns searchable field options with stable labels.
 */
export function useMasterDataFieldPickerOptions(params: MasterDataFieldListParams) {
    const query = useMasterDataFieldList(params);
    const options = useMemo(
        () => (query.data ? toMasterDataFieldOptions(query.data.items) : []),
        [query.data],
    );
    return { ...query, options };
}

/** Loads all master data fields for the schema mapping dropdown. */
export function useAllMasterDataFieldPickerOptions() {
    return useMasterDataFieldPickerOptions({
        page: 1,
        pageSize: 500,
    });
}

export function useCreateMasterDataField() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateMasterDataFieldRequest) => fieldsApi.createMasterDataField(request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.fields() });
        },
    });
}

export function useUpdateMasterDataField(id: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: UpdateMasterDataFieldRequest) => fieldsApi.updateMasterDataField(id, request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.fieldDetail(id) });
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.fields() });
        },
    });
}

export function useDeleteMasterDataField() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => fieldsApi.deleteMasterDataField(id),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.fields() });
        },
    });
}
