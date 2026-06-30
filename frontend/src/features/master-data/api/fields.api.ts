import { deleteData, getData, getPaginatedData, postData, putData } from '@/api/client';
import type { PageMeta } from '@/types/api/common';
import type {
    CreateMasterDataFieldRequest,
    MasterDataFieldDetail,
    MasterDataFieldListItem,
    MasterDataFieldListParams,
    UpdateMasterDataFieldRequest,
} from '@/features/master-data/types/master-data.types';

export async function fetchMasterDataFields(
    params: MasterDataFieldListParams,
): Promise<{ items: MasterDataFieldListItem[]; meta: PageMeta }> {
    const { data, meta } = await getPaginatedData<MasterDataFieldListItem[]>('/master-data/fields', {
        typeId: params.typeId,
        page: params.page ?? 1,
        pageSize: params.pageSize ?? 20,
        keyword: params.keyword || undefined,
    });
    return { items: data, meta };
}

export async function fetchMasterDataField(id: number): Promise<MasterDataFieldDetail> {
    return getData<MasterDataFieldDetail>(`/master-data/fields/${id}`);
}

export async function createMasterDataField(request: CreateMasterDataFieldRequest): Promise<{ id: number }> {
    return postData<{ id: number }, CreateMasterDataFieldRequest>('/master-data/fields', request);
}

export async function updateMasterDataField(id: number, request: UpdateMasterDataFieldRequest): Promise<void> {
    await putData(`/master-data/fields/${id}`, request);
}

export async function deleteMasterDataField(id: number): Promise<void> {
    await deleteData(`/master-data/fields/${id}`);
}
