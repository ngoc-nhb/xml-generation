import { NavLink, Outlet, useLocation } from 'react-router-dom';
import { Database, FileCode2, History, Home, LogOut, Settings, Shapes, Users } from 'lucide-react';

import { Button } from '@/components/ui/button';
import {
    NoWorkspaceEmptyState,
    WorkspaceSwitcher,
    useWorkspace,
} from '@/features/workspace';
import { usePageMetaContext, PageMetaProvider } from '@/providers/PageMetaProvider';
import { useAuth } from '@/providers/AuthProvider';
import { useNavigationGuard } from '@/providers/NavigationGuardProvider';
import { cn } from '@/utils/cn';

function AppShellContent() {
    const { user, logout } = useAuth();
    const { currentWorkspace, hasPermission, workspaces } = useWorkspace();
    const { requestLeave } = useNavigationGuard();
    const { meta } = usePageMetaContext();
    const location = useLocation();

    const hasAnyWorkspace = workspaces.some((workspace) => workspace.status === 'ACTIVE');
    const workspaceScoped = Boolean(currentWorkspace);
    const onSafeRoute =
        location.pathname === '/dashboard' ||
        location.pathname === '/settings' ||
        location.pathname.startsWith('/administration');

    const navItems = [
        { to: '/dashboard', label: 'Dashboard', icon: Home, visible: true, requiresWorkspace: false },
        { to: '/templates', label: 'Templates', icon: Shapes, visible: workspaceScoped, requiresWorkspace: true },
        {
            to: '/master-data',
            label: 'Master Data',
            icon: Database,
            visible: workspaceScoped && Boolean(user?.isAdmin || hasPermission('MANAGE_MASTER_DATA')),
            requiresWorkspace: true,
        },
        {
            to: '/xml-generation',
            label: 'XML Generation',
            icon: FileCode2,
            visible: workspaceScoped,
            requiresWorkspace: true,
        },
        {
            to: '/export-history',
            label: 'Export History',
            icon: History,
            visible: workspaceScoped,
            requiresWorkspace: true,
        },
        { to: '/settings', label: 'Settings', icon: Settings, visible: true, requiresWorkspace: false },
    ];

    const adminNavItems = [{ to: '/administration/users', label: 'User Management', icon: Users }];

    const showEmptyWorkspace = !hasAnyWorkspace && !onSafeRoute;

    return (
        <div className="flex min-h-screen bg-background">
            <aside className="flex w-64 flex-col border-r border-border bg-card">
                <div className="border-b border-border px-6 py-5">
                    <p className="text-lg font-semibold text-foreground">XMLGen</p>
                    <p className="text-xs text-muted-foreground">Template-driven XML</p>
                </div>
                <WorkspaceSwitcher />
                <nav className="flex-1 space-y-1 p-4">
                    {navItems
                        .filter((item) => item.visible)
                        .map(({ to, label, icon: Icon }) => (
                            <NavLink
                                key={to}
                                to={to}
                                className={({ isActive }) =>
                                    cn(
                                        'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                                        isActive
                                            ? 'bg-accent text-accent-foreground'
                                            : 'text-muted-foreground hover:bg-muted hover:text-foreground',
                                    )
                                }
                            >
                                <Icon className="h-4 w-4" aria-hidden />
                                {label}
                            </NavLink>
                        ))}
                    {user?.isAdmin ? (
                        <div className="pt-4">
                            <p className="px-3 pb-2 text-xs font-semibold uppercase tracking-wide text-muted-foreground">
                                Administration
                            </p>
                            {adminNavItems.map(({ to, label, icon: Icon }) => (
                                <NavLink
                                    key={to}
                                    to={to}
                                    className={({ isActive }) =>
                                        cn(
                                            'flex items-center gap-3 rounded-md px-3 py-2 text-sm font-medium transition-colors',
                                            isActive
                                                ? 'bg-accent text-accent-foreground'
                                                : 'text-muted-foreground hover:bg-muted hover:text-foreground',
                                        )
                                    }
                                >
                                    <Icon className="h-4 w-4" aria-hidden />
                                    {label}
                                </NavLink>
                            ))}
                        </div>
                    ) : null}
                </nav>
                <div className="border-t border-border p-4">
                    <div className="mb-3 space-y-0.5">
                        <p className="text-sm font-medium text-foreground">{user?.username ?? 'Unknown'}</p>
                        {user?.isAdmin ? <p className="text-xs text-muted-foreground">Administrator</p> : null}
                    </div>
                    <Button
                        variant="outline"
                        size="sm"
                        className="w-full"
                        onClick={() => requestLeave(() => void logout())}
                    >
                        <LogOut className="h-4 w-4" />
                        Logout
                    </Button>
                </div>
            </aside>
            <div className="flex min-h-screen flex-1 flex-col">
                <header className="shrink-0 border-b border-border bg-card px-6 py-4">
                    <h1 className="text-xl font-semibold text-foreground">{meta.title}</h1>
                    <p className="mt-1 text-sm text-muted-foreground">{meta.description}</p>
                </header>
                <main className="flex-1 overflow-auto p-6">
                    {showEmptyWorkspace ? <NoWorkspaceEmptyState /> : <Outlet />}
                </main>
            </div>
        </div>
    );
}

export function AppShell() {
    return (
        <PageMetaProvider>
            <AppShellContent />
        </PageMetaProvider>
    );
}
