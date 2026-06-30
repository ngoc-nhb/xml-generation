import { RouterProvider } from 'react-router-dom';

import { ErrorBoundary } from '@/components/error-boundary';
import { AuthProvider } from '@/providers/AuthProvider';
import { QueryProvider } from '@/providers/QueryProvider';
import { ToastProvider } from '@/providers/ToastProvider';
import { router } from '@/routes/router';

export default function App() {
    return (
        <ErrorBoundary>
            <QueryProvider>
                <AuthProvider>
                    <RouterProvider router={router} />
                    <ToastProvider />
                </AuthProvider>
            </QueryProvider>
        </ErrorBoundary>
    );
}
