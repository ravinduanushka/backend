-- user-service (user_service.db) Port 8081
-- Owns: users, student_profiles
PRAGMA foreign_keys = ON;

CREATE TABLE users (
                       id                TEXT PRIMARY KEY CHECK (length(id) = 36),
                       external_auth_id  TEXT NOT NULL UNIQUE,                 -- Asgardeo "sub" claim
                       email             TEXT NOT NULL UNIQUE COLLATE NOCASE,
                       first_name        TEXT NOT NULL,
                       last_name         TEXT NOT NULL,
                       phone             TEXT,
                       profile_image_key TEXT,                                 -- Cloudflare R2 object key
                       role              TEXT NOT NULL
                           CHECK (role IN ('STUDENT', 'MENTOR', 'ADMIN')),
                       account_status    TEXT NOT NULL DEFAULT 'ACTIVE'
                           CHECK (account_status IN ('ACTIVE', 'SUSPENDED', 'DEACTIVATED')),
                       created_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                       updated_at        TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_users_role   ON users (role);
CREATE INDEX idx_users_status ON users (account_status);

-- One profile per STUDENT user (users 1 : 0..1 student_profiles)
CREATE TABLE student_profiles (
                                  id            TEXT PRIMARY KEY CHECK (length(id) = 36),
                                  user_id       TEXT NOT NULL UNIQUE
                                      REFERENCES users (id) ON DELETE CASCADE,
                                  university    TEXT,
                                  faculty       TEXT,
                                  degree        TEXT,
                                  academic_year INTEGER CHECK (academic_year BETWEEN 1 AND 10),
                                  bio           TEXT,
                                  interests     TEXT,
                                  updated_at    TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);