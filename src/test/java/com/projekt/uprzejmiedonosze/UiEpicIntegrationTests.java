package com.projekt.uprzejmiedonosze;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class UiEpicIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void anonymousHomeUsesSharedLayoutAndGuestNavigation() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk())
                .andExpect(view().name("home"))
                .andExpect(content().string(containsString("href=\"#main\"")))
                .andExpect(content().string(containsString("<main id=\"main\"")))
                .andExpect(content().string(containsString("Logowanie")))
                .andExpect(content().string(containsString("Rejestracja")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminViewsExposeRoleAwareNavigationAndLogout() throws Exception {
        mockMvc.perform(get("/paragraphs"))
                .andExpect(status().isOk())
                .andExpect(view().name("paragraphs/list"))
                .andExpect(content().string(containsString("<main id=\"main\"")))
                .andExpect(content().string(containsString("Użytkownicy")))
                .andExpect(content().string(containsString("Wyloguj")));
    }

    @Test
    void registrationValidationShowsWcagErrorSummary() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "")
                        .param("password", "abc")
                        .param("confirmPassword", "xyz")
                        .param("firstName", "")
                        .param("lastName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("Formularz zawiera błędy")));
    }

    @Test
    @WithMockUser
    void notFoundPageUsesStyledErrorTemplate() throws Exception {
        mockMvc.perform(get("/nie-ma-takich-akt"))
                .andExpect(status().isNotFound())
                .andExpect(content().string(containsString("<main id=\"main\"")))
                .andExpect(content().string(containsString("Sprawa nie istnieje")));
    }

    @Test
    void wcagChecklistAndServerErrorTemplateExist() {
        assertThat(Files.exists(Path.of("docs", "WCAG_CHECKLIST.md"))).isTrue();
        assertThat(Files.exists(Path.of("src", "main", "resources", "templates", "error", "500.html"))).isTrue();
    }
}
