export const masterDataQueryKeys = {
    all: ['master-data'] as const,
    types: () => [...masterDataQueryKeys.all, 'types'] as const,
    typeList: (params: { page: number; pageSize: number; keyword: string }) =>
        [...masterDataQueryKeys.types(), 'list', params] as const,
    typeDetail: (id: number) => [...masterDataQueryKeys.types(), 'detail', id] as const,
    fields: () => [...masterDataQueryKeys.all, 'fields'] as const,
    fieldList: (params: { typeId?: number; page: number; pageSize: number; keyword: string }) =>
        [...masterDataQueryKeys.fields(), 'list', params] as const,
    fieldDetail: (id: number) => [...masterDataQueryKeys.fields(), 'detail', id] as const,
    records: () => [...masterDataQueryKeys.all, 'records'] as const,
    recordList: (params: { typeId: number; page: number; pageSize: number; keyword: string }) =>
        [...masterDataQueryKeys.records(), 'list', params] as const,
    recordDetail: (id: number) => [...masterDataQueryKeys.records(), 'detail', id] as const,
};
