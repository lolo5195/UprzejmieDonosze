package com.projekt.uprzejmiedonosze.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "reports")
@Getter
@Setter
@NoArgsConstructor
public class Report {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tytuł jest wymagany")
    @Size(min = 3, max = 80, message = "Tytuł musi mieć od 3 do 80 znaków")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Opis jest wymagany")
    @Size(min = 10, max = 1000, message = "Opis musi mieć od 10 do 1000 znaków")
    @Column(nullable = false, length = 1000)
    private String description;

    @ManyToOne
    @JoinColumn(name = "accused_user_id")
    private AppUser accusedUser;

    @PastOrPresent(message = "Data przewinienia nie może być z przyszłości")
    @NotNull(message = "Data nie może być pusta")
    private LocalDate eventDate;

    @PastOrPresent
    @NotNull(message = "Data nie może być pusta")
    private LocalDate createdAt = LocalDate.now();

    @Enumerated(EnumType.STRING)
    private ReportStatus status = ReportStatus.NEW;

    @ManyToOne
    @JoinColumn(name = "author_id")
    private AppUser author;

    @ManyToOne
    @JoinColumn(name = "paragraph_id")
    private Paragraph paragraph;

    @Column(unique = true)
    private String shareToken;
}
