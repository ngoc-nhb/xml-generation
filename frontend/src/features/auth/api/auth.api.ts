import { postData } from '@/api/client';
import type { LoginRequest, LoginResponse } from '@/features/auth/types/auth.types';

export async function login(request: LoginRequest): Promise<LoginResponse> {
    return postData<LoginResponse, LoginRequest>('/auth/login', request);
}

export async function logout(): Promise<void> {
    await postData<void>('/auth/logout');
}
