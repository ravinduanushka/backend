# MentorConnect ER Diagrams

Each service has its own SQLite file. `logical` means the id points to another service (no foreign key).

## user-service

```mermaid
erDiagram
    users ||--o| student_profiles : has
    users {
        TEXT id PK
        TEXT external_auth_id UK
        TEXT email UK
        TEXT role
        TEXT account_status
    }
    student_profiles {
        TEXT id PK
        TEXT user_id FK
        TEXT university
        TEXT degree
    }
```

## mentor-service

```mermaid
erDiagram
    mentors ||--o{ availability_slots : offers
    mentors ||--o{ mentor_verifications : submits
    mentors ||--o{ mentor_expertises : has
    expertises ||--o{ mentor_expertises : usedBy
    expertise_categories ||--|{ expertises : contains
    mentors {
        TEXT id PK
        TEXT user_id UK "logical"
        INTEGER hourly_rate_cents
        REAL rating_avg
        TEXT verification_status
    }
    availability_slots {
        TEXT id PK
        TEXT mentor_id FK
        TEXT start_time
        TEXT end_time
        TEXT status
        INTEGER version
    }
    mentor_verifications {
        TEXT id PK
        TEXT mentor_id FK
        TEXT document_key
        TEXT status
        TEXT reviewed_by "logical"
    }
    expertise_categories {
        TEXT id PK
        TEXT name UK
    }
    expertises {
        TEXT id PK
        TEXT category_id FK
        TEXT name
    }
    mentor_expertises {
        TEXT mentor_id PK
        TEXT expertise_id PK
        TEXT proficiency
    }
```

## booking-service

```mermaid
erDiagram
    bookings ||--o| mentoring_sessions : produces
    bookings ||--o| reviews : reviewedOnce
    bookings |o--o{ bookings : rescheduledFrom
    bookings {
        TEXT id PK
        TEXT student_id "logical"
        TEXT mentor_id "logical"
        TEXT slot_id "logical"
        TEXT status
        INTEGER price_cents
        TEXT rescheduled_from_id FK
    }
    mentoring_sessions {
        TEXT id PK
        TEXT booking_id FK
        TEXT meeting_url
        TEXT status
    }
    reviews {
        TEXT id PK
        TEXT booking_id FK
        INTEGER rating
        TEXT comment
    }
```

## payment-service

```mermaid
erDiagram
    payments ||--o{ refunds : refundedBy
    payments {
        TEXT id PK
        TEXT booking_id "logical"
        INTEGER amount_cents
        TEXT transaction_id UK
        TEXT status
    }
    refunds {
        TEXT id PK
        TEXT payment_id FK
        INTEGER amount_cents
        TEXT status
    }
```

## mentorship-service

```mermaid
erDiagram
    mentorship_programs ||--|{ goals : defines
    mentorship_programs ||--o{ tasks : contains
    mentorship_programs ||--o{ mentor_notes : has
    goals ||--o{ tasks : breaksInto
    goals ||--o{ progress_updates : tracks
    mentorship_programs {
        TEXT id PK
        TEXT student_id "logical"
        TEXT mentor_id "logical"
        INTEGER duration_months
        TEXT status
    }
    goals {
        TEXT id PK
        TEXT program_id FK
        INTEGER progress_percentage
    }
    tasks {
        TEXT id PK
        TEXT program_id FK
        TEXT goal_id FK
        TEXT priority
        TEXT status
    }
    progress_updates {
        TEXT id PK
        TEXT goal_id FK
        INTEGER percentage
    }
    mentor_notes {
        TEXT id PK
        TEXT program_id FK
        TEXT visibility
    }
```

## notification-service

```mermaid
erDiagram
    conversations ||--|{ conversation_participants : includes
    conversations ||--o{ messages : contains
    notifications {
        TEXT id PK
        TEXT user_id "logical"
        TEXT channel
        TEXT status
    }
    conversations {
        TEXT id PK
    }
    conversation_participants {
        TEXT conversation_id PK
        TEXT user_id PK
    }
    messages {
        TEXT id PK
        TEXT conversation_id FK
        TEXT sender_id "logical"
    }
```

## analytic-service

```mermaid
erDiagram
    audit_logs {
        TEXT id PK
        TEXT service_name
        TEXT action
        TEXT metadata
    }
    reports {
        TEXT id PK
        TEXT reported_by "logical"
        TEXT status
    }
    recommendations {
        TEXT id PK
        TEXT student_id "logical"
        TEXT mentor_id "logical"
        REAL score
    }
    daily_stats {
        TEXT stat_date PK
        INTEGER revenue_cents
    }
```