PRAGMA foreign_keys = ON;

-- student_id, mentor_id point to other services (no FK)
CREATE TABLE mentorship_programs (
                                     id              TEXT PRIMARY KEY CHECK (length(id) = 36),
                                     student_id      TEXT NOT NULL,
                                     mentor_id       TEXT NOT NULL,
                                     title           TEXT NOT NULL,
                                     description     TEXT,
                                     duration_months INTEGER NOT NULL CHECK (duration_months IN (6, 12, 24, 48)),
                                     start_date      TEXT NOT NULL,
                                     end_date        TEXT NOT NULL,
                                     status          TEXT NOT NULL DEFAULT 'ACTIVE'
                                         CHECK (status IN ('ACTIVE', 'COMPLETED', 'CANCELLED')),
                                     created_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                     updated_at      TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                     CHECK (end_date > start_date)
);

CREATE INDEX idx_programs_student ON mentorship_programs (student_id, status);
CREATE INDEX idx_programs_mentor  ON mentorship_programs (mentor_id, status);

CREATE TABLE goals (
                       id                  TEXT PRIMARY KEY CHECK (length(id) = 36),
                       program_id          TEXT NOT NULL REFERENCES mentorship_programs (id) ON DELETE CASCADE,
                       title               TEXT NOT NULL,
                       description         TEXT,
                       target_date         TEXT,
                       status              TEXT NOT NULL DEFAULT 'NOT_STARTED'
                           CHECK (status IN ('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED')),
                       progress_percentage INTEGER NOT NULL DEFAULT 0
                           CHECK (progress_percentage BETWEEN 0 AND 100),
                       created_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                       updated_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_goals_program ON goals (program_id);

CREATE TABLE tasks (
                       id          TEXT PRIMARY KEY CHECK (length(id) = 36),
                       program_id  TEXT NOT NULL REFERENCES mentorship_programs (id) ON DELETE CASCADE,
                       goal_id     TEXT REFERENCES goals (id) ON DELETE SET NULL,
                       assigned_by TEXT NOT NULL,
                       assigned_to TEXT NOT NULL,
                       title       TEXT NOT NULL,
                       description TEXT,
                       due_date    TEXT,
                       priority    TEXT NOT NULL DEFAULT 'MEDIUM'
                           CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH')),
                       status      TEXT NOT NULL DEFAULT 'TODO'
                           CHECK (status IN ('TODO', 'IN_PROGRESS', 'DONE', 'CANCELLED')),
                       created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                       updated_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_tasks_program  ON tasks (program_id, status);
CREATE INDEX idx_tasks_goal     ON tasks (goal_id);
CREATE INDEX idx_tasks_assignee ON tasks (assigned_to, status, due_date);

CREATE TABLE progress_updates (
                                  id          TEXT PRIMARY KEY CHECK (length(id) = 36),
                                  goal_id     TEXT NOT NULL REFERENCES goals (id) ON DELETE CASCADE,
                                  student_id  TEXT NOT NULL,
                                  percentage  INTEGER NOT NULL CHECK (percentage BETWEEN 0 AND 100),
                                  description TEXT,
                                  created_at  TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_progress_goal ON progress_updates (goal_id, created_at DESC);

-- PRIVATE notes are hidden from the student
CREATE TABLE mentor_notes (
                              id         TEXT PRIMARY KEY CHECK (length(id) = 36),
                              program_id TEXT REFERENCES mentorship_programs (id) ON DELETE CASCADE,
                              mentor_id  TEXT NOT NULL,
                              student_id TEXT NOT NULL,
                              session_id TEXT,
                              content    TEXT NOT NULL,
                              visibility TEXT NOT NULL DEFAULT 'PRIVATE'
                                  CHECK (visibility IN ('PRIVATE', 'SHARED')),
                              created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_notes_student ON mentor_notes (student_id, visibility);
CREATE INDEX idx_notes_program ON mentor_notes (program_id);