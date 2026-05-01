# UserService – консольное CRUD-приложение

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Maven](https://img.shields.io/badge/Maven-3.9.14-blue?logo=apachemaven)
![Hibernate](https://img.shields.io/badge/Hibernate-7.3.2.Final-purple?logo=hibernate)
![HikariCP](https://img.shields.io/badge/HikariCP-7.0.2-lightgrey)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
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

Схема БД управляется миграциями **Flyway** из `src/main/resources/db/migration` (V1...V5).  
Покрыт юнит-тестами (JUnit, Mockito) и интеграционными тестами (Testcontainers).  
Настроен CI (GitHub Actions) с авто-тестами, сборкой Docker-образа и Smoke-тестом.  
Приложение и PostgreSQL запускаются через `docker-compose`. В образ включён **healthcheck** (класс `HealthCheck`).

## Требования к окружению

- **Docker Desktop** (для локального запуска PostgreSQL и интеграционных тестов)
- **Java 21** (установлена и настроена)
- **Maven 3.9+** (или использовать встроенный Maven в IDEA)

## Быстрый старт через Docker

### 1. Клонирование репозитория

```bash
git clone https://github.com/charset-8utf/UserService.git
cd UserService
```

### 2. Запуск PostgreSQL и приложения

Запустить базу данных:

```bash
docker-compose up -d postgres
```

Собрать образ приложения:

```bash
docker-compose build app
```

Запуск приложения:

```bash
docker-compose run --rm app
```

### 3. Остановка и очистка

Остановить контейнеры (данные сохраняются):

```bash
docker-compose down
```

Полностью удалить всё (включая том с БД)

```bash
docker-compose down -v
```

## Локальный запуск с PostgreSQL в контейнере:

### 1. Запуск PostgreSQL в Docker
```bash
docker run --name user-postgres \
  -e POSTGRES_DB=userdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 postgres:17-alpine
```

### 2. Локальная сборка и запуск приложения

```bash
mvn clean package
java -jar target/UserService-1.0-SNAPSHOT.jar
```

Или через Maven:

```bash
mvn compile exec:java -Dexec.mainClass="com.crud.api.Console"
```

## Архитектура проекта

```text
com.crud
├── api         # консольный интерфейс (меню, команды)
├── controller  # слой контроллера (работа с DTO)
├── service     # слой сервиса (бизнес-логика, валидация)
├── repository  # слой репозитория (Hibernate, транзакции)
├── entity      # JPA-сущности User/Note/Role/Profile
├── dto         # DTO запросов/ответов и пагинации
├── mapper      # преобразование DTO ↔ Entity
├── exception   # иерархия кастомных исключений
└── util        # HibernateUtil (SessionFactory) и HealthCheck (Docker healthcheck)
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

#### **Модульные тесты** (JUnit + Mockito) проверяют:
- Сервис
- Контроллер 
- Команды
- Маппер
- Исключения
- Консольный ввод

#### **Интеграционные тесты** (Testcontainers) проверяют:
- Репозиторий с помощью поднятия временного PostgreSQL
- Успешную инициализацию SessionFactory и применение миграций Flyway
- HealthCheck

#### **End-to-end тест** симулирует пользовательский ввод/вывод

Все тесты автоматически запускаются в GitHub Actions при каждом push в ветки `main`/`develop`.

## CI

Файл `.github/workflows/UserServiceCI.yml`:

- Установка JDK 21 и кеширование Maven.
- Запуск `mvn clean verify` (тесты).
- Сборка Docker-образа.
- Запуск PostgreSQL в отдельном контейнере.
- Smoke‑тест приложения в docker compose (подача команд `1\n6\n0\n0` в консоль).
- Загрузка отчётов тестов в артефакты.

## Логирование

Используется **SLF4J + Logback**. Конфигурация – `src/main/resources/logback.xml`.  
Все сообщения (меню, результаты операций, ошибки) выводятся через логгер, что обеспечивает единообразие.

## Безопасность

- **Параметризованные логи** – избегание конкатенации строк в `log.error`.
- **Использование Criteria API** в `findAll` вместо динамического HQL (исключение потенциальных SQL-инъекций).
- `hibernate.hbm2ddl.auto = validate` – схема изменяется только миграциями Flyway.
- **Кастомные исключения** для бизнес-ошибок.
- **HealthCheck** позволяет оркестраторам (Docker, Kubernetes) контролировать состояние приложения.

## Автор
[charset-8utf](https://github.com/charset-8utf)
