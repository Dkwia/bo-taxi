# OpenAPI Swagger test scenarios

Start the stack:

```bash
docker compose up --build
```

Swagger UI:

- User Service: `http://localhost:8081/swagger-ui.html`
- Trip Service: `http://localhost:8082/swagger-ui.html`
- Notification Service: `http://localhost:8083/swagger-ui.html`

OpenAPI JSON:

- User Service: `http://localhost:8081/v3/api-docs`
- Trip Service: `http://localhost:8082/v3/api-docs`
- Notification Service: `http://localhost:8083/v3/api-docs`

## Scenario 1: passenger, driver, JWT

1. In User Service, call `POST /api/v1/passengers`.
2. In User Service, call `POST /api/v1/drivers`.
3. In User Service, call `POST /api/v1/auth/token` with passenger credentials and save the token.
4. Click `Authorize` in Trip Service Swagger UI and enter `Bearer <passenger-token>`.
5. In User Service, call `POST /api/v1/auth/token` with driver credentials and save the token.
6. Click `Authorize` in User Service Swagger UI and enter `Bearer <driver-token>`.
7. In User Service, call `PATCH /api/v1/drivers/{id}/status` with `AVAILABLE`.

## Scenario 2: create trip and verify atomic driver assignment

1. In Trip Service, call `POST /api/v1/trips` with the passenger token:

```json
{
  "passenger_id": 1,
  "origin": "Office",
  "destination": "Airport",
  "distance_km": 12.5
}
```

2. Verify the response has a `driver_id`, status, and price.
3. Send two create-trip requests quickly while only one driver is `AVAILABLE`.
4. Expected result: one request reserves the driver; the other returns a conflict when no other driver is available.

## Scenario 3: trip lifecycle and notifications

1. In Trip Service, authorize with the driver token and call `PATCH /api/v1/trips/{id}/status`:

```json
{
  "status": "DRIVER_ACCEPTED"
}
```

2. Call the same endpoint with `IN_PROGRESS`.
3. Call the same endpoint with `COMPLETED`.
4. In Notification Service, authorize with any valid JWT and call `GET /api/v1/notifications?trip_id=<trip-id>`.
5. Expected result: notification tasks exist for trip status changes and eventually move to `SENT` or `FAILED`.

## Scenario 4: rating, history, and statistics

1. In Trip Service, authorize with the passenger token.
2. Call `PATCH /api/v1/trips/{id}/rating`:

```json
{
  "rating": 5
}
```

3. Call `GET /api/v1/trips?passenger_id=<passenger-id>` to verify trip history.
4. Call `GET /api/v1/trips/statistics/daily?date=2026-05-04`.
5. Expected result: statistics include trip count and average price for the selected day.

## Scenario 5: manual notification queue task

1. In Notification Service, call `POST /api/v1/notifications`:

```json
{
  "trip_id": 1,
  "recipient_type": "PASSENGER",
  "recipient_id": 1,
  "message": "Manual queue test from Swagger UI."
}
```

2. Call `GET /api/v1/notifications?trip_id=1`.
3. Expected result: the worker pool claims the task once and updates its status after processing.
