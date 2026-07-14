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
    WorkspacePermissionsPage,
    WorkspaceProvider,
    WorkspaceRequiredPage,
} from '@/features/workspace';
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
import { UserListPage } from '@/features/user-management';
import { ProtectedRoute } from '@/routes/ProtectedRoute';
import { DefaultHomeRedirect } from '@/routes/DefaultHomeRedirect';
import { NavigationGuardProvider } from '@/providers/NavigationGuardProvider';

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
                        <NavigationGuardProvider>
                            <AppShell />
                        </NavigationGuardProvider>
                    </WorkspaceProvider>
                ),
                children: [
                    { index: true, element: <DefaultHomeRedirect /> },
                    { path: 'dashboard', element: <DashboardPage /> },
                    {
                        path: 'templates',
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
                            {
                                element: <ProtectedRoute requireAdmin />,
                                children: [
                                    { index: true, element: <WorkspaceListPage /> },
                                    { path: 'new', element: <WorkspaceCreatePage /> },
                                ],
                            },
                            { path: ':id/edit', element: <WorkspaceEditPage /> },
                            { path: ':id/settings/permissions', element: <WorkspacePermissionsPage /> },
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
