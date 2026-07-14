ALTER TABLE users
    ADD COLUMN can_import_template BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN can_import_master_data BOOLEAN NOT NULL DEFAULT false;

-- Workspace membership becomes an access requirement: make sure every existing
-- workspace's creator is a member (as WORKSPACE_ADMIN) so nobody is locked out
-- of a workspace they created before enforcement existed.
INSERT INTO workspace_members (workspace_id, user_id, role, joined_at, created_at, updated_at)
SELECT w.id, w.created_by, 'WORKSPACE_ADMIN', NOW(), NOW(), NOW()
FROM workspaces w
WHERE w.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1
      FROM workspace_members wm
      WHERE wm.workspace_id = w.id
        AND wm.user_id = w.created_by
  );
