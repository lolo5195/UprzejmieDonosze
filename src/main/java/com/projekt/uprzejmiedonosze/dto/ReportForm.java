package com.projekt.uprzejmiedonosze.dto;

import com.projekt.uprzejmiedonosze.model.ReportStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
public class ReportForm {

    private Long id;

    @NotBlank(message = "Tytuł jest wymagany")
    @Size(min = 3, max = 80, message = "Tytuł musi mieć od 3 do 80 znaków")
    private String title;

    @NotBlank(message = "Opis jest wymagany")
    @Size(min = 10, max = 1000, message = "Opis musi mieć od 10 do 1000 znaków")
    private String description;

    @NotNull(message = "Zgłaszany użytkownik jest wymagany")
    private Long accusedUserId;

    @PastOrPresent(message = "Data przewinienia nie może być z przyszłości")
    @NotNull(message = "Data nie może być pusta")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate eventDate;

    private ReportStatus status = ReportStatus.NEW;


    private Long authorId;

    @NotNull(message = "Paragraf jest wymagany")
    private Long paragraphId;
}
