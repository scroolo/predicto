DROP INDEX IF EXISTS idx_leagues_external_id;
DROP INDEX IF EXISTS idx_teams_external_id;
DROP INDEX IF EXISTS idx_players_external_id;

UPDATE teams t
SET league_id = (SELECT l2.id FROM leagues l2 WHERE l2.external_id = l.external_id ORDER BY l2.id LIMIT 1)
FROM leagues l
WHERE t.league_id = l.id
  AND l.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM leagues l2 WHERE l2.external_id = l.external_id AND l2.id < l.id);

UPDATE matches m
SET league_id = (SELECT l2.id FROM leagues l2 WHERE l2.external_id = l.external_id ORDER BY l2.id LIMIT 1)
FROM leagues l
WHERE m.league_id = l.id
  AND l.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM leagues l2 WHERE l2.external_id = l.external_id AND l2.id < l.id);

UPDATE pickban_predictions p
SET league_id = (SELECT l2.id FROM leagues l2 WHERE l2.external_id = l.external_id ORDER BY l2.id LIMIT 1)
FROM leagues l
WHERE p.league_id = l.id
  AND l.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM leagues l2 WHERE l2.external_id = l.external_id AND l2.id < l.id);

DELETE FROM leagues l
WHERE l.external_id IS NOT NULL
  AND l.id != (SELECT l2.id FROM leagues l2 WHERE l2.external_id = l.external_id ORDER BY l2.id LIMIT 1);

UPDATE players p
SET team_id = (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1)
FROM teams t
WHERE p.team_id = t.id
  AND t.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM teams t2 WHERE t2.external_id = t.external_id AND t2.id < t.id);

UPDATE matches m
SET team_a_id = (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1)
FROM teams t
WHERE m.team_a_id = t.id
  AND t.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM teams t2 WHERE t2.external_id = t.external_id AND t2.id < t.id);

UPDATE matches m
SET team_b_id = (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1)
FROM teams t
WHERE m.team_b_id = t.id
  AND t.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM teams t2 WHERE t2.external_id = t.external_id AND t2.id < t.id);

UPDATE matches m
SET result_winner_team_id = (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1)
FROM teams t
WHERE m.result_winner_team_id = t.id
  AND t.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM teams t2 WHERE t2.external_id = t.external_id AND t2.id < t.id);

UPDATE match_odds mo
SET team_id = (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1)
FROM teams t
WHERE mo.team_id = t.id
  AND t.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM teams t2 WHERE t2.external_id = t.external_id AND t2.id < t.id);

UPDATE bets b
SET winner_team_id = (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1)
FROM teams t
WHERE b.winner_team_id = t.id
  AND t.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM teams t2 WHERE t2.external_id = t.external_id AND t2.id < t.id);

DELETE FROM teams t
WHERE t.external_id IS NOT NULL
  AND t.id != (SELECT t2.id FROM teams t2 WHERE t2.external_id = t.external_id ORDER BY t2.id LIMIT 1);

UPDATE matches m
SET result_mvp_player_id = (SELECT p2.id FROM players p2 WHERE p2.external_id = p.external_id ORDER BY p2.id LIMIT 1)
FROM players p
WHERE m.result_mvp_player_id = p.id
  AND p.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM players p2 WHERE p2.external_id = p.external_id AND p2.id < p.id);

UPDATE bets b
SET mvp_player_id = (SELECT p2.id FROM players p2 WHERE p2.external_id = p.external_id ORDER BY p2.id LIMIT 1)
FROM players p
WHERE b.mvp_player_id = p.id
  AND p.external_id IS NOT NULL
  AND EXISTS (SELECT 1 FROM players p2 WHERE p2.external_id = p.external_id AND p2.id < p.id);

DELETE FROM players p
WHERE p.external_id IS NOT NULL
  AND p.id != (SELECT p2.id FROM players p2 WHERE p2.external_id = p.external_id ORDER BY p2.id LIMIT 1);

ALTER TABLE leagues ADD CONSTRAINT uq_leagues_external_id UNIQUE (external_id);
ALTER TABLE teams ADD CONSTRAINT uq_teams_external_id UNIQUE (external_id);
ALTER TABLE players ADD CONSTRAINT uq_players_external_id UNIQUE (external_id);
