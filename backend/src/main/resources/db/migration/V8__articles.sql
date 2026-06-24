CREATE TABLE articles (
    id UUID PRIMARY KEY,
    title VARCHAR(255) NOT NULL,
    slug VARCHAR(255) NOT NULL UNIQUE,
    summary VARCHAR(500) NOT NULL,
    content TEXT NOT NULL,
    cover_image_url VARCHAR(500),
    category VARCHAR(50) NOT NULL,
    game VARCHAR(10) NOT NULL,
    author_id UUID NOT NULL REFERENCES users(id),
    status VARCHAR(20) NOT NULL DEFAULT 'DRAFT',
    featured BOOLEAN NOT NULL DEFAULT FALSE,
    published_at TIMESTAMPTZ,
    created_at TIMESTAMPTZ NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_articles_status ON articles(status);
CREATE INDEX idx_articles_category ON articles(category);
CREATE INDEX idx_articles_game ON articles(game);
CREATE INDEX idx_articles_featured ON articles(featured);
CREATE INDEX idx_articles_published_at ON articles(published_at DESC);
CREATE INDEX idx_articles_slug ON articles(slug);
