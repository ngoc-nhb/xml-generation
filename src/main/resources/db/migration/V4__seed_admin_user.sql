INSERT INTO users (username, password_hash, is_admin, is_active, created_at, updated_at)
VALUES (
    'admin',
    '$2a$10$gbMcWTY3rvH.98tIAV1HE.bdJ4Rj7Mp6A/NfbHf15qSG0VJ.m1HQC',
    true,
    true,
    NOW(),
    NOW()
);
