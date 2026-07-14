import { Button } from '@/components/ui/button';
import { useAuth } from '@/providers/AuthProvider';
import { useWorkspace } from '@/features/workspace';

export function DashboardPage() {
    const { user } = useAuth();
    const { workspaces, createPersonalWorkspace, isCreatingPersonalWorkspace } = useWorkspace();
    const hasPersonal = workspaces.some(
        (workspace) => workspace.type === 'PERSONAL' && workspace.status === 'ACTIVE',
    );
    const hasAnyWorkspace = workspaces.some((workspace) => workspace.status === 'ACTIVE');

    return (
        <div className="flex min-h-[50vh] flex-col justify-center space-y-6">
            <div className="space-y-3">
                <p className="text-sm text-muted-foreground">Hello,</p>
                <h1 className="text-3xl font-semibold tracking-tight text-foreground">
                    {user?.username ?? 'User'}
                </h1>
                <p className="max-w-lg text-base text-muted-foreground">Welcome to XML Generator.</p>
            </div>
            {!hasAnyWorkspace ? (
                <div className="space-y-3 rounded-md border border-border p-4">
                    <p className="text-sm font-medium text-foreground">No Workspace Available</p>
                    <p className="text-sm text-muted-foreground">
                        Create a personal workspace to start working, or wait for an administrator to
                        assign a global workspace.
                    </p>
                    <Button
                        disabled={isCreatingPersonalWorkspace}
                        onClick={() => void createPersonalWorkspace()}
                    >
                        {isCreatingPersonalWorkspace ? 'Creating…' : 'Create Personal Workspace'}
                    </Button>
                </div>
            ) : !hasPersonal ? (
                <div className="space-y-3">
                    <p className="text-sm text-muted-foreground">
                        You can also create a personal workspace for private work.
                    </p>
                    <Button
                        variant="outline"
                        disabled={isCreatingPersonalWorkspace}
                        onClick={() => void createPersonalWorkspace()}
                    >
                        {isCreatingPersonalWorkspace ? 'Creating…' : 'Create Personal Workspace'}
                    </Button>
                </div>
            ) : null}
        </div>
    );
}
