DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_sessions' AND column_name='result_p1_driver_number'
    ) THEN
        ALTER TABLE f1_sessions ADD COLUMN result_p1_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_sessions' AND column_name='result_p2_driver_number'
    ) THEN
        ALTER TABLE f1_sessions ADD COLUMN result_p2_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_sessions' AND column_name='result_p3_driver_number'
    ) THEN
        ALTER TABLE f1_sessions ADD COLUMN result_p3_driver_number INTEGER;
    END IF;
END $$;
