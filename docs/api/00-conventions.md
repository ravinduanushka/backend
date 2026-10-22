# API Conventions

Applies to every service.

## Headers

| Header | Value | When |
|---|---|---|
| `Authorization` | `Bearer <JWT>` | Every endpoint except Public ones and the payment webhook |
| `Content-Type` | `application/json` | Requests with a body |

## Data formats

- JSON field names are camelCase (`firstName`). Database columns stay snake_case
- Ids are UUID strings
- Timestamps are ISO-8601 UTC: `2026-11-01T10:00:00.000Z`
- Dates are `YYYY-MM-DD`
- Money is an integer in cents (`priceCents`) with a `currency` field
- Enums are uppercase strings (`STUDENT`, `PENDING`)
- Fields set by the server (`id`, `createdAt`, `updatedAt`) are never accepted in a request

## Success status codes

| Code | Use |
|---|---|
| 200 | GET, PUT, or POST that does not create a row |
| 201 | POST that creates a row |
| 204 | DELETE, no body |

## Error response

Same shape from every service.

```json
{
  "timestamp": "2026-11-01T10:00:00.000Z",
  "status": 409,
  "error": "CONFLICT",
  "message": "Slot is already booked",
  "path": "/api/bookings"
}
```

Validation errors (400) add `fieldErrors`:

```json
{
  "timestamp": "2026-11-01T10:00:00.000Z",
  "status": 400,
  "error": "BAD_REQUEST",
  "message": "Validation failed",
  "path": "/api/users/register",
  "fieldErrors": [
    { "field": "firstName", "message": "must not be blank" }
  ]
}
```

## Error status codes

| Code | Meaning |
|---|---|
| 400 | Invalid body or query |
| 401 | JWT missing, expired or invalid |
| 403 | Wrong role, or account is not ACTIVE |
| 404 | Not found |
| 409 | Conflict (already booked, duplicate) |
| 500 | Unexpected error |

## Pagination

List endpoints take `page` (starts at 0) and `size` (default 20, max 100).

```json
{
  "content": [],
  "page": 0,
  "size": 20,
  "totalElements": 0,
  "totalPages": 0
}



```

## Caller identity

- The JWT `sub` claim is the user's id. On register, `users.id` and `externalAuthId` are both set to `sub`
- `me` and "own" in every service mean this id
- Mentor-service maps a user id to a mentor id through `mentors.user_id`. Other services use `GET /internal/mentors/by-user/{userId}`
- To confirm in Phase 5: Asgardeo `sub` is a UUID for every login type