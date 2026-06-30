export const templateQueryKeys = {
    all: ['templates'] as const,
    lists: () => [...templateQueryKeys.all, 'list'] as const,
    list: (params: { page: number; pageSize: number; keyword: string }) =>
        [...templateQueryKeys.lists(), params] as const,
    details: () => [...templateQueryKeys.all, 'detail'] as const,
    detail: (id: number) => [...templateQueryKeys.details(), id] as const,
};
