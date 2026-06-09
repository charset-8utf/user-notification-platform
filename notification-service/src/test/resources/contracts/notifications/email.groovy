package contracts.notifications

import org.springframework.cloud.contract.spec.Contract

[
        Contract.make {
            description "POST /api/notifications/email with service JWT"
            request {
                method POST()
                url "/api/notifications/email"
                headers {
                    contentType(applicationJson())
                    header("Authorization", $(consumer(regex("Bearer [A-Za-z0-9._-]+")),
                            producer("Bearer eyJraWQiOiJ0ZXN0LXNlcnZpY2Utand0IiwiYWxnIjoiSFMyNTYifQ.eyJpc3MiOiJ1c2VyLW5vdGlmaWNhdGlvbi1wbGF0Zm9ybSIsInN1YiI6InVzZXItc2VydmljZSIsImF1ZCI6Im5vdGlmaWNhdGlvbi1zZXJ2aWNlIiwiZXhwIjoyMDk1NDU5MjAwLCJpYXQiOjE3ODAwOTkyMDAsInNjb3BlIjoibm90aWZpY2F0aW9uczp3cml0ZSJ9.LiLDhQrYw1UsuXl36IwrYI3eMh84V5fHY9AOKJPVmqA")))
                }
                body([
                        eventId: $(consumer(anyUuid()), producer("550e8400-e29b-41d4-a716-446655440000")),
                        operation: "USER_CREATED",
                        email: "contract@example.com"
                ])
            }
            response {
                status NO_CONTENT()
            }
        }
]
