# C4: System Context

```mermaid
flowchart TB
    User[Пользователь / клиент]
    Admin[Администратор]
    Mail[SMTP / Mailpit]

    subgraph Platform[User Notification Platform]
        Edge[NGINX / Ingress]
        GW[API Gateway]
        BFF[Web BFF]
        US[user-service]
        NS[notification-service]
        CS[config-server]
    end

    PG[(PostgreSQL)]
    MG[(MongoDB)]
    KF[Kafka]
    RD[Redis]
    KC[Keycloak OIDC]

    User --> Edge
    Admin --> Edge
    Edge --> GW
    Edge --> BFF
    BFF --> GW
    GW --> US
    GW --> NS
    US --> PG
    US --> KF
    US --> RD
    NS --> MG
    NS --> KF
    NS --> Mail
    CS -.-> US
    CS -.-> NS
    CS -.-> GW
    KC -.-> GW
```

## Containers

| Container | Ответственность |
|-----------|-----------------|
| user-service | CRUD, JWT login, outbox |
| notification-service | inbox, email, service JWT / API key |
| api-gateway | JWT/OIDC edge, rate limit, TokenRelay |
| web-bff | API composition `/bff/me` |
