# mentor-service API

Base URL (dev): `http://localhost:8082`. See `00-conventions.md` for headers, errors, pagination and formats.

The mentors table has no name column. Names and profile images are fetched from user-service with `GET /internal/users?ids=...`.

## Shared shapes

MentorSummary (search results):

```json
{
  "id": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "firstName": "Nimal",
  "lastName": "Silva",
  "profileImageKey": "avatars/8d2e/1f3a.png",
  "title": "Senior Software Engineer",
  "organization": "WSO2",
  "industry": "Software",
  "experienceYears": 8,
  "hourlyRateCents": 5000,
  "currency": "USD",
  "ratingAvg": 4.6,
  "ratingCount": 12,
  "expertises": [{ "id": "e1...", "name": "Java" }]
}
```

MentorResponse (detail and own profile): all MentorSummary fields plus:

```json
{
  "userId": "8d2e7a10-5b3c-4d6e-9f01-2a3b4c5d6e7f",
  "bio": "I help students move into backend roles.",
  "verificationStatus": "APPROVED",
  "expertises": [
    {
      "expertiseId": "e1a2b3c4-1111-4222-8333-444455556666",
      "name": "Java",
      "categoryName": "Programming",
      "proficiency": "EXPERT"
    }
  ],
  "createdAt": "2026-11-01T10:00:00.000Z",
  "updatedAt": "2026-11-01T10:00:00.000Z"
}
```

SlotResponse:

```json
{
  "id": "b7c8d9e0-1234-4abc-8def-0123456789ab",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "startTime": "2026-11-05T09:00:00.000Z",
  "endTime": "2026-11-05T10:00:00.000Z",
  "timezone": "Asia/Colombo",
  "status": "AVAILABLE"
}
```

VerificationResponse:

```json
{
  "id": "f0e1d2c3-b4a5-4968-8776-655443322110",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "documentKey": "verifications/c1d2e3f4/7a9b.pdf",
  "status": "PENDING",
  "reviewedBy": null,
  "reviewNote": null,
  "submittedAt": "2026-11-01T10:00:00.000Z",
  "reviewedAt": null
}
```

## Mentor profile

### GET /api/mentors/me

Access: Mentor. Responses: `200` MentorResponse, `401`, `403`, `404` profile not created yet.

### PUT /api/mentors/me

Create or update the own profile. A new profile starts with `verificationStatus` `PENDING`. Access: Mentor.

```json
{
  "title": "Senior Software Engineer",
  "organization": "WSO2",
  "industry": "Software",
  "bio": "I help students move into backend roles.",
  "experienceYears": 8,
  "hourlyRateCents": 5000,
  "currency": "USD"
}
```

| Field | Rule |
|---|---|
| `title` | required, 1 to 150 characters |
| `organization`, `industry`, `bio` | optional text |
| `experienceYears` | required, 0 or more |
| `hourlyRateCents` | required, 0 or more |
| `currency` | optional, 3 letters, default `USD` |

Responses: `201` MentorResponse (first time), `200` MentorResponse (update), `400`, `401`, `403`.

### GET /api/mentors

Search APPROVED mentors only. Access: Student.

Query: `q` (title or organization), `industry`, `expertiseId`, `minRating` (0 to 5), `page`, `size`. Sorted by `ratingAvg` descending. Search by mentor name is not supported, because names live in user-service.

Responses: `200` page of MentorSummary, `401`, `403`.

### GET /api/mentors/{id}

Access: Any logged-in user can view an APPROVED mentor. A mentor that is not APPROVED is visible only to that mentor and to ADMIN, others get 404.

Responses: `200` MentorResponse, `401`, `404`.

## Expertise

### GET /api/expertise-categories

Access: Any. Response `200`, a plain array (no paging):

```json
[
  {
    "id": "a1b2c3d4-0000-4111-8222-333344445555",
    "name": "Programming",
    "description": "Software development",
    "expertises": [{ "expertiseId": "e1a2b3c4-1111-4222-8333-444455556666", "name": "Java" }]
  }
]
```

### POST /api/expertise-categories

Access: Admin.

```json
{ "name": "Programming", "description": "Software development" }
```

`name` is required, 1 to 100 characters, unique (case-insensitive). Responses: `201` category with an empty `expertises` array, `400`, `401`, `403`, `409` name exists.

### POST /api/expertise-categories/{id}/expertises

Access: Admin.

```json
{ "name": "Java", "description": "Java and Spring Boot" }
```

`name` is required and unique inside the category. Responses: `201`:

```json
{
  "id": "e1a2b3c4-1111-4222-8333-444455556666",
  "categoryId": "a1b2c3d4-0000-4111-8222-333344445555",
  "name": "Java",
  "description": "Java and Spring Boot"
}
```

Also `400`, `401`, `403`, `404` category not found, `409` name exists.

### PUT /api/mentors/me/expertises

Replace the whole skills list. Access: Mentor.

```json
{
  "expertises": [
    { "expertiseId": "e1a2b3c4-1111-4222-8333-444455556666", "proficiency": "EXPERT" }
  ]
}
```

`proficiency` is `BEGINNER`, `INTERMEDIATE`, `ADVANCED` or `EXPERT`. Response `200`:

```json
{
  "expertises": [
    {
      "expertiseId": "e1a2b3c4-1111-4222-8333-444455556666",
      "name": "Java",
      "categoryName": "Programming",
      "proficiency": "EXPERT"
    }
  ]
}
```

Also `400` unknown id or duplicate id, `401`, `403`, `404` no mentor profile.

## Availability slots

### POST /api/mentors/me/slots

Access: Mentor.

```json
{
  "startTime": "2026-11-05T09:00:00.000Z",
  "endTime": "2026-11-05T10:00:00.000Z",
  "timezone": "Asia/Colombo"
}
```

| Field | Rule |
|---|---|
| `startTime` | required, in the future |
| `endTime` | required, after `startTime` |
| `timezone` | optional, IANA name, default `UTC` |

Responses: `201` SlotResponse, `400`, `401`, `403`, `404` no mentor profile, `409` a slot with this start already exists.

### GET /api/mentors/me/slots

Own slots. Access: Mentor. Query: `status`, `from`, `to` (ISO timestamps). No paging, use `from` and `to`. Response `200` array of SlotResponse.

### PUT /api/mentors/me/slots/{slotId}

Only slots with status `AVAILABLE` or `BLOCKED` can be changed. Access: Mentor.

```json
{
  "startTime": "2026-11-05T11:00:00.000Z",
  "endTime": "2026-11-05T12:00:00.000Z",
  "timezone": "Asia/Colombo",
  "status": "BLOCKED"
}
```

`status` is optional and may only be `AVAILABLE` or `BLOCKED`. Responses: `200` SlotResponse, `400`, `401`, `403`, `404`, `409` slot is HELD or BOOKED, or the start already exists.

### DELETE /api/mentors/me/slots/{slotId}

Access: Mentor. Responses: `204`, `401`, `403`, `404`, `409` slot is HELD or BOOKED.

### GET /api/mentors/{id}/slots

AVAILABLE future slots of one mentor. Access: Student. Query: `from`, `to` (default: now to 30 days ahead, maximum range 31 days).

Responses: `200` array of SlotResponse, `400` range too large, `401`, `403`, `404` mentor not found or not APPROVED.

## Verification

### POST /api/mentors/me/verification-upload-url

Access: Mentor.

```json
{ "contentType": "application/pdf" }
```

Allowed: `application/pdf`, `image/jpeg`, `image/png`. Response `200`:

```json
{
  "uploadUrl": "https://<account>.r2.cloudflarestorage.com/...",
  "key": "verifications/c1d2e3f4/7a9b.pdf",
  "expiresInSeconds": 300
}
```

Responses: `200`, `400`, `401`, `403`, `404` no mentor profile.

### POST /api/mentors/me/verifications

Submit an uploaded document. Access: Mentor.

```json
{ "documentKey": "verifications/c1d2e3f4/7a9b.pdf" }
```

`documentKey` must start with `verifications/{mentorId}/`. Responses: `201` VerificationResponse, `400`, `401`, `403`, `404` no mentor profile, `409` a PENDING submission already exists.

### GET /api/mentors/me/verifications

Access: Mentor. Newest first. Response `200` array of VerificationResponse.

### GET /api/verifications

Access: Admin. Query: `status`, `page`, `size`. Each item is a VerificationResponse plus a short-lived download link:

```json
{
  "id": "f0e1d2c3-b4a5-4968-8776-655443322110",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "documentKey": "verifications/c1d2e3f4/7a9b.pdf",
  "documentUrl": "https://<account>.r2.cloudflarestorage.com/...",
  "status": "PENDING",
  "reviewedBy": null,
  "reviewNote": null,
  "submittedAt": "2026-11-01T10:00:00.000Z",
  "reviewedAt": null
}
```

`documentUrl` expires after 300 seconds. Responses: `200` page, `401`, `403`.

### PUT /api/verifications/{id}

Approve or reject. Access: Admin. Also sets `reviewedBy` and `reviewedAt`, and copies the result to the mentor's `verificationStatus`.

```json
{ "status": "REJECTED", "reviewNote": "Document is not readable" }
```

| Field | Rule |
|---|---|
| `status` | required, `APPROVED` or `REJECTED` |
| `reviewNote` | required when `REJECTED` |

Responses: `200` VerificationResponse, `400`, `401`, `403`, `404`, `409` already reviewed.

## Internal endpoints

Called only by other services. Not for the frontend.

### GET /internal/mentors/{id}

Used by booking-service. Response `200`:

```json
{
  "id": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "userId": "8d2e7a10-5b3c-4d6e-9f01-2a3b4c5d6e7f",
  "verificationStatus": "APPROVED",
  "hourlyRateCents": 5000,
  "currency": "USD"
}
```

Responses: `200`, `404`.

### PUT /internal/slots/{slotId}/status

Used by booking-service. Allowed changes: `AVAILABLE` to `HELD`, `HELD` to `BOOKED`, `HELD` to `AVAILABLE`, `BOOKED` to `AVAILABLE`.

```json
{ "status": "HELD" }
```

Response `200`:

```json
{
  "id": "b7c8d9e0-1234-4abc-8def-0123456789ab",
  "mentorId": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "startTime": "2026-11-05T09:00:00.000Z",
  "endTime": "2026-11-05T10:00:00.000Z",
  "status": "HELD"
}
```

Responses: `200`, `400` unknown status, `404`, `409` change not allowed (for example, the slot is no longer AVAILABLE).

### POST /internal/mentors/{id}/rating

Used by booking-service after a review. New average = (old average x count + rating) / (count + 1).

```json
{ "rating": 5 }
```

Response `200`:

```json
{ "ratingAvg": 4.67, "ratingCount": 13 }
```

Responses: `200`, `400` rating not 1 to 5, `404`.


### GET /internal/mentors/by-user/{userId}

Used by booking-service to find the mentor id of a logged-in mentor. Response `200`:

```json
{
  "id": "c1d2e3f4-0a1b-4c2d-8e3f-4a5b6c7d8e9f",
  "verificationStatus": "APPROVED"
}
```

Responses: `200`, `404` the user has no mentor profile.