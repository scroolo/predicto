DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='users' AND column_name='preferred_game'
    ) THEN
        ALTER TABLE users ADD COLUMN preferred_game VARCHAR(10) NOT NULL DEFAULT 'LOL';
    END IF;
END $$;
