package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.dto.ParagraphForm;
import com.projekt.uprzejmiedonosze.service.ParagraphService;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/paragraphs")
public class ParagraphController {

    private final ParagraphService paragraphService;

    public ParagraphController(ParagraphService paragraphService) {
        this.paragraphService = paragraphService;
    }

    @GetMapping
    public String listParagraphs(Model model) {
        model.addAttribute("paragraphs", paragraphService.findAll());
        return "paragraphs/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("paragraphForm", paragraphService.createForm());
        return "paragraphs/form";
    }

    @PostMapping("/save")
    public String saveParagraph(
            @Valid @ModelAttribute("paragraphForm") ParagraphForm paragraphForm,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "paragraphs/form";
        }

        paragraphService.save(paragraphForm);
        return "redirect:/paragraphs";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        model.addAttribute("paragraphForm", paragraphService.findFormById(id));
        return "paragraphs/form";
    }

    @PostMapping("/{id}/delete")
    public String deleteParagraph(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            paragraphService.delete(id);
            redirectAttributes.addFlashAttribute("successMessage", "Paragraf został usunięty.");
        } catch (DataIntegrityViolationException exception) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Nie można usunąć paragrafu powiązanego z istniejącymi donosami. Oznacz go jako nieaktywny."
            );
        }
        return "redirect:/paragraphs";
    }
}
