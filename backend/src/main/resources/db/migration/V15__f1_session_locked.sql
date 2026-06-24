DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_sessions' AND column_name='locked'
    ) THEN
        ALTER TABLE f1_sessions ADD COLUMN locked BOOLEAN NOT NULL DEFAULT FALSE;
    END IF;
END $$;
