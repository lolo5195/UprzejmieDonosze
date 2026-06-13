package com.projekt.uprzejmiedonosze.service;

import com.projekt.uprzejmiedonosze.dto.RegisterForm;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class RegistrationService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public AppUser register(RegisterForm form) {
        if (appUserRepository.existsByUsername(form.getUsername())) {
            throw new UsernameAlreadyTakenException();
        }

        AppUser user = new AppUser();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setRole(Role.USER);
        user.setCreatedAt(LocalDate.now());

        return appUserRepository.save(user);
    }

    public static class UsernameAlreadyTakenException extends RuntimeException {
    }
}
