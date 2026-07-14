import { useWorkspaceList } from '@/features/workspace';
import {
    WORKSPACE_PERMISSION_OPTIONS,
    type WorkspacePermissionCode,
} from '@/features/workspace/types/workspace.types';
import type { WorkspaceMembershipAssignment } from '@/features/user-management/types/user-management.types';

interface WorkspaceAssignmentFieldProps {
    value: WorkspaceMembershipAssignment[];
    onChange: (memberships: WorkspaceMembershipAssignment[]) => void;
    disabled?: boolean;
}

/** Global workspaces only — with per-membership permission checkboxes. */
export function WorkspaceAssignmentField({
    value,
    onChange,
    disabled = false,
}: WorkspaceAssignmentFieldProps) {
    const { data: workspaces, isLoading } = useWorkspaceList();

    const globalWorkspaces = (workspaces ?? []).filter((workspace) => workspace.type === 'GLOBAL');

    function isSelected(workspaceId: number): boolean {
        return value.some((item) => item.workspaceId === workspaceId);
    }

    function permissionsFor(workspaceId: number): WorkspacePermissionCode[] {
        return value.find((item) => item.workspaceId === workspaceId)?.permissions ?? [];
    }

    function toggleWorkspace(workspaceId: number, checked: boolean) {
        if (checked) {
            onChange([...value, { workspaceId, permissions: [] }]);
            return;
        }
        onChange(value.filter((item) => item.workspaceId !== workspaceId));
    }

    function togglePermission(
        workspaceId: number,
        code: WorkspacePermissionCode,
        checked: boolean,
    ) {
        onChange(
            value.map((item) => {
                if (item.workspaceId !== workspaceId) {
                    return item;
                }
                const permissions = checked
                    ? item.permissions.includes(code)
                        ? item.permissions
                        : [...item.permissions, code]
                    : item.permissions.filter((permission) => permission !== code);
                return { ...item, permissions };
            }),
        );
    }

    if (isLoading) {
        return <p className="text-sm text-muted-foreground">Loading workspaces…</p>;
    }

    if (globalWorkspaces.length === 0) {
        return <p className="text-sm text-muted-foreground">No global workspaces available.</p>;
    }

    return (
        <div className="max-h-64 space-y-3 overflow-y-auto rounded-md border border-border p-3">
            {globalWorkspaces.map((workspace) => {
                const selected = isSelected(workspace.id);
                const selectedPermissions = permissionsFor(workspace.id);
                return (
                    <div key={workspace.id} className="space-y-2 border-b border-border pb-3 last:border-b-0 last:pb-0">
                        <label className="flex items-center gap-2 text-sm font-medium">
                            <input
                                type="checkbox"
                                disabled={disabled}
                                checked={selected}
                                onChange={(event) => toggleWorkspace(workspace.id, event.target.checked)}
                            />
                            <span>
                                {workspace.code} — {workspace.name}
                            </span>
                        </label>
                        {selected ? (
                            <div className="ml-6 space-y-1">
                                <p className="text-xs text-muted-foreground">Permissions</p>
                                {WORKSPACE_PERMISSION_OPTIONS.map((option) => (
                                    <label key={option.code} className="flex items-center gap-2 text-sm">
                                        <input
                                            type="checkbox"
                                            disabled={disabled}
                                            checked={selectedPermissions.includes(option.code)}
                                            onChange={(event) =>
                                                togglePermission(workspace.id, option.code, event.target.checked)
                                            }
                                        />
                                        {option.label}
                                    </label>
                                ))}
                            </div>
                        ) : null}
                    </div>
                );
            })}
        </div>
    );
}
