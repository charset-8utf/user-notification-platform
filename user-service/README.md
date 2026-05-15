# UserServiceSpringBoot – REST-сервис управления пользователями

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-green?logo=springsecurity)
![PostgreSQL](https://img.shields.io/badge/PostgreSQL-17-blue?logo=postgresql)
![Hibernate](https://img.shields.io/badge/Hibernate-7.3.2.Final-purple?logo=hibernate)
![Maven](https://img.shields.io/badge/Maven-3.9.14-blue?logo=apachemaven)
![Liquibase](https://img.shields.io/badge/Liquibase-4.x-red?logo=liquibase)
![Caffeine](https://img.shields.io/badge/Caffeine-Cache-brightgreen)
![JUnit](https://img.shields.io/badge/JUnit%20Jupiter-6.0.3-green)
![Mockito](https://img.shields.io/badge/Mockito-5.23.0-orange)
![Testcontainers](https://img.shields.io/badge/Testcontainers-2.0.5-blue)
[![CI](https://github.com/charset-8utf/UserService/actions/workflows/UserServiceCI.yml/badge.svg)](https://github.com/charset-8utf/UserService/actions/workflows/UserServiceCI.yml)
![Docker](https://img.shields.io/badge/Docker-Ready-blue?logo=docker)

## Описание проекта

REST-сервис для управления пользователями с поддержкой операций **Create**, **Read**, **Update**, **Delete** (CRUD).  
Построен на **Spring Boot 4** с использованием **Spring Data JPA**, **PostgreSQL** в Docker и пула соединений **HikariCP**.  
**Архитектура:** трёхслойная (Controller → Service → Repository) с DTO, ручными мапперами.

Схема БД управляется миграциями **Liquibase**: точка входа — `src/main/resources/db/changelog/db.changelog-master.yaml`, сами изменения — отдельные SQL-файлы в `src/main/resources/db/changelog/changes/` (формат *Liquibase formatted sql*).
Покрыт юнит-тестами (JUnit, Mockito) и интеграционными тестами (Testcontainers, H2).  
Настроен CI (GitHub Actions) с авто-тестами и сборкой Docker-образа.  
Приложение и PostgreSQL запускаются через `docker-compose`. В образ включён **healthcheck** (Actuator).

## API-эндпоинты

| Метод  | Путь                                | Описание                            |
|--------|-------------------------------------|-------------------------------------|
| POST   | `/api/users`                        | Создание пользователя               |
| GET    | `/api/users/{id}`                   | Получение пользователя по ID        |
| GET    | `/api/users`                        | Список пользователей (с пагинацией) |
| PUT    | `/api/users/{id}`                   | Обновление пользователя             |
| DELETE | `/api/users/{id}`                   | Удаление пользователя               |
| GET    | `/api/users/by-email?email=`        | Поиск по email                      |
| GET    | `/api/users/search?email=`          | Поиск по части email                |
| POST   | `/api/users/{userId}/notes`         | Создание заметки                    |
| GET    | `/api/users/{userId}/notes`         | Список заметок пользователя         |
| GET    | `/api/users/{userId}/notes/{id}`    | Получение заметки                   |
| PUT    | `/api/users/{userId}/notes/{id}`    | Обновление заметки                  |
| DELETE | `/api/users/{userId}/notes/{id}`    | Удаление заметки                    |
| POST   | `/api/profiles/user/{userId}`       | Создание профиля                    |
| GET    | `/api/profiles/user/{userId}`       | Получение профиля пользователя      |
| GET    | `/api/profiles`                     | Список профилей (с пагинацией)      |
| PUT    | `/api/profiles/user/{userId}`       | Обновление профиля                  |
| DELETE | `/api/profiles/user/{userId}`       | Удаление профиля                    |
| POST   | `/api/roles`                        | Создание роли                       |
| GET    | `/api/roles/{id}`                   | Получение роли по ID                |
| GET    | `/api/roles`                        | Список ролей (с пагинацией)         |
| PUT    | `/api/roles/{id}`                   | Обновление роли                     |
| DELETE | `/api/roles/{id}`                   | Удаление роли                       |
| POST   | `/api/roles/assign?userId=&roleId=` | Назначение роли пользователю        |
| POST   | `/api/roles/remove?userId=&roleId=` | Снятие роли с пользователя          |

## Требования к окружению

- **Docker Desktop** (для локального запуска PostgreSQL и интеграционных тестов)
- **Java 21** (установлена и настроена)
- **Maven 3.9+** (или использовать встроенный Maven в IDEA)

## Быстрый старт через Docker

### 1. Клонирование репозитория

```bash
git clone https://github.com/charset-8utf/UserService.git
cd UserServiceSpringBoot
```

### 2. Настройка переменных окружения

Скопируйте пример файла окружения и при необходимости измените значения:

```bash
cp .env.example .env
```

По умолчанию `.env.example` содержит:

```properties
DB_USER=postgres
DB_PASSWORD=postgres
KEYSTORE_PASSWORD=changeit
APP_SEED_ADMIN_PASSWORD=admin123
APP_SEED_USER_PASSWORD=user123
APP_HTTPS_PORT=8443
POSTGRES_PUBLISH_PORT=5432
```

При необходимости порты приложения или Postgres на хосте можно переопределить через `APP_HTTPS_PORT` и `POSTGRES_PUBLISH_PORT` (см. `.env.example`).

> **Важно:** переменные `APP_SEED_ADMIN_PASSWORD` и `APP_SEED_USER_PASSWORD` задают пароли для начальных пользователей. Если они не заданы, учётные записи не будут созданы и API вернёт 401.

### 3. Запуск PostgreSQL и приложения

При первом запуске нужна сборка образа приложения:

```bash
docker compose up --build -d
```

Повторный старт без пересборки: `docker compose up -d`.

Приложение будет доступно по **HTTPS**: **https://localhost:8443** (если не меняли `APP_HTTPS_PORT` в `.env`).

**Режим разработки** (Maven `spring-boot:run`, монтирование `src`):  
`docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build`.

Если нужна **совсем новая база** (сброс схемы и истории миграций Liquibase во внутреннем томе Postgres):

```bash
docker compose down -v && docker compose up --build -d
```

> **Примечание:** браузер покажет предупреждение о самоподписанном сертификате — нажмите «Дополнительно» → «Перейти на сайт». Учётные данные в URL вида `https://user:pass@localhost/...` современные браузеры обычно **не передают** в HTTP Basic Auth: откройте URL без логина в адресе и введите логин в системном окне, либо используйте **curl** (`-u`) или **Postman** (см. Тестирование). Если ранее сохранили неверный пароль и ответ неожиданный, откройте сайт в режиме инкогнито или сбросьте сохранённые пароли для `localhost`.

### 4. Аутентификация

Приложение использует HTTP Basic авторизацию. При первом запуске создаются два пользователя:

| Логин   | Пароль (из .env)          | Роль         |
|---------|---------------------------|--------------|
| `admin` | `APP_SEED_ADMIN_PASSWORD` | ADMIN + USER |
| `user`  | `APP_SEED_USER_PASSWORD`  | USER         |

### 5. Проверка

Healthcheck (без авторизации)
```bash
curl -k https://localhost:8443/actuator/health
```
Список пользователей
```bash
curl -k -u admin:admin123 https://localhost:8443/api/users
```

### 6. Остановка и очистка

Остановить контейнеры (данные сохраняются):

```bash
docker compose down
```

Полностью удалить всё (включая том с БД Postgres):

```bash
docker compose down -v
```

## Локальный запуск с PostgreSQL в контейнере

### 1. Запуск PostgreSQL в Docker

```bash
docker run --name user-postgres \
  -e POSTGRES_DB=userdb \
  -e POSTGRES_USER=postgres \
  -e POSTGRES_PASSWORD=postgres \
  -p 5432:5432 -d postgres:17-alpine
```

### 2. Локальная сборка и запуск приложения

```bash
mvn clean package -DskipTests
java -jar target/UserServiceSpringBoot-1.0.0.jar
```

Или через Maven:

```bash
mvn spring-boot:run
```

Для создания seed-пользователей передайте пароли:

```bash
APP_SEED_ADMIN_PASSWORD=admin123 APP_SEED_USER_PASSWORD=user123 mvn spring-boot:run
```

## Архитектура проекта

```text
com.crud
├── config/      # конфигурация (безопасность, retry, rate limit, санитизация, CORS)
├── controller/  # REST-контроллеры (API-эндпоинты)
├── service/     # слой сервиса (бизнес-логика, валидация, retry при оптимистичных блокировках)
├── repository/  # Spring Data JPA репозитории
├── entity/      # JPA-сущности с оптимистичными блокировками и кэшированием
├── dto/         # объекты передачи данных (DTO)
├── mapper/      # мапперы для преобразования DTO ↔ Entity
├── exception/   # кастомные исключения и глобальный обработчик ошибок
└── security/    # Аутентификация через JPA
```

## Тестирование

Автоматические тесты дополняются **ручной проверкой API в Postman** (коллекция в каталоге `postman/`, см. раздел «Проверка API в Postman» выше).

### Запуск тестов

Только модульные (unit) тесты:

```bash
mvn test
```

Все тесты (unit + интеграционные + E2E)

```bash
mvn verify
```

Интеграционные тесты используют **H2** in-memory БД и/или **Testcontainers** (требуется запущенный Docker).

### Типы тестов

#### **Модульные тесты** (JUnit + Mockito) проверяют:
- Сервисы (с моками репозиториев)
- Контроллеры
- Мапперы (Entity ↔ DTO)
- Исключения и глобальный обработчик

#### **Интеграционные тесты** (H2 / Testcontainers) проверяют:
- Репозитории с реальной БД
- Миграции Liquibase
- Полный цикл запросов через REST API (E2E)

### Покрытие кода (JaCoCo)
- Порог покрытия **80% инструкций** для всех пакетов, кроме `com.crud` и `com.crud.entity`.

Все тесты автоматически запускаются в GitHub Actions при каждом push в ветки `main`/`develop`.

### Проверка API в Postman

Проверка REST API выполняется в **Postman**: в репозитории лежит файловая коллекция и окружение под локальный запуск по HTTPS.

**Коллекция** — каталог [`postman/collections/UserServiceSpringBoot API-1`](postman/collections/UserServiceSpringBoot%20API-1) — запросы сгруппированы по разделам (мониторинг Actuator, пользователи, заметки, профили, роли, сценарии под учёткой USER).

**Окружение** — [`postman/environments/UserServiceSpringBoot local-1.environment.yaml`](postman/environments/UserServiceSpringBoot%20local-1.environment.yaml) — `baseUrl`, логины и пароли по умолчанию совпадают с `.env.example` (при необходимости скорректируйте значения под свой `.env`).

**TLS:** в *Settings → General* временно отключите **SSL certificate verification** для работы с самоподписанным сертификатом `localhost`.
>Выберите окружение в Postman и отправляйте запросы: так проверяются все маршруты из таблицы API, включая разграничение прав (например, назначение ролей только у **ADMIN**).
## CI

Файл `.github/workflows/UserServiceCI.yml`:

- Установка JDK 21 и кеширование Maven.
- Запуск `mvn clean verify` (тесты).
- Сборка Docker-образа с кешем (Buildx).
- Smoke-тест приложения в docker compose.
- Загрузка отчётов тестов в артефакты.

## Логирование

Используется **SLF4J** (вывод через Spring Boot по умолчанию). Уровни и шаблон консоли — в `application.yml`.

## Особенности реализации

- **5 сущностей** со связями:
  - User ↔ Profile (OneToOne)
  - User ↔ Note (OneToMany)
  - User ↔ Role (ManyToMany через `user_role`)
  - User ↔ Credential (OneToOne)
- **Spring Security** – HTTP Basic аутентификация, ролевая модель (USER, ADMIN), stateless-сессии
- **HTTPS** – TLS 1.2/1.3 с PKCS12 keystore, порт 8443
- **Оптимистичные блокировки** – `@Version` + `@Retryable` (3 попытки, backoff 100ms) при конфликтах, fallback через `@Recover`
- **Кэш 2-го уровня** – Caffeine + JCache (Hibernate L2 cache + query cache)
- **Rate Limiting** – скользящее окно по IP/Authorization (20 запросов/60с по умолчанию)
- **XSS-санитизация** – Экранирование HTML параметров запросов через фильтр
- **CORS** – настраиваемые разрешенные источники
- **Пагинация** – Spring Data Pageable (макс. 100 на страницу)
- **Actuator** – health/readiness/liveness пробы и метрики
- `spring.jpa.hibernate.ddl-auto: validate` – схема изменяется только миграциями Liquibase
- **Параметризованные логи** – избегание конкатенации строк в `log.error`
- **Кастомные исключения** для бизнес-ошибок с глобальным обработчиком (`@RestControllerAdvice`)
- **Healthcheck** в Docker через Actuator

## Автор
[charset-8utf](https://github.com/charset-8utf)
