import { Link } from 'react-router-dom';
import { ChevronDown, Plus } from 'lucide-react';

import { Button } from '@/components/ui/button';
import { Select } from '@/components/ui/select';
import { useWorkspace } from '@/features/workspace/providers/WorkspaceProvider';

export function WorkspaceSwitcher() {
    const { currentWorkspace, workspaces, switchWorkspace } = useWorkspace();

    if (!currentWorkspace) {
        return null;
    }

    const activeWorkspaces = workspaces.filter((workspace) => workspace.status === 'ACTIVE');
    const hasMultiple = activeWorkspaces.length > 1;

    return (
        <div className="border-b border-border px-4 py-4">
            <div className="mb-2 flex items-center justify-between gap-2">
                <p className="text-xs font-medium uppercase tracking-wide text-muted-foreground">Workspace</p>
                <Button asChild variant="ghost" size="icon" className="h-7 w-7 shrink-0">
                    <Link to="/workspaces" title="Manage Workspaces" aria-label="Manage Workspaces">
                        <Plus className="h-4 w-4" />
                    </Link>
                </Button>
            </div>
            {hasMultiple ? (
                <div className="relative">
                    <Select
                        className="appearance-none pr-8"
                        value={String(currentWorkspace.id)}
                        onChange={(event) => switchWorkspace(Number.parseInt(event.target.value, 10))}
                        aria-label="Switch workspace"
                    >
                        {activeWorkspaces.map((workspace) => (
                            <option key={workspace.id} value={workspace.id}>
                                {workspace.name}
                            </option>
                        ))}
                    </Select>
                    <ChevronDown
                        className="pointer-events-none absolute right-2 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground"
                        aria-hidden
                    />
                </div>
            ) : (
                <p className="text-sm font-medium text-foreground">{currentWorkspace.name}</p>
            )}
        </div>
    );
}
