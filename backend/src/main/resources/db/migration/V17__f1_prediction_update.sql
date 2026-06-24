DO $$
BEGIN
    -- Add new prediction fields to f1_predictions
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_predictions' AND column_name='predicted_pole_driver_number'
    ) THEN
        ALTER TABLE f1_predictions ADD COLUMN predicted_pole_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_predictions' AND column_name='predicted_p1_driver_number'
    ) THEN
        ALTER TABLE f1_predictions ADD COLUMN predicted_p1_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_predictions' AND column_name='predicted_p2_driver_number'
    ) THEN
        ALTER TABLE f1_predictions ADD COLUMN predicted_p2_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_predictions' AND column_name='predicted_p3_driver_number'
    ) THEN
        ALTER TABLE f1_predictions ADD COLUMN predicted_p3_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_predictions' AND column_name='points_earned'
    ) THEN
        ALTER TABLE f1_predictions ADD COLUMN points_earned INTEGER DEFAULT 0;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_predictions' AND column_name='settled_at'
    ) THEN
        ALTER TABLE f1_predictions ADD COLUMN settled_at TIMESTAMP;
    END IF;

    -- Add result fields to f1_sessions
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_sessions' AND column_name='result_pole_driver_number'
    ) THEN
        ALTER TABLE f1_sessions ADD COLUMN result_pole_driver_number INTEGER;
    END IF;
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns
        WHERE table_name='f1_sessions' AND column_name='predictions_locked'
    ) THEN
        ALTER TABLE f1_sessions ADD COLUMN predictions_locked BOOLEAN DEFAULT FALSE;
    END IF;

    -- F1 leaderboard view
    CREATE OR REPLACE VIEW f1_leaderboard AS
    SELECT
      u.id AS user_id,
      u.username,
      u.avatar_url,
      COALESCE(SUM(p.points_earned), 0) AS total_points,
      COUNT(p.id) AS total_predictions,
      COUNT(CASE WHEN p.points_earned > 0 THEN 1 END) AS correct_predictions
    FROM users u
    LEFT JOIN f1_predictions p ON p.user_id = u.id AND p.status = 'SETTLED'
    GROUP BY u.id, u.username, u.avatar_url
    ORDER BY total_points DESC;
END $$;
