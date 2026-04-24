# UserService – консольное CRUD-приложение на Java 21 + Hibernate + PostgreSQL

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Maven](https://img.shields.io/badge/Maven-3.9.14-blue?logo=apachemaven)
![Hibernate](https://img.shields.io/badge/Hibernate-7.3.2.Final-purple?logo=hibernate)
![HikariCP](https://img.shields.io/badge/HikariCP-7.0.2-lightgrey)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-18-blue?logo=postgresql)
![SLF4J](https://img.shields.io/badge/SLF4J-2.0.17-yellow)
![Logback](https://img.shields.io/badge/Logback-1.5.32-brightgreen)
![JUnit](https://img.shields.io/badge/JUnit%20Jupiter-6.0.3-green)
![Mockito](https://img.shields.io/badge/Mockito-5.23.0-orange)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
[![CI](https://github.com/charset-8utf/UserService/actions/workflows/UserServiceCI.yml/badge.svg)](https://github.com/charset-8utf/UserService/actions/workflows/UserServiceCI.yml)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)
## Описание проекта

Консольное приложение для управления пользователями с поддержкой операций **Create**, **Read**, **Update**, **Delete** (CRUD).  
Использует **Hibernate ORM**, **PostgreSQL** в Docker и пул соединений **HikariCP**.  
**Архитектура:** трёхслойная (Controller → Service → Repository) с DTO, ручным маппером, паттернами GoF.

Покрыт юнит-тестами (JUnit, Mockito) и интеграционными тестами (Testcontainers).  
Настроен CI (GitHub Actions) с авто-тестами, сборкой Docker-образа и Smoke-тестом.

## Требования к окружению

- **Docker Desktop** (для локального запуска PostgreSQL и интеграционных тестов)
- **Java 21** (установлена и настроена)
- **Maven 3.9+** (или использовать встроенный Maven в IDEA)

## Быстрый старт

### 1. Клонирование репозитория

```bash
git clone https://github.com/charset-8utf/UserService.git
cd UserService
```

### 2. Запуск PostgreSQL в Docker
```bash
docker run --name user-postgres -e POSTGRES_DB=userdb -e POSTGRES_USER=postgres -e POSTGRES_PASSWORD=postgres -p 5432:5432 -d postgres:latest
```

### 3. Создание таблицы (выполняется один раз)

Таблица создаётся вручную с помощью SQL-скрипта `src/main/resources/db/schema.sql`:

Копируем скрипт в контейнер
```bash
docker cp src/main/resources/db/schema.sql user-postgres:/tmp/schema.sql
```

Выполняем скрипт
```bash
docker exec -it user-postgres psql -U postgres -d userdb -f /tmp/schema.sql
```

Содержимое `schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS users (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(150) NOT NULL UNIQUE,
    age INTEGER NOT NULL,
    created_at TIMESTAMP NOT NULL
);
```

### 4. Локальная сборка и запуск приложения

```bash
mvn clean package
java -jar target/UserService-1.0-SNAPSHOT.jar
```

Или через Maven:

```bash
mvn compile exec:java -Dexec.mainClass="com.crud.api.Console"
```

### 5. Запуск через Docker

Собрать образ и запустить приложение вместе с PostgreSQL:
```bash
docker-compose up postgres -d
docker-compose run --rm app
```

## Архитектура проекта

```text
com.crud
├── api         # консольный интерфейс (меню, команды)
├── controller  # слой контроллера (работа с DTO)
├── service     # слой сервиса (бизнес-логика, валидация)
├── repository  # слой репозитория (Hibernate, транзакции)
├── entity      # JPA-сущность User
├── dto         # DTO (UserRequest, UserResponse)
├── mapper      # преобразование DTO ↔ Entity
├── exception   # иерархия кастомных исключений
└── util        # HibernateUtil (SessionFactory)
```

**Паттерны GoF:**
- *Builder* – для создания `User`
- *Command* – для пунктов меню
- *Template Method* – для транзакций в `AbstractRepository`
- *Strategy* – для валидации

## Тестирование

### Запуск всех тестов

```bash
mvn clean test
```

### Типы тестов

- **Интеграционные тесты репозитория** – поднимают временный PostgreSQL через Testcontainers.
- **Юнит-тесты сервиса** – используют Mockito для мока репозитория.
- **Юнит-тесты контроллера** – мокают сервис.
- **Юнит-тесты команд** – проверяют работу каждой команды меню.
- **End-to-end тест консоли** – симулируют пользовательский ввод/вывод.

Все тесты автоматически запускаются в GitHub Actions при каждом push в ветки `main`/`develop`.

## CI

Файл `.github/workflows/UserServiceCI.yml`:

- Запускает Maven-тесты (Юнит и интеграционные).
- Собирает Docker-образ приложения.
- Запускает PostgreSQL в отдельном контейнере.
- Выполняет smoke-тест
- Загружает отчёты о тестах в артефакты.

## Логирование

Используется **SLF4J + Logback**. Конфигурация – `src/main/resources/logback.xml`.  
Все сообщения (меню, результаты операций, ошибки) выводятся через логгер, что обеспечивает единообразие.

## Безопасность

- **Параметризованные логи** – избегание конкатенации строк в `log.error`.
- **Использование Criteria API** в `findAll` вместо динамического HQL (исключение потенциальных SQL-инъекций).
- `hibernate.hbm2ddl.auto = validate` – схема создаётся вручную через SQL-скрипт.
- **Кастомные исключения** для бизнес-ошибок.

## Автор
[charset-8utf](https://github.com/charset-8utf)