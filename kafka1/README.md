**Описание проекта**
Это Spring Boot приложение демонстрирует два аспекта:

1. **@Metric** – измеряет время выполнения метода, если время превышает порог (500 ms), то пытается отправить сообщение в Kafka; при недоступности Kafka сохраняет запись в таблицу `time_limit_exceed_log`.
2. **@LogDatasourceError** – перехватывает любые исключения из методов, работающих с БД (называемых «Data Source»), пытается отправить сообщение об ошибке в Kafka; при недоступности Kafka сохраняет запись в таблицу `data_source_error_log`.

В дополнение к ним реализован простой CRUD для сущности `User` с кэшированием (`@Cached`) и HTTP‑эндпойнт `/api/users/heavy`, который эмулирует «долгую» операцию (> 500 ms) и помечен `@Metric`.

## Структура

* **Controller**:

    * `GET /api/users` – получить всех пользователей
    * `GET /api/users/{id}` – получить пользователя по ID (в методе `@LogDatasourceError`, т. к. обращается в репозиторий)
    * `POST /api/users?username=…&email=…` – создать пользователя
    * `PUT /api/users/{id}` – обновить пользователя
    * `DELETE /api/users/{id}` – удалить пользователя (также `@LogDatasourceError`)
    * `POST /api/users/heavy` – эмулированная «тяжёлая» операция (`@Metric`)

* **Service**:

    * `UserService#getById`, `create`, `update`, `delete` – помечены `@LogDatasourceError`, если метод бросит `UserNotFoundException` или любая другая ошибка БД, Aspect отправит данные в Kafka или в таблицу `data_source_error_log`.
    * `UserService#someHeavyOperation()` – помечен `@Metric`, спит 600 ms, Aspect замеряет время, и если > 500 ms, отправляет данные в Kafka (или в `time_limit_exceed_log`).
    * Остальные CRUD‑методы обычные.

* **Aspects**:

    * `DataSourceErrorAspect` ловит `@LogDatasourceError`
    * `MetricAspect` ловит `@Metric`
    * Оба используют `ErrorPublisher` для отсылки в Kafka и fallback в БД.

* **ErrorPublisher**:

    * отправляет JSON‑payload в топик `t1_demo_metrics`, устанавливает заголовок `ERROR_TYPE` (METRICS или DATA\_SOURCE).
    * при ошибке отправки (блокируется более 500 ms) кидает в catch и вызывает `fallbackSaver`, который сохраняет в соответствующие таблицы.

* **Liquibase**:

    * `db/changelog/db.changelog-master.yaml` создаёт таблицы: `users`, `time_limit_exceed_log`, `data_source_error_log` и вставляет 3 тестовых пользователя (`alice`, `bob`, `charlie`).

## Настройка `application.yml`

```yaml
spring:
  datasource:
    url: jdbc:postgresql://db:5432/kafka_example
    username: kafkaUser
    password: kafkaPassword
    driver-class-name: org.postgresql.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    properties:
      hibernate:
        format_sql: true
        show_sql: true

  kafka:
    bootstrap-servers: kafka:9092
    producer:
      retries: 3
      max.block.ms: 500      # если Kafka недоступна более 500 ms, бросит исключение
      acks: all
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

metrics:
  time-limit-ms: 500        # порог для @Metric

cache:
  default-ttl-seconds: 60   # кэшовые TTL

logging:
  level:
    ru.t1.homework.cache.*: DEBUG
    ru.t1.homework.metrics.aspect.MetricAspect: DEBUG
    ru.t1.homework.datasource.aspect.DataSourceErrorAspect: DEBUG
    ru.t1.homework.service.ErrorPublisher: DEBUG
    ru.t1.homework.users.controller: DEBUG
    ru.t1.homework.users.service: DEBUG
    org.hibernate.SQL: DEBUG

liquibase:
  change-log: classpath:db/changelog/db.changelog-master.yaml
```

* **`producer.max.block.ms = 500`** и `retries = 3` гарантируют, что при недоступности Kafka `send(...)` бросит исключение через \~500 ms и fallback сохранится в БД.


## Запуск и тестирование

1. **Соберите и запустите** все сервисы:

   ```bash
   docker-compose down -v
   docker-compose up --build -d
   ```
2. Дождитесь, пока все контейнеры будут в состоянии **Up** и **Healthy** (для `db`):

   ```bash
   docker ps
   ```

### A. Проверка DataSourceError -> Kafka или БД

#### 1.1. Запустите консольного потребителя Kafka (в одном терминале):

```bash
docker exec -it kafka-example-kafka bash -c "kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic t1_demo_metrics \
  --from-beginning"
```

Оставьте окно открытым для вывода сообщений.

#### 1.2. В другом терминале сделайте запрос, приводящий к ошибке (Kafka запущена):

```bash
curl -i http://localhost:8080/api/users/9999
```

* Ожидаемый ответ:

  ```http
  HTTP/1.1 404
  Content-Type: application/json
  ...
  {"timestamp":"...","status":404,"error":"Not Found","message":"User with id=9999 not found","path":"/api/users/9999"}
  ```
* **В логах Spring Boot** должно появиться:

  ```
  Исключение в DATA SOURCE методе UserService.getById: User with id=9999 not found
  Не удалось отправить? (DATA_SOURCE)? ← если Kafka упала
  Сообщение (DATA_SOURCE) успешно отправлено: {"className":"UserService","methodName":"getById","exceptionMessage":"User with id=9999 not found"}
  ```
* **В окне консольного потребителя Kafka** появится:

  ```json
  {"className":"UserService","methodName":"getById","exceptionMessage":"User with id=9999 not found"}
  ```

#### 1.3. Симуляция падения Kafka -> fallback в БД

1. Остановите Kafka:

   ```bash
   docker stop kafka-example-kafka
   ```
2. Снова вызовите ошибочный запрос:

   ```bash
   curl -i http://localhost:8080/api/users/9999
   ```
3. **Проверьте таблицу `data_source_error_log`** (в новом терминале):

   ```bash
   docker exec -it kafka-example-db psql -U kafkaUser -d kafka_example -c \
     "SELECT * FROM data_source_error_log ORDER BY id DESC LIMIT 1;"
   ```

   В таблице должна появиться запись с соответствующими полями.

### B. Проверка MetricError → Kafka или БД

#### 2.1. Перезапустите Kafka (если остановлена):

```bash
docker start kafka-example-kafka
```

Подождите 5–10 секунд, пока Kafka снова будет «Up».

#### 2.2. Запустите консольного потребителя Kafka (новое окно или перезапустить):

```bash
docker exec -it kafka-example-kafka bash -c "kafka-console-consumer \
  --bootstrap-server localhost:9092 \
  --topic t1_demo_metrics \
  --from-beginning"
```

#### 2.3. В другом терминале сделайте «тяжёлый» запрос (Kafka запущена):

```bash
curl -i -X POST http://localhost:8080/api/users/heavy
```

* Метод `someHeavyOperation()` засыпает 600 ms, порог 500 ms → Aspect вызывает `publishMetricError`.
* **В логах Spring Boot** увидите:

  ```
  [MetricAspect] Duration 600ms > 500ms, calling ErrorPublisher
  [ErrorPublisher] Сообщение (METRICS) успешно отправлено: {"className":"UserService","methodName":"someHeavyOperation","durationMs":600}
  ```
* **В окне консольного потребителя Kafka** появится:

  ```json
  {"className":"UserService","methodName":"someHeavyOperation","durationMs":600}
  ```

#### 2.4. Симуляция падения Kafka → fallback в БД

1. Остановите Kafka:

   ```bash
   docker stop kafka-example-kafka
   ```
2. Снова сделайте «тяжёлый» запрос:

   ```bash
   curl -i -X POST http://localhost:8080/api/users/heavy
   ```
3. **Проверьте таблицу `time_limit_exceed_log`**:

   ```bash
   docker exec -it kafka-example-db psql -U kafkaUser -d kafka_example -c \
     "SELECT id, class_name, method_name, execution_time_ms, error_message, logged_at FROM time_limit_exceed_log ORDER BY id DESC LIMIT 1;"
   ```

   Там должна быть запись:

   ```
    id | class_name  |    method_name      | execution_time_ms |         error_message          |        logged_at
   ----+-------------+---------------------+-------------------+--------------------------------+-------------------------
    1  | UserService | someHeavyOperation  |               600 | Kafka unreachable или Timeout  | 2025‑05‑31 XX:XX:XX
   ```

## C. Проверка кэша (`@Cached`)

1. **Кэширование работает в методе** `UserService.getById(Long id)`.
2. **Первый запрос**:

   ```bash
   curl -i http://localhost:8080/api/users/1
   ```

   – достаёт из БД, записывает в кэш, возвращает JSON.
3. **Второй запрос** (в течение TTL = 60 сек):

   ```bash
   curl -i http://localhost:8080/api/users/1
   ```

   – данные берутся из кэша, SQL-запроса в логах Hibernate не будет (потому что кэш).
4. **Через 60 сек кэш сбросится**, аналогично через `CacheService.evictExpiredEntries()`.
5. **Если хотите убедиться в логировании**:

    * В `logging.level.ru.t1.homework.cache.service.CacheService: DEBUG` будет видно, когда кэш очищен, а `get` поможет понять, когда берутся данные из кэша.

## Полезные команды

```bash
# Запуск всех сервисов
docker-compose up --build -d

# Проверка статусов контейнеров
docker ps

# Остановка Kafka (эмуляция недоступности)
docker stop kafka-example-kafka

# Запуск консольного потребителя Kafka (читаем топик t1_demo_metrics)
docker exec -it kafka-example-kafka bash -c "kafka-console-consumer --bootstrap-server localhost:9092 --topic t1_demo_metrics --from-beginning"

# Просмотр содержимого таблицы time_limit_exceed_log
docker exec -it kafka-example-db psql -U kafkaUser -d kafka_example -c "SELECT * FROM time_limit_exceed_log;"

# Просмотр содержимого таблицы data_source_error_log
docker exec -it kafka-example-db psql -U kafkaUser -d kafka_example -c "SELECT * FROM data_source_error_log;"

# Примеры HTTP-запросов для тестирования:
# 1. Получить пользователя (кэширование)
curl -i http://localhost:8080/api/users/1

# 2. Получить несуществующего пользователя (triggers @LogDatasourceError)
curl -i http://localhost:8080/api/users/9999

# 3. Удалить несуществующего пользователя (тоже @LogDatasourceError)
curl -i -X DELETE http://localhost:8080/api/users/9999

# 4. Создать пользователя
curl -i -X POST "http://localhost:8080/api/users?username=john&email=john@example.com"

# 5. «Тяжёлый» запрос (triggers @Metric)
curl -i -X POST http://localhost:8080/api/users/heavy
```
