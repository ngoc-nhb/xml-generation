ALTER TABLE templates
    ADD CONSTRAINT fk_templates_created_by
        FOREIGN KEY (created_by) REFERENCES users (id);
