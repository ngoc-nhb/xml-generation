import { useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { Plus } from 'lucide-react';

import { EmptyPlaceholder } from '@/components/empty-placeholder';
import { FullPageError } from '@/components/full-page-error';
import { LoadingSpinner } from '@/components/loading-spinner';
import { Button } from '@/components/ui/button';
import { CreateUserDialog, type CreateUserSubmitValues } from '@/features/user-management/components/CreateUserDialog';
import { EditUserDialog, type EditUserSubmitValues } from '@/features/user-management/components/EditUserDialog';
import { ResetPasswordDialog } from '@/features/user-management/components/ResetPasswordDialog';
import { UserListTable } from '@/features/user-management/components/UserListTable';
import {
    useCreateUser,
    useResetUserPassword,
    useUpdateUser,
    useUserList,
} from '@/features/user-management/hooks/useUsers';
import { assignUserWorkspaces } from '@/features/user-management/api/users.api';
import type { UserListItem } from '@/features/user-management/types/user-management.types';
import { MasterDataPagination } from '@/features/master-data/components/MasterDataPagination';
import { ApiClientError } from '@/types/api/common';
import { getPrimaryErrorMessage } from '@/utils/errorMessages';
import { toast } from '@/providers/ToastProvider';

export function UserListPage() {
    const [searchParams, setSearchParams] = useSearchParams();
    const [createOpen, setCreateOpen] = useState(false);
    const [editTarget, setEditTarget] = useState<UserListItem | null>(null);
    const [resetTarget, setResetTarget] = useState<UserListItem | null>(null);

    const page = Number(searchParams.get('page') ?? '1');

    const listParams = useMemo(
        () => ({ page: Number.isNaN(page) ? 1 : page, pageSize: 20 }),
        [page],
    );

    const { data, isLoading, isError, error, refetch } = useUserList(listParams);
    const createMutation = useCreateUser();
    const updateMutation = useUpdateUser(editTarget?.id ?? 0);
    const resetMutation = useResetUserPassword(resetTarget?.id ?? 0);

    function handlePageChange(nextPage: number) {
        const next = new URLSearchParams(searchParams);
        next.set('page', String(nextPage));
        setSearchParams(next);
    }

    async function handleCreate(values: CreateUserSubmitValues) {
        try {
            const { memberships, ...request } = values;
            const created = await createMutation.mutateAsync(request);
            await assignUserWorkspaces(created.id, memberships);
            toast.success('User created successfully.');
            setCreateOpen(false);
        } catch (createError) {
            toast.error(createError instanceof ApiClientError ? getPrimaryErrorMessage(createError.errors) : 'Create failed');
        }
    }

    async function handleEdit(values: EditUserSubmitValues) {
        if (!editTarget) return;
        try {
            const { memberships, ...request } = values;
            await updateMutation.mutateAsync(request);
            await assignUserWorkspaces(editTarget.id, memberships);
            toast.success('User updated successfully.');
            setEditTarget(null);
        } catch (updateError) {
            toast.error(updateError instanceof ApiClientError ? getPrimaryErrorMessage(updateError.errors) : 'Update failed');
        }
    }

    async function handleResetPassword(values: { password: string; confirmPassword: string }) {
        if (!resetTarget) return;
        try {
            await resetMutation.mutateAsync(values);
            toast.success('Password reset successfully.');
            setResetTarget(null);
        } catch (resetError) {
            toast.error(resetError instanceof ApiClientError ? getPrimaryErrorMessage(resetError.errors) : 'Reset failed');
        }
    }

    return (
        <div className="space-y-6">
            <div className="flex items-center justify-end">
                <Button onClick={() => setCreateOpen(true)}>
                    <Plus className="h-4 w-4" />
                    Create User
                </Button>
            </div>
            {isLoading ? <LoadingSpinner label="Loading users…" /> : null}
            {isError ? (
                <FullPageError
                    title="Unable to load users"
                    description={error instanceof ApiClientError ? getPrimaryErrorMessage(error.errors) : 'Request failed'}
                    onRetry={() => void refetch()}
                />
            ) : null}
            {!isLoading && !isError && data ? (
                data.items.length === 0 ? (
                    <EmptyPlaceholder
                        title="No users found"
                        description="Create a user account to grant access to the system."
                        action={
                            <Button onClick={() => setCreateOpen(true)}>
                                <Plus className="h-4 w-4" />
                                Create User
                            </Button>
                        }
                    />
                ) : (
                    <div className="space-y-4">
                        <UserListTable
                            items={data.items}
                            onEdit={setEditTarget}
                            onResetPassword={setResetTarget}
                        />
                        <MasterDataPagination meta={data.meta} onPageChange={handlePageChange} />
                    </div>
                )
            ) : null}
            <CreateUserDialog
                open={createOpen}
                loading={createMutation.isPending}
                onSubmit={(values) => void handleCreate(values)}
                onClose={() => setCreateOpen(false)}
            />
            <EditUserDialog
                open={editTarget !== null}
                user={editTarget}
                loading={updateMutation.isPending}
                onSubmit={(values) => void handleEdit(values)}
                onClose={() => setEditTarget(null)}
            />
            <ResetPasswordDialog
                open={resetTarget !== null}
                user={resetTarget}
                loading={resetMutation.isPending}
                onSubmit={(values) => void handleResetPassword(values)}
                onClose={() => setResetTarget(null)}
            />
        </div>
    );
}
