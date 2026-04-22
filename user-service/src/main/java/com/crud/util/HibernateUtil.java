package com.crud.util;

import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Утилитарный класс для управления Hibernate SessionFactory.
 * <p>
 * Реализует паттерн Singleton через статическую инициализацию.
 * SessionFactory создаётся один раз при загрузке класса и доступен через {@link #getSessionFactory()}.
 * </p>
 * <p>
 * При создании фабрики используется файл конфигурации {@code hibernate.cfg.xml},
 * который должен находиться в classpath (обычно в {@code src/main/resources}).
 * </p>
 * <p>
 * В случае ошибки инициализации логируется сообщение и выбрасывается {@link ExceptionInInitializerError},
 * что предотвращает загрузку класса и дальнейшие попытки работы с БД.
 * </p>
 * <p>
 * Для корректного завершения приложения рекомендуется вызывать {@link #shutdown()} при остановке.
 * </p>
 *
 *
 * @author charset-8utf
 * @version 1.0
 */
public class HibernateUtil {
    private static final Logger log = LoggerFactory.getLogger(HibernateUtil.class);
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private HibernateUtil() {
        throw new UnsupportedOperationException("Утилитарный класс, создание экземпляров запрещено");
    }

    /**
     * Создаёт и настраивает Hibernate SessionFactory.
     * <p>
     * Загружает конфигурацию из {@code hibernate.cfg.xml}.
     * В случае успеха возвращает готовую фабрику.
     * При возникновении ошибки логирует детали и выбрасывает {@link ExceptionInInitializerError},
     * который оборачивает исходное исключение.
     * </p>
     *
     * @return сконфигурированная SessionFactory
     * @throws ExceptionInInitializerError если создание фабрики невозможно
     */
    private static SessionFactory buildSessionFactory() {
        try {
            log.info("Загрузка конфигурации Hibernate из hibernate.cfg.xml");
            Configuration configuration = new Configuration();
            configuration.configure(); // загружает hibernate.cfg.xml
            log.info("Конфигурация загружена успешно, создаётся SessionFactory");
            return configuration.buildSessionFactory();
        } catch (Exception ex) {
            log.error("Не удалось создать SessionFactory", ex);
            throw new ExceptionInInitializerError("Ошибка инициализации Hibernate: " + ex.getMessage());
        }
    }

    /**
     * Возвращает экземпляр SessionFactory (единственный в приложении).
     * <p>
     * SessionFactory потокобезопасна и используется для открытия сессий.
     * </p>
     *
     * @return SessionFactory (не null)
     */
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    /**
     * Закрывает SessionFactory и освобождает все связанные ресурсы.
     * <p>
     * После вызова этого метода дальнейшее использование {@link #getSessionFactory()}
     * приведёт к ошибке, так как фабрика будет закрыта.
     * Рекомендуется вызывать этот метод при завершении приложения.
     * </p>
     */
    public static void shutdown() {
        if (sessionFactory != null && !sessionFactory.isClosed()) {
            log.info("Закрытие SessionFactory");
            sessionFactory.close();
            log.info("SessionFactory закрыта успешно");
        }
    }
}