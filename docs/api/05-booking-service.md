# booking-service API

Base URL (dev): `http://localhost:8083`. See `00-conventions.md` for headers, errors, pagination, formats and caller identity.

## Booking flow

1. Student calls `POST /api/bookings`. The booking is `PENDING`, the slot is `HELD`, and `expiresAt` is set 15 minutes ahead
2. Student calls `POST /api/payments/checkout` (payment-service) and pays on Lemon Squeezy
3. The webhook reaches payment-service, which calls `PUT /internal/bookings/{id}/confirm`
4. The booking is `CONFIRMED`, the slot is `BOOKED`, and a session is created as `SCHEDULED`
5. Mentor starts and completes the session. The booking becomes `COMPLETED`
6. Student can now review the booking

If payment is not done by `expiresAt`, a scheduler cancels the booking and frees the slot.

`RESCHEDULED` is used by the reschedule feature and `NO_SHOW` is reserved for later. Neither has a Core endpoint.

Price = `hourlyRateCents` x session minutes / 60, rounded to the nearest cent.

## Shared shapes

BookingResponse:

```json
{
  "id": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "studentId": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "slotId": "b7c8d9e0-1234-4abc-8def-0123456789ab",
  "scheduledStart": "2026-11-05T09:00:00.000Z",
  "scheduledEnd": "2026-11-05T10:00:00.000Z",
  "topic": "Career guidance",
  "priceCents": 5000,
  "currency": "USD",
  "status": "PENDING",
  "cancellationReason": null,
  "rescheduledFromId": null,
  "expiresAt": "2026-11-01T10:15:00.000Z",
  "createdAt": "2026-11-01T10:00:00.000Z",
  "updatedAt": "2026-11-01T10:00:00.000Z"
}
```

SessionResponse:

```json
{
  "id": "0a1b2c3d-4e5f-4a6b-8c7d-8e9f0a1b2c3d",
  "bookingId": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "meetingUrl": "https://meet.jit.si/mentorconnect-d4e5f6a7",
  "meetingProvider": "JITSI",
  "actualStart": null,
  "actualEnd": null,
  "status": "SCHEDULED"
}
```

ReviewResponse:

```json
{
  "id": "9f8e7d6c-5b4a-4392-8170-6f5e4d3c2b1a",
  "bookingId": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "studentId": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "rating": 5,
  "comment": "Very helpful session",
  "createdAt": "2026-11-05T11:00:00.000Z"
}
```

Names are not included. The frontend gets a mentor's name from `GET /api/mentors/{id}`.

## Bookings

### POST /api/bookings

Access: Student.

```json
{
  "slotId": "b7c8d9e0-1234-4abc-8def-0123456789ab",
  "topic": "Career guidance"
}
```

| Field | Rule |
|---|---|
| `slotId` | required, UUID |
| `topic` | optional, up to 200 characters |

Responses: `201` BookingResponse (status `PENDING`), `400`, `401`, `403` not a STUDENT or account not ACTIVE, `404` slot not found, `409` slot is not available or the mentor is not APPROVED.

### GET /api/bookings

Own bookings. A student sees bookings they made. A mentor sees bookings made with them. Newest first. Access: Student, Mentor.

Query: `status`, `page`, `size`. Response `200` page of BookingResponse.

### GET /api/bookings/{id}

Access: the student or mentor of the booking, and Admin (read only).

Responses: `200` BookingResponse, `401`, `403` not a party to this booking, `404`.

### POST /api/bookings/{id}/cancel

Only `PENDING` or `CONFIRMED` bookings can be cancelled. The slot becomes `AVAILABLE` again and the session (if any) becomes `CANCELLED`. The money is not returned automatically. For a paid booking the student asks for a refund with `POST /api/refunds` (Secondary, see `06-payment-service.md`). Access: the student or mentor of the booking.

```json
{ "reason": "I have an exam that day" }
```

`reason` is optional, up to 500 characters. Responses: `200` BookingResponse with status `CANCELLED`, `400`, `401`, `403`, `404`, `409` booking is not cancellable.

## Sessions

### GET /api/bookings/{id}/session

Access: the student or mentor of the booking. Responses: `200` SessionResponse, `401`, `403`, `404` booking not found or not paid yet.

### POST /api/bookings/{id}/session/start

Allowed from 15 minutes before `scheduledStart` until `scheduledEnd`. The booking must be `CONFIRMED`. Access: the mentor of the booking. No body.

Response `200` SessionResponse with status `IN_PROGRESS` and `actualStart` set. Also `401`, `403`, `404`, `409` too early, too late, booking not CONFIRMED, or session already started.

### POST /api/bookings/{id}/session/complete

Access: the mentor of the booking. No body.

Response `200` SessionResponse with status `COMPLETED` and `actualEnd` set. The booking becomes `COMPLETED`. Also `401`, `403`, `404`, `409` session is not IN_PROGRESS.

## Reviews

### POST /api/bookings/{id}/review

Access: the student of the booking.

```json
{ "rating": 5, "comment": "Very helpful session" }
```

| Field | Rule |
|---|---|
| `rating` | required, 1 to 5 |
| `comment` | optional, up to 1000 characters |

After saving, the service calls mentor-service `POST /internal/mentors/{id}/rating`. Responses: `201` ReviewResponse, `400`, `401`, `403`, `404`, `409` booking is not COMPLETED or already reviewed.

### GET /api/reviews

Reviews of one mentor, newest first. Access: Any.

Query: `mentorId` (required), `page`, `size`. Responses: `200` page of ReviewResponse, `400` missing `mentorId`, `401`.

## Internal endpoints

Called only by other services. Not for the frontend.

### GET /internal/bookings/{id}

Used by payment-service before creating a checkout. Response `200`:

```json
{
  "id": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "studentId": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "status": "PENDING",
  "priceCents": 5000,
  "currency": "USD",
  "expiresAt": "2026-11-01T10:15:00.000Z"
}
```

Responses: `200`, `404`.

### PUT /internal/bookings/{id}/confirm

Used by payment-service after a verified payment. `PENDING` to `CONFIRMED`, slot `HELD` to `BOOKED`, session created, `expiresAt` cleared. Safe to call twice: an already `CONFIRMED` booking returns `200` again. No body.

Response `200`:

```json
{ "id": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90", "status": "CONFIRMED" }
```

Responses: `200`, `404`, `409` booking was cancelled or expired before the payment arrived.

### PUT /internal/bookings/{id}/payment-failed

`PENDING` to `CANCELLED` with reason `Payment failed`, slot back to `AVAILABLE`. Safe to call twice. No body.

Response `200`:

```json
{ "id": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90", "status": "CANCELLED" }
```

Responses: `200`, `404`.