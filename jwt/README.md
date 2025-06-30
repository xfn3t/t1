# Задание 5 JWT

## Технологический стек
- Java 17
- Spring Boot 3
- Spring Kafka
- Spring Security
- JWT
- PostgreSQL
- Docker
- Docker Compose
- Maven

## Обзор системы

Этот проект представляет собой демонстрационную банковскую систему, построенную на микросервисной архитектуре с использованием:

- Spring Boot для создания микросервисов
- Kafka для обработки транзакций
- JWT для аутентификации между сервисами
- Docker для контейнеризации

Система состоит из двух основных сервисов:

1. **Основной банковский сервис (service1)** - порт 8080
    - Управление клиентами, счетами и транзакциями
    - Обработка финансовых операций
    - Взаимодействие с сервисом черного списка

2. **Сервис черного списка (service2)** - порт 8081
    - Проверка клиентов на наличие в черном списке
    - Возвращает статус клиента (OK/BLACKLISTED)

## Требования для запуска
- Docker
- Docker Compose
- PowerShell (для примеров скриптов) или любой HTTP-клиент

## Запуск системы

Запустите систему с помощью Docker Compose:

```bash
docker-compose up --build
```

Система будет доступна:

- Основной сервис: http://localhost:8080
- Сервис черного списка: http://localhost:8081

## Примеры работы со статусами счетов

### 1. Статус OPEN (Счет открыт)

```powershell
# Создаем клиента
$client = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/clients" `
    -ContentType 'application/json' -Body '{
        "firstName": "Иван",
        "middleName": "Иванович",
        "lastName": "Иванов"
    }'

# Создаем счет
$account = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/accounts" `
    -ContentType 'application/json' -Body (@{
        "clientId" = $client.id
        "initialBalance" = 10000.00
    } | ConvertTo-Json)

# Совершаем успешную транзакцию
Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/transactions" `
    -ContentType 'application/json' -Body (@{
        "accountId" = $account.accountId
        "amount" = 1000.00
    } | ConvertTo-Json)

# Проверяем статус
$accountStatus = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/accounts/$($account.id)"
Write-Host "Статус счета: $($accountStatus.status)"  # OPEN
```

### 2. Статус BLOCKED (Счет заблокирован)

```powershell
# Создаем клиента
$client = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/clients" `
    -ContentType 'application/json' -Body '{
        "firstName": "Петр",
        "middleName": "Петрович",
        "lastName": "Петров"
    }'

# Создаем счет с малым балансом
$account = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/accounts" `
    -ContentType 'application/json' -Body (@{
        "clientId" = $client.id
        "initialBalance" = 10.00
    } | ConvertTo-Json)

# Совершаем несколько транзакций
1..3 | ForEach-Object {
    Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/transactions" `
        -ContentType 'application/json' -Body (@{
            "accountId" = $account.accountId
            "amount" = 1000.00
        } | ConvertTo-Json)
    Start-Sleep -Seconds 1
}

# Проверяем статус
$accountStatus = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/accounts/$($account.id)"
Write-Host "Статус счета: $($accountStatus.status)"  # BLOCKED
```

### 3. Статус ARRESTED (Счет арестован)

```powershell
# Создаем клиента
$client = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/clients" `
    -ContentType 'application/json' -Body '{
        "firstName": "Сергей",
        "middleName": "Сергеевич",
        "lastName": "Сергеев"
    }'

# Создаем счет
$account = Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/accounts" `
    -ContentType 'application/json' -Body (@{
        "clientId" = $client.id
        "initialBalance" = 10000.00
    } | ConvertTo-Json)

# Совершаем транзакции
1..6 | ForEach-Object {
    Invoke-RestMethod -Method Post -Uri "http://localhost:8080/api/transactions" `
        -ContentType 'application/json' -Body (@{
            "accountId" = $account.accountId
            "amount" = 1000.00
        } | ConvertTo-Json)
    Start-Sleep -Seconds 1
}

# Проверяем статус
$accountStatus = Invoke-RestMethod -Method Get -Uri "http://localhost:8080/api/accounts/$($account.id)"
Write-Host "Статус счета: $($accountStatus.status)"  # ARRESTED
```

## API Endpoints

### Основной сервис (service1:8080)

**Клиенты:**

- `POST /api/clients` - Создать нового клиента
- `GET /api/clients` - Получить всех клиентов
- `GET /api/clients/{id}` - Получить клиента по ID

**Счета:**

- `POST /api/accounts` - Создать новый счет
- `GET /api/accounts` - Получить все счета
- `GET /api/accounts/{id}` - Получить счет по ID

**Транзакции:**

- `POST /api/transactions` - Создать новую транзакцию
- `GET /api/transactions/{id}` - Получить транзакцию по ID
- `GET /api/transactions?accountId={id}` - Получить транзакции по счету

### Сервис черного списка (service2:8081)

**Статус клиента:**

- `GET /api/clients/{clientId}/status` - Получить статус клиента (OK/BLACKLISTED)

## Конфигурация системы

Основные параметры конфигурации (находятся в application.yml):

```yaml
# Основной сервис (service1)
security:
  jwt:
    secret: my-super-secret-key-that-nobody-else-knows-123456
    expiration-ms: 3600000

service2:
  url: http://service2:8081

transactions:
  reject:
    threshold: 5  # Порог отклоненных транзакций для ареста счета
```

```yaml
# Сервис черного списка (service2)
security:
  jwt:
    secret: my-super-secret-key-that-nobody-else-knows-123456
```

## Логика работы системы

1. **Создание транзакции:**

    - Проверка клиента в сервисе черного списка
    - Если клиент в черном списке:
        - Транзакция немедленно отклоняется (REJECTED)
        - При достижении порога отклоненных транзакций счет блокируется (BLOCKED)
        - При превышении порога счет арестовывается (ARRESTED)
    - Если клиент не в черном списке:
        - Транзакция отправляется в Kafka для обработки
        - Средства резервируются на счете

2. **Обработка транзакций в Kafka:**

    - Consumer обрабатывает транзакции из топика Kafka
    - Возвращает результат обработки (ACCEPTED/REJECTED/BLOCKED)
    - Обновляет статус счета и баланс на основе результата

