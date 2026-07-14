import { Link } from 'react-router-dom';
import { ChevronDown, Plus } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { useWorkspace } from '@/features/workspace/providers/WorkspaceProvider';
import { useAuth } from '@/providers/AuthProvider';
import { useNavigationGuard } from '@/providers/NavigationGuardProvider';

export function WorkspaceSwitcher() {
    const { currentWorkspace, workspaces, switchWorkspace } = useWorkspace();
    const { user } = useAuth();
    const { requestLeave } = useNavigationGuard();

    const activeWorkspaces = workspaces.filter((workspace) => workspace.status === 'ACTIVE');
    const globalWorkspaces = activeWorkspaces.filter((workspace) => workspace.type === 'GLOBAL');
    const personalWorkspaces = activeWorkspaces.filter((workspace) => workspace.type === 'PERSONAL');
    const hasMultiple = activeWorkspaces.length > 1;
    const hasPersonal = personalWorkspaces.length > 0;

    return (
        <div className="border-b border-border px-4 py-4">
            <div className="mb-2 flex items-center justify-between gap-2">
                <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Workspace</p>
                {user?.isAdmin ? (
                    <Button asChild variant="ghost" size="icon" className="h-7 w-7 shrink-0">
                        <Link to="/workspaces" title="Manage Workspaces" aria-label="Manage Workspaces">
                            <Plus className="h-4 w-4" />
                        </Link>
                    </Button>
                ) : null}
            </div>
            {!currentWorkspace ? (
                <p className="text-sm text-muted-foreground">No workspace selected</p>
            ) : hasMultiple ? (
                <div className="relative">
                    <Select
                        className="appearance-none pr-8"
                        value={String(currentWorkspace.id)}
                        onChange={(event) => {
                            const nextId = Number.parseInt(event.target.value, 10);
                            requestLeave(() => switchWorkspace(nextId));
                        }}
                        aria-label="Switch workspace"
                    >
                        {globalWorkspaces.length > 0 ? (
                            <optgroup label="Global">
                                {globalWorkspaces.map((workspace) => (
                                    <option key={workspace.id} value={workspace.id}>
                                        {workspace.name}
                                    </option>
                                ))}
                            </optgroup>
                        ) : null}
                        {hasPersonal ? (
                            <optgroup label="Personal">
                                {personalWorkspaces.map((workspace) => (
                                    <option key={workspace.id} value={workspace.id}>
                                        {workspace.name}
                                    </option>
                                ))}
                            </optgroup>
                        ) : (
                            <optgroup label="Personal">
                                <option disabled value="">
                                    No Personal Workspace
                                </option>
                            </optgroup>
                        )}
                    </Select>
                    <ChevronDown
                        className="pointer-events-none absolute right-2 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                        aria-hidden
                    />
                </div>
            ) : (
                <div className="space-y-1">
                    <p className="text-xs text-muted-foreground">
                        {currentWorkspace.type === 'PERSONAL' ? 'Personal' : 'Global'}
                    </p>
                    <p className="text-sm font-medium text-foreground">{currentWorkspace.name}</p>
                    {!hasPersonal ? (
                        <p className="text-xs text-muted-foreground">No Personal Workspace</p>
                    ) : null}
                </div>
            )}
        </div>
    );
}
