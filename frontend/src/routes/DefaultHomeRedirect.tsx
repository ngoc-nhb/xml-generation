import { Navigate } from 'react-router-dom';

export function DefaultHomeRedirect() {
    return <Navigate to="/dashboard" replace />;
}
