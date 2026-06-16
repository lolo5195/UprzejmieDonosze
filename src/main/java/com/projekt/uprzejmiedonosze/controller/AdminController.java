package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.model.ReportStatus;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.LinkedHashMap;
import java.util.Map;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AppUserRepository appUserRepository;
    private final ReportRepository reportRepository;
    private final ParagraphRepository paragraphRepository;

    public AdminController(
            AppUserRepository appUserRepository,
            ReportRepository reportRepository,
            ParagraphRepository paragraphRepository
    ) {
        this.appUserRepository = appUserRepository;
        this.reportRepository = reportRepository;
        this.paragraphRepository = paragraphRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", appUserRepository.count());
        model.addAttribute("totalReports", reportRepository.count());
        model.addAttribute("totalParagraphs", paragraphRepository.count());
        model.addAttribute("statuses", ReportStatus.values());
        model.addAttribute("reportStatusCounts", countReportsByStatus());

        return "admin/dashboard";
    }

    private Map<String, Long> countReportsByStatus() {
        Map<String, Long> counts = new LinkedHashMap<>();

        for (ReportStatus status : ReportStatus.values()) {
            counts.put(status.name(), reportRepository.countByStatus(status));
        }

        return counts;
    }
}
