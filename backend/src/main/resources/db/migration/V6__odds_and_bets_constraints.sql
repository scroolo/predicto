ALTER TABLE match_odds ADD CONSTRAINT uq_match_odds_match_team UNIQUE (match_id, team_id);
ALTER TABLE score_odds ADD CONSTRAINT uq_score_odds_match_score UNIQUE (match_id, score_value);
ALTER TABLE bets ADD CONSTRAINT uq_bets_user_match UNIQUE (user_id, match_id);
