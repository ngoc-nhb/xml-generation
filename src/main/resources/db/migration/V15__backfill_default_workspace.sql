INSERT INTO workspaces (id, code, name, description, status, created_by, created_at, updated_at)
SELECT
    1,
    'DEFAULT',
    'Default Workspace',
    NULL,
    'ACTIVE',
    (SELECT id FROM users WHERE is_active = true ORDER BY id LIMIT 1),
    NOW(),
    NOW()
WHERE NOT EXISTS (SELECT 1 FROM workspaces WHERE code = 'DEFAULT');

SELECT setval(
    pg_get_serial_sequence('workspaces', 'id'),
    COALESCE((SELECT MAX(id) FROM workspaces), 1));

UPDATE templates
SET workspace_id = 1
WHERE workspace_id IS NULL;

UPDATE master_data_types
SET workspace_id = 1
WHERE workspace_id IS NULL;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'saved_inputs'
    ) THEN
        UPDATE saved_inputs
        SET workspace_id = 1
        WHERE workspace_id IS NULL;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'export_histories'
    ) THEN
        UPDATE export_histories
        SET workspace_id = 1
        WHERE workspace_id IS NULL;
    END IF;
END $$;

INSERT INTO workspace_members (workspace_id, user_id, role, joined_at, created_at, updated_at)
SELECT
    1,
    u.id,
    CASE WHEN u.is_admin THEN 'WORKSPACE_ADMIN' ELSE 'WORKSPACE_USER' END,
    NOW(),
    NOW(),
    NOW()
FROM users u
WHERE u.deleted_at IS NULL
  AND u.is_active = true
  AND NOT EXISTS (
      SELECT 1
      FROM workspace_members wm
      WHERE wm.workspace_id = 1
        AND wm.user_id = u.id
  );
