# user-service API

Base URL (dev): `http://localhost:8081`. See `00-conventions.md` for headers, errors and formats.

`email` comes from the JWT `email` claim. The new user's `id` and `externalAuthId` are both set to the JWT `sub` claim. None of them are accepted in the body.

## UserResponse

```json
{
  "id": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "email": "kamal@example.com",
  "firstName": "Kamal",
  "lastName": "Perera",
  "phone": "+94771234567",
  "profileImageKey": null,
  "role": "STUDENT",
  "accountStatus": "ACTIVE",
  "createdAt": "2026-11-01T10:00:00.000Z",
  "updatedAt": "2026-11-01T10:00:00.000Z"
}
```

## POST /api/users/register

Create the user after the first Asgardeo login. Also creates an empty student profile when the role is STUDENT. Access: Any logged-in user.

Request:

```json
{
  "role": "STUDENT",
  "firstName": "Kamal",
  "lastName": "Perera",
  "phone": "+94771234567"
}
```

| Field | Rule |
|---|---|
| `role` | required, `STUDENT` or `MENTOR` (ADMIN is rejected) |
| `firstName`, `lastName` | required, 1 to 100 characters |
| `phone` | optional, 7 to 15 digits, may start with `+` |

Responses: `201` UserResponse, `400` validation error, `401`, `409` user already registered.

## GET /api/users/me

Current user. Access: Any.

Responses: `200` UserResponse, `401`, `403` account not ACTIVE, `404` not registered yet (the frontend then calls register).

## PUT /api/users/me

Update the current user. Access: Any.

Request:

```json
{
  "firstName": "Kamal",
  "lastName": "Perera",
  "phone": "+94771234567",
  "profileImageKey": "avatars/5f1c0b7e/9d2e.png"
}
```

| Field | Rule |
|---|---|
| `firstName`, `lastName` | required, 1 to 100 characters |
| `phone`, `profileImageKey` | optional |

Responses: `200` UserResponse, `400`, `401`, `403`.

## POST /api/users/me/avatar-upload-url

Get a presigned Cloudflare R2 URL for the profile image. Access: Any.

Request:

```json
{ "contentType": "image/png" }
```

Allowed `contentType`: `image/jpeg`, `image/png`, `image/webp`.

Response `200`:

```json
{
  "uploadUrl": "https://<account>.r2.cloudflarestorage.com/...",
  "key": "avatars/5f1c0b7e/9d2e.png",
  "expiresInSeconds": 300
}
```

Flow: the client sends the file with `PUT` to `uploadUrl`, then calls `PUT /api/users/me` with `profileImageKey` set to `key`.

Responses: `200`, `400` unsupported type, `401`.

## GET /api/students/me/profile

Access: Student.

Response `200` StudentProfileResponse:

```json
{
  "id": "a3b8d1c4-7f20-4e31-8c4d-2b9f6e7a1d55",
  "userId": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "university": "University of Ruhuna",
  "faculty": "Engineering",
  "degree": "Computer Engineering",
  "academicYear": 3,
  "bio": "Interested in backend development",
  "interests": "Java, Cloud",
  "updatedAt": "2026-11-01T10:00:00.000Z"
}
```

Responses: `200`, `401`, `403` not a STUDENT.

## PUT /api/students/me/profile

Access: Student.

Request:

```json
{
  "university": "University of Ruhuna",
  "faculty": "Engineering",
  "degree": "Computer Engineering",
  "academicYear": 3,
  "bio": "Interested in backend development",
  "interests": "Java, Cloud"
}
```

| Field | Rule |
|---|---|
| `academicYear` | optional, 1 to 10 |
| all others | optional text |

Responses: `200` StudentProfileResponse, `400`, `401`, `403`.

## GET /internal/users/{id}

Called by booking-service. Not for the frontend.

Response `200`:

```json
{
  "id": "5f1c0b7e-2d1a-4c55-9a77-1c2f6f3a9b10",
  "role": "STUDENT",
  "accountStatus": "ACTIVE"
}
```

Responses: `200`, `404`.


## GET /internal/users?ids=id1,id2

Called by mentor-service to get names for mentor lists. Not for the frontend. Maximum 100 ids.

Response `200`:

```json
[
  {
    "id": "8d2e7a10-5b3c-4d6e-9f01-2a3b4c5d6e7f",
    "firstName": "Nimal",
    "lastName": "Silva",
    "profileImageKey": "avatars/8d2e/1f3a.png"
  }
]
```

Ids that do not exist are left out. Responses: `200`, `400` more than 100 ids.