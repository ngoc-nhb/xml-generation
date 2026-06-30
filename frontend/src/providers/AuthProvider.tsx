import { createContext, useCallback, useContext, useMemo, useState, type ReactNode } from 'react';

import * as authApi from '@/features/auth/api/auth.api';
import type { AuthUser, LoginRequest } from '@/features/auth/types/auth.types';
import {
    clearAuthStorage,
    getAccessToken,
    getStoredUser,
    setAccessToken,
    setStoredUser,
} from '@/utils/storage';

interface AuthContextValue {
    user: AuthUser | null;
    isAuthenticated: boolean;
    isLoading: boolean;
    login: (request: LoginRequest) => Promise<void>;
    logout: () => Promise<void>;
}

const AuthContext = createContext<AuthContextValue | undefined>(undefined);

function readInitialAuth(): AuthUser | null {
    const token = getAccessToken();
    const user = getStoredUser();
    if (!token || !user) {
        return null;
    }
    return user;
}

export function AuthProvider({ children }: { children: ReactNode }) {
    const [user, setUser] = useState<AuthUser | null>(() => readInitialAuth());
    const [isLoading, setIsLoading] = useState(false);

    const login = useCallback(async (request: LoginRequest) => {
        setIsLoading(true);
        try {
            const response = await authApi.login(request);
            setAccessToken(response.accessToken);
            const authUser: AuthUser = {
                userId: response.userId,
                username: response.username,
                isAdmin: response.isAdmin,
            };
            setStoredUser(authUser);
            setUser(authUser);
        } finally {
            setIsLoading(false);
        }
    }, []);

    const logout = useCallback(async () => {
        try {
            await authApi.logout();
        } finally {
            clearAuthStorage();
            setUser(null);
        }
    }, []);

    const value = useMemo<AuthContextValue>(
        () => ({
            user,
            isAuthenticated: user !== null,
            isLoading,
            login,
            logout,
        }),
        [user, isLoading, login, logout],
    );

    return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>;
}

// eslint-disable-next-line react-refresh/only-export-components
export function useAuth(): AuthContextValue {
    const context = useContext(AuthContext);
    if (!context) {
        throw new Error('useAuth must be used within AuthProvider');
    }
    return context;
}
