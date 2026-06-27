ALTER TABLE master_data_fields
    ADD COLUMN description                    TEXT,
    ADD COLUMN default_value                  TEXT,
    ADD COLUMN "unique"                       BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN searchable                     BOOLEAN NOT NULL DEFAULT false,
    ADD COLUMN master_data_reference_type_id  BIGINT,
    ADD CONSTRAINT fk_master_data_fields_reference_type
        FOREIGN KEY (master_data_reference_type_id) REFERENCES master_data_types (id);
