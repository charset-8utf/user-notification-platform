package com.notification.support;

public final class ServiceJwtSmokeToken {

    private ServiceJwtSmokeToken() {
    }

    public static void main(String[] args) {
        String secret = System.getenv("APP_SERVICE_JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = "dev-service-jwt-secret-change-in-production-min-32b";
        }
        System.out.print(ServiceJwtTestSupport.accessToken(
                secret,
                ServiceJwtTestSupport.ISSUER,
                ServiceJwtTestSupport.SUBJECT,
                ServiceJwtTestSupport.AUDIENCE,
                ServiceJwtTestSupport.SCOPE,
                java.time.Duration.ofMinutes(5)));
    }
}
