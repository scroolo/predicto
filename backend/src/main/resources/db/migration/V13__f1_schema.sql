DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name='f1_meetings') THEN
        CREATE TABLE f1_meetings (
            id UUID PRIMARY KEY,
            meeting_key INTEGER NOT NULL UNIQUE,
            meeting_name VARCHAR(255) NOT NULL,
            meeting_official_name VARCHAR(500),
            country_name VARCHAR(255),
            country_flag_url VARCHAR(500),
            circuit_short_name VARCHAR(255),
            circuit_image_url VARCHAR(500),
            location VARCHAR(255),
            date_start TIMESTAMPTZ NOT NULL,
            date_end TIMESTAMPTZ,
            year INTEGER NOT NULL,
            is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
        );
        CREATE INDEX idx_f1_meetings_year ON f1_meetings(year);
        CREATE INDEX idx_f1_meetings_date_start ON f1_meetings(date_start);

        CREATE TABLE f1_sessions (
            id UUID PRIMARY KEY,
            session_key INTEGER NOT NULL UNIQUE,
            meeting_id UUID NOT NULL REFERENCES f1_meetings(id),
            session_name VARCHAR(100) NOT NULL,
            session_type VARCHAR(50) NOT NULL,
            date_start TIMESTAMPTZ NOT NULL,
            date_end TIMESTAMPTZ,
            is_cancelled BOOLEAN NOT NULL DEFAULT FALSE,
            status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
        );
        CREATE INDEX idx_f1_sessions_meeting_id ON f1_sessions(meeting_id);
        CREATE INDEX idx_f1_sessions_date_start ON f1_sessions(date_start);
        CREATE INDEX idx_f1_sessions_status ON f1_sessions(status);

        CREATE TABLE f1_drivers (
            id UUID PRIMARY KEY,
            driver_number INTEGER NOT NULL,
            session_key INTEGER NOT NULL,
            full_name VARCHAR(255) NOT NULL,
            name_acronym VARCHAR(10),
            headshot_url VARCHAR(500),
            team_name VARCHAR(255),
            team_colour VARCHAR(10),
            UNIQUE(driver_number, session_key)
        );
        CREATE INDEX idx_f1_drivers_session_key ON f1_drivers(session_key);

        CREATE TABLE f1_predictions (
            id UUID PRIMARY KEY,
            user_id UUID NOT NULL REFERENCES users(id),
            session_id UUID NOT NULL REFERENCES f1_sessions(id),
            predicted_winner_number INTEGER,
            predicted_pole_number INTEGER,
            predicted_p2_number INTEGER,
            predicted_p3_number INTEGER,
            predicted_driver_of_day_number INTEGER,
            actual_winner_number INTEGER,
            actual_pole_number INTEGER,
            actual_p2_number INTEGER,
            actual_p3_number INTEGER,
            actual_driver_of_day_number INTEGER,
            points_awarded INTEGER NOT NULL DEFAULT 0,
            status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
            locked BOOLEAN NOT NULL DEFAULT FALSE,
            created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
            updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
            UNIQUE(user_id, session_id)
        );
        CREATE INDEX idx_f1_predictions_user_id ON f1_predictions(user_id);
        CREATE INDEX idx_f1_predictions_session_id ON f1_predictions(session_id);
    END IF;
END $$;
