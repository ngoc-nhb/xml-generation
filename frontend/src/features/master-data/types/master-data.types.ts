export type MasterDataTypeStatus = 'ACTIVE' | 'INACTIVE';

export type MasterDataFieldDataType =
    | 'STRING'
    | 'INTEGER'
    | 'LONG'
    | 'DECIMAL'
    | 'BOOLEAN'
    | 'DATE'
    | 'DATETIME';

export interface MasterDataTypeListItem {
    id: number;
    code: string;
    name: string;
    status: MasterDataTypeStatus;
}

export interface MasterDataTypeDetail {
    id: number;
    code: string;
    name: string;
    status: MasterDataTypeStatus;
}

export interface CreateMasterDataTypeRequest {
    code: string;
    name: string;
    description?: string | null;
    status: MasterDataTypeStatus;
}

export interface UpdateMasterDataTypeRequest {
    name: string;
    description?: string | null;
    status: MasterDataTypeStatus;
}

export interface MasterDataFieldListItem {
    id: number;
    typeId: number;
    typeCode: string;
    typeName: string;
    code: string;
    name: string;
    dataType: MasterDataFieldDataType;
    required: boolean;
    displayOrder: number;
    description: string | null;
    defaultValue: string | null;
    unique: boolean;
    searchable: boolean;
    masterDataReferenceTypeId: number | null;
}

export interface MasterDataFieldDetail {
    id: number;
    typeId: number;
    code: string;
    name: string;
    dataType: MasterDataFieldDataType;
    required: boolean;
    displayOrder: number;
    description: string | null;
    defaultValue: string | null;
    unique: boolean;
    searchable: boolean;
    masterDataReferenceTypeId: number | null;
}

export interface CreateMasterDataFieldRequest {
    typeId: number;
    code: string;
    name: string;
    dataType: MasterDataFieldDataType;
    required: boolean;
    displayOrder: number;
    description?: string | null;
    defaultValue?: string | null;
    unique?: boolean;
    searchable?: boolean;
    masterDataReferenceTypeId?: number | null;
}

export interface UpdateMasterDataFieldRequest {
    name: string;
    dataType: MasterDataFieldDataType;
    required: boolean;
    displayOrder: number;
    description?: string | null;
    defaultValue?: string | null;
    unique: boolean;
    searchable: boolean;
    masterDataReferenceTypeId?: number | null;
}

export interface MasterDataRecordListItem {
    id: number;
    typeId: number;
    data: Record<string, unknown>;
}

export interface MasterDataRecordDetail {
    id: number;
    typeId: number;
    data: Record<string, unknown>;
    createdAt: string;
    updatedAt: string;
}

export interface CreateMasterDataRecordRequest {
    typeId: number;
    data: Record<string, unknown>;
}

export interface UpdateMasterDataRecordRequest {
    data: Record<string, unknown>;
}

export interface MasterDataListParams {
    page?: number;
    pageSize?: number;
    keyword?: string;
}

export interface MasterDataFieldListParams extends MasterDataListParams {
    typeId?: number;
}

export interface MasterDataRecordListParams extends MasterDataListParams {
    typeId: number;
}

/** Public shape for future Template mapping picker integration. */
export interface MasterDataFieldOption {
    id: number;
    typeId: number;
    typeCode: string;
    typeName: string;
    code: string;
    name: string;
    label: string;
}
