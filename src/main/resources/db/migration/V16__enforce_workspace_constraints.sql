ALTER TABLE templates
    ALTER COLUMN workspace_id SET DEFAULT 1;

ALTER TABLE templates
    ALTER COLUMN workspace_id SET NOT NULL;

ALTER TABLE templates
    ADD CONSTRAINT fk_templates_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id);

ALTER TABLE templates
    DROP CONSTRAINT uk_templates_code;

ALTER TABLE templates
    ADD CONSTRAINT uk_templates_workspace_code UNIQUE (workspace_id, code);

CREATE INDEX idx_templates_workspace_id ON templates (workspace_id);

ALTER TABLE master_data_types
    ALTER COLUMN workspace_id SET DEFAULT 1;

ALTER TABLE master_data_types
    ALTER COLUMN workspace_id SET NOT NULL;

ALTER TABLE master_data_types
    ADD CONSTRAINT fk_master_data_types_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id);

ALTER TABLE master_data_types
    DROP CONSTRAINT uk_master_data_types_code;

ALTER TABLE master_data_types
    ADD CONSTRAINT uk_master_data_types_workspace_code UNIQUE (workspace_id, code);

CREATE INDEX idx_master_data_types_workspace_id ON master_data_types (workspace_id);

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'saved_inputs'
    ) THEN
        ALTER TABLE saved_inputs
            ALTER COLUMN workspace_id SET DEFAULT 1;

        ALTER TABLE saved_inputs
            ALTER COLUMN workspace_id SET NOT NULL;

        ALTER TABLE saved_inputs
            ADD CONSTRAINT fk_saved_inputs_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id);

        IF EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'saved_inputs_user_id_template_id_key'
        ) THEN
            ALTER TABLE saved_inputs
                DROP CONSTRAINT saved_inputs_user_id_template_id_key;
        END IF;

        IF EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'uk_saved_inputs_user_template'
        ) THEN
            ALTER TABLE saved_inputs
                DROP CONSTRAINT uk_saved_inputs_user_template;
        END IF;

        IF NOT EXISTS (
            SELECT 1
            FROM pg_constraint
            WHERE conname = 'uk_saved_inputs_workspace_user_template'
        ) THEN
            ALTER TABLE saved_inputs
                ADD CONSTRAINT uk_saved_inputs_workspace_user_template
                    UNIQUE (workspace_id, user_id, template_id);
        END IF;

        CREATE INDEX IF NOT EXISTS idx_saved_inputs_workspace_id ON saved_inputs (workspace_id);
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'export_histories'
    ) THEN
        ALTER TABLE export_histories
            ALTER COLUMN workspace_id SET DEFAULT 1;

        ALTER TABLE export_histories
            ALTER COLUMN workspace_id SET NOT NULL;

        ALTER TABLE export_histories
            ADD CONSTRAINT fk_export_histories_workspace FOREIGN KEY (workspace_id) REFERENCES workspaces (id);

        CREATE INDEX IF NOT EXISTS idx_export_histories_workspace_id ON export_histories (workspace_id);
    END IF;
END $$;
