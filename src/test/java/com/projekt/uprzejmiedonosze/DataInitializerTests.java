package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.config.DataInitializer;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DataInitializerTests {

    private final AppUserRepository appUserRepository = mock(AppUserRepository.class);
    private final PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Test
    void createsEncodedAdminWhenNoAdminExists() {
        when(appUserRepository.existsByRole(Role.ADMIN)).thenReturn(false);
        when(appUserRepository.findByUsername("admin")).thenReturn(Optional.empty());

        DataInitializer initializer = new DataInitializer(
                appUserRepository,
                passwordEncoder,
                "admin",
                "admin123",
                "Administrator",
                "Systemu"
        );

        initializer.run();

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(appUserRepository).save(captor.capture());

        AppUser admin = captor.getValue();
        assertThat(admin.getUsername()).isEqualTo("admin");
        assertThat(admin.getRole()).isEqualTo(Role.ADMIN);
        assertThat(admin.getPassword()).isNotEqualTo("admin123");
        assertThat(passwordEncoder.matches("admin123", admin.getPassword())).isTrue();
    }

    @Test
    void doesNotCreateDuplicateWhenAdminExists() {
        when(appUserRepository.existsByRole(Role.ADMIN)).thenReturn(true);

        DataInitializer initializer = new DataInitializer(
                appUserRepository,
                passwordEncoder,
                "admin",
                "admin123",
                "Administrator",
                "Systemu"
        );

        initializer.run();

        verify(appUserRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
