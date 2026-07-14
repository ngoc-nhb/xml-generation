-- Phase 9.2: move permissions onto workspace memberships (extensible JSONB codes)
-- and ensure every user has a personal workspace.

ALTER TABLE workspace_members
    ADD COLUMN permissions JSONB NOT NULL DEFAULT '[]'::jsonb;

-- Preserve previous effective access:
--   * global user flags → grant matching codes on every membership
--   * WORKSPACE_ADMIN previously bypassed guards → grant both codes
UPDATE workspace_members wm
SET permissions = (
    SELECT COALESCE(jsonb_agg(DISTINCT code), '[]'::jsonb)
    FROM (
        SELECT 'IMPORT_TEMPLATE'::text AS code
        WHERE wm.role = 'WORKSPACE_ADMIN'
           OR EXISTS (
                SELECT 1 FROM users u
                WHERE u.id = wm.user_id AND u.can_import_template = true
           )
        UNION ALL
        SELECT 'MANAGE_MASTER_DATA'::text AS code
        WHERE wm.role = 'WORKSPACE_ADMIN'
           OR EXISTS (
                SELECT 1 FROM users u
                WHERE u.id = wm.user_id AND u.can_import_master_data = true
           )
    ) granted
);

-- Personal workspace for every active user with zero memberships
INSERT INTO workspaces (code, name, status, created_by, created_at, updated_at)
SELECT
    'PERSONAL_' || u.id,
    u.username || ' Personal',
    'ACTIVE',
    u.id,
    NOW(),
    NOW()
FROM users u
WHERE u.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM workspace_members wm WHERE wm.user_id = u.id
  )
  AND NOT EXISTS (
      SELECT 1 FROM workspaces w
      WHERE w.code = 'PERSONAL_' || u.id AND w.deleted_at IS NULL
  );

INSERT INTO workspace_members (workspace_id, user_id, role, permissions, joined_at, created_at, updated_at)
SELECT
    w.id,
    u.id,
    'WORKSPACE_ADMIN',
    '["IMPORT_TEMPLATE","MANAGE_MASTER_DATA"]'::jsonb,
    NOW(),
    NOW(),
    NOW()
FROM users u
JOIN workspaces w
  ON w.code = 'PERSONAL_' || u.id
 AND w.deleted_at IS NULL
WHERE u.deleted_at IS NULL
  AND NOT EXISTS (
      SELECT 1 FROM workspace_members wm WHERE wm.user_id = u.id
  );

ALTER TABLE users
    DROP COLUMN can_import_template,
    DROP COLUMN can_import_master_data;
