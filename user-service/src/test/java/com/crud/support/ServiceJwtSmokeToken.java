package com.crud.support;

public final class ServiceJwtSmokeToken {

    private ServiceJwtSmokeToken() {
    }

    public static void main(String[] args) {
        String secret = System.getenv("APP_SERVICE_JWT_SECRET");
        if (secret == null || secret.isBlank()) {
            secret = "dev-service-jwt-secret-change-in-production-min-32b";
        }
        var props = ServiceJwtTestSupport.properties(secret);
        var provider = new com.crud.security.servicejwt.ServiceJwtTokenProvider(
                ServiceJwtTestSupport.createEncoder(props), props);
        System.out.print(provider.getToken());
    }
}
