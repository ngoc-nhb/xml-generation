const TOKEN_KEY = 'xmlgen.accessToken';
const USER_KEY = 'xmlgen.authUser';
const WORKSPACE_ID_KEY = 'xmlgen.workspaceId';

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
    clearStoredWorkspaceId();
}

export function getStoredWorkspaceId(): number | null {
    const raw = sessionStorage.getItem(WORKSPACE_ID_KEY);
    if (!raw) {
        return null;
    }
    const parsed = Number.parseInt(raw, 10);
    return Number.isFinite(parsed) && parsed > 0 ? parsed : null;
}

export function setStoredWorkspaceId(workspaceId: number): void {
    sessionStorage.setItem(WORKSPACE_ID_KEY, String(workspaceId));
}

export function clearStoredWorkspaceId(): void {
    sessionStorage.removeItem(WORKSPACE_ID_KEY);
}
