package com.projekt.uprzejmiedonosze.dto;

import com.projekt.uprzejmiedonosze.model.Role;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class AppUserForm {

    private Long id;

    @NotBlank(message = "Login jest wymagany")
    @Size(min = 3, max = 50, message = "Login musi mieć od 3 do 50 znaków")
    private String username;

    @NotBlank(message = "Hasło jest wymagane")
    @Size(min = 6, message = "Hasło musi mieć co najmniej 6 znaków")
    private String password;

    @NotBlank(message = "Imię jest wymagane")
    @Size(min = 2, max = 30, message = "Imię musi mieć od 2 do 30 znaków")
    private String firstName;

    @NotBlank(message = "Nazwisko jest wymagane")
    @Size(min = 2, max = 30, message = "Nazwisko musi mieć od 2 do 30 znaków")
    private String lastName;

    @NotNull(message = "Rola jest wymagana")
    private Role role = Role.USER;
}
