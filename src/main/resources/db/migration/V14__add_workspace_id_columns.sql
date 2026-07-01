ALTER TABLE templates
    ADD COLUMN workspace_id BIGINT;

ALTER TABLE master_data_types
    ADD COLUMN workspace_id BIGINT;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'saved_inputs'
    ) THEN
        ALTER TABLE saved_inputs
            ADD COLUMN workspace_id BIGINT;
    END IF;

    IF EXISTS (
        SELECT 1
        FROM information_schema.tables
        WHERE table_schema = 'public'
          AND table_name = 'export_histories'
    ) THEN
        ALTER TABLE export_histories
            ADD COLUMN workspace_id BIGINT;
    END IF;
END $$;
