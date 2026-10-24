PRAGMA foreign_keys = ON;

-- user_id, sender_id point to the user service (no FK)
CREATE TABLE notifications (
                               id           TEXT PRIMARY KEY CHECK (length(id) = 36),
                               user_id      TEXT NOT NULL,
                               type         TEXT NOT NULL,
                               title        TEXT NOT NULL,
                               message      TEXT NOT NULL,
                               channel      TEXT NOT NULL
                                   CHECK (channel IN ('EMAIL', 'PUSH', 'IN_APP')),
                               status       TEXT NOT NULL DEFAULT 'PENDING'
                                   CHECK (status IN ('PENDING', 'SENT', 'FAILED', 'READ')),
                               retry_count  INTEGER NOT NULL DEFAULT 0 CHECK (retry_count >= 0),
                               scheduled_at TEXT,
                               sent_at      TEXT,
                               read_at      TEXT,
                               created_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_notifications_user    ON notifications (user_id, status, created_at DESC);
CREATE INDEX idx_notifications_pending ON notifications (status, scheduled_at);

CREATE TABLE conversations (
                               id         TEXT PRIMARY KEY CHECK (length(id) = 36),
                               created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE conversation_participants (
                                           conversation_id TEXT NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
                                           user_id         TEXT NOT NULL,
                                           joined_at       TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                           PRIMARY KEY (conversation_id, user_id)
);

CREATE INDEX idx_participants_user ON conversation_participants (user_id);

CREATE TABLE messages (
                          id              TEXT PRIMARY KEY CHECK (length(id) = 36),
                          conversation_id TEXT NOT NULL REFERENCES conversations (id) ON DELETE CASCADE,
                          sender_id       TEXT NOT NULL,
                          content         TEXT NOT NULL,
                          attachment_key  TEXT,
                          sent_at         TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                          read_at         TEXT
);

CREATE INDEX idx_messages_conversation ON messages (conversation_id, sent_at);