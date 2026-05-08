package com.crud.config;

import com.crud.entity.Credential;
import com.crud.entity.Role;
import com.crud.entity.User;
import com.crud.repository.CredentialRepository;
import com.crud.repository.RoleRepository;
import com.crud.repository.UserRepository;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final CredentialRepository credentialRepository;
    private final PasswordEncoder passwordEncoder;
    private final TransactionTemplate transactionTemplate;

    @Value("${app.seed.admin-password:}")
    private String adminPassword;

    @Value("${app.seed.user-password:}")
    private String userPassword;

    @PostConstruct
    public void init() {
        if (credentialRepository.count() > 0) {
            return;
        }
        if (adminPassword.isBlank() || userPassword.isBlank()) {
            log.warn("Seed-пароли не заданы (app.seed.admin-password, app.seed.user-password). Пропуск инициализации — API будет недоступен без аутентификации.");
            return;
        }
        transactionTemplate.executeWithoutResult(status -> insertSeedData());
    }

    private void insertSeedData() {
        log.info("Инициализация seed-данных...");

        User adminUser = userRepository.findByEmail("admin@userservice.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("Admin")
                        .email("admin@userservice.local")
                        .age(30)
                        .build()));

        User regularUser = userRepository.findByEmail("user@userservice.local")
                .orElseGet(() -> userRepository.save(User.builder()
                        .name("User")
                        .email("user@userservice.local")
                        .age(25)
                        .build()));

        Role adminRole = roleRepository.findByName("ADMIN")
                .orElseGet(() -> roleRepository.save(Role.builder().name("ADMIN").build()));
        Role userRole = roleRepository.findByName("USER")
                .orElseGet(() -> roleRepository.save(Role.builder().name("USER").build()));

        adminUser.getRoles().clear();
        adminUser.getRoles().add(adminRole);
        adminUser.getRoles().add(userRole);
        
        regularUser.getRoles().clear();
        regularUser.getRoles().add(userRole);

        userRepository.save(adminUser);
        userRepository.save(regularUser);
        userRepository.flush();

        credentialRepository.save(Credential.builder()
                .user(adminUser)
                .username("admin")
                .password(passwordEncoder.encode(adminPassword))
                .enabled(true)
                .build());

        credentialRepository.save(Credential.builder()
                .user(regularUser)
                .username("user")
                .password(passwordEncoder.encode(userPassword))
                .enabled(true)
                .build());

        log.info("Seed-данные созданы: admin/***, user/***");
    }
}
