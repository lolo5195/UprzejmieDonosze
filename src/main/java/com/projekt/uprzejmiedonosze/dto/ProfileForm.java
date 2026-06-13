package com.projekt.uprzejmiedonosze.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ProfileForm {

    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 2, max = 30, message = "Imię musi mieć od 2 do 30 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 2, max = 30, message = "Nazwisko musi mieć od 2 do 30 znaków")
    private String lastName;
}
