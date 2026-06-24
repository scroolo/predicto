ALTER TABLE articles ADD COLUMN language VARCHAR(5) NOT NULL DEFAULT 'sk';
CREATE INDEX idx_articles_language ON articles(language);
