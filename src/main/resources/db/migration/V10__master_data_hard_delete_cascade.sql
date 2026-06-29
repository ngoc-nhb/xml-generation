ALTER TABLE master_data_fields
    DROP CONSTRAINT IF EXISTS fk_master_data_fields_type;

ALTER TABLE master_data_fields
    ADD CONSTRAINT fk_master_data_fields_type
        FOREIGN KEY (master_data_type_id) REFERENCES master_data_types (id)
        ON DELETE CASCADE;

ALTER TABLE master_data_records
    DROP CONSTRAINT IF EXISTS fk_master_data_records_type;

ALTER TABLE master_data_records
    ADD CONSTRAINT fk_master_data_records_type
        FOREIGN KEY (master_data_type_id) REFERENCES master_data_types (id)
        ON DELETE CASCADE;
