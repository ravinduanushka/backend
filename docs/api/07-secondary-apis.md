# Secondary APIs

Built after Core. See `00-conventions.md` for headers, errors, pagination and formats.
Access names: Any, Student, Mentor, Admin. Responses use the same status codes as Core. Only the extra cases are listed.

## Path prefixes added

| Service | Port | New prefixes |
|---|---|---|
| user-service | 8081 | `/api/users` (admin routes) |
| payment-service | 8084 | `/api/payments`, `/api/refunds` |
| mentorship-service | 8085 | `/api/programs`, `/api/goals`, `/api/tasks` |
| notification-service | 8086 | `/api/notifications`, `/api/conversations` |
| analytic-service | 8087 | `/api/recommendations`, `/api/reports`, `/api/admin` |

## booking-service

### POST /api/bookings/{id}/reschedule

Move a booking to another slot of the same mentor. Access: Student of the booking.

```json
{ "newSlotId": "b7c8d9e0-1234-4abc-8def-0123456789ab" }
```

Rules: the booking is `CONFIRMED`, it starts in more than 24 hours, the new slot belongs to the same mentor and has the same length (so the price stays the same). The old booking becomes `RESCHEDULED` and its slot is released. A new booking is created as `CONFIRMED` with `rescheduledFromId` set and no new payment.

Responses: `201` BookingResponse (the new booking), `400`, `401`, `403`, `404`, `409` not allowed or new slot not available.

## payment-service

PaymentResponse:

```json
{
  "id": "7c6b5a49-3827-4165-9403-f2e1d0c9b8a7",
  "bookingId": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "amountCents": 5000,
  "currency": "USD",
  "status": "PAID",
  "paidAt": "2026-11-01T10:05:00.000Z",
  "createdAt": "2026-11-01T10:01:00.000Z"
}
```

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/api/payments` | Own payments (query: `status`, `page`, `size`) | Student |

`GET /api/payments`: `200` page of PaymentResponse.

Refunds (`POST /api/refunds`, `GET /api/refunds`, `PUT /api/refunds/{id}`) are described in `06-payment-service.md`.

## mentorship-service

ProgramResponse:

```json
{
  "id": "11111111-2222-4333-8444-555555555555",
  "studentId": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "title": "Backend career plan",
  "description": "12 month plan",
  "durationMonths": 12,
  "startDate": "2026-11-01",
  "endDate": "2027-11-01",
  "status": "ACTIVE",
  "createdAt": "2026-11-01T10:00:00.000Z"
}
```

GoalResponse:

```json
{
  "id": "22222222-3333-4444-8555-666666666666",
  "programId": "11111111-2222-4333-8444-555555555555",
  "title": "Learn Spring Boot",
  "description": null,
  "targetDate": "2027-02-01",
  "status": "IN_PROGRESS",
  "progressPercentage": 40
}
```

TaskResponse:

```json
{
  "id": "33333333-4444-4555-8666-777777777777",
  "programId": "11111111-2222-4333-8444-555555555555",
  "goalId": "22222222-3333-4444-8555-666666666666",
  "assignedBy": "8d2e7a10-5b3c-4d6e-9f01-2a3b4c5d6e7f",
  "assignedTo": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "title": "Build a REST API",
  "description": null,
  "dueDate": "2026-12-01",
  "priority": "HIGH",
  "status": "TODO"
}
```

ProgressResponse: `{ "id", "goalId", "studentId", "percentage": 40, "description": "Finished controllers", "createdAt" }`.

NoteResponse: `{ "id", "programId", "mentorId", "studentId", "sessionId": null, "content", "visibility": "SHARED", "createdAt" }`.

| Method | Path | Purpose | Access |
|---|---|---|---|
| POST | `/api/programs` | Create a program | Mentor |
| GET | `/api/programs` | Own programs (query: `status`, `page`, `size`) | Student, Mentor |
| GET | `/api/programs/{id}` | One program | Student, Mentor of it |
| PUT | `/api/programs/{id}/status` | Complete or cancel | Mentor |
| POST | `/api/programs/{id}/goals` | Add a goal | Mentor |
| GET | `/api/programs/{id}/goals` | List goals | Student, Mentor of it |
| PUT | `/api/goals/{id}` | Edit a goal | Mentor |
| POST | `/api/goals/{id}/tasks` | Add a task | Mentor |
| GET | `/api/programs/{id}/tasks` | List tasks (query: `status`, `goalId`) | Student, Mentor of it |
| PUT | `/api/tasks/{id}` | Edit a task | Mentor |
| PUT | `/api/tasks/{id}/status` | Change task status | Student, Mentor of it |
| POST | `/api/goals/{id}/progress` | Add a progress update | Student |
| GET | `/api/goals/{id}/progress` | List progress updates | Student, Mentor of it |
| POST | `/api/programs/{id}/notes` | Add a note | Mentor |
| GET | `/api/programs/{id}/notes` | List notes. A student gets `SHARED` notes only | Student, Mentor of it |

Request bodies:

```json
{ "studentId": "5f1c0b7e-...", "title": "Backend career plan", "description": "12 month plan", "durationMonths": 12, "startDate": "2026-11-01" }
```
`POST /api/programs`. `durationMonths` is 6, 12, 24 or 48. `endDate` is calculated. `409` if the mentor already has an `ACTIVE` program with this student.

```json
{ "status": "COMPLETED" }
```
`PUT /api/programs/{id}/status`. Allowed: `COMPLETED`, `CANCELLED`.

```json
{ "title": "Learn Spring Boot", "description": null, "targetDate": "2027-02-01", "status": "IN_PROGRESS" }
```
`POST /api/programs/{id}/goals` and `PUT /api/goals/{id}`. `status` is only used on PUT.

```json
{ "title": "Build a REST API", "description": null, "dueDate": "2026-12-01", "priority": "HIGH" }
```
`POST /api/goals/{id}/tasks` and `PUT /api/tasks/{id}`. `priority` is `LOW`, `MEDIUM` or `HIGH`. `assignedBy` is the mentor and `assignedTo` is the program's student, both set by the server.

```json
{ "status": "IN_PROGRESS" }
```
`PUT /api/tasks/{id}/status`. Values: `TODO`, `IN_PROGRESS`, `DONE`, `CANCELLED`.

```json
{ "percentage": 40, "description": "Finished controllers" }
```
`POST /api/goals/{id}/progress`. `percentage` is 0 to 100. The goal's `progressPercentage` is set to the latest update.

```json
{ "content": "Good progress on APIs", "visibility": "SHARED", "sessionId": null }
```
`POST /api/programs/{id}/notes`. `visibility` is `PRIVATE` or `SHARED`.

## notification-service

NotificationResponse:

```json
{
  "id": "44444444-5555-4666-8777-888888888888",
  "type": "BOOKING_CONFIRMED",
  "title": "Booking confirmed",
  "message": "Your session is on 5 Nov at 09:00",
  "channel": "IN_APP",
  "status": "SENT",
  "createdAt": "2026-11-01T10:05:00.000Z",
  "readAt": null
}
```

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/api/notifications` | Own notifications (query: `status`, `page`, `size`) | Any |
| PUT | `/api/notifications/{id}/read` | Mark one as read | Any |
| PUT | `/api/notifications/read-all` | Mark all as read | Any |

Responses: `200` page, `200` NotificationResponse, `204`. A user can only read their own notifications (`404` otherwise).

ConversationResponse:

```json
{
  "id": "55555555-6666-4777-8888-999999999999",
  "participants": ["5f1c0b7e-...", "8d2e7a10-..."],
  "lastMessage": { "content": "See you tomorrow", "sentAt": "2026-11-04T18:00:00.000Z" },
  "unreadCount": 2
}
```

MessageResponse:

```json
{
  "id": "66666666-7777-4888-8999-aaaaaaaaaaaa",
  "conversationId": "55555555-6666-4777-8888-999999999999",
  "senderId": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "content": "See you tomorrow",
  "attachmentKey": null,
  "sentAt": "2026-11-04T18:00:00.000Z",
  "readAt": null
}
```

| Method | Path | Purpose | Access |
|---|---|---|---|
| POST | `/api/conversations` | Start a chat with one user, or get the existing one | Any |
| GET | `/api/conversations` | Own chats, newest message first | Any |
| GET | `/api/conversations/{id}/messages` | Messages, newest first (query: `page`, `size`) | Participant |
| POST | `/api/conversations/{id}/messages` | Send a message | Participant |
| PUT | `/api/conversations/{id}/read` | Mark messages from others as read | Participant |
| POST | `/api/conversations/{id}/attachment-upload-url` | Presigned R2 URL for an attachment | Participant |

```json
{ "participantId": "8d2e7a10-5b3c-4d6e-9f01-2a3b4c5d6e7f" }
```
`POST /api/conversations`: `201` ConversationResponse when new, `200` when it already exists, `404` user not found, `400` chatting with yourself.

```json
{ "content": "See you tomorrow", "attachmentKey": null }
```
`POST .../messages`: `content` is required, up to 2000 characters. `201` MessageResponse, `403` not a participant.

Chat uses polling in this version. Real-time delivery (WebSocket) is a later improvement.

## analytic-service

RecommendationResponse:

```json
{
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "score": 0.87,
  "reason": "Matches your interest in Java and has a 4.6 rating",
  "generatedAt": "2026-11-01T02:00:00.000Z"
}
```

ReportResponse:

```json
{
  "id": "77777777-8888-4999-8aaa-bbbbbbbbbbbb",
  "reportedBy": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "reportedUserId": "8d2e7a10-5b3c-4d6e-9f01-2a3b4c5d6e7f",
  "bookingId": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "category": "NO_SHOW",
  "description": "Mentor did not join",
  "status": "OPEN",
  "assignedAdminId": null,
  "resolution": null,
  "createdAt": "2026-11-05T11:00:00.000Z",
  "resolvedAt": null
}
```

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/api/recommendations` | Top mentors for me (query: `limit`, default 10, max 20) | Student |
| POST | `/api/reports` | Submit a report | Any |
| GET | `/api/reports/mine` | Own reports | Any |
| GET | `/api/reports` | All reports (query: `status`, `page`, `size`) | Admin |
| GET | `/api/reports/{id}` | One report | Admin |
| PUT | `/api/reports/{id}` | Assign, review, resolve or dismiss | Admin |
| GET | `/api/admin/analytics` | Platform numbers (query: `from`, `to` as dates) | Admin |
| GET | `/api/admin/audit-logs` | Audit trail (query: `userId`, `entityType`, `action`, `from`, `to`, `page`, `size`) | Admin |

```json
{ "reportedUserId": "8d2e7a10-...", "bookingId": "d4e5f6a7-...", "category": "NO_SHOW", "description": "Mentor did not join" }
```
`POST /api/reports`: `category` is `NO_SHOW`, `INAPPROPRIATE_BEHAVIOR`, `PAYMENT_ISSUE` or `OTHER`. `description` is required, up to 2000 characters. `reportedUserId` and `bookingId` are optional. `201` ReportResponse.

```json
{ "status": "RESOLVED", "assignedAdminId": "9a8b7c6d-...", "resolution": "Warned the mentor" }
```
`PUT /api/reports/{id}`: `status` is `UNDER_REVIEW`, `RESOLVED` or `DISMISSED`. `resolution` is required for `RESOLVED` and `DISMISSED`. `200` ReportResponse, `409` report is already closed.

`GET /api/admin/analytics` response `200`:

```json
{
  "from": "2026-11-01",
  "to": "2026-11-30",
  "totals": {
    "newUsers": 120,
    "totalBookings": 85,
    "completedBookings": 60,
    "cancelledBookings": 10,
    "revenueCents": 300000,
    "activeMentors": 25
  },
  "daily": [
    { "date": "2026-11-01", "newUsers": 5, "totalBookings": 3, "completedBookings": 2, "cancelledBookings": 0, "revenueCents": 10000, "activeMentors": 4 }
  ]
}
```

`GET /api/admin/audit-logs` response `200`: page of

```json
{
  "id": "88888888-9999-4aaa-8bbb-cccccccccccc",
  "userId": "5f1c0b7e-...",
  "serviceName": "booking-service",
  "action": "BOOKING_CREATED",
  "entityType": "Booking",
  "entityId": "d4e5f6a7-...",
  "metadata": { "slotId": "b7c8d9e0-..." },
  "createdAt": "2026-11-01T10:00:00.000Z"
}
```

## user-service (admin)

| Method | Path | Purpose | Access |
|---|---|---|---|
| GET | `/api/users` | List users (query: `role`, `accountStatus`, `q`, `page`, `size`) | Admin |
| PUT | `/api/users/{id}/status` | Suspend or activate | Admin |

`GET /api/users`: `200` page of UserResponse.

```json
{ "accountStatus": "SUSPENDED" }
```
`PUT /api/users/{id}/status`: allowed `ACTIVE` or `SUSPENDED`. `200` UserResponse, `404`, `409` an admin cannot suspend their own account.

## Internal endpoints

Called only by other services.

| Method | Path | Service | Called by | Body |
|---|---|---|---|---|
| POST | `/internal/notifications` | notification | booking, payment, mentorship | `{ "userId", "type", "title", "message", "channel", "scheduledAt" }` |
| POST | `/internal/audit-logs` | analytic | all services | `{ "userId", "serviceName", "action", "entityType", "entityId", "metadata" }` |

Both answer `201` with `{ "id": "..." }`.

## Open decisions


- Who may start a chat: any two users, or only a student and mentor who share a booking or program
- Reschedule: 24 hour limit and same mentor only
- Chat delivery: polling now, WebSocket later