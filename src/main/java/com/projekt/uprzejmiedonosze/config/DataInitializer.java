package com.projekt.uprzejmiedonosze.config;

import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Component
public class DataInitializer implements CommandLineRunner {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminUsername;
    private final String adminPassword;
    private final String adminFirstName;
    private final String adminLastName;

    public DataInitializer(
            AppUserRepository appUserRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.admin.username:admin}") String adminUsername,
            @Value("${app.admin.password:admin123}") String adminPassword,
            @Value("${app.admin.first-name:Administrator}") String adminFirstName,
            @Value("${app.admin.last-name:Systemu}") String adminLastName
    ) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminUsername = adminUsername;
        this.adminPassword = adminPassword;
        this.adminFirstName = adminFirstName;
        this.adminLastName = adminLastName;
    }

    @Override
    public void run(String... args) {
        if (appUserRepository.existsByRole(Role.ADMIN)) {
            return;
        }

        AppUser admin = appUserRepository.findByUsername(adminUsername).orElseGet(AppUser::new);
        admin.setUsername(adminUsername);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setFirstName(adminFirstName);
        admin.setLastName(adminLastName);
        admin.setRole(Role.ADMIN);

        if (admin.getCreatedAt() == null) {
            admin.setCreatedAt(LocalDate.now());
        }

        appUserRepository.save(admin);
    }
}
