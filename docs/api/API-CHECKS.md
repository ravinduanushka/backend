# API checks

Built = code written (Claude Code). Success, 400, 401/403, 404/409 = requests run and passed (developer). Cross = checked by the other developer. `-` = not applicable.
⬜ not done, ✅ done

## user-service (8081)
| Endpoint | Built | Success | 400 | 401/403 | 404/409 | Cross |
|---|---|---|---|---|---|---|
| POST /api/users/register | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/users/me | ✅ | ✅ | - | ⬜ | ✅ | ⬜ |
| PUT /api/users/me | ✅ | ✅ | ✅ | ⬜ | ✅ | ⬜ |
| POST /api/users/me/avatar-upload-url | ⬜ | ⬜ | ⬜ | ⬜ | - | ⬜ |
| GET /api/students/me/profile | ✅ | ✅ | - | ✅ | ✅ | ⬜ |
| PUT /api/students/me/profile | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /internal/users/{id} | ✅ | ✅ | - | - | ✅ | ⬜ |
| GET /internal/users?ids= | ✅ | ✅ | ✅ | - | - | ⬜ |
## mentor-service (8082)
| Endpoint | Built | Success | 400 | 401/403 | 404/409 | Cross |
|---|---|---|---|---|---|---|
| GET /api/mentors/me | ✅ | ✅ | - | ✅ | ✅ | ⬜ |
| PUT /api/mentors/me | ✅ | ✅ | ✅ | ✅ | - | ⬜ |
| GET /api/mentors | ✅ | ⬜ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/mentors/{id} | ✅ | ✅ | - | ✅ | - | ⬜ |
| GET /api/expertise-categories | ✅ | ✅ | - | ✅ | - | ⬜ |
| POST /api/expertise-categories | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| POST /api/expertise-categories/{id}/expertises | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| PUT /api/mentors/me/expertises | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| POST /api/mentors/me/slots | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/mentors/me/slots | ✅ | ✅ | - | ✅ | ✅ | ⬜ |
| PUT /api/mentors/me/slots/{slotId} | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| DELETE /api/mentors/me/slots/{slotId} | ✅ | ✅ | - | ✅ | ✅ | ⬜ |
| GET /api/mentors/{id}/slots | ✅ | ⬜ | ✅ | ✅ | ✅ | ⬜ |
| POST /api/mentors/me/verification-upload-url | ⬜ | ⬜ | ⬜ | ⬜ | - | ⬜ |
| POST /api/mentors/me/verifications | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/mentors/me/verifications | ✅ | ✅ | - | ✅ | - | ⬜ |
| GET /api/verifications | ✅ | ✅ | ✅ | ✅ | - | ⬜ |
| PUT /api/verifications/{id} | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /internal/mentors/{id} | ✅ | ✅ | - | - | - | ⬜ |
| GET /internal/mentors/by-user/{userId} | ✅ | ✅ | - | - | - | ⬜ |
| PUT /internal/slots/{slotId}/status | ✅ | ✅ | ✅ | - | - | ⬜ |
| POST /internal/mentors/{id}/rating | ✅ | ✅ | ✅ | - | - | ⬜ |
## booking-service (8083)
| Endpoint | Built | Success | 400 | 401/403 | 404/409 | Cross |
|---|---|---|---|---|---|---|
| POST /api/bookings | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/bookings | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/bookings/{id} | ✅ | ✅ | - | ✅ | ✅ | ⬜ |
| POST /api/bookings/{id}/cancel | ✅ | ✅ | ✅ | ✅ | ✅ | ⬜ |
| GET /api/bookings/{id}/session | ⬜ | ⬜ | - | ⬜ | ⬜ | ⬜ |
| POST /api/bookings/{id}/session/start | ⬜ | ⬜ | - | ⬜ | ⬜ | ⬜ |
| POST /api/bookings/{id}/session/complete | ⬜ | ⬜ | - | ⬜ | ⬜ | ⬜ |
| POST /api/bookings/{id}/review | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| GET /api/reviews | ⬜ | ⬜ | ⬜ | ⬜ | - | ⬜ |
| GET /internal/bookings/{id} | ✅ | ✅ | - | - | - | ⬜ |
| PUT /internal/bookings/{id}/confirm | ✅ | ✅ | ✅ | - | - | ⬜ |
| PUT /internal/bookings/{id}/payment-failed | ✅ | ✅ | ✅ | - | - | ⬜ |

## payment-service (8084)
| Endpoint | Built | Success | 400 | 401/403 | 404/409 | Cross |
|---|---|---|---|---|---|---|
| POST /api/payments/checkout | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ | ⬜ |
| POST /api/webhooks/lemon-squeezy | ⬜ | ⬜ | ⬜ | ⬜ | - | ⬜ |

## notification-service (8086) and analytic-service (8087)
| Endpoint | Built | Success | 400 | 401/403 | 404/409 | Cross |
|---|---|---|---|---|---|---|
| POST /internal/notifications | ⬜ | ⬜ | ⬜ | - | - | ⬜ |
| POST /internal/audit-logs | ⬜ | ⬜ | ⬜ | - | - | ⬜ |

Secondary endpoints (refunds, history, reschedule, mentorship, chat, notifications list, recommendations, reports, admin) are added in Phase 4 from `docs/api/06` and `docs/api/07`.