package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.dto.ChangePasswordForm;
import com.projekt.uprzejmiedonosze.dto.ProfileForm;
import com.projekt.uprzejmiedonosze.model.AppUser;
import com.projekt.uprzejmiedonosze.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public String profile(Authentication authentication, Model model) {
        addProfileModel(authentication.getName(), model);
        model.addAttribute("changePasswordForm", new ChangePasswordForm());
        return "profile";
    }

    @GetMapping("/edit")
    public String editProfile(Authentication authentication, Model model) {
        AppUser user = profileService.getByUsername(authentication.getName());
        model.addAttribute("profileForm", toProfileForm(user));
        return "profile-edit";
    }

    @PostMapping("/edit")
    public String updateProfile(
            Authentication authentication,
            @Valid @ModelAttribute("profileForm") ProfileForm profileForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "profile-edit";
        }

        profileService.updateProfile(authentication.getName(), profileForm);
        return "redirect:/profile?updated";
    }

    @PostMapping("/password")
    public String changePassword(
            Authentication authentication,
            @Valid @ModelAttribute("changePasswordForm") ChangePasswordForm changePasswordForm,
            BindingResult bindingResult,
            Model model
    ) {
        validateNewPassword(changePasswordForm, bindingResult);

        if (!bindingResult.hasErrors()) {
            try {
                profileService.changePassword(authentication.getName(), changePasswordForm);
                return "redirect:/profile?passwordChanged";
            } catch (ProfileService.InvalidCurrentPasswordException exception) {
                bindingResult.rejectValue("currentPassword", "password.invalid", "Obecne hasło jest nieprawidłowe");
            }
        }

        addProfileModel(authentication.getName(), model);
        return "profile";
    }

    private void addProfileModel(String username, Model model) {
        model.addAttribute("user", profileService.getByUsername(username));
    }

    private ProfileForm toProfileForm(AppUser user) {
        ProfileForm form = new ProfileForm();
        form.setFirstName(user.getFirstName());
        form.setLastName(user.getLastName());
        return form;
    }

    private void validateNewPassword(ChangePasswordForm form, BindingResult bindingResult) {
        if (form.getNewPassword() == null || form.getConfirmNewPassword() == null) {
            return;
        }

        if (!form.getNewPassword().equals(form.getConfirmNewPassword())) {
            bindingResult.rejectValue("confirmNewPassword", "password.mismatch", "Nowe hasła muszą być takie same");
        }
    }
}
