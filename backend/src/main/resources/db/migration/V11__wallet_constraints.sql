ALTER TABLE wallets ADD CONSTRAINT chk_balance_positive CHECK (balance >= 0);
ALTER TABLE wallets ADD CONSTRAINT chk_lifetime_wagered_lol_positive CHECK (lifetime_wagered_lol >= 0);
ALTER TABLE wallets ADD CONSTRAINT chk_lifetime_wagered_cs2_positive CHECK (lifetime_wagered_cs2 >= 0);
