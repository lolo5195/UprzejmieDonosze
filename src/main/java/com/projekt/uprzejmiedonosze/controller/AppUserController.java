package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.dto.AppUserForm;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.model.Role;
import com.projekt.uprzejmiedonosze.repository.AppUserRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Controller
@RequestMapping("/users")
public class AppUserController {

    private final AppUserRepository appUserRepository;

    public AppUserController(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @GetMapping
    public String listUsers(Model model) {
        model.addAttribute("users", appUserRepository.findAll());
        return "users/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("userForm", new AppUserForm());
        addFormLists(model);
        return "users/form";
    }

    @PostMapping("/save")
    public String saveUser(
            @Valid @ModelAttribute("userForm") AppUserForm userForm,
            BindingResult bindingResult,
            Model model
    ) {
        if (bindingResult.hasErrors()) {
            addFormLists(model);
            return "users/form";
        }

        AppUser user = toEntity(userForm);
        appUserRepository.save(user);
        return "redirect:/users";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        AppUser user = appUserRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o id: " + id));

        model.addAttribute("userForm", toForm(user));
        addFormLists(model);
        return "users/form";
    }

    @GetMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        appUserRepository.deleteById(id);
        return "redirect:/users";
    }

    private void addFormLists(Model model) {
        model.addAttribute("roles", Role.values());
    }

    private AppUserForm toForm(AppUser user) {
        AppUserForm form = new AppUserForm();
        form.setId(user.getId());
        form.setUsername(user.getUsername());
        form.setPassword(user.getPassword());
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        form.setRole(user.getRole());
        return form;
    }

    private AppUser toEntity(AppUserForm form) {
        AppUser user;

        if (form.getId() != null) {
            user = appUserRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono użytkownika o id: " + form.getId()));
        } else {
            user = new AppUser();
        }

        user.setUsername(form.getUsername());
        user.setPassword(form.getPassword());
        user.setFirstName(form.getFirstName());
        user.setLastName(form.getLastName());
        user.setRole(form.getRole() != null ? form.getRole() : Role.USER);

        if (user.getCreatedAt() == null) {
            user.setCreatedAt(LocalDate.now());
        }

        return user;
    }
}
