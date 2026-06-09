package com.projekt.uprzejmiedonosze.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "paragraphs")
@Getter
@Setter
@NoArgsConstructor
public class Paragraph {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Nazwa paragrafu jest wymagana")
    @Size(min = 3, max = 60, message = "Nazwa musi mieć od 3 do 60 znaków")
    @Column(nullable = false)
    private String title;

    @NotBlank(message = "Opis jest wymagany")
    @Size(min = 10, max = 500, message = "Opis musi mieć od 10 do 500 znaków")
    @Column(nullable = false, length = 500)
    private String description;

    @Min(value = 1, message = "Waga musi być minimum 1")
    @Max(value = 10, message = "Waga może być maksymalnie 10")
    private int severity;

    private boolean active = true;
}