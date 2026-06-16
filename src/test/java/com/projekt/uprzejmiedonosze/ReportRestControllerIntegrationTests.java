package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.model.Report;
import com.projekt.uprzejmiedonosze.model.ReportStatus;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class ReportRestControllerIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ParagraphRepository paragraphRepository;

    @Autowired
    private ReportRepository reportRepository;

    private AppUser owner;
    private AppUser otherUser;
    private AppUser admin;
    private AppUser accusedUser;
    private Paragraph paragraph;

    @BeforeEach
    void setUp() {
        cleanUp();
        owner = saveUser("owner", Role.USER);
        otherUser = saveUser("other", Role.USER);
        admin = saveUser("admin", Role.ADMIN);
        accusedUser = saveUser("accused-rest", Role.USER);
        paragraph = saveParagraph();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    @Test
    @WithMockUser(username = "owner", roles = "USER")
    void ownerCanUpdateOwnReport() throws Exception {
        Report report = saveReport(owner);

        mockMvc.perform(put("/api/reports/{id}", report.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(owner, "Zmieniony tytul")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Zmieniony tytul"))
                .andExpect(jsonPath("$.authorUsername").value("owner"));

        Report updated = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(updated.getAuthor().getUsername()).isEqualTo("owner");
        assertThat(updated.getTitle()).isEqualTo("Zmieniony tytul");
    }

    @Test
    @WithMockUser(username = "other", roles = "USER")
    void userCannotUpdateSomeoneElsesReport() throws Exception {
        Report report = saveReport(owner);

        mockMvc.perform(put("/api/reports/{id}", report.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(otherUser, "Nielegalna zmiana")))
                .andExpect(status().isForbidden());

        Report unchanged = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(unchanged.getTitle()).isEqualTo("Pierwotny donos");
        assertThat(unchanged.getAuthor().getUsername()).isEqualTo("owner");
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanUpdateSomeoneElsesReportWithoutChangingOwner() throws Exception {
        Report report = saveReport(owner);

        mockMvc.perform(put("/api/reports/{id}", report.getId())
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson(admin, "Zmiana administratora")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("Zmiana administratora"))
                .andExpect(jsonPath("$.authorUsername").value("owner"));

        Report updated = reportRepository.findById(report.getId()).orElseThrow();
        assertThat(updated.getAuthor().getUsername()).isEqualTo("owner");
    }

    @Test
    @WithMockUser(username = "other", roles = "USER")
    void userCannotDeleteSomeoneElsesReport() throws Exception {
        Report report = saveReport(owner);

        mockMvc.perform(delete("/api/reports/{id}", report.getId()).with(csrf()))
                .andExpect(status().isForbidden());

        assertThat(reportRepository.existsById(report.getId())).isTrue();
    }

    @Test
    @WithMockUser(username = "admin", roles = "ADMIN")
    void adminCanDeleteSomeoneElsesReport() throws Exception {
        Report report = saveReport(owner);

        mockMvc.perform(delete("/api/reports/{id}", report.getId()).with(csrf()))
                .andExpect(status().isNoContent());

        assertThat(reportRepository.existsById(report.getId())).isFalse();
    }

    private String requestJson(AppUser authenticatedAuthor, String title) {
        return """
                {
                  "title": "%s",
                  "description": "Opis donosu testowego wyslany przez REST.",
                  "accusedUserId": %d,
                  "eventDate": "%s",
                  "status": "ACCEPTED",
                  "authorId": %d,
                  "paragraphId": %d
                }
                """.formatted(
                title,
                accusedUser.getId(),
                LocalDate.now(),
                authenticatedAuthor.getId(),
                paragraph.getId()
        );
    }

    private Report saveReport(AppUser author) {
        Report report = new Report();
        report.setTitle("Pierwotny donos");
        report.setDescription("Opis pierwotnego donosu testowego.");
        report.setAccusedUser(accusedUser);
        report.setEventDate(LocalDate.now());
        report.setAuthor(author);
        report.setParagraph(paragraph);
        report.setStatus(ReportStatus.NEW);
        return reportRepository.save(report);
    }

    private Paragraph saveParagraph() {
        Paragraph savedParagraph = new Paragraph();
        savedParagraph.setTitle("Paragraf REST");
        savedParagraph.setDescription("Opis paragrafu uzywanego w testach REST.");
        savedParagraph.setSeverity(3);
        savedParagraph.setActive(true);
        return paragraphRepository.save(savedParagraph);
    }

    private AppUser saveUser(String username, Role role) {
        AppUser user = appUserRepository.findByUsername(username).orElseGet(AppUser::new);
        user.setUsername(username);
        user.setPassword("sekret123");
        user.setFirstName("Jan");
        user.setLastName("Testowy");
        user.setRole(role);
        return appUserRepository.save(user);
    }

    private void cleanUp() {
        reportRepository.deleteAll();
        paragraphRepository.deleteAll();
        appUserRepository.findByUsername("owner").ifPresent(appUserRepository::delete);
        appUserRepository.findByUsername("other").ifPresent(appUserRepository::delete);
        appUserRepository.findByUsername("accused-rest").ifPresent(appUserRepository::delete);
        appUserRepository.findByUsername("admin").ifPresent(appUserRepository::delete);
    }
}
