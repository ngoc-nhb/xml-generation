import { Button } from '@/components/ui/button';
import { useWorkspace } from '@/features/workspace/providers/WorkspaceProvider';

export function WorkspaceRequiredPage() {
    const { refreshWorkspaces } = useWorkspace();

    return (
        <div className="mx-auto flex max-w-lg flex-col items-center gap-4 py-16 text-center">
            <h1 className="text-2xl font-semibold text-foreground">Workspace selection required</h1>
            <p className="text-sm text-muted-foreground">
                Choose a workspace before accessing application data. If the problem persists, reload the workspace
                list and try again.
            </p>
            <Button type="button" onClick={() => void refreshWorkspaces()}>
                Reload workspaces
            </Button>
        </div>
    );
}
