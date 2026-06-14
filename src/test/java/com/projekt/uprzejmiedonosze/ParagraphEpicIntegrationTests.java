package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.model.Report;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsString;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.flash;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class ParagraphEpicIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ParagraphRepository paragraphRepository;

    @Autowired
    private ReportRepository reportRepository;

    @Autowired
    private AppUserRepository appUserRepository;

    @BeforeEach
    void setUp() {
        cleanUp();
    }

    @AfterEach
    void tearDown() {
        cleanUp();
    }

    private void cleanUp() {
        reportRepository.deleteAll();
        paragraphRepository.deleteAll();
        appUserRepository.findByUsername("reporter").ifPresent(appUserRepository::delete);
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCreatesParagraphThroughServiceBackedController() throws Exception {
        mockMvc.perform(post("/paragraphs/save")
                        .with(csrf())
                        .param("title", "Spóźnienie")
                        .param("description", "Spóźnienie na zajęcia bez usprawiedliwienia.")
                        .param("severity", "3")
                        .param("active", "true"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/paragraphs"));

        Paragraph paragraph = paragraphRepository.findAll().stream()
                .filter(saved -> "Spóźnienie".equals(saved.getTitle()))
                .findFirst()
                .orElseThrow();

        assertThat(paragraph.getDescription()).isEqualTo("Spóźnienie na zajęcia bez usprawiedliwienia.");
        assertThat(paragraph.getSeverity()).isEqualTo(3);
        assertThat(paragraph.isActive()).isTrue();
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void paragraphValidationShowsWcagErrorSummaryAndFieldError() throws Exception {
        mockMvc.perform(post("/paragraphs/save")
                        .with(csrf())
                        .param("title", "Hałas")
                        .param("description", "Hałasowanie w czytelni podczas ciszy.")
                        .param("severity", "11")
                        .param("active", "true"))
                .andExpect(status().isOk())
                .andExpect(view().name("paragraphs/form"))
                .andExpect(model().attributeHasFieldErrors("paragraphForm", "severity"))
                .andExpect(content().string(containsString("role=\"alert\"")))
                .andExpect(content().string(containsString("Formularz zawiera błędy")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void paragraphListVisuallyMarksInactiveParagraphs() throws Exception {
        saveParagraph("Nieaktywny paragraf", false);

        mockMvc.perform(get("/paragraphs"))
                .andExpect(status().isOk())
                .andExpect(view().name("paragraphs/list"))
                .andExpect(content().string(containsString("Nieaktywny")))
                .andExpect(content().string(containsString("row-muted")));
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void deletingUsedParagraphShowsReadableMessageAndKeepsParagraph() throws Exception {
        Paragraph paragraph = saveParagraph("Używany paragraf", true);
        saveReport(paragraph);

        mockMvc.perform(post("/paragraphs/{id}/delete", paragraph.getId()).with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/paragraphs"))
                .andExpect(flash().attribute("errorMessage", containsString("Nie można usunąć paragrafu")));

        assertThat(paragraphRepository.existsById(paragraph.getId())).isTrue();
    }

    private Paragraph saveParagraph(String title, boolean active) {
        Paragraph paragraph = new Paragraph();
        paragraph.setTitle(title);
        paragraph.setDescription("Opis paragrafu testowego dla przewinienia.");
        paragraph.setSeverity(5);
        paragraph.setActive(active);
        return paragraphRepository.save(paragraph);
    }

    private void saveReport(Paragraph paragraph) {
        AppUser author = appUserRepository.findByUsername("reporter").orElseGet(() -> {
            AppUser user = new AppUser();
            user.setUsername("reporter");
            user.setPassword("sekret123");
            user.setFirstName("Jan");
            user.setLastName("Nowak");
            user.setRole(Role.USER);
            return appUserRepository.save(user);
        });

        Report report = new Report();
        report.setTitle("Donos testowy");
        report.setDescription("Opis donosu testowego powiązanego z paragrafem.");
        report.setAccusedStudentName("Adam Kowalski");
        report.setEventDate(LocalDate.now());
        report.setAuthor(author);
        report.setParagraph(paragraph);
        reportRepository.save(report);
    }
}
