import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import * as typesApi from '@/features/master-data/api/types.api';
import { masterDataQueryKeys } from '@/features/master-data/hooks/queryKeys';
import type {
    CreateMasterDataTypeRequest,
    MasterDataListParams,
    UpdateMasterDataTypeRequest,
} from '@/features/master-data/types/master-data.types';

export function useMasterDataTypeList(params: MasterDataListParams) {
    return useQuery({
        queryKey: masterDataQueryKeys.typeList({
            page: params.page ?? 1,
            pageSize: params.pageSize ?? 20,
            keyword: params.keyword ?? '',
        }),
        queryFn: () => typesApi.fetchMasterDataTypes(params),
    });
}

export function useMasterDataTypeDetail(id: number | undefined) {
    return useQuery({
        queryKey: masterDataQueryKeys.typeDetail(id ?? 0),
        queryFn: () => typesApi.fetchMasterDataType(id!),
        enabled: id !== undefined && !Number.isNaN(id),
    });
}

export function useCreateMasterDataType() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateMasterDataTypeRequest) => typesApi.createMasterDataType(request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.types() });
        },
    });
}

export function useUpdateMasterDataType(id: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: UpdateMasterDataTypeRequest) => typesApi.updateMasterDataType(id, request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.typeDetail(id) });
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.types() });
        },
    });
}

export function useDeleteMasterDataType() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => typesApi.deleteMasterDataType(id),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.types() });
        },
    });
}
