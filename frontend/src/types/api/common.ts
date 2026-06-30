export interface ApiError {
    field?: string | null;
    code: string;
}

export interface PageMeta {
    page: number;
    pageSize: number;
    totalRecords: number;
    totalPages: number;
}

export interface ApiResponse<T> {
    success: boolean;
    data: T;
    errors?: ApiError[];
    meta?: PageMeta;
    message?: string | null;
}

export class ApiClientError extends Error {
    readonly errors: ApiError[];
    readonly status?: number;

    constructor(message: string, errors: ApiError[], status?: number) {
        super(message);
        this.name = 'ApiClientError';
        this.errors = errors;
        this.status = status;
    }
}
