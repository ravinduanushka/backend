# payment-service API

Base URL (dev): `http://localhost:8084`. See `00-conventions.md` for headers, errors and formats.

Core has two endpoints. The frontend checks the result of a payment with `GET /api/bookings/{id}`, which changes from `PENDING` to `CONFIRMED`.

## POST /api/payments/checkout

Create a Lemon Squeezy checkout for a booking. Access: Student.

```json
{ "bookingId": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90" }
```

Steps:

1. Call booking-service `GET /internal/bookings/{id}`
2. Check the caller is the booking's student, the status is `PENDING` and `expiresAt` is in the future
3. If a `PENDING` payment already exists for this booking, return it (same `checkoutUrl`)
4. Otherwise insert a `payments` row (`PENDING`, amount and currency copied from the booking) and create the checkout on Lemon Squeezy with custom data `payment_id` and `booking_id`

Response (`201` for a new payment, `200` when an existing one is returned):

```json
{
  "paymentId": "7c6b5a49-3827-4165-9403-f2e1d0c9b8a7",
  "bookingId": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90",
  "amountCents": 5000,
  "currency": "USD",
  "status": "PENDING",
  "checkoutUrl": "https://<store>.lemonsqueezy.com/checkout/custom/...",
  "expiresAt": "2026-11-01T10:15:00.000Z"
}
```

| Code | When |
|---|---|
| 400 | `bookingId` missing or not a UUID |
| 401 | JWT missing or invalid |
| 403 | caller is not a STUDENT, or is not the student of this booking |
| 404 | booking not found |
| 409 | booking is not `PENDING`, already paid, or expired |
| 500 | Lemon Squeezy could not create the checkout (the payment is marked `FAILED`, the booking stays `PENDING` so the student can try again) |

## POST /api/webhooks/lemon-squeezy

Lemon Squeezy calls this after a payment. Access: Public (no JWT). The request is trusted only after the signature check.

Headers:

| Header | Value |
|---|---|
| `X-Signature` | HMAC-SHA256 (hex) of the raw request body, made with the webhook signing secret |
| `X-Event-Name` | event name, for example `order_created` |
| `Content-Type` | `application/json` |

Body (only the fields we use):

```json
{
  "meta": {
    "event_name": "order_created",
    "custom_data": {
      "payment_id": "7c6b5a49-3827-4165-9403-f2e1d0c9b8a7",
      "booking_id": "d4e5f6a7-0b1c-4d2e-8f3a-4b5c6d7e8f90"
    }
  },
  "data": {
    "type": "orders",
    "id": "2049387",
    "attributes": {
      "status": "paid",
      "total": 5000,
      "currency": "USD"
    }
  }
}
```

`total` is in cents. `data.id` is saved as `transaction_id`.

Processing order:

1. Verify `X-Signature` on the raw body (constant-time compare). If it is wrong, stop with `401` and do nothing else
2. If the event is not `order_created` with status `paid`, answer `200` and ignore it
3. Find the payment by `custom_data.payment_id`. Check `total` and `currency` match the payment row
4. In one transaction set `transaction_id`, `status` = `PAID` and `paid_at`. The unique `transaction_id` stops duplicates
5. Call booking-service `PUT /internal/bookings/{id}/confirm`
6. Answer `200`

A repeated delivery repeats step 5 only. The confirm call is safe to repeat, so if booking-service was down the first time, the next delivery finishes the job.

Response `200`:

```json
{ "received": true }
```

| Code | When |
|---|---|
| 200 | processed, ignored event, or duplicate delivery |
| 400 | body is not valid or `custom_data` is missing |
| 401 | signature is wrong |
| 404 | payment not found |
| 409 | amount or currency does not match (the payment is not marked paid, written to the log for review) |
| 500 | booking-service could not be reached (Lemon Squeezy retries later) |

If the confirm call returns `409` (the booking expired or was cancelled before the money arrived), the payment stays `PAID` and `200` is returned. Returning the money is a Secondary feature (refunds).

## Settings (Phase 5)

| Environment variable | Meaning |
|---|---|
| `LEMON_SQUEEZY_API_KEY` | API key (test mode first) |
| `LEMON_SQUEEZY_STORE_ID` | store id |
| `LEMON_SQUEEZY_VARIANT_ID` | product variant used for session payments |
| `LEMON_SQUEEZY_SIGNING_SECRET` | secret used to verify `X-Signature` |

Never commit these. To test the webhook on a laptop, the URL must be public (a tunnel such as Cloudflare Tunnel or ngrok).


## Refunds (Secondary)

A refund returns the money of a `PAID` payment. The status values are `PENDING`, `COMPLETED`, `FAILED` and `REJECTED`.

### POST /api/refunds

Ask for a refund. Access: Student.

```json
{ "paymentId": "7c6b5a49-3827-4165-9403-f2e1d0c9b8a7", "reason": "Mentor did not join the session" }
```

Creates a refund with status `PENDING`. The amount is the full payment amount.

Response `201`:

```json
{
  "id": "e1f2a3b4-5c6d-4e7f-8a9b-0c1d2e3f4a5b",
  "paymentId": "7c6b5a49-3827-4165-9403-f2e1d0c9b8a7",
  "amountCents": 5000,
  "currency": "USD",
  "reason": "Mentor did not join the session",
  "adminNote": null,
  "status": "PENDING",
  "createdAt": "2026-11-02T08:00:00.000Z",
  "processedAt": null
}
```

| Code | When |
|---|---|
| 400 | `paymentId` missing or not a UUID |
| 403 | caller is not the student of this payment |
| 404 | payment not found |
| 409 | payment is not `PAID`, or a `PENDING` or `COMPLETED` refund already exists for it |

### GET /api/refunds

List refunds. Access: Student (own refunds), Admin (all). Query: `status`, `page`, `size`. Response is a page of the object above.

### PUT /api/refunds/{id}

Decide a refund. Access: Admin. The refund must be `PENDING`.

```json
{ "status": "APPROVED" }
```

or

```json
{ "status": "REJECTED", "adminNote": "Session was completed" }
```

`APPROVED` is an action, not a stored status:

1. `APPROVED`: call Lemon Squeezy to refund the order (`transaction_id`)
2. If it works, refund status becomes `COMPLETED`, `processed_at` is set and the payment status becomes `REFUNDED`
3. If it fails, refund status becomes `FAILED` and the payment stays `PAID`
4. `REJECTED`: refund status becomes `REJECTED`, `admin_note` is saved and the payment stays `PAID`

Response `200`: the refund object with the new status.

| Code | When |
|---|---|
| 400 | `status` is not `APPROVED` or `REJECTED` |
| 403 | caller is not ADMIN |
| 404 | refund not found |
| 409 | refund is not `PENDING` |