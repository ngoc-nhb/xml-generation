import axios, { type AxiosError, type AxiosInstance } from 'axios';

import { ApiClientError, type ApiError, type ApiResponse, type PageMeta } from '@/types/api/common';
import { clearAuthStorage, getAccessToken } from '@/utils/storage';

const baseURL = import.meta.env.VITE_API_BASE_URL ?? '/api/v1';

function normalizeErrors(errors: ApiError[] | undefined, fallbackCode: string): ApiError[] {
    if (errors && errors.length > 0) {
        return errors;
    }
    return [{ code: fallbackCode }];
}

function toApiClientError(error: AxiosError<ApiResponse<unknown>>): ApiClientError {
    const status = error.response?.status;
    const payload = error.response?.data;

    if (payload && payload.success === false) {
        return new ApiClientError(
            payload.errors?.[0]?.code ?? 'REQUEST_FAILED',
            normalizeErrors(payload.errors, 'REQUEST_FAILED'),
            status,
        );
    }

    if (status === 401) {
        return new ApiClientError('UNAUTHORIZED', [{ code: 'UNAUTHORIZED' }], status);
    }

    if (!error.response) {
        return new ApiClientError('NETWORK_ERROR', [{ code: 'NETWORK_ERROR' }]);
    }

    return new ApiClientError('INTERNAL_SERVER_ERROR', [{ code: 'INTERNAL_SERVER_ERROR' }], status);
}

function createApiClient(): AxiosInstance {
    const client = axios.create({
        baseURL,
        headers: {
            Accept: 'application/json',
            'Content-Type': 'application/json',
        },
    });

    client.interceptors.request.use((config) => {
        const token = getAccessToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    });

    client.interceptors.response.use(
        (response) => {
            const payload = response.data as ApiResponse<unknown> | undefined;
            if (payload && payload.success === false) {
                throw new ApiClientError(
                    payload.errors?.[0]?.code ?? 'REQUEST_FAILED',
                    normalizeErrors(payload.errors, 'REQUEST_FAILED'),
                    response.status,
                );
            }
            return response;
        },
        (error: AxiosError<ApiResponse<unknown>>) => {
            if (error.response?.status === 401) {
                clearAuthStorage();
            }
            throw toApiClientError(error);
        },
    );

    return client;
}

export const apiClient = createApiClient();

export async function getData<T>(url: string): Promise<T> {
    const response = await apiClient.get<ApiResponse<T>>(url);
    return response.data.data;
}

export async function postData<T, B = unknown>(url: string, body?: B): Promise<T> {
    const response = await apiClient.post<ApiResponse<T>>(url, body);
    return response.data.data;
}

export async function putData<T, B = unknown>(url: string, body?: B): Promise<T> {
    const response = await apiClient.put<ApiResponse<T>>(url, body);
    return response.data.data;
}

export async function deleteData(url: string): Promise<void> {
    await apiClient.delete<ApiResponse<void>>(url);
}

export async function getPaginatedData<T>(
    url: string,
    params?: Record<string, string | number | boolean | undefined | null>,
): Promise<{ data: T; meta: PageMeta }> {
    const response = await apiClient.get<ApiResponse<T>>(url, { params });
    if (!response.data.meta) {
        throw new ApiClientError('MISSING_PAGE_META', [{ code: 'MISSING_PAGE_META' }]);
    }
    return { data: response.data.data, meta: response.data.meta };
}
