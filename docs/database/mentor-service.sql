-- mentor-service (mentor_service.db) Port 8082
-- Owns: mentors, expertise taxonomy, availability_slots, mentor_verifications
PRAGMA foreign_keys = ON;

CREATE TABLE mentors (
                         id                  TEXT PRIMARY KEY CHECK (length(id) = 36),
                         user_id             TEXT NOT NULL UNIQUE,               -- -> user-service.users.id (logical)
                         title               TEXT,
                         organization        TEXT,
                         industry            TEXT,
                         bio                 TEXT,
                         experience_years    INTEGER NOT NULL DEFAULT 0 CHECK (experience_years >= 0),
                         hourly_rate_cents   INTEGER NOT NULL DEFAULT 0 CHECK (hourly_rate_cents >= 0),
                         currency            TEXT NOT NULL DEFAULT 'USD',
                         rating_avg          REAL NOT NULL DEFAULT 0 CHECK (rating_avg BETWEEN 0 AND 5),
                         rating_count        INTEGER NOT NULL DEFAULT 0 CHECK (rating_count >= 0),
                         verification_status TEXT NOT NULL DEFAULT 'PENDING'
                             CHECK (verification_status IN ('PENDING', 'APPROVED', 'REJECTED')),
                         created_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                         updated_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_mentors_verification ON mentors (verification_status);
CREATE INDEX idx_mentors_rating       ON mentors (rating_avg DESC);
CREATE INDEX idx_mentors_industry     ON mentors (industry);

-- ---------- Expertise taxonomy ----------
CREATE TABLE expertise_categories (
                                      id          TEXT PRIMARY KEY CHECK (length(id) = 36),
                                      name        TEXT NOT NULL UNIQUE COLLATE NOCASE,
                                      description TEXT
);

CREATE TABLE expertises (
                            id          TEXT PRIMARY KEY CHECK (length(id) = 36),
                            category_id TEXT NOT NULL REFERENCES expertise_categories (id) ON DELETE RESTRICT,
                            name        TEXT NOT NULL COLLATE NOCASE,
                            description TEXT,
                            UNIQUE (category_id, name)
);

CREATE TABLE mentor_expertises (
                                   mentor_id    TEXT NOT NULL REFERENCES mentors (id) ON DELETE CASCADE,
                                   expertise_id TEXT NOT NULL REFERENCES expertises (id) ON DELETE RESTRICT,
                                   proficiency  TEXT NOT NULL DEFAULT 'INTERMEDIATE'
                                       CHECK (proficiency IN ('BEGINNER', 'INTERMEDIATE', 'ADVANCED', 'EXPERT')),
                                   PRIMARY KEY (mentor_id, expertise_id)
);

CREATE INDEX idx_mentor_expertises_expertise ON mentor_expertises (expertise_id);

-- ---------- Availability ----------
CREATE TABLE availability_slots (
                                    id         TEXT PRIMARY KEY CHECK (length(id) = 36),
                                    mentor_id  TEXT NOT NULL REFERENCES mentors (id) ON DELETE CASCADE,
                                    start_time TEXT NOT NULL,                               -- UTC ISO-8601
                                    end_time   TEXT NOT NULL,                               -- UTC ISO-8601
                                    timezone   TEXT NOT NULL DEFAULT 'UTC',                 -- mentor display tz (IANA)
                                    status     TEXT NOT NULL DEFAULT 'AVAILABLE'
                                        CHECK (status IN ('AVAILABLE', 'HELD', 'BOOKED', 'BLOCKED')),
                                    version    INTEGER NOT NULL DEFAULT 0,                  -- optimistic lock (@Version)
                                    created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                    updated_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                    CHECK (end_time > start_time),
                                    UNIQUE (mentor_id, start_time)
);

CREATE INDEX idx_slots_mentor_status_start ON availability_slots (mentor_id, status, start_time);
CREATE INDEX idx_slots_status_start        ON availability_slots (status, start_time);

-- ---------- Verification ----------
CREATE TABLE mentor_verifications (
                                      id            TEXT PRIMARY KEY CHECK (length(id) = 36),
                                      mentor_id     TEXT NOT NULL REFERENCES mentors (id) ON DELETE CASCADE,
                                      document_key  TEXT NOT NULL,                            -- Cloudflare R2 object key
                                      status        TEXT NOT NULL DEFAULT 'PENDING'
                                          CHECK (status IN ('PENDING', 'APPROVED', 'REJECTED')),
                                      reviewed_by   TEXT,                                     -- -> user-service.users.id (admin)
                                      review_note   TEXT,
                                      submitted_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                      reviewed_at   TEXT
);

CREATE INDEX idx_verifications_mentor ON mentor_verifications (mentor_id);
CREATE INDEX idx_verifications_status ON mentor_verifications (status);