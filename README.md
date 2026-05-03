# bo-taxi

Микросервисный backend сервиса такси на `Java 21 + Spring Boot`.

Состав:
- `user-service` — пассажиры, водители, JWT, Redis-кэш доступных водителей.
- `trip-service` — создание поездок, атомарное резервирование водителя, статусы, рейтинг, статистика.
- `notification-service` — очередь уведомлений в БД, обработка событий поездок и пул фоновых воркеров.
- `common-contracts` — DTO, enum'ы, AMQP-контракты и общий JWT helper.

Инфраструктура:
- `PostgreSQL`
- `Redis`
- `RabbitMQ`
- `Docker Compose`

## Запуск

```bash
docker compose up --build
```

Порты:
- `user-service`: `8081`
- `trip-service`: `8082`
- `notification-service`: `8083`
- `RabbitMQ UI`: `15672` (`guest` / `guest`)

## Схема взаимодействия

1. Пассажир регистрируется в `user-service`, затем получает JWT через `POST /api/v1/auth/token`.
2. `trip-service` при создании поездки:
   - проверяет существование пассажира через RabbitMQ;
   - резервирует свободного водителя через RabbitMQ;
   - рассчитывает цену по формуле `distance_km * fare_per_km`;
   - публикует событие смены статуса поездки.
3. `notification-service` слушает события поездок, создаёт задачи в таблице `notification_tasks` и обрабатывает их пулом из 4 потоков.

## Основные API

### User Service
- `POST /api/v1/passengers`
- `GET /api/v1/passengers/{id}`
- `POST /api/v1/drivers`
- `GET /api/v1/drivers/{id}`
- `PATCH /api/v1/drivers/{id}/status`
- `POST /api/v1/auth/token`

### Trip Service
- `POST /api/v1/trips`
- `GET /api/v1/trips/{id}`
- `GET /api/v1/trips?passenger_id=...`
- `PATCH /api/v1/trips/{id}/status`
- `PATCH /api/v1/trips/{id}/rating`
- `GET /api/v1/trips/statistics/daily?date=2026-04-26`

### Notification Service
- `POST /api/v1/notifications`
- `GET /api/v1/notifications?trip_id=...`

## Аутентификация

JWT используется во всех сервисах. Для доступа к защищённым endpoint'ам передавайте:

```http
Authorization: Bearer <token>
```

`POST /api/v1/auth/token` принимает:

```json
{
  "email": "passenger@example.com",
  "password": "secret",
  "role": "PASSENGER"
}
```

## Примечания по бизнес-логике

- Водитель при создании поездки резервируется атомарно через update по статусу `AVAILABLE -> RESERVED`.
- При `DRIVER_ACCEPTED` водитель переводится в `BUSY`.
- При `COMPLETED` или `CANCELLED` водитель возвращается в `AVAILABLE`.
- Если `distance_km` не передан, `trip-service` пытается вычислить расстояние из `origin` и `destination` в формате `latitude,longitude`.
- Воркер уведомлений повторяет задачу до `3` попыток. После этого задача остаётся в статусе `FAILED`.
