package com.crud.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

/**
 * Сущность пользователя, отображаемая на таблицу {@code users} в базе данных.
 * <p>
 * Содержит следующие поля:
 * <ul>
 *     <li>{@code id} – уникальный идентификатор, генерируется автоматически</li>
 *     <li>{@code name} – имя пользователя (не может быть null или пустым)</li>
 *     <li>{@code email} – электронная почта (уникальна, не может быть null)</li>
 *     <li>{@code age} – возраст (от 0 до 150)</li>
 *     <li>{@code createdAt} – дата и время создания записи (устанавливается один раз)</li>
 *     <li>{@code version} – версия для оптимистической блокировки (управляется JPA)</li>
 * </ul>
 * </p>
 * <p>
 * Для создания объектов рекомендуется использовать {@link Builder}.
 * </p>
 *
 * @see Builder
 * @author charset-8utf
 * @version 1.2
 */
@Entity
@Table(name = "users", uniqueConstraints = {
        @UniqueConstraint(columnNames = "email", name = "uk_users_email")
})
@NamedQuery(name = "User.findByEmail", query = "SELECT u FROM User u WHERE u.email = :email")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", updatable = false)
    private Long id;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "email", nullable = false, unique = true, length = 150)
    private String email;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Version
    private Long version;

    /**
     * Конструктор по умолчанию (требуется Hibernate).
     * Не предназначен для прямого использования.
     */
    protected User() {}

    /**
     * Приватный конструктор, используемый {@link Builder}.
     *
     * @param builder объект Builder с заполненными полями
     */
    private User(Builder builder) {
        this.name = builder.name;
        this.email = builder.email;
        this.age = builder.age;
    }

    /**
     * Возвращает идентификатор пользователя.
     * @return id (может быть null до сохранения в БД)
     */
    public Long getId() {
        return id;
    }

    /**
     * Возвращает имя пользователя.
     * @return имя (не null)
     */
    public String getName() {
        return name;
    }

    /**
     * Возвращает email пользователя.
     * @return email (не null, уникальный)
     */
    public String getEmail() {
        return email;
    }

    /**
     * Возвращает возраст пользователя.
     * @return возраст (целое число)
     */
    public Integer getAge() {
        return age;
    }

    /**
     * Возвращает дату и время создания записи.
     * @return дата создания
     */
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    /**
     * Возвращает версию для оптимистической блокировки.
     * @return версия (управляется JPA)
     */
    public Long getVersion() {
        return version;
    }

    /**
     * Устанавливает идентификатор (обычно вызывается Hibernate после сохранения).
     * @param id новый идентификатор
     */
    public void setId(Long id) {
        this.id = id;
    }

    /**
     * Устанавливает имя пользователя.
     * @param name новое имя (не должно быть null или пустым)
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Устанавливает email пользователя.
     * @param email новый email (должен быть уникальным)
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Устанавливает возраст пользователя.
     * @param age новый возраст (должен быть в диапазоне 0-150)
     */
    public void setAge(Integer age) {
        this.age = age;
    }

    /**
     * Устанавливает дату создания (обычно только для целей тестирования или при миграции).
     * @param createdAt дата и время
     */
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    /**
     * Устанавливает версию (обычно не требуется вызывать вручную).
     * @param version новое значение версии
     */
    public void setVersion(Long version) {
        this.version = version;
    }

    /**
     * Создаёт новый экземпляр {@link Builder} для пошагового конструирования объекта User.
     *
     * @return новый Builder
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * Автоматически вызывается перед первым сохранением сущности в базу данных.
     * Устанавливает текущую дату и время создания записи.
     */
    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    /**
     * Внутренний класс Builder для создания объектов {@link User} с валидацией.
     * <p>
     * Реализует паттерн "Строитель". Позволяет задать имя, email и возраст,
     * после чего вызвать {@link #build()} для получения экземпляра User.
     * Валидация выполняется в методе {@code build()}: имя и email не могут быть пустыми,
     * возраст должен быть в диапазоне [0, 150].
     * </p>
     */
    public static class Builder {
        private String name;
        private String email;
        private Integer age;

        /**
         * Устанавливает имя пользователя.
         * @param name имя (не может быть null или пустым)
         * @return текущий Builder для цепочки вызовов
         */
        public Builder name(String name) {
            this.name = name;
            return this;
        }

        /**
         * Устанавливает email пользователя.
         * @param email email (не может быть null или пустым)
         * @return текущий Builder для цепочки вызовов
         */
        public Builder email(String email) {
            this.email = email;
            return this;
        }

        /**
         * Устанавливает возраст пользователя.
         * @param age возраст (должен быть от 0 до 150)
         * @return текущий Builder для цепочки вызовов
         */
        public Builder age(Integer age) {
            this.age = age;
            return this;
        }

        /**
         * Создаёт объект {@link User} после проверки корректности полей.
         *
         * @return новый экземпляр User
         * @throws IllegalArgumentException если имя или email пусты, либо возраст вне допустимого диапазона
         */
        public User build() {
            if (name == null || name.isBlank()) {
                throw new IllegalArgumentException("Имя не может быть пустым");
            }
            if (email == null || email.isBlank()) {
                throw new IllegalArgumentException("Email не может быть пустым");
            }
            if (age == null || age < 0 || age > 150) {
                throw new IllegalArgumentException("Возраст должен быть от 0 до 150");
            }
            return new User(this);
        }
    }
}