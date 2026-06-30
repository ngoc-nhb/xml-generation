import { deleteData, getData, getPaginatedData, postData, putData } from '@/api/client';
import type { PageMeta } from '@/types/api/common';
import type {
    CreateMasterDataTypeRequest,
    MasterDataListParams,
    MasterDataTypeDetail,
    MasterDataTypeListItem,
    UpdateMasterDataTypeRequest,
} from '@/features/master-data/types/master-data.types';

export async function fetchMasterDataTypes(
    params: MasterDataListParams,
): Promise<{ items: MasterDataTypeListItem[]; meta: PageMeta }> {
    const { data, meta } = await getPaginatedData<MasterDataTypeListItem[]>('/master-data/types', {
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 20,
        keyword: params.keyword || undefined,
    });
    return { items: data, meta };
}

export async function fetchMasterDataType(id: number): Promise<MasterDataTypeDetail> {
    return getData<MasterDataTypeDetail>(`/master-data/types/${id}`);
}

export async function createMasterDataType(request: CreateMasterDataTypeRequest): Promise<{ id: number }> {
    return postData<{ id: number }, CreateMasterDataTypeRequest>('/master-data/types', request);
}

export async function updateMasterDataType(id: number, request: UpdateMasterDataTypeRequest): Promise<void> {
    await putData(`/master-data/types/${id}`, request);
}

export async function deleteMasterDataType(id: number): Promise<void> {
    await deleteData(`/master-data/types/${id}`);
}
