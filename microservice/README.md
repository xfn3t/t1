# Задание 7 Microservices

## Состав системы

* **Service 1**
  Основной сервис, обрабатывающий клиентов и счета. Отвечает за регистрацию, хранение статусов (`OPEN`, `ARRESTED`, `BLACKLISTED`) и выдаёт Micrometer-метрики.

* **Service 2 / Service 3**
  Сервисы, которые отправляют транзакции на счета. Используются в тестах и для генерации нагрузки. Передают данные напрямую через HTTP-запросы в Service 1 (не через Kafka).



## Логика работы

1. Создаётся клиент.
2. Клиент автоматически уходит в статус `BLACKLISTED` (например, при совпадении имени/фамилии с шаблоном).
3. Создаётся счёт, привязанный к этому клиенту.
4. Сервис получает транзакции:

   * Если количество отклонённых транзакций (`REJECTED`) превышает `transactions.reject.threshold`, счёт переводится в статус `ARRESTED`.
5. В фоне запускается `UnblockScheduler` — периодически сканирует и разблокирует (`OPEN`) клиентов и счета.



## Конфигурация (из `application.yml`)

### Общие параметры:

* `transactions.reject.threshold`: порог отклонённых транзакций, после которого счёт уходит в `ARRESTED`.
* `tasks.unblock.accounts.period`: период работы разблокировщика счетов (в миллисекундах).
* `tasks.unblock.clients.period`: период работы разблокировщика клиентов.

### Метрики:

Micrometer-метрики активированы и доступны через Spring Boot Actuator.



## Метрики

**Доступные endpoints:**

* `/actuator/metrics/clients.blocked.count`
  Количество клиентов в статусе `BLACKLISTED`.

* `/actuator/metrics/accounts.arrested.count`
  Количество счетов в статусе `ARRESTED`.



## Пример проверки работоспособности

Тестовый PowerShell-скрипт запускает полный end-to-end сценарий, включая создание клиента, перевод счёта в `ARRESTED`, ожидание `UnblockScheduler`, и проверку метрик.

Пример:

```powershell
# Выполняется напрямую в терминале PowerShell
$BaseUrl = "http://localhost:8080"
$H = @{ 'Content-Type' = 'application/json' }

# 1. Создать клиента
$cli = Invoke-RestMethod -Uri "$BaseUrl/api/clients" -Method Post -Headers $H -Body (@{
  firstName='Stub'; middleName='Forced'; lastName='Blacklist'
} | ConvertTo-Json)

# 2. Ждать BLACKLISTED
do {
  Start-Sleep 1
  $status = (Invoke-RestMethod -Uri "$BaseUrl/api/clients/$($cli.id)/status").status
} until ($status -eq 'BLACKLISTED')

# 3. Создать счёт
$acc = Invoke-RestMethod -Uri "$BaseUrl/api/accounts" -Method Post -Headers $H -Body (@{
  clientId=$cli.id; initialBalance=1000
} | ConvertTo-Json)

# 4. Отправить транзакции (количество влияет на статус)
1..15 | ForEach-Object {
  Invoke-RestMethod -Uri "$BaseUrl/api/transactions" -Method Post -Headers $H -Body (@{
    accountId = $acc.accountId; amount = 100
  } | ConvertTo-Json)
  Start-Sleep 1
}

# 5. Подождать анблок (~10 секунд)
Start-Sleep 10

# 6. Получить метрики
$blocked = Invoke-RestMethod "$BaseUrl/actuator/metrics/clients.blocked.count"
$arrested = Invoke-RestMethod "$BaseUrl/actuator/metrics/accounts.arrested.count"

Write-Host "clients.blocked.count = $($blocked.measurements[0].value)"
Write-Host "accounts.arrested.count = $($arrested.measurements[0].value)"
```

Изменяя количество транзакций, можно получить различные финальные статусы.


## Запуск проекта

Требования: `Docker`, `docker-compose`

```bash
git clone <repo-url>
cd <project-root>
docker-compose up --build
```

Все сервисы поднимутся автоматически. Основные порты:

* `Service 1`: [http://localhost:8080](http://localhost:8080)
* `Service 2`: [http://localhost:8081](http://localhost:8081)
* `Service 3`: [http://localhost:8082](http://localhost:8082)
* `Postgres`: localhost:5432


## Используемые технологии

* Java 17 + Spring Boot
* Docker / docker-compose
* PostgreSQL
* Micrometer + Spring Actuator
* HTTP (взаимодействие между сервисами)
