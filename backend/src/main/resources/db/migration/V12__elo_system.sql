DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='wallets' AND column_name='lol_elo'
    ) THEN
        ALTER TABLE wallets
            ADD COLUMN lol_elo INTEGER NOT NULL DEFAULT 100,
            ADD COLUMN cs2_elo INTEGER NOT NULL DEFAULT 100;
        UPDATE wallets SET lol_elo = 100, cs2_elo = 100;
    END IF;
END $$;
