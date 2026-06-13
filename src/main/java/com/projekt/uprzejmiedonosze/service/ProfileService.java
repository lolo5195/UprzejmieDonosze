package com.projekt.uprzejmiedonosze.service;

import com.projekt.uprzejmiedonosze.dto.ChangePasswordForm;
import com.projekt.uprzejmiedonosze.dto.ProfileForm;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProfileService {

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileService(AppUserRepository appUserRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public AppUser getByUsername(String username) {
        return appUserRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika: " + username));
    }

    @Transactional
    public void updateProfile(String username, ProfileForm form) {
        AppUser user = getByUsername(username);
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
    }

    @Transactional
    public void changePassword(String username, ChangePasswordForm form) {
        AppUser user = getByUsername(username);

        if (!passwordEncoder.matches(form.getCurrentPassword(), user.getPassword())) {
            throw new InvalidCurrentPasswordException();
        }

        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
    }

    public static class InvalidCurrentPasswordException extends RuntimeException {
    }
}
