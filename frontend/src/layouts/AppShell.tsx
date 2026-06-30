import { NavLink, Outlet } from 'react-router-dom';
import {
    Database,
    FileCode2,
    History,
    LayoutDashboard,
    LogOut,
    Settings,
    Shapes,
} from 'lucide-react';

import { Button } from '@/components/ui/button';
import { useAuth } from '@/providers/AuthProvider';
import { cn } from '@/utils/cn';

const navItems = [
    { to: '/dashboard', label: 'Dashboard', icon: LayoutDashboard, adminOnly: false },
    { to: '/templates', label: 'Templates', icon: Shapes, adminOnly: true },
    { to: '/master-data', label: 'Master Data', icon: Database, adminOnly: true },
    { to: '/xml-generation', label: 'XML Generation', icon: FileCode2, adminOnly: false },
    { to: '/export-history', label: 'Export History', icon: History, adminOnly: false },
    { to: '/settings', label: 'Settings', icon: Settings, adminOnly: false },
];

export function AppShell() {
    const { user, logout } = useAuth();

    return (
        <div className="flex min-h-screen bg-background">
            <aside className="flex w-64 flex-col border-r border-border bg-card">
                <div className="border-b border-border px-6 py-5">
                    <p className="text-lg font-semibold text-foreground">XMLGen</p>
                    <p className="text-xs text-muted-foreground">Template-driven XML</p>
                </div>
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
                </nav>
            </aside>
            <div className="flex min-h-screen flex-1 flex-col">
                <header className="flex h-16 items-center justify-between border-b border-border bg-card px-6">
                    <div>
                        <p className="text-sm text-muted-foreground">Signed in as</p>
                        <p className="font-medium text-foreground">{user?.username ?? 'Unknown'}</p>
                    </div>
                    <Button variant="outline" size="sm" onClick={() => void logout()}>
                        <LogOut className="h-4 w-4" />
                        Logout
                    </Button>
                </header>
                <main className="flex-1 overflow-auto p-6">
                    <Outlet />
                </main>
            </div>
        </div>
    );
}
