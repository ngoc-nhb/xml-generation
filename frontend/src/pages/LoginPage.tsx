import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';

import { Button } from '@/components/ui/button';
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from '@/components/ui/card';
import { Input } from '@/components/ui/input';
import { Label } from '@/components/ui/label';
import { ApiClientError } from '@/types/api/common';
import { useAuth } from '@/providers/AuthProvider';
import { toast } from '@/providers/ToastProvider';

export function LoginPage() {
    const navigate = useNavigate();
    const location = useLocation();
    const { login, isLoading } = useAuth();
    const [username, setUsername] = useState('admin');
    const [password, setPassword] = useState('admin123');

    const from = (location.state as { from?: string } | null)?.from ?? '/dashboard';

    async function handleSubmit(event: React.FormEvent<HTMLFormElement>) {
        event.preventDefault();
        try {
            await login({ username, password });
            toast.success('Signed in successfully');
            navigate(from, { replace: true });
        } catch (error) {
            const message =
                error instanceof ApiClientError ? error.errors[0]?.code ?? error.message : 'Login failed';
            toast.error(message);
        }
    }

    return (
        <Card>
            <CardHeader>
                <CardTitle>Sign in</CardTitle>
                <CardDescription>Authentication placeholder for Phase 6.1 foundation.</CardDescription>
            </CardHeader>
            <CardContent>
                <form className="space-y-4" onSubmit={(event) => void handleSubmit(event)}>
                    <div className="space-y-2">
                        <Label htmlFor="username">Username</Label>
                        <Input
                            id="username"
                            autoComplete="username"
                            value={username}
                            onChange={(event) => setUsername(event.target.value)}
                        />
                    </div>
                    <div className="space-y-2">
                        <Label htmlFor="password">Password</Label>
                        <Input
                            id="password"
                            type="password"
                            autoComplete="current-password"
                            value={password}
                            onChange={(event) => setPassword(event.target.value)}
                        />
                    </div>
                    <Button type="submit" className="w-full" disabled={isLoading}>
                        {isLoading ? 'Signing in…' : 'Sign in'}
                    </Button>
                </form>
            </CardContent>
        </Card>
    );
}
