package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.dto.ReportForm;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.model.Report;
import com.projekt.uprzejmiedonosze.model.ReportStatus;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import org.springframework.security.core.Authentication;
import org.springframework.data.domain.Sort;
import java.util.List;
import org.springframework.security.core.Authentication;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.CookieValue;

@Controller
@RequestMapping("/reports")
public class ReportController {

    private final ReportRepository reportRepository;
    private final AppUserRepository appUserRepository;
    private final ParagraphRepository paragraphRepository;

    public ReportController(
            ReportRepository reportRepository,
            AppUserRepository appUserRepository,
            ParagraphRepository paragraphRepository
    ) {
        this.reportRepository = reportRepository;
        this.appUserRepository = appUserRepository;
        this.paragraphRepository = paragraphRepository;
    }
    @GetMapping
    public String listReports(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String paragraphId,
            @RequestParam(required = false) String sort,
            @RequestParam(required = false) String dir,
            @RequestParam(required = false) String clearFilters,

            @CookieValue(value = "reportStatus", required = false) String cookieStatus,
            @CookieValue(value = "reportParagraphId", required = false) String cookieParagraphId,
            @CookieValue(value = "reportSort", required = false) String cookieSort,
            @CookieValue(value = "reportDir", required = false) String cookieDir,

            HttpServletResponse response,
            Model model
    ) {
        if (clearFilters != null) {
            deleteCookie(response, "reportStatus");
            deleteCookie(response, "reportParagraphId");
            deleteCookie(response, "reportSort");
            deleteCookie(response, "reportDir");

            return "redirect:/reports";
        }

        boolean requestHasFilterParams = status != null
                || paragraphId != null
                || sort != null
                || dir != null;

        if (!requestHasFilterParams) {
            status = cookieStatus;
            paragraphId = cookieParagraphId;
            sort = cookieSort;
            dir = cookieDir;
        } else {
            addCookie(response, "reportStatus", status);
            addCookie(response, "reportParagraphId", paragraphId);
            addCookie(response, "reportSort", sort);
            addCookie(response, "reportDir", dir);
        }

        ReportStatus selectedStatus = parseReportStatus(status);
        Long selectedParagraphId = parseLong(paragraphId);

        String safeSort = sanitizeSort(sort);
        Sort.Direction direction = "asc".equalsIgnoreCase(dir)
                ? Sort.Direction.ASC
                : Sort.Direction.DESC;

        Sort sorting = Sort.by(direction, safeSort);

        List<Report> reports;

        if (selectedStatus != null && selectedParagraphId != null) {
            reports = reportRepository.findByStatusAndParagraphId(selectedStatus, selectedParagraphId, sorting);
        } else if (selectedStatus != null) {
            reports = reportRepository.findByStatus(selectedStatus, sorting);
        } else if (selectedParagraphId != null) {
            reports = reportRepository.findByParagraphId(selectedParagraphId, sorting);
        } else {
            reports = reportRepository.findAll(sorting);
        }

        model.addAttribute("reports", reports);
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("paragraphs", paragraphRepository.findAll());

        model.addAttribute("selectedStatus", selectedStatus);
        model.addAttribute("selectedParagraphId", selectedParagraphId);
        model.addAttribute("selectedSort", safeSort);
        model.addAttribute("selectedDir", direction.name().toLowerCase());

        return "reports/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("reportForm", new ReportForm());
        addFormLists(model);
        return "reports/form";
    }

    @PostMapping("/save")
    public String saveReport(
            @Valid @ModelAttribute("reportForm") ReportForm reportForm,
            BindingResult bindingResult,
            Model model,
            Authentication authentication
    ) {
        if (bindingResult.hasErrors()) {
            addFormLists(model);
            return "reports/form";
        }

        Report report = toEntity(reportForm, authentication.getName());
        reportRepository.save(report);
        return "redirect:/reports";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model, Authentication authentication) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono donosu o id: " + id));

        if (!canModifyReport(report, authentication)) {
            return "redirect:/access-denied";
        }

        model.addAttribute("reportForm", toForm(report));
        addFormLists(model);
        return "reports/form";
    }

    @PostMapping("/{id}/delete")
    public String deleteReport(@PathVariable Long id, Authentication authentication) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono donosu o id: " + id));

        if (!canModifyReport(report, authentication)) {
            return "redirect:/access-denied";
        }

        reportRepository.delete(report);
        return "redirect:/reports";
    }

    @GetMapping("{id}")
    public String showReportDetails(@PathVariable Long id, Model model) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono donosu o id: " + id));

        model.addAttribute("report", report);
        return "reports/detail";
    }
    private boolean canModifyReport(Report report, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        boolean isAuthor = report.getAuthor() != null
                && report.getAuthor().getUsername().equals(authentication.getName());

        return isAdmin || isAuthor;
    }

    private void addFormLists(Model model) {
        model.addAttribute("paragraphs", paragraphRepository.findAll());
        model.addAttribute("statuses", ReportStatus.values());
    }
    private void addCookie(HttpServletResponse response, String name, String value) {
        Cookie cookie = new Cookie(name, value == null ? "" : value);
        cookie.setPath("/reports");
        cookie.setMaxAge(30 * 24 * 60 * 60); // 30 dni
        response.addCookie(cookie);
    }

    private void deleteCookie(HttpServletResponse response, String name) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath("/reports");
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }

    private Long parseLong(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return Long.parseLong(value);
        } catch (NumberFormatException exception) {
            return null;
        }
    }

    private ReportStatus parseReportStatus(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        try {
            return ReportStatus.valueOf(value);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

    private String sanitizeSort(String sort) {
        if (sort == null || sort.isBlank()) {
            return "eventDate";
        }

        return switch (sort) {
            case "title", "status", "eventDate", "createdAt" -> sort;
            default -> "eventDate";
        };
    }

    private ReportForm toForm(Report report) {
        ReportForm form = new ReportForm();
        form.setId(report.getId());
        form.setTitle(report.getTitle());
        form.setDescription(report.getDescription());
        form.setAccusedStudentName(report.getAccusedStudentName());
        form.setEventDate(report.getEventDate());
        form.setStatus(report.getStatus());

        if (report.getAuthor() != null) {
            form.setAuthorId(report.getAuthor().getId());
        }

        if (report.getParagraph() != null) {
            form.setParagraphId(report.getParagraph().getId());
        }

        return form;
    }

    private Report toEntity(ReportForm form, String username) {
        Report report;

        if (form.getId() != null) {
            report = reportRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono donosu o id: " + form.getId()));
        } else {
            report = new Report();
        }

        report.setTitle(form.getTitle());
        report.setDescription(form.getDescription());
        report.setAccusedStudentName(form.getAccusedStudentName());
        report.setEventDate(form.getEventDate());
        report.setStatus(form.getStatus() != null ? form.getStatus() : ReportStatus.NEW);

        if (report.getCreatedAt() == null) {
            report.setCreatedAt(LocalDate.now());
        }

        if (report.getAuthor() == null) {
            AppUser author = appUserRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono zalogowanego użytkownika: " + username));
            report.setAuthor(author);
        }

        Paragraph paragraph = paragraphRepository.findById(form.getParagraphId())
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono paragrafu o id: " + form.getParagraphId()));
        report.setParagraph(paragraph);

        return report;
    }
}
