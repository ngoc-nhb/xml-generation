-- Phase 9.3: explicit workspace types (GLOBAL | PERSONAL)

ALTER TABLE workspaces
    ADD COLUMN workspace_type VARCHAR(20) NOT NULL DEFAULT 'GLOBAL';

-- Existing PERSONAL_{userId} workspaces from Phase 9.2 become PERSONAL
UPDATE workspaces
SET workspace_type = 'PERSONAL'
WHERE code LIKE 'PERSONAL_%'
  AND deleted_at IS NULL;

CREATE INDEX idx_workspaces_type ON workspaces (workspace_type);

-- At most one active personal workspace per owner
CREATE UNIQUE INDEX uk_workspaces_one_personal_per_user
    ON workspaces (created_by)
    WHERE workspace_type = 'PERSONAL' AND deleted_at IS NULL;
