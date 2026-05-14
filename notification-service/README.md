# notification-service

Микросервис уведомлений (заготовка под Kafka, почту, MongoDB по общему плану). Сейчас поднимается как обычное Spring Boot-приложение на порту **8081**.

Версии ориентированы на тот же стек, что и `UserServiceSpringBoot`: **Java 21**, **Spring Boot 4.0.6**.

## Быстрая проверка

```bash
cd /Users/igor/IdeaProjects/notification-service
mvn -q verify
mvn spring-boot:run
```

Health: http://localhost:8081/actuator/health  

## Где лежит проект относительно user-service

Сейчас каталог создан **рядом** с существующим репозиторием:

```text
IdeaProjects/
├── UserServiceSpringBoot/     ← user-service (текущий git)
└── notification-service/      ← этот проект (отдельный git)
```

## Как открыть оба проекта в IntelliJ IDEA

### Вариант A — два окна (проще всего)

1. **File → Open** → выберите `UserServiceSpringBoot` (как раньше).
2. **File → New → Project from Existing Sources** во втором окне: **File → Open** → `notification-service` — IDEA может открыть в новом окне; если нет: после Open согласитесь с «Open in New Window».

Так у каждого сервиса свой модуль Maven, свои run configuration и Git.

### Вариант B — одно окно, два Maven-проекта

1. Откройте родительскую папку: **File → Open** → `IdeaProjects` (или создайте пустую папку `platform` и перенесите/клонируйте оба репо внутрь).
2. В окне с `IdeaProjects`: правый клик по `notification-service/pom.xml` → **Add as Maven Project** (аналогично, если второй проект не подхватился).

В Project видны оба дерева исходников; Git по-прежнему **разный** в каждой подпапке (две корневые `.git`).

## Как открыть в Cursor (чтобы ассистент видел оба кода)

1. **File → Open Folder** (или аналог) → выберите **`IdeaProjects`** (родитель), если оба репозитория лежат только там.
2. Либо оставьте workspace на одном репо и при работе над вторым **File → Open Folder** на `notification-service` — тогда в чате явно указывайте, какой сервис правите, или прикрепляйте файлы через `@`.

Оптимально для совместной работы по плану: **один workspace = родительская папка, внутри которой оба каталога**.

## Git (второй репозиторий)

В каталоге уже выполнен `git init`. Дальше создайте пустой репозиторий на GitHub/GitLab и:

```bash
cd /Users/igor/IdeaProjects/notification-service
git add .
git commit -m "Initial notification-service skeleton"
git branch -M main
git remote add origin <URL-вашего-репозитория>
git push -u origin main
```

`UserServiceSpringBoot` и `notification-service` — **два независимых** репозитория; общий compose/инфра можно позже вынести в третий репозиторий или оставить в одном из них.

## Что будет добавлено по плану

- Spring Kafka (consumer), Mail, Data MongoDB, Redis-клиент  
- REST API отправки письма и обработка топика `user-notifications`  
- Docker Compose вместе с user-service, Testcontainers в тестах  

Сейчас в `pom` только web + actuator + Prometheus registry, чтобы проект собирался без Zookeeper/Kafka/Mongo.
