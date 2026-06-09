package com.projekt.uprzejmiedonosze.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Entity
@Table(name = "app_users")
@Getter
@Setter
@NoArgsConstructor
public class AppUser {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message="Login jest wymagany")
    @Size(min=3, max=50, message="Login musi mieć od 3 do 50 znaków")
    @Column(unique = true, nullable = false)
    private String username;

    @NotBlank(message="Hasło jest wymagane")
    @Size(min=6, message="Hasło musi mieć co najmniej 6 znaków")
    @Column(nullable = false)
    private String password;

    @NotBlank(message="Imię jest wymagane")
    @Size(min =2, max=30)
    private String firstName;

    @NotBlank(message="Nazwisko jest wymagane")
    @Size(min =2, max=30)
    private String lastName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role = Role.USER;

    @PastOrPresent
    private LocalDate createdAt = LocalDate.now();
}
