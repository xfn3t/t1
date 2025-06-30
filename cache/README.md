# Metrics & Cache

## Технологии

* Java 17
* Spring Boot 3.1.x
* Spring Data JPA (Hibernate)
* Spring AOP
* Spring Validation
* MapStruct 1.5.x
* Lombok 1.18.x
* Liquibase 4.x (YAML)
* PostgreSQL 15
* Docker & Docker Compose

## Сборка и запуск

### Через Docker Compose

```bash
docker-compose up -d
```

## Endpoints

### Пользователи

| Метод  | URL                | Описание                                 | Пример запроса                                                                                                                                           |
| ------ | ------------------ | ---------------------------------------- | -------------------------------------------------------------------------------------------------------------------------------------------------------- |
| GET    | `/api/users`       | Список всех пользователей                | `curl -X GET http://localhost:8080/api/users`                                                                                                            |
| GET    | `/api/users/{id}`  | Получить пользователя по ID (кэшируется) | 1. `curl -X GET http://localhost:8080/api/users/1` – cache MISS + SQL SELECT<br>2. `curl -X GET http://localhost:8080/api/users/1` – cache HIT (без SQL) |
| POST   | `/api/users`       | Создать пользователя                     | `curl -X POST http://localhost:8080/api/users -H "Content-Type: application/json" -d '{"username":"david","email":"david@example.com"}'`                 |
| PUT    | `/api/users/{id}`  | Обновить пользователя по ID              | `curl -X PUT http://localhost:8080/api/users/4 -H "Content-Type: application/json" -d '{"username":"david2","email":"david2@example.com"}'`              |
| DELETE | `/api/users/{id}`  | Удалить пользователя по ID               | `curl -X DELETE http://localhost:8080/api/users/4`                                                                                                       |
| POST   | `/api/users/heavy` | «Тяжёлая» операция (логируется @Metric)  | `curl -X POST http://localhost:8080/api/users/heavy`                                                                                                     |

* **GET /api/users/{id}**:

    1. При первом обращении – `CacheAspect` выводит «Cache MISS», Hibernate делает `SELECT`.
    2. Повторно в течение 60 с – «Cache HIT», повторного SQL нет.
* **GET /api/users/heavy**:
  Метод засыпает на 600 мс → порог 500 мс превышен → `MetricAspect` сохраняет запись в `time_limit_exceed_log`.


## Ошибки и валидация

* **400 Bad Request** – если `UserRequestDto` не прошёл валидацию (`@NotBlank`, `@Email`).
* **404 Not Found** – если пользователь с указанным ID не найден.

Пример 404:

```json
{
  "timestamp":"2025-05-30T12:00:00.000",
  "status":404,
  "error":"Not Found",
  "message":"User with id=999 not found",
  "path":"/api/users/999"
}
```

Пример 400:

```json
{
  "timestamp":"2025-05-30T12:00:00.000",
  "status":400,
  "error":"Bad Request",
  "message":"Validation failed",
  "fieldErrors":{
    "email":"Email невалидный"
  },
  "path":"/api/users"
}
```

## Проверка работы кэша и метрик

1. **Кэш:**

   ```bash
   curl -i http://localhost:8080/api/users/1   # Cache MISS
   curl -i http://localhost:8080/api/users/1   # Cache HIT
   ```

2. **Метрика:**

   ```bash
   curl -i -X POST http://localhost:8080/api/users/heavy
   ```

   — в логах увидите, что метод занял 600 мс (> 500) и запись сохранилась в `time_limit_exceed_log`.


## Таблицы в БД

* **users** (создаётся Liquibase)

  ```sql
  CREATE TABLE users (
    id BIGSERIAL PRIMARY KEY,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL
  );
  ```

  После миграций вставлены строки:

  ```
  ('alice','alice@example.com'),
  ('bob','bob@example.com'),
  ('charlie','charlie@example.com')
  ```

* **time\_limit\_exceed\_log** (создаётся Liquibase)

  ```sql
  CREATE TABLE time_limit_exceed_log (
    id BIGSERIAL PRIMARY KEY,
    class_name VARCHAR(255) NOT NULL,
    method_name VARCHAR(255) NOT NULL,
    execution_time_ms BIGINT NOT NULL,
    exceeded_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL
  );
  ```


## Команды

```bash
# Получить список пользователей
curl http://localhost:8080/api/users

# Получить одного с кэшем
curl http://localhost:8080/api/users/1

# Создать пользователя
curl -X POST http://localhost:8080/api/users \
  -H "Content-Type: application/json" \
  -d '{"username":"david","email":"david@example.com"}'

# Обновить пользователя
curl -X PUT http://localhost:8080/api/users/4 \
  -H "Content-Type: application/json" \
  -d '{"username":"david2","email":"david2@example.com"}'

# Удалить пользователя
curl -X DELETE http://localhost:8080/api/users/4

# Выполнить “тяжёлую” операцию
curl -X POST http://localhost:8080/api/users/heavy
```

---

## Логирование

* **CacheAspect** (уровень DEBUG):

    * Cache MISS: `Cache MISS ru.t1.homework.users.service.UserService.getById[1]`
    * Cache HIT: `Cache HIT ru.t1.homework.users.service.UserService.getById[1]`

* **MetricAspect** (уровень INFO/DEBUG):

    * При превышении порога: `Method UserService.someHeavyOperation() took 600ms (>500)`
    * При нормальном времени: `Method UserService.someHeavyOperation() took 450ms (<=500)`

* **Hibernate.SQL** (уровень DEBUG) показывает реальные SQL запросы.

