import { NavLink, Outlet } from 'react-router-dom';
import { Database, FileCode2, History, LogOut, Settings, Shapes, Users } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { WorkspaceSwitcher } from '@/features/workspace';
import { usePageMetaContext, PageMetaProvider } from '@/providers/PageMetaProvider';
import { useAuth } from '@/providers/AuthProvider';
import { cn } from '@/utils/cn';

const navItems = [
    { to: '/templates', label: 'Templates', icon: Shapes, adminOnly: true },
    { to: '/master-data', label: 'Master Data', icon: Database, adminOnly: true },
    { to: '/xml-generation', label: 'XML Generation', icon: FileCode2, adminOnly: false },
    { to: '/export-history', label: 'Export History', icon: History, adminOnly: false },
    { to: '/settings', label: 'Settings', icon: Settings, adminOnly: false },
];

const adminNavItems = [{ to: '/administration/users', label: 'User Management', icon: Users }];

function AppShellContent() {
    const { user, logout } = useAuth();
    const { meta } = usePageMetaContext();

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
                        .filter((item) => !item.adminOnly || user?.isAdmin)
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
                    <Button variant="outline" size="sm" className="w-full" onClick={() => void logout()}>
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
                    <Outlet />
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
