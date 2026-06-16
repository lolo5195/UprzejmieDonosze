package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.model.Report;
import com.projekt.uprzejmiedonosze.model.ReportStatus;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.allOf;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasEntry;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.model;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@SpringBootTest
@AutoConfigureMockMvc
class AdminDashboardIntegrationTests {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AppUserRepository appUserRepository;

    @Autowired
    private ParagraphRepository paragraphRepository;

    @Autowired
    private ReportRepository reportRepository;

    @BeforeEach
    void setUp() {
        reportRepository.deleteAll();
        paragraphRepository.deleteAll();
        appUserRepository.deleteAll();
    }

    @Test
    void anonymousUserIsRedirectedFromAdminDashboardToLogin() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void userCannotOpenAdminDashboard() throws Exception {
        mockMvc.perform(get("/admin"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminCanOpenDashboardAndSeeCounts() throws Exception {
        saveDashboardData();

        mockMvc.perform(get("/admin"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attribute("totalUsers", 2L))
                .andExpect(model().attribute("totalReports", 3L))
                .andExpect(model().attribute("totalParagraphs", 1L))
                .andExpect(model().attribute("reportStatusCounts", allOf(
                        hasEntry("NEW", 1L),
                        hasEntry("ACCEPTED", 1L),
                        hasEntry("REJECTED", 1L)
                )))
                .andExpect(content().string(containsString("Panel admina")))
                .andExpect(content().string(containsString("href=\"/admin\"")))
                .andExpect(content().string(containsString("href=\"/users\"")))
                .andExpect(content().string(containsString("href=\"/paragraphs\"")))
                .andExpect(content().string(containsString("href=\"/reports\"")))
                .andExpect(content().string(containsString("Użytkownicy")))
                .andExpect(content().string(containsString("<td>2</td>")))
                .andExpect(content().string(containsString("Donosy")))
                .andExpect(content().string(containsString("<td>3</td>")))
                .andExpect(content().string(containsString("Paragrafy")))
                .andExpect(content().string(containsString("<td>1</td>")))
                .andExpect(content().string(containsString(">NEW</span>")))
                .andExpect(content().string(containsString(">ACCEPTED</span>")))
                .andExpect(content().string(containsString(">REJECTED</span>")));
    }

    private void saveDashboardData() {
        AppUser author = saveUser("author", "Jan", "Nowak");
        AppUser accused = saveUser("accused", "Adam", "Kowalski");
        Paragraph paragraph = saveParagraph();

        saveReport(author, accused, paragraph, ReportStatus.NEW);
        saveReport(author, accused, paragraph, ReportStatus.ACCEPTED);
        saveReport(author, accused, paragraph, ReportStatus.REJECTED);
    }

    private AppUser saveUser(String username, String firstName, String lastName) {
        AppUser user = new AppUser();
        user.setUsername(username);
        user.setPassword("sekret123");
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setRole(Role.USER);
        return appUserRepository.save(user);
    }

    private Paragraph saveParagraph() {
        Paragraph paragraph = new Paragraph();
        paragraph.setTitle("Spóźnienie");
        paragraph.setDescription("Spóźnienie na zajęcia bez usprawiedliwienia.");
        paragraph.setSeverity(3);
        paragraph.setActive(true);
        return paragraphRepository.save(paragraph);
    }

    private void saveReport(
            AppUser author,
            AppUser accused,
            Paragraph paragraph,
            ReportStatus status
    ) {
        Report report = new Report();
        report.setTitle("Donos " + status);
        report.setDescription("Opis testowego donosu administracyjnego.");
        report.setAccusedUser(accused);
        report.setEventDate(LocalDate.now());
        report.setStatus(status);
        report.setAuthor(author);
        report.setParagraph(paragraph);
        reportRepository.save(report);
    }
}
