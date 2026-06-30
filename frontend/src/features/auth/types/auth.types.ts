export interface LoginRequest {
    username: string;
    password: string;
}

export interface LoginResponse {
    userId: number;
    username: string;
    isAdmin: boolean;
    accessToken: string;
}

export interface AuthUser {
    userId: number;
    username: string;
    isAdmin: boolean;
}
