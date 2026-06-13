package com.projekt.uprzejmiedonosze.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class ChangePasswordForm {

    @NotBlank(message = "Obecne hasło jest wymagane")
    private String currentPassword;

    @NotBlank(message = "Nowe hasło jest wymagane")
    @Size(min = 6, message = "Nowe hasło musi mieć co najmniej 6 znaków")
    private String newPassword;

    @NotBlank(message = "Potwierdzenie nowego hasła jest wymagane")
    private String confirmNewPassword;
}
