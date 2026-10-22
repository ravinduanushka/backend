PRAGMA foreign_keys = ON;

-- student_id, mentor_id, slot_id point to other services (no FK)
CREATE TABLE bookings (
                          id                  TEXT PRIMARY KEY CHECK (length(id) = 36),
                          student_id          TEXT NOT NULL,
                          mentor_id           TEXT NOT NULL,
                          slot_id             TEXT NOT NULL,
                          scheduled_start     TEXT NOT NULL,
                          scheduled_end       TEXT NOT NULL,
                          topic               TEXT,
                          price_cents         INTEGER NOT NULL CHECK (price_cents >= 0),
                          currency            TEXT NOT NULL DEFAULT 'USD',
                          status              TEXT NOT NULL DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING', 'CONFIRMED', 'COMPLETED',
                                                'CANCELLED', 'RESCHEDULED', 'NO_SHOW')),
                          cancellation_reason TEXT,
                          rescheduled_from_id TEXT REFERENCES bookings (id),
                          expires_at          TEXT,
                          created_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                          updated_at          TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                          CHECK (scheduled_end > scheduled_start)
);

-- one live booking per slot (prevents double booking)
CREATE UNIQUE INDEX uq_bookings_active_slot
    ON bookings (slot_id)
    WHERE status NOT IN ('CANCELLED', 'RESCHEDULED');

CREATE INDEX idx_bookings_student   ON bookings (student_id, status);
CREATE INDEX idx_bookings_mentor    ON bookings (mentor_id, status);
CREATE INDEX idx_bookings_expiry    ON bookings (status, expires_at);
CREATE INDEX idx_bookings_scheduled ON bookings (scheduled_start);

CREATE TABLE mentoring_sessions (
                                    id               TEXT PRIMARY KEY CHECK (length(id) = 36),
                                    booking_id       TEXT NOT NULL UNIQUE REFERENCES bookings (id) ON DELETE CASCADE,
                                    meeting_url      TEXT,
                                    meeting_provider TEXT,
                                    actual_start     TEXT,
                                    actual_end       TEXT,
                                    status           TEXT NOT NULL DEFAULT 'SCHEDULED'
                                        CHECK (status IN ('SCHEDULED', 'IN_PROGRESS', 'COMPLETED', 'CANCELLED')),
                                    created_at       TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                                    updated_at       TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE TABLE reviews (
                         id         TEXT PRIMARY KEY CHECK (length(id) = 36),
                         booking_id TEXT NOT NULL UNIQUE REFERENCES bookings (id) ON DELETE CASCADE,
                         student_id TEXT NOT NULL,
                         mentor_id  TEXT NOT NULL,
                         rating     INTEGER NOT NULL CHECK (rating BETWEEN 1 AND 5),
                         comment    TEXT,
                         created_at TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_reviews_mentor ON reviews (mentor_id, created_at DESC);

-- reviews allowed only for COMPLETED bookings
CREATE TRIGGER trg_reviews_require_completed
    BEFORE INSERT ON reviews
    FOR EACH ROW
    WHEN (SELECT status FROM bookings WHERE id = NEW.booking_id) IS NOT 'COMPLETED'
BEGIN
SELECT RAISE(ABORT, 'Review allowed only for COMPLETED bookings');
END;