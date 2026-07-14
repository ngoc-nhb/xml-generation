import { useEffect, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';

import { Card, CardContent, CardHeader, CardTitle } from '@/components/ui/card';
import { Button } from '@/components/ui/button';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { WorkspacePageHeader } from '@/features/workspace/components/WorkspacePageHeader';
import {
    useUpdateWorkspaceMemberPermissions,
    useWorkspaceDetail,
    useWorkspaceMembers,
} from '@/features/workspace/hooks/useWorkspaces';
import {
    WORKSPACE_PERMISSION_OPTIONS,
    type WorkspaceMember,
    type WorkspacePermissionCode,
} from '@/features/workspace/types/workspace.types';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function WorkspacePermissionsPage() {
    const navigate = useNavigate();
    const { id } = useParams();
    const workspaceId = Number(id);
    const detailQuery = useWorkspaceDetail(Number.isNaN(workspaceId) ? undefined : workspaceId);
    const membersQuery = useWorkspaceMembers(Number.isNaN(workspaceId) ? undefined : workspaceId);
    const updateMutation = useUpdateWorkspaceMemberPermissions(workspaceId);
    const [drafts, setDrafts] = useState<Record<number, WorkspacePermissionCode[]>>({});
    const [savingUserId, setSavingUserId] = useState<number | null>(null);

    useEffect(() => {
        if (!membersQuery.data) {
            return;
        }
        const next: Record<number, WorkspacePermissionCode[]> = {};
        for (const member of membersQuery.data) {
            next[member.userId] = [...member.permissions];
        }
        setDrafts(next);
    }, [membersQuery.data]);

    function togglePermission(member: WorkspaceMember, code: WorkspacePermissionCode, checked: boolean) {
        setDrafts((current) => {
            const existing = current[member.userId] ?? [];
            const next = checked
                ? existing.includes(code)
                    ? existing
                    : [...existing, code]
                : existing.filter((item) => item !== code);
            return { ...current, [member.userId]: next };
        });
    }

    async function handleSave(member: WorkspaceMember) {
        setSavingUserId(member.userId);
        try {
            await updateMutation.mutateAsync({
                userId: member.userId,
                permissions: drafts[member.userId] ?? [],
            });
            toast.success(`Permissions updated for ${member.username}`);
        } catch (saveError) {
            const message =
                saveError instanceof ApiClientError
                    ? getPrimaryErrorMessage(saveError.errors)
                    : 'Failed to update permissions';
            toast.error(message);
        } finally {
            setSavingUserId(null);
        }
    }

    if (Number.isNaN(workspaceId)) {
        return <FullPageError title="Invalid workspace" description="The workspace id in the URL is not valid." />;
    }

    const loading = detailQuery.isLoading || membersQuery.isLoading;
    const error = detailQuery.error ?? membersQuery.error;
    const isError = detailQuery.isError || membersQuery.isError;

    return (
        <div className="space-y-6">
            <WorkspacePageHeader
                backTo={`/workspaces/${workspaceId}/edit`}
                backLabel="Back to workspace settings"
            />
            {loading ? <LoadingSpinner label="Loading permissions…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load permissions"
                    description={
                        error instanceof ApiClientError
                            ? getPrimaryErrorMessage(error.errors)
                            : 'Request failed'
                    }
                    onRetry={() => {
                        void detailQuery.refetch();
                        void membersQuery.refetch();
                    }}
                />
            ) : null}
            {!loading && !isError && detailQuery.data && membersQuery.data ? (
                <Card>
                    <CardHeader>
                        <CardTitle>{detailQuery.data.name} — Permissions</CardTitle>
                        <p className="text-sm text-muted-foreground">
                            Configure capabilities for each workspace member. Changes apply immediately.
                        </p>
                    </CardHeader>
                    <CardContent className="space-y-6">
                        {membersQuery.data.length === 0 ? (
                            <p className="text-sm text-muted-foreground">No members in this workspace.</p>
                        ) : (
                            membersQuery.data.map((member) => {
                                const selected = drafts[member.userId] ?? [];
                                return (
                                    <div
                                        key={member.userId}
                                        className="space-y-3 rounded-md border border-border p-4"
                                    >
                                        <div>
                                            <p className="text-sm font-medium text-foreground">
                                                {member.username}
                                            </p>
                                            <p className="text-xs text-muted-foreground">{member.role}</p>
                                        </div>
                                        <div className="space-y-2">
                                            {WORKSPACE_PERMISSION_OPTIONS.map((option) => (
                                                <label
                                                    key={option.code}
                                                    className="flex items-center gap-2 text-sm"
                                                >
                                                    <input
                                                        type="checkbox"
                                                        checked={selected.includes(option.code)}
                                                        onChange={(event) =>
                                                            togglePermission(
                                                                member,
                                                                option.code,
                                                                event.target.checked,
                                                            )
                                                        }
                                                    />
                                                    {option.label}
                                                </label>
                                            ))}
                                        </div>
                                        <Button
                                            size="sm"
                                            disabled={savingUserId === member.userId}
                                            onClick={() => void handleSave(member)}
                                        >
                                            {savingUserId === member.userId ? 'Saving…' : 'Save'}
                                        </Button>
                                    </div>
                                );
                            })
                        )}
                        <div className="flex justify-end">
                            <Button variant="outline" onClick={() => navigate(`/workspaces/${workspaceId}/edit`)}>
                                Done
                            </Button>
                        </div>
                    </CardContent>
                </Card>
            ) : null}
        </div>
    );
}
