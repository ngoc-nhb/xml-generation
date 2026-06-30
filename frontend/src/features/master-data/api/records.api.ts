import { deleteData, getData, getPaginatedData, postData, putData } from '@/api/client';
import type { PageMeta } from '@/types/api/common';
import type {
    CreateMasterDataRecordRequest,
    MasterDataRecordDetail,
    MasterDataRecordListItem,
    MasterDataRecordListParams,
    UpdateMasterDataRecordRequest,
} from '@/features/master-data/types/master-data.types';

export async function fetchMasterDataRecords(
    params: MasterDataRecordListParams,
): Promise<{ items: MasterDataRecordListItem[]; meta: PageMeta }> {
    const { data, meta } = await getPaginatedData<MasterDataRecordListItem[]>('/master-data/records', {
        typeId: params.typeId,
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 20,
        keyword: params.keyword || undefined,
    });
    return { items: data, meta };
}

export async function fetchMasterDataRecord(id: number): Promise<MasterDataRecordDetail> {
    return getData<MasterDataRecordDetail>(`/master-data/records/${id}`);
}

export async function createMasterDataRecord(request: CreateMasterDataRecordRequest): Promise<MasterDataRecordDetail> {
    return postData<MasterDataRecordDetail, CreateMasterDataRecordRequest>('/master-data/records', request);
}

export async function updateMasterDataRecord(
    id: number,
    request: UpdateMasterDataRecordRequest,
): Promise<MasterDataRecordDetail> {
    return putData<MasterDataRecordDetail, UpdateMasterDataRecordRequest>(`/master-data/records/${id}`, request);
}

export async function deleteMasterDataRecord(id: number): Promise<void> {
    await deleteData(`/master-data/records/${id}`);
}
