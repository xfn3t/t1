### Система обработки транзакций с Kafka


#### Архитектура системы

Система состоит из двух взаимодействующих микросервисов:

1. **Core Service (service1)** - порт 8080:
    - REST API для управления клиентами, счетами и транзакциями
    - Отправка транзакций на обработку
    - Обработка результатов транзакций
    - База данных: PostgreSQL

2. **Transaction Processor (service2)** - порт 8081:
    - Проверка лимитов операций и баланса
    - Определение статуса транзакций
    - Отправка результатов обработки



### Статусы транзакций
Статус определяется в service2 и передается в service1 через Kafka:

| Статус       | Условия установки                                     | Действия в service1                                      |
|--------------|-------------------------------------------------------|----------------------------------------------------------|
| **REQUESTED**| Исходный статус при создании транзакции               | - Списывает сумму с баланса<br>- Отправляет в Kafka      |
| **ACCEPTED** | - Баланс ≥ суммы списания<br>- Не превышен лимит операций | Фиксирует транзакцию без изменений                     |
| **REJECTED** | Сумма списания > текущего баланса                     | Возвращает сумму на баланс                               |
| **BLOCKED**  | Превышен лимит операций (N транзакций за T времени)   | Блокирует счет, сумму переводит в frozenAmount           |
| **CANCELLED**| Не используется в текущей реализации                  | -                                                        |



### Статусы счетов
Управляются service1 на основе результатов транзакций:

| Статус       | Условия установки                         | Возможность операций |
|--------------|-------------------------------------------|----------------------|
| **OPEN**     | Счет создан                               | ✅ Все операции      |
| **BLOCKED**  | Получена транзакция со статусом `BLOCKED` | ❌ Запрещены         |
| **ARRESTED** | Не используется                           | -                    |
| **CLOSED**   | Счет удален                               | ❌ Запрещены         |


### Ключевые бизнес-правила

#### 1. Лимит операций (service2)

- Конфигурируется в `service2/application.properties`:
  ```properties
  transaction.limit.count=5    # Макс. операций (N)
  transaction.limit.period=60  # Период в секундах (T)
  ```

- Логика проверки:
- 
  ```java
  String key = clientId + ":" + accountId;
  Queue<Transaction> queue = cache.get(key); 
  if (queue.size() > limitCount) {
      return TransactionStatus.BLOCKED;
  }
  ```

#### 2. Проверка баланса (service2)

```java
if (transaction.getAmount().compareTo(accountBalance) > 0) {
    return TransactionStatus.REJECTED;
}
```

#### 3. Блокировка счета (service1)

При получении статуса `BLOCKED`:

```java
account.setStatus(AccountStatus.BLOCKED);
account.setFrozenAmount(account.getFrozenAmount().add(transactionAmount));
account.setBalance(account.getBalance().add(transactionAmount));
```



### Примеры сценариев

#### Сценарий 1: Успешная транзакция

```bash
# Баланс: 1000.00
curl -X POST -d '{"accountId":"acc_123", "amount":100.00}' \
http://localhost:8080/api/transactions

# Результат:
# - Статус транзакции: ACCEPTED
# - Новый баланс: 900.00
```

#### Сценарий 2: Превышение баланса

```bash
# Баланс: 1000.00
curl -X POST -d '{"accountId":"acc_123", "amount":1500.00}' \
http://localhost:8080/api/transactions

# Результат:
# - Статус транзакции: REJECTED
# - Баланс остаётся: 1000.00
```

#### Сценарий 3: Блокировка счета

```bash
# 6 транзакций подряд с интервалом <60 сек
for i in {1..6}; do
  curl -X POST -d '{"accountId":"acc_123", "amount":1.00}' \
  http://localhost:8080/api/transactions
done

# Результат:
# - 1-5 транзакций: ACCEPTED
# - 6-я транзакция: BLOCKED
# - Счёт переведён в статус BLOCKED
```

### Запуск системы

1. Запуск инфраструктуры:

   ```bash
   docker-compose up -d  # Kafka + PostgreSQL
   ```

2. Запуск сервисов:

   ```bash
   # Service1 (REST API)
   cd service1
   ./mvnw spring-boot:run

   # Service2 (обработчик)
   cd service2
   ./mvnw spring-boot:run -Dspring-boot.run.arguments=--server.port=8081
   ```



### Настройки

**service1/application.properties:**

```properties
server.port=8080
spring.kafka.bootstrap-servers=localhost:9092
```

**service2/application.properties:**

```properties
server.port=8081
spring.kafka.bootstrap-servers=localhost:9092
transaction.limit.count=5
transaction.limit.period=60
```



### Тестирование

1. Создайте клиента и счёт:

```bash
   curl -X POST -d '{"firstName":"Иван", "middleName":"Иванович", "lastName":"Иванов"}' \
   http://localhost:8080/api/clients
   
   curl -X POST -d '{"clientId":1, "initialBalance":1000.00}' \
   http://localhost:8080/api/accounts
   ```

2. Проверьте баланс:

   ```bash
   curl http://localhost:8080/api/accounts/1
   ```

3. Отправьте транзакции:

   ```bash
   # Успешная
   curl -X POST -d '{"accountId":"acc_123", "amount":100.00}' \
   http://localhost:8080/api/transactions
   
   # На блокировку
   for i in {1..6}; do
     curl -X POST -d '{"accountId":"acc_123", "amount":1.00}' \
     http://localhost:8080/api/transactions
   done
   ```

4. Просмотрите историю:

   ```bash
   curl "http://localhost:8080/api/transactions?accountId=acc_123"
   ```