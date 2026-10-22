# API Categories (Core and Secondary)

Core = needed for the MVP, built first. Secondary = built later.
Login is handled by Asgardeo. The backend only validates the JWT.

## Core (MVP)

| # | API area | Service | Who |
|---|---|---|---|
| 1 | Current user (`me`) from JWT, create user on first login | user | All |
| 2 | Student profile: view and update | user | Student |
| 3 | Mentor profile: create, view, update | mentor | Mentor |
| 4 | Browse and search mentors, view one mentor | mentor | Student |
| 5 | Expertise categories and mentor skills | mentor | Mentor, Public |
| 6 | Availability slots: create, list, update, delete | mentor | Mentor |
| 7 | View a mentor's available slots | mentor | Student |
| 8 | Mentor verification: submit document, approve or reject | mentor | Mentor, Admin |
| 9 | Presigned upload URL (profile image, verification document) | user, mentor | Student, Mentor |
| 10 | Booking: create, list mine, view one, cancel | booking | Student, Mentor |
| 11 | Session: start and complete | booking | Mentor |
| 12 | Review: submit, list a mentor's reviews | booking | Student, Public |
| 13 | Payment: create checkout | payment | Student |
| 14 | Payment webhook from Lemon Squeezy | payment | Lemon Squeezy |

## Secondary

| # | API area | Service | Who |
|---|---|---|---|
| 15 | Reschedule a booking | booking | Student |
| 16 | Payment history | payment | Student |
| 17 | Refund request and approval | payment | Student, Admin |
| 18 | Mentorship programs, goals, tasks, progress updates | mentorship | Mentor, Student |
| 19 | Mentor notes (private and shared) | mentorship | Mentor, Student |
| 20 | Notifications: list and mark read | notification | All |
| 21 | Chat: conversations and messages | notification | All |
| 22 | Mentor recommendations | analytic | Student |
| 23 | Reports: submit, admin handles | analytic | All, Admin |
| 24 | Audit logs and analytics dashboard | analytic | Admin |
| 25 | Admin user management: list, suspend, activate | user | Admin |

## Open decisions

- How the frontend reaches 7 services (one gateway or direct calls). Not in the file tree yet
- Which service issues the presigned upload URLs
- Whether Cloudflare Workers sit in front of these APIs