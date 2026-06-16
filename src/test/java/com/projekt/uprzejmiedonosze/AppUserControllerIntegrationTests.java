package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AppUserControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreatesUserWithEncodedInitialPassword() throws Exception {
        mockMvc.perform(post("/users/save")
                        .with(csrf())
                        .param("username", "nowy")
                        .param("password", "sekret123")
                        .param("firstName", "Jan")
                        .param("lastName", "Nowak")
                        .param("role", "USER"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        AppUser user = appUserRepository.findByUsername("nowy").orElseThrow();
        assertThat(user.getPassword()).isNotEqualTo("sekret123");
        assertThat(passwordEncoder.matches("sekret123", user.getPassword())).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreateUserRequiresInitialPassword() throws Exception {
        mockMvc.perform(post("/users/save")
                        .with(csrf())
                        .param("username", "nowy")
                        .param("password", "")
                        .param("firstName", "Jan")
                        .param("lastName", "Nowak")
                        .param("role", "USER"))
                .andExpect(status().isOk())
                .andExpect(view().name("users/form"))
                .andExpect(model().attributeHasFieldErrors("userForm", "password"));

        assertThat(appUserRepository.existsByUsername("nowy")).isFalse();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEditFormDoesNotShowPasswordField() throws Exception {
        AppUser user = saveUser("edytowany", "sekret123", Role.USER);

        mockMvc.perform(get("/users/{id}/edit", user.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("users/form"))
                .andExpect(content().string(containsString("Edytuj użytkownika")))
                .andExpect(content().string(not(containsString("id=\"password\""))));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminEditPreservesExistingEncodedPassword() throws Exception {
        AppUser user = saveUser("edytowany", "stare123", Role.USER);
        String existingPassword = user.getPassword();

        mockMvc.perform(post("/users/save")
                        .with(csrf())
                        .param("id", user.getId().toString())
                        .param("username", "zmieniony")
                        .param("password", "nowe123")
                        .param("firstName", "Adam")
                        .param("lastName", "Kowalski")
                        .param("role", "ADMIN"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/users"));

        AppUser updated = appUserRepository.findById(user.getId()).orElseThrow();
        assertThat(updated.getUsername()).isEqualTo("zmieniony");
        assertThat(updated.getFirstName()).isEqualTo("Adam");
        assertThat(updated.getLastName()).isEqualTo("Kowalski");
        assertThat(updated.getRole()).isEqualTo(Role.ADMIN);
        assertThat(updated.getPassword()).isEqualTo(existingPassword);
        assertThat(passwordEncoder.matches("stare123", updated.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("nowe123", updated.getPassword())).isFalse();
    }

    private AppUser saveUser(String username, String rawPassword, Role role) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setFirstName("Jan");
        user.setLastName("Nowak");
        user.setRole(role);
        return appUserRepository.save(user);
    }
}
