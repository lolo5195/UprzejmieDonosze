package com.projekt.uprzejmiedonosze.controller.api;

import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.model.Report;
import com.projekt.uprzejmiedonosze.model.ReportStatus;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/reports")
public class ReportRestController {

    private final ReportRepository reportRepository;
    private final AppUserRepository appUserRepository;
    private final ParagraphRepository paragraphRepository;

    public ReportRestController(
            ReportRepository reportRepository,
            AppUserRepository appUserRepository,
            ParagraphRepository paragraphRepository
    ) {
        this.reportRepository = reportRepository;
        this.appUserRepository = appUserRepository;
        this.paragraphRepository = paragraphRepository;
    }

    @GetMapping
    public List<ReportResponse> getAllReports() {
        return reportRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    @GetMapping("/{id}")
    public ReportResponse getReportById(@PathVariable Long id) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono donosu"));

        return toResponse(report);
    }

    @PostMapping
    public ResponseEntity<ReportResponse> createReport(
            @Valid @RequestBody ReportRequest request,
            Authentication authentication
    ) {
        Report report = new Report();
        fillReportFromRequest(report, request, authentication);

        if (report.getCreatedAt() == null) {
            report.setCreatedAt(LocalDate.now());
        }

        if (report.getStatus() == null) {
            report.setStatus(ReportStatus.NEW);
        }

        Report savedReport = reportRepository.save(report);

        return ResponseEntity.status(HttpStatus.CREATED).body(toResponse(savedReport));
    }

    @PutMapping("/{id}")
    public ReportResponse updateReport(
            @PathVariable Long id,
            @Valid @RequestBody ReportRequest request,
            Authentication authentication
    ) {
        Report report = reportRepository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono donosu"));

        fillReportFromRequest(report, request, authentication);

        Report savedReport = reportRepository.save(report);

        return toResponse(savedReport);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteReport(@PathVariable Long id) {
        if (!reportRepository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono donosu");
        }

        reportRepository.deleteById(id);

        return ResponseEntity.noContent().build();
    }

    private void fillReportFromRequest(Report report, ReportRequest request, Authentication authentication) {
        AppUser author = appUserRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie znaleziono zalogowanego użytkownika"));

        if (!author.getId().equals(request.authorId())) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Nie możesz utworzyć donosu jako inny użytkownik");
        }

        if (author.getId().equals(request.accusedUserId())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie możesz zgłosić siebie");
        }

        AppUser accusedUser = appUserRepository.findById(request.accusedUserId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie znaleziono zgłaszanego użytkownika"));

        Paragraph paragraph = paragraphRepository.findById(request.paragraphId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST, "Nie znaleziono paragrafu"));

        report.setTitle(request.title());
        report.setDescription(request.description());
        report.setAccusedUser(accusedUser);
        report.setEventDate(request.eventDate());
        report.setAuthor(author);
        report.setParagraph(paragraph);

        if (request.status() != null) {
            report.setStatus(request.status());
        } else if (report.getStatus() == null) {
            report.setStatus(ReportStatus.NEW);
        }
    }

    private ReportResponse toResponse(Report report) {
        AppUser accusedUser = report.getAccusedUser();

        return new ReportResponse(
                report.getId(),
                report.getTitle(),
                report.getDescription(),
                accusedUser != null ? accusedUser.getId() : null,
                accusedUser != null ? accusedUser.getUsername() : null,
                formatFullName(accusedUser),
                report.getEventDate(),
                report.getCreatedAt(),
                report.getStatus(),
                report.getAuthor() != null ? report.getAuthor().getId() : null,
                report.getAuthor() != null ? report.getAuthor().getUsername() : null,
                report.getParagraph() != null ? report.getParagraph().getId() : null,
                report.getParagraph() != null ? report.getParagraph().getTitle() : null
        );
    }

    private String formatFullName(AppUser user) {
        if (user == null) {
            return null;
        }

        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        String fullName = (firstName + " " + lastName).trim();

        return fullName.isBlank() ? null : fullName;
    }

    public record ReportRequest(
            @NotBlank(message = "Tytuł jest wymagany")
            @Size(min = 3, max = 80, message = "Tytuł musi mieć od 3 do 80 znaków")
            String title,

            @NotBlank(message = "Opis jest wymagany")
            @Size(min = 10, max = 1000, message = "Opis musi mieć od 10 do 1000 znaków")
            String description,

            @NotNull(message = "Zgłaszany użytkownik jest wymagany")
            Long accusedUserId,

            @PastOrPresent(message = "Data zdarzenia nie może być z przyszłości")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate eventDate,

            ReportStatus status,

            @NotNull(message = "Autor jest wymagany")
            Long authorId,

            @NotNull(message = "Paragraf jest wymagany")
            Long paragraphId
    ) {
    }

    public record ReportResponse(
            Long id,
            String title,
            String description,
            Long accusedUserId,
            String accusedUsername,
            String accusedFullName,
            LocalDate eventDate,
            LocalDate createdAt,
            ReportStatus status,
            Long authorId,
            String authorUsername,
            Long paragraphId,
            String paragraphTitle
    ) {
    }
}
