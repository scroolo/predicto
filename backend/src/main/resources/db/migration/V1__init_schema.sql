CREATE TABLE users (
    id UUID PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    display_name VARCHAR(255) NOT NULL,
    email VARCHAR(255),
    password_hash VARCHAR(255),
    discord_id VARCHAR(255),
    avatar_url VARCHAR(255),
    badge VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE UNIQUE INDEX idx_users_username ON users(username);
CREATE UNIQUE INDEX idx_users_email ON users(email) WHERE email IS NOT NULL;
CREATE UNIQUE INDEX idx_users_discord_id ON users(discord_id) WHERE discord_id IS NOT NULL;

CREATE TABLE wallets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL UNIQUE REFERENCES users(id),
    balance INTEGER NOT NULL DEFAULT 100,
    lifetime_wagered_lol INTEGER NOT NULL DEFAULT 0,
    lifetime_wagered_cs2 INTEGER NOT NULL DEFAULT 0,
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_wallets_user_id ON wallets(user_id);

CREATE TABLE rank_tiers (
    id UUID PRIMARY KEY,
    game VARCHAR(10) NOT NULL,
    tier_name VARCHAR(255) NOT NULL,
    min_wagered INTEGER NOT NULL,
    sort_order INTEGER NOT NULL
);

CREATE INDEX idx_rank_tiers_game ON rank_tiers(game);

CREATE TABLE seasons (
    id UUID PRIMARY KEY,
    game VARCHAR(10) NOT NULL,
    type VARCHAR(20) NOT NULL,
    name VARCHAR(255) NOT NULL,
    starts_at TIMESTAMPTZ NOT NULL,
    ends_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'UPCOMING',
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_seasons_status ON seasons(status);
CREATE INDEX idx_seasons_game ON seasons(game);

CREATE TABLE leagues (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255),
    source VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    game VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    region VARCHAR(255),
    logo_url VARCHAR(255)
);

CREATE INDEX idx_leagues_game ON leagues(game);

CREATE TABLE teams (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255),
    source VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    league_id UUID REFERENCES leagues(id),
    game VARCHAR(10) NOT NULL,
    name VARCHAR(255) NOT NULL,
    short_code VARCHAR(50),
    logo_url VARCHAR(255),
    color VARCHAR(50)
);

CREATE INDEX idx_teams_league_id ON teams(league_id);
CREATE INDEX idx_teams_game ON teams(game);

CREATE TABLE players (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255),
    source VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    team_id UUID NOT NULL REFERENCES teams(id),
    nickname VARCHAR(255) NOT NULL,
    role VARCHAR(50),
    photo_url VARCHAR(255)
);

CREATE INDEX idx_players_team_id ON players(team_id);

CREATE TABLE matches (
    id UUID PRIMARY KEY,
    external_id VARCHAR(255) UNIQUE,
    source VARCHAR(10) NOT NULL DEFAULT 'MANUAL',
    game VARCHAR(10) NOT NULL,
    league_id UUID NOT NULL REFERENCES leagues(id),
    team_a_id UUID NOT NULL REFERENCES teams(id),
    team_b_id UUID NOT NULL REFERENCES teams(id),
    format VARCHAR(10) NOT NULL,
    stage VARCHAR(255),
    starts_at TIMESTAMPTZ NOT NULL,
    status VARCHAR(20) NOT NULL DEFAULT 'SCHEDULED',
    result_winner_team_id UUID REFERENCES teams(id),
    result_score VARCHAR(50),
    result_mvp_player_id UUID REFERENCES players(id),
    locked_at TIMESTAMPTZ,
    finished_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_matches_league_id ON matches(league_id);
CREATE INDEX idx_matches_team_a_id ON matches(team_a_id);
CREATE INDEX idx_matches_team_b_id ON matches(team_b_id);
CREATE INDEX idx_matches_starts_at ON matches(starts_at);
CREATE INDEX idx_matches_status ON matches(status);
CREATE INDEX idx_matches_game ON matches(game);

CREATE TABLE match_odds (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches(id),
    team_id UUID NOT NULL REFERENCES teams(id),
    odds_value NUMERIC(5,2) NOT NULL,
    set_by_user_id UUID NOT NULL REFERENCES users(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_match_odds_match_id ON match_odds(match_id);
CREATE INDEX idx_match_odds_team_id ON match_odds(team_id);

CREATE TABLE score_odds (
    id UUID PRIMARY KEY,
    match_id UUID NOT NULL REFERENCES matches(id),
    score_value VARCHAR(20) NOT NULL,
    odds_value NUMERIC(5,2) NOT NULL,
    set_by_user_id UUID NOT NULL REFERENCES users(id),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_score_odds_match_id ON score_odds(match_id);

CREATE TABLE bets (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    match_id UUID NOT NULL REFERENCES matches(id),
    season_id UUID REFERENCES seasons(id),
    winner_team_id UUID REFERENCES teams(id),
    winner_odds_snapshot NUMERIC(5,2),
    stake INTEGER NOT NULL,
    potential_return INTEGER NOT NULL,
    mvp_player_id UUID REFERENCES players(id),
    exact_score VARCHAR(20),
    score_odds_snapshot NUMERIC(5,2),
    score_stake INTEGER,
    score_potential_return INTEGER,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    points_awarded INTEGER NOT NULL DEFAULT 0,
    actual_return INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    settled_at TIMESTAMPTZ
);

CREATE INDEX idx_bets_user_id ON bets(user_id);
CREATE INDEX idx_bets_match_id ON bets(match_id);
CREATE INDEX idx_bets_season_id ON bets(season_id);
CREATE INDEX idx_bets_status ON bets(status);
CREATE INDEX idx_bets_user_id_season_id ON bets(user_id, season_id);

CREATE TABLE pickban_predictions (
    id UUID PRIMARY KEY,
    user_id UUID NOT NULL REFERENCES users(id),
    league_id UUID NOT NULL REFERENCES leagues(id),
    season_id UUID REFERENCES seasons(id),
    most_picked_champion VARCHAR(100),
    most_banned_champion VARCHAR(100),
    most_kills_champion VARCHAR(100),
    most_assists_champion VARCHAR(100),
    pentakill_champion VARCHAR(100),
    locked BOOLEAN NOT NULL DEFAULT FALSE,
    points_awarded INTEGER NOT NULL DEFAULT 0,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_pickban_user_id ON pickban_predictions(user_id);
CREATE INDEX idx_pickban_league_id ON pickban_predictions(league_id);
CREATE INDEX idx_pickban_season_id ON pickban_predictions(season_id);

CREATE TABLE leaderboard_entries (
    id UUID PRIMARY KEY,
    season_id UUID NOT NULL REFERENCES seasons(id),
    user_id UUID NOT NULL REFERENCES users(id),
    points INTEGER NOT NULL,
    correct_picks INTEGER NOT NULL,
    mvp_correct INTEGER NOT NULL,
    score_correct INTEGER NOT NULL,
    rank_position INTEGER,
    computed_at TIMESTAMPTZ NOT NULL
);

CREATE INDEX idx_lb_season_id ON leaderboard_entries(season_id);
CREATE INDEX idx_lb_user_id ON leaderboard_entries(user_id);
CREATE INDEX idx_lb_season_points ON leaderboard_entries(season_id, points DESC);

CREATE TABLE rewards (
    id UUID PRIMARY KEY,
    season_id UUID NOT NULL REFERENCES seasons(id),
    rank_position INTEGER NOT NULL CHECK (rank_position >= 1 AND rank_position <= 5),
    description VARCHAR(255) NOT NULL,
    user_id UUID REFERENCES users(id),
    claimed BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_rewards_season_id ON rewards(season_id);
CREATE INDEX idx_rewards_user_id ON rewards(user_id);

CREATE TABLE audit_log (
    id UUID PRIMARY KEY,
    actor_user_id UUID REFERENCES users(id),
    action VARCHAR(255) NOT NULL,
    entity_type VARCHAR(255) NOT NULL,
    entity_id VARCHAR(255) NOT NULL,
    details JSONB,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_audit_actor ON audit_log(actor_user_id);
CREATE INDEX idx_audit_entity ON audit_log(entity_type, entity_id);
