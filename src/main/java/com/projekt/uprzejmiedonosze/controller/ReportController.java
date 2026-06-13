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
    public String listReports(Model model) {
        model.addAttribute("reports", reportRepository.findAll());
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
    public String showEditForm(@PathVariable Long id, Model model) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono donosu o id: " + id));

        model.addAttribute("reportForm", toForm(report));
        addFormLists(model);
        return "reports/form";
    }

    @PostMapping("/{id}/delete")
    public String deleteReport(@PathVariable Long id) {
        reportRepository.deleteById(id);
        return "redirect:/reports";
    }

    @GetMapping("{id}")
    public String showReportDetails(@PathVariable Long id, Model model) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono donosu o id: " + id));

        model.addAttribute("report", report);
        return "reports/detail";
    }

    private void addFormLists(Model model) {
        model.addAttribute("authors", appUserRepository.findAll());
        model.addAttribute("paragraphs", paragraphRepository.findAll());
        model.addAttribute("statuses", ReportStatus.values());
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
