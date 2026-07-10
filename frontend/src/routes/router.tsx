import { createBrowserRouter } from 'react-router-dom';

import { AppShell } from '@/layouts/AppShell';
import { AuthLayout } from '@/layouts/AuthLayout';
import {
    TemplateCreatePage,
    TemplateDetailPage,
    TemplateEditPage,
    TemplateImportReviewPage,
    TemplateListPage,
    TemplateSchemaEditorPage,
} from '@/features/templates';
import {
    WorkspaceCreatePage,
    WorkspaceEditPage,
    WorkspaceListPage,
    WorkspaceProvider,
    WorkspaceRequiredPage,
} from '@/features/workspace';
import { AccessDeniedPage } from '@/pages/AccessDeniedPage';
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
import { UserListPage } from '@/features/user-management';
import { ProtectedRoute } from '@/routes/ProtectedRoute';
import { DefaultHomeRedirect } from '@/routes/DefaultHomeRedirect';

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
                element: (
                    <WorkspaceProvider>
                        <AppShell />
                    </WorkspaceProvider>
                ),
                children: [
                    { index: true, element: <DefaultHomeRedirect /> },
                    {
                        path: 'templates',
                        element: <ProtectedRoute requireAdmin />,
                        children: [
                            { index: true, element: <TemplateListPage /> },
                            { path: 'new', element: <TemplateCreatePage /> },
                            { path: 'import', element: <TemplateImportReviewPage /> },
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
                        path: 'administration/users',
                        element: <ProtectedRoute requireAdmin />,
                        children: [{ index: true, element: <UserListPage /> }],
                    },
                    {
                        path: 'workspaces',
                        children: [
                            { index: true, element: <WorkspaceListPage /> },
                            { path: 'new', element: <WorkspaceCreatePage /> },
                            { path: ':id/edit', element: <WorkspaceEditPage /> },
                        ],
                    },
                    {
                        path: 'xml-generation',
                        element: <XmlGenerationPage />,
                    },
                    {
                        path: 'export-history',
                        element: <PlaceholderPage />,
                    },
                    {
                        path: 'settings',
                        element: <PlaceholderPage />,
                    },
                    { path: 'workspace-required', element: <WorkspaceRequiredPage /> },
                    { path: 'access-denied', element: <AccessDeniedPage /> },
                ],
            },
        ],
    },
    { path: '*', element: <NotFoundPage /> },
]);
