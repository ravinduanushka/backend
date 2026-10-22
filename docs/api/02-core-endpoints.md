# Core Endpoints

Paths and methods only. Request and response JSON come in the next step.

Access: **Public** = no JWT. **Any** = any logged-in user. Otherwise the role named.
All endpoints need a JWT, except Public ones and the payment webhook.
Ids in paths are UUIDs. `me` means the user in the JWT.

## Path prefix per service

| Service | Port | Prefixes |
|---|---|---|
| user-service | 8081 | `/api/users`, `/api/students` |
| mentor-service | 8082 | `/api/mentors`, `/api/expertise-categories`, `/api/verifications` |
| booking-service | 8083 | `/api/bookings`, `/api/reviews` |
| payment-service | 8084 | `/api/payments`, `/api/webhooks` |

## user-service

| Method | Path | Purpose | Access | # |
|---|---|---|---|---|
| POST | `/api/users/register` | Create the user row after the first Asgardeo login (role STUDENT or MENTOR) | Any | 1 |
| GET | `/api/users/me` | Current user | Any | 1 |
| PUT | `/api/users/me` | Update name and phone | Any | 1 |
| POST | `/api/users/me/avatar-upload-url` | Get a presigned R2 URL for the profile image | Any | 9 |
| GET | `/api/students/me/profile` | View student profile | Student | 2 |
| PUT | `/api/students/me/profile` | Update student profile | Student | 2 |

## mentor-service

| Method | Path | Purpose | Access | # |
|---|---|---|---|---|
| GET | `/api/mentors/me` | View own mentor profile | Mentor | 3 |
| PUT | `/api/mentors/me` | Create or update own mentor profile | Mentor | 3 |
| GET | `/api/mentors` | Search approved mentors (query: `q`, `industry`, `expertiseId`, `minRating`, `page`, `size`) | Student | 4 |
| GET | `/api/mentors/{id}` | View one mentor | Student | 4 |
| GET | `/api/expertise-categories` | List categories with their expertises | Any | 5 |
| POST | `/api/expertise-categories` | Add a category | Admin | 5 |
| POST | `/api/expertise-categories/{id}/expertises` | Add an expertise to a category | Admin | 5 |
| PUT | `/api/mentors/me/expertises` | Replace own skills list | Mentor | 5 |
| POST | `/api/mentors/me/slots` | Create a slot | Mentor | 6 |
| GET | `/api/mentors/me/slots` | List own slots | Mentor | 6 |
| PUT | `/api/mentors/me/slots/{slotId}` | Update a slot | Mentor | 6 |
| DELETE | `/api/mentors/me/slots/{slotId}` | Delete a slot | Mentor | 6 |
| GET | `/api/mentors/{id}/slots` | List AVAILABLE slots (query: `from`, `to`) | Student | 7 |
| POST | `/api/mentors/me/verification-upload-url` | Get a presigned R2 URL for a document | Mentor | 9 |
| POST | `/api/mentors/me/verifications` | Submit an uploaded document | Mentor | 8 |
| GET | `/api/mentors/me/verifications` | List own submissions | Mentor | 8 |
| GET | `/api/verifications` | List submissions (query: `status`) | Admin | 8 |
| PUT | `/api/verifications/{id}` | Approve or reject | Admin | 8 |

## booking-service

| Method | Path | Purpose | Access | # |
|---|---|---|---|---|
| POST | `/api/bookings` | Book a slot | Student | 10 |
| GET | `/api/bookings` | List own bookings (query: `status`, `page`, `size`) | Student, Mentor | 10 |
| GET | `/api/bookings/{id}` | View one booking | Student, Mentor | 10 |
| POST | `/api/bookings/{id}/cancel` | Cancel a booking | Student, Mentor | 10 |
| GET | `/api/bookings/{id}/session` | View the session | Student, Mentor | 11 |
| POST | `/api/bookings/{id}/session/start` | Start the session | Mentor | 11 |
| POST | `/api/bookings/{id}/session/complete` | Complete the session | Mentor | 11 |
| POST | `/api/bookings/{id}/review` | Review a COMPLETED booking | Student | 12 |
| GET | `/api/reviews` | List a mentor's reviews (query: `mentorId`, `page`, `size`) | Any | 12 |

## payment-service

| Method | Path | Purpose | Access | # |
|---|---|---|---|---|
| POST | `/api/payments/checkout` | Create a Lemon Squeezy checkout for a booking | Student | 13 |
| POST | `/api/webhooks/lemon-squeezy` | Payment result (signature verified, no JWT) | Lemon Squeezy | 14 |

## Internal endpoints

Called only by other services through Feign. Not for the frontend.

| Method | Path | Called by | Purpose |
|---|---|---|---|
| GET | `/internal/users/{id}` | booking | Check a user exists and is ACTIVE |
| GET | `/internal/mentors/{id}` | booking | Check a mentor is APPROVED, get the rate |
| PUT | `/internal/slots/{slotId}/status` | booking | Set slot to HELD, BOOKED or AVAILABLE |
| POST | `/internal/mentors/{id}/rating` | booking | Update the mentor's average after a review |
| PUT | `/internal/bookings/{id}/confirm` | payment | Move a booking from PENDING to CONFIRMED |
| PUT | `/internal/bookings/{id}/payment-failed` | payment | Release the booking and slot |