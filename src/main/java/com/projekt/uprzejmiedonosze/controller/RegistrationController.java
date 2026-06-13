package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.dto.RegisterForm;
import com.projekt.uprzejmiedonosze.service.RegistrationService;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

@Controller
public class RegistrationController {

    private final RegistrationService registrationService;

    public RegistrationController(RegistrationService registrationService) {
        this.registrationService = registrationService;
    }

    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("registerForm", new RegisterForm());
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @Valid @ModelAttribute("registerForm") RegisterForm registerForm,
            BindingResult bindingResult
    ) {
        validatePasswords(registerForm, bindingResult);

        if (bindingResult.hasErrors()) {
            return "register";
        }

        try {
            registrationService.register(registerForm);
        } catch (RegistrationService.UsernameAlreadyTakenException exception) {
            bindingResult.rejectValue("username", "username.taken", "Ten login jest już zajęty");
            return "register";
        }

        return "redirect:/login?registered";
    }

    private void validatePasswords(RegisterForm registerForm, BindingResult bindingResult) {
        if (registerForm.getPassword() == null || registerForm.getConfirmPassword() == null) {
            return;
        }

        if (!registerForm.getPassword().equals(registerForm.getConfirmPassword())) {
            bindingResult.rejectValue("confirmPassword", "password.mismatch", "Hasła muszą być takie same");
        }
    }
}
