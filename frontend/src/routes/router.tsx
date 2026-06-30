import { Navigate, createBrowserRouter } from 'react-router-dom';

import { AppShell } from '@/layouts/AppShell';
import { AuthLayout } from '@/layouts/AuthLayout';
import {
    TemplateCreatePage,
    TemplateDetailPage,
    TemplateEditPage,
    TemplateListPage,
    TemplateSchemaEditorPage,
} from '@/features/templates';
import { AccessDeniedPage } from '@/pages/AccessDeniedPage';
import { DashboardPage } from '@/pages/DashboardPage';
import { LoginPage } from '@/pages/LoginPage';
import { NotFoundPage } from '@/pages/NotFoundPage';
import { PlaceholderPage } from '@/pages/PlaceholderPage';
import { XmlGenerationPage } from '@/features/xml-generation';
import {
    MasterDataFieldListPage,
    MasterDataRecordListPage,
    MasterDataTypeDetailPage,
    MasterDataTypeEditPage,
    MasterDataTypeListPage,
} from '@/features/master-data';
import { ProtectedRoute } from '@/routes/ProtectedRoute';

export const router = createBrowserRouter([
    {
        path: '/login',
        element: <AuthLayout />,
        children: [{ index: true, element: <LoginPage /> }],
    },
    {
        path: '/',
        element: <ProtectedRoute />,
        children: [
            {
                element: <AppShell />,
                children: [
                    { index: true, element: <Navigate to="/dashboard" replace /> },
                    { path: 'dashboard', element: <DashboardPage /> },
                    {
                        path: 'templates',
                        element: <ProtectedRoute requireAdmin />,
                        children: [
                            { index: true, element: <TemplateListPage /> },
                            { path: 'new', element: <TemplateCreatePage /> },
                            { path: ':id', element: <TemplateDetailPage /> },
                            { path: ':id/edit', element: <TemplateEditPage /> },
                            { path: ':id/schema', element: <TemplateSchemaEditorPage /> },
                        ],
                    },
                    {
                        path: 'master-data',
                        element: <ProtectedRoute requireAdmin />,
                        children: [
                            { index: true, element: <MasterDataTypeListPage /> },
                            { path: 'types/:typeId', element: <MasterDataTypeDetailPage /> },
                            { path: 'types/:typeId/edit', element: <MasterDataTypeEditPage /> },
                            { path: 'types/:typeId/fields', element: <MasterDataFieldListPage /> },
                            { path: 'types/:typeId/records', element: <MasterDataRecordListPage /> },
                        ],
                    },
                    {
                        path: 'xml-generation',
                        element: <XmlGenerationPage />,
                    },
                    {
                        path: 'export-history',
                        element: (
                            <PlaceholderPage
                                title="Export History"
                                description="Export history UI will be implemented in a later phase."
                            />
                        ),
                    },
                    {
                        path: 'settings',
                        element: (
                            <PlaceholderPage
                                title="Settings"
                                description="Account and session settings placeholder."
                            />
                        ),
                    },
                    { path: 'access-denied', element: <AccessDeniedPage /> },
                ],
            },
        ],
    },
    { path: '*', element: <NotFoundPage /> },
]);
