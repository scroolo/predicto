CREATE TABLE sync_runs (
    id UUID PRIMARY KEY,
    job_name VARCHAR(100) NOT NULL,
    started_at TIMESTAMPTZ NOT NULL,
    finished_at TIMESTAMPTZ,
    status VARCHAR(20) NOT NULL,
    items_processed INTEGER NOT NULL DEFAULT 0,
    error_message TEXT
);

CREATE INDEX idx_sync_runs_job_name ON sync_runs(job_name);
CREATE INDEX idx_sync_runs_started_at ON sync_runs(started_at);
