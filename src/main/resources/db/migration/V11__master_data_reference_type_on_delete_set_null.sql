ALTER TABLE master_data_fields
    DROP CONSTRAINT IF EXISTS fk_master_data_fields_reference_type;

ALTER TABLE master_data_fields
    ADD CONSTRAINT fk_master_data_fields_reference_type
        FOREIGN KEY (master_data_reference_type_id) REFERENCES master_data_types (id)
        ON DELETE SET NULL;
