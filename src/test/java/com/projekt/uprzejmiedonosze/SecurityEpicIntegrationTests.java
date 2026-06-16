package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.dto.UserStats;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.service.StatisticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrlPattern;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityEpicIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private UserDetailsService userDetailsService;

    @MockitoBean
    private StatisticsService statisticsService;

    @BeforeEach
    void setUp() {
        appUserRepository.deleteAll();
    }

    @Test
    void anonymousUserIsRedirectedFromReportsToLogin() throws Exception {
        mockMvc.perform(get("/reports"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenParagraphs() throws Exception {
        mockMvc.perform(get("/paragraphs"))
                .andExpect(status().isOk())
                .andExpect(view().name("paragraphs/list"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotOpenParagraphsAdminArea() throws Exception {
        mockMvc.perform(get("/paragraphs/new"))
                .andExpect(status().isForbidden());
    }

    @Test
    void passwordEncoderUsesBCryptAndMatchesRawPassword() {
        String encoded = passwordEncoder.encode("tajne123");

        assertThat(encoded).startsWith("$2");
        assertThat(passwordEncoder.matches("tajne123", encoded)).isTrue();
    }

    @Test
    void customUserDetailsServiceLoadsUserWithRoleAuthority() {
        saveUser("jan", "tajne123", Role.USER);

        UserDetails userDetails = userDetailsService.loadUserByUsername("jan");

        assertThat(userDetails.getUsername()).isEqualTo("jan");
        assertThat(userDetails.getAuthorities())
                .extracting("authority")
                .contains("ROLE_USER");
    }

    @Test
    void registrationCreatesUserWithEncodedPassword() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "nowy")
                        .param("password", "sekret123")
                        .param("confirmPassword", "sekret123")
                        .param("firstName", "Jan")
                        .param("lastName", "Nowak"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?registered"));

        AppUser user = appUserRepository.findByUsername("nowy").orElseThrow();
        assertThat(user.getRole()).isEqualTo(Role.USER);
        assertThat(user.getPassword()).isNotEqualTo("sekret123");
        assertThat(passwordEncoder.matches("sekret123", user.getPassword())).isTrue();
    }

    @Test
    void registrationRejectsPasswordMismatch() throws Exception {
        mockMvc.perform(post("/register")
                        .with(csrf())
                        .param("username", "nowy")
                        .param("password", "sekret123")
                        .param("confirmPassword", "inne123")
                        .param("firstName", "Jan")
                        .param("lastName", "Nowak"))
                .andExpect(status().isOk())
                .andExpect(view().name("register"))
                .andExpect(model().attributeHasFieldErrors("registerForm", "confirmPassword"));

        assertThat(appUserRepository.existsByUsername("nowy")).isFalse();
    }

    @Test
    @WithMockUser(username = "profil")
    void profileShowsAuthenticatedUserData() throws Exception {
        AppUser user = saveUser("profil", "tajne123", Role.USER);
        when(statisticsService.getStatsForUser(user.getId())).thenReturn(new UserStats(4, 2));

        mockMvc.perform(get("/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeExists("user", "userStats", "changePasswordForm"))
                .andExpect(content().string(containsString("Złożone donosy")))
                .andExpect(content().string(containsString("Otrzymane donosy")))
                .andExpect(content().string(containsString("<dd>4</dd>")))
                .andExpect(content().string(containsString("<dd>2</dd>")));
    }

    @Test
    @WithMockUser(username = "profil")
    void profilePasswordChangeVerifiesOldPasswordAndEncodesNewOne() throws Exception {
        saveUser("profil", "stare123", Role.USER);

        mockMvc.perform(post("/profile/password")
                        .with(csrf())
                        .param("currentPassword", "stare123")
                        .param("newPassword", "nowe123")
                        .param("confirmNewPassword", "nowe123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/profile?passwordChanged"));

        AppUser user = appUserRepository.findByUsername("profil").orElseThrow();
        assertThat(passwordEncoder.matches("nowe123", user.getPassword())).isTrue();
        assertThat(passwordEncoder.matches("stare123", user.getPassword())).isFalse();
    }

    @Test
    @WithMockUser(username = "profil")
    void profilePasswordChangeRejectsInvalidOldPassword() throws Exception {
        AppUser user = saveUser("profil", "stare123", Role.USER);
        when(statisticsService.getStatsForUser(user.getId())).thenReturn(new UserStats(0, 0));

        mockMvc.perform(post("/profile/password")
                        .with(csrf())
                        .param("currentPassword", "bledne123")
                        .param("newPassword", "nowe123")
                        .param("confirmNewPassword", "nowe123"))
                .andExpect(status().isOk())
                .andExpect(view().name("profile"))
                .andExpect(model().attributeHasFieldErrors("changePasswordForm", "currentPassword"));
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
