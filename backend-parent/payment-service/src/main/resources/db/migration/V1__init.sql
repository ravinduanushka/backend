PRAGMA foreign_keys = ON;

-- booking_id, student_id point to other services (no FK)
CREATE TABLE payments (
                          id                   TEXT PRIMARY KEY CHECK (length(id) = 36),
                          booking_id           TEXT NOT NULL,
                          student_id           TEXT NOT NULL,
                          amount_cents         INTEGER NOT NULL CHECK (amount_cents > 0),
                          currency             TEXT NOT NULL DEFAULT 'USD',
                          provider             TEXT NOT NULL DEFAULT 'LEMON_SQUEEZY',
                          provider_checkout_id TEXT,
    -- unique id blocks duplicate webhook deliveries
                          transaction_id       TEXT UNIQUE,
                          status               TEXT NOT NULL DEFAULT 'PENDING'
                              CHECK (status IN ('PENDING', 'PAID', 'FAILED', 'REFUNDED')),
                          paid_at              TEXT,
                          created_at           TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                          updated_at           TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now'))
);

CREATE INDEX idx_payments_booking ON payments (booking_id);
CREATE INDEX idx_payments_student ON payments (student_id, created_at DESC);
CREATE INDEX idx_payments_status  ON payments (status);

CREATE TABLE refunds (
                         id           TEXT PRIMARY KEY CHECK (length(id) = 36),
                         payment_id   TEXT NOT NULL REFERENCES payments (id) ON DELETE RESTRICT,
                         amount_cents INTEGER NOT NULL CHECK (amount_cents > 0),
                         reason       TEXT,
                         admin_note   TEXT,
                         status       TEXT NOT NULL DEFAULT 'PENDING'
                             CHECK (status IN ('PENDING', 'COMPLETED', 'FAILED', 'REJECTED')),
                         created_at   TEXT NOT NULL DEFAULT (strftime('%Y-%m-%dT%H:%M:%fZ', 'now')),
                         processed_at TEXT
);

CREATE INDEX idx_refunds_payment ON refunds (payment_id);