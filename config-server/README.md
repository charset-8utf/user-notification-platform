# config-server – централизованная конфигурация платформы

![Java](https://img.shields.io/badge/Java-21-blue?logo=openjdk&color=orange)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-4.0.6-green?logo=springboot)
![Spring Cloud Config](https://img.shields.io/badge/Spring%20Cloud%20Config-5.0.3-green)
![Gradle](https://img.shields.io/badge/Gradle-8.14+-blue?logo=gradle)

## Описание

[Spring Cloud Config Server](https://docs.spring.io/spring-cloud-config/reference/) для монорепозитория [`user-notification-platform`](../README.md).  
Отдаёт YAML-свойства сервисам из [`config-repo`](../config-repo) по HTTP (`:8888`).

Поддерживает два backend'а (профили):

| Профиль            | Backend        | Назначение                                  |
|--------------------|----------------|---------------------------------------------|
| `native` (default) | filesystem     | Локальная разработка, `file:../config-repo` |
| `git`              | git repository | CI/prod, версионирование конфигурации       |

## Быстрый старт

```bash
# из корня платформы
./gradlew :config-server:bootRun
```

Проверка:

```bash
curl http://localhost:8888/actuator/health
curl http://localhost:8888/user-service/default
curl http://localhost:8888/actuator/info   # configBackend.profile + repository
```

Docker:

```bash
docker build -f docker/spring-service/Dockerfile \
  --build-arg MODULE=config-server \
  --build-arg JAR_FILE=config-server.jar \
  -t config-server:local .
```

## Профили и переменные

| Переменная              | Default                                       | Описание                                                                              |
|-------------------------|-----------------------------------------------|---------------------------------------------------------------------------------------|
| `CONFIG_SERVER_PROFILE` | `native`                                      | `native` или `git`                                                                    |
| `CONFIG_SERVER_PORT`    | `8888`                                        | HTTP-порт                                                                             |
| `CONFIG_SERVER_URI`     | `https://localhost:8888` (default в клиентах) | Полный URL для `spring.config.import`; в Docker/Compose — `http://config-server:8888` |
| `CONFIG_REPO_PATH`      | `file:../config-repo`                         | Native search-locations                                                               |
| `CONFIG_GIT_URI`        | `file:///${user.dir}/../config-repo/.git`     | Git URI                                                                               |

## Архитектура

```text
com.platform.config
├── ConfigServerApplication.java     # @EnableConfigServer
├── config/                          # typed @ConfigurationProperties
│   ├── NativeConfigServerProperties
│   └── GitConfigServerProperties
├── backend/                         # Strategy: native vs git
│   ├── ConfigBackendStrategy
│   ├── NativeConfigBackendStrategy  # @Profile("native")
│   └── GitConfigBackendStrategy     # @Profile("git")
└── info/
    └── ConfigBackendInfoContributor # /actuator/info
```

### GoF-паттерны

| Паттерн      | Где                            | Назначение                           |
|--------------|--------------------------------|--------------------------------------|
| **Strategy** | `ConfigBackendStrategy`        | Описание active backend (native/git) |
| **Adapter**  | `ConfigBackendInfoContributor` | Strategy → Actuator `Info`           |

Активная strategy выбирается Spring через `@Profile` — ровно один bean `ConfigBackendStrategy` в runtime.

## Конфигурация

Свойства Spring Cloud Config Server биндятся в records:

| Record                         | Префикс                             |
|--------------------------------|-------------------------------------|
| `NativeConfigServerProperties` | `spring.cloud.config.server.native` |
| `GitConfigServerProperties`    | `spring.cloud.config.server.git`    |

## Тестирование

```bash
./gradlew :config-server:check
```

JaCoCo: порог **≥ 70%**.

## Автор

[charset-8utf](https://github.com/charset-8utf)
