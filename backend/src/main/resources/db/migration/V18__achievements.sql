-- Achievement definitions
CREATE TABLE achievements (
    id VARCHAR(64) PRIMARY KEY,
    name VARCHAR(128) NOT NULL,
    description TEXT NOT NULL,
    category VARCHAR(32) NOT NULL,
    rarity VARCHAR(16) NOT NULL,
    icon VARCHAR(128),
    points INT DEFAULT 0,
    hidden BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT NOW()
);

-- User achievements (unlocked)
CREATE TABLE user_achievements (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES users(id),
    achievement_id VARCHAR(64) NOT NULL REFERENCES achievements(id),
    unlocked_at TIMESTAMP DEFAULT NOW(),
    UNIQUE(user_id, achievement_id)
);

-- Seed initial achievements
INSERT INTO achievements (id, name, description, category, rarity, icon, points) VALUES
-- Prediction
('first_prediction', 'Prvý krok', 'Vytvor svoju prvú predikciu', 'PREDICTION', 'COMMON', '🥉', 10),
('predictions_10', 'Desať z desiatich', 'Vytvor 10 predikcií', 'PREDICTION', 'COMMON', '🥈', 25),
('predictions_100', 'Stovka', 'Vytvor 100 predikcií', 'PREDICTION', 'UNCOMMON', '🥇', 100),
('predictions_1000', 'Veterán', 'Vytvor 1000 predikcií', 'PREDICTION', 'RARE', '🏆', 500),
('hot_streak', 'Hot Streak', '5 správnych predikcií za sebou', 'PREDICTION', 'UNCOMMON', '🔥', 75),
('on_fire', 'On Fire', '10 správnych predikcií za sebou', 'PREDICTION', 'RARE', '⚡', 200),
('legendary_streak', 'Legendary Streak', '25 správnych predikcií za sebou', 'PREDICTION', 'LEGENDARY', '👑', 1000),
('exact_score', 'Presná muška', 'Trafil presný výsledok', 'PREDICTION', 'RARE', '🎯', 150),
-- Loyalty
('day_1', 'Prvý deň', 'Prvý deň na Predicto', 'LOYALTY', 'COMMON', '🟢', 5),
('week_1', 'Prvý týždeň', 'Týždeň na Predicto', 'LOYALTY', 'COMMON', '🟢', 15),
('month_1', 'Prvý mesiac', 'Mesiac na Predicto', 'LOYALTY', 'UNCOMMON', '🟢', 50),
('days_100', '100 dní', '100 dní na Predicto', 'LOYALTY', 'RARE', '🟢', 200),
('days_365', 'Celý rok', '365 dní na Predicto', 'LOYALTY', 'EPIC', '🟢', 500),
('founder', 'Founder', 'Jeden z prvých členov Predicto', 'LOYALTY', 'MYTHIC', '👑', 2000),
-- Leaderboard
('top_100', 'TOP 100', 'Umiestni sa v TOP 100', 'LEADERBOARD', 'UNCOMMON', '🏅', 100),
('top_50', 'TOP 50', 'Umiestni sa v TOP 50', 'LEADERBOARD', 'RARE', '🏅', 250),
('top_10', 'TOP 10', 'Umiestni sa v TOP 10', 'LEADERBOARD', 'EPIC', '🏅', 500),
('rank_1', '#1', 'Dostaneš sa na prvé miesto', 'LEADERBOARD', 'LEGENDARY', '🥇', 2000),
-- Sports
('first_lol', 'LoL Fanúšik', 'Prvá predikcia v League of Legends', 'SPORTS', 'COMMON', '🎮', 10),
('first_cs2', 'CS2 Fanúšik', 'Prvá predikcia v CS2', 'SPORTS', 'COMMON', '🎮', 10),
('first_f1', 'F1 Fanúšik', 'Prvá predikcia vo F1', 'SPORTS', 'COMMON', '🏎️', 10);
