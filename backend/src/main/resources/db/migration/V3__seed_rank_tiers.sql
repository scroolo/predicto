INSERT INTO rank_tiers (id, game, tier_name, min_wagered, sort_order) VALUES
    (gen_random_uuid(), 'LOL', 'Rookie', 0, 1),
    (gen_random_uuid(), 'LOL', 'Silver', 500, 2),
    (gen_random_uuid(), 'LOL', 'Gold', 2500, 3),
    (gen_random_uuid(), 'LOL', 'Diamond', 5000, 4),
    (gen_random_uuid(), 'LOL', 'Master', 7500, 5),
    (gen_random_uuid(), 'LOL', 'Challenger', 10000, 6),
    (gen_random_uuid(), 'CS2', 'FACEIT 1', 0, 1),
    (gen_random_uuid(), 'CS2', 'FACEIT 2', 500, 2),
    (gen_random_uuid(), 'CS2', 'FACEIT 4', 2500, 3),
    (gen_random_uuid(), 'CS2', 'FACEIT 6', 5000, 4),
    (gen_random_uuid(), 'CS2', 'FACEIT 8', 7500, 5),
    (gen_random_uuid(), 'CS2', 'FACEIT 10', 10000, 6);
