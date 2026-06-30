const TOKEN_KEY = 'xmlgen.accessToken';
const USER_KEY = 'xmlgen.authUser';

export interface StoredAuthUser {
    userId: number;
    username: string;
    isAdmin: boolean;
}

export function getAccessToken(): string | null {
    return sessionStorage.getItem(TOKEN_KEY);
}

export function setAccessToken(token: string): void {
    sessionStorage.setItem(TOKEN_KEY, token);
}

export function clearAccessToken(): void {
    sessionStorage.removeItem(TOKEN_KEY);
}

export function getStoredUser(): StoredAuthUser | null {
    const raw = sessionStorage.getItem(USER_KEY);
    if (!raw) {
        return null;
    }
    try {
        return JSON.parse(raw) as StoredAuthUser;
    } catch {
        return null;
    }
}

export function setStoredUser(user: StoredAuthUser): void {
    sessionStorage.setItem(USER_KEY, JSON.stringify(user));
}

export function clearStoredUser(): void {
    sessionStorage.removeItem(USER_KEY);
}

export function clearAuthStorage(): void {
    clearAccessToken();
    clearStoredUser();
}
