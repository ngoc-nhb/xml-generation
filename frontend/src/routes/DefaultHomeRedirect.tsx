import { Navigate } from 'react-router-dom';

import { useAuth } from '@/providers/AuthProvider';

export function DefaultHomeRedirect() {
    const { user } = useAuth();

    if (user?.isAdmin) {
        return <Navigate to="/templates" replace />;
    }

    return <Navigate to="/xml-generation" replace />;
}
