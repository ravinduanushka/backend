PRAGMA foreign_keys = ON;

-- user, booking and mentor ids point to other services (no FK)
CREATE TABLE audit_logs (
                            id           TEXT PRIMARY KEY CHECK (length(id) = 36),
                            user_id      TEXT,
                            service_name TEXT NOT NULL,
                            action       TEXT NOT NULL,
                            entity_type  TEXT NOT NULL,
                            entity_id    TEXT,
                            metadata     TEXT CHECK (metadata IS NULL OR json_valid(metadata)),
                            created_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_audit_user   ON audit_logs (user_id, created_at DESC);
CREATE INDEX idx_audit_entity ON audit_logs (entity_type, entity_id);
CREATE INDEX idx_audit_action ON audit_logs (service_name, action, created_at DESC);

-- audit rows are append-only
CREATE TRIGGER trg_audit_no_update BEFORE UPDATE ON audit_logs
BEGIN SELECT RAISE(ABORT, 'audit_logs is append-only'); END;

CREATE TRIGGER trg_audit_no_delete BEFORE DELETE ON audit_logs
BEGIN SELECT RAISE(ABORT, 'audit_logs is append-only'); END;

CREATE TABLE reports (
                         id                TEXT PRIMARY KEY CHECK (length(id) = 36),
                         reported_by       TEXT NOT NULL,
                         reported_user_id  TEXT,
                         booking_id        TEXT,
                         category          TEXT NOT NULL,
                         description       TEXT NOT NULL,
                         status            TEXT NOT NULL DEFAULT 'OPEN'
                             CHECK (status IN ('OPEN', 'UNDER_REVIEW', 'RESOLVED', 'DISMISSED')),
                         assigned_admin_id TEXT,
                         resolution        TEXT,
                         created_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                         updated_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                         resolved_at       TEXT
);

CREATE INDEX idx_reports_status   ON reports (status, created_at DESC);
CREATE INDEX idx_reports_reported ON reports (reported_user_id);

CREATE TABLE recommendations (
                                 id           TEXT PRIMARY KEY CHECK (length(id) = 36),
                                 student_id   TEXT NOT NULL,
                                 mentor_id    TEXT NOT NULL,
                                 score        REAL NOT NULL CHECK (score >= 0),
                                 reason       TEXT,
                                 generated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                 UNIQUE (student_id, mentor_id)
);

CREATE INDEX idx_recommendations_student ON recommendations (student_id, score DESC);

-- one row per day, filled by DailyAnalyticsAggregationScheduler
CREATE TABLE daily_stats (
                             stat_date          TEXT PRIMARY KEY,
                             new_users          INTEGER NOT NULL DEFAULT 0,
                             total_bookings     INTEGER NOT NULL DEFAULT 0,
                             completed_bookings INTEGER NOT NULL DEFAULT 0,
                             cancelled_bookings INTEGER NOT NULL DEFAULT 0,
                             revenue_cents      INTEGER NOT NULL DEFAULT 0,
                             active_mentors     INTEGER NOT NULL DEFAULT 0,
                             created_at         TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);