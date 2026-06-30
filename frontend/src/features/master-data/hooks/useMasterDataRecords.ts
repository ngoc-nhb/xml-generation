import { useMutation, useQuery, useQueryClient } from '@tanstack/react-query';

import * as recordsApi from '@/features/master-data/api/records.api';
import { masterDataQueryKeys } from '@/features/master-data/hooks/queryKeys';
import type {
    CreateMasterDataRecordRequest,
    MasterDataRecordListParams,
    UpdateMasterDataRecordRequest,
} from '@/features/master-data/types/master-data.types';

export function useMasterDataRecordList(params: MasterDataRecordListParams) {
    return useQuery({
        queryKey: masterDataQueryKeys.recordList({
            typeId: params.typeId,
            page: params.page ?? 1,
            pageSize: params.pageSize ?? 20,
            keyword: params.keyword ?? '',
        }),
        queryFn: () => recordsApi.fetchMasterDataRecords(params),
        enabled: !Number.isNaN(params.typeId),
    });
}

export function useMasterDataRecordDetail(id: number | undefined) {
    return useQuery({
        queryKey: masterDataQueryKeys.recordDetail(id ?? 0),
        queryFn: () => recordsApi.fetchMasterDataRecord(id!),
        enabled: id !== undefined && !Number.isNaN(id),
    });
}

export function useCreateMasterDataRecord() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: CreateMasterDataRecordRequest) => recordsApi.createMasterDataRecord(request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.records() });
        },
    });
}

export function useUpdateMasterDataRecord(id: number) {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (request: UpdateMasterDataRecordRequest) => recordsApi.updateMasterDataRecord(id, request),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.recordDetail(id) });
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.records() });
        },
    });
}

export function useDeleteMasterDataRecord() {
    const queryClient = useQueryClient();
    return useMutation({
        mutationFn: (id: number) => recordsApi.deleteMasterDataRecord(id),
        onSuccess: () => {
            void queryClient.invalidateQueries({ queryKey: masterDataQueryKeys.records() });
        },
    });
}
