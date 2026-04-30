package com.crud;

/**
 * Healthcheck для Docker.
 */
public final class HealthCheck {

    private HealthCheck() {
        throw new UnsupportedOperationException("Утилитарный класс");
    }

    public static void main(String[] args) {
        try {
            HibernateUtil hibernateUtil = new HibernateUtil();
            if (hibernateUtil.getSessionFactory() == null) {
                System.exit(1);
            }
            System.exit(0);
        } catch (RuntimeException e) {
            System.exit(1);
        }
    }
}
