package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/paragraphs")
public class ParagraphController {

    private final ParagraphRepository paragraphRepository;

    public ParagraphController(ParagraphRepository paragraphRepository) {
        this.paragraphRepository = paragraphRepository;
    }

    @GetMapping
    public String listParagraphs(Model model) {
        model.addAttribute("paragraphs", paragraphRepository.findAll());
        return "paragraphs/list";
    }

    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("paragraph", new Paragraph());
        return "paragraphs/form";
    }

    @PostMapping("/save")
    public String saveParagraph(
            @Valid @ModelAttribute("paragraph") Paragraph paragraph,
            BindingResult bindingResult
    ) {
        if (bindingResult.hasErrors()) {
            return "paragraphs/form";
        }

        paragraphRepository.save(paragraph);
        return "redirect:/paragraphs";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Paragraph paragraph = paragraphRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono paragrafu o id: " + id));

        model.addAttribute("paragraph", paragraph);
        return "paragraphs/form";
    }

    @GetMapping("/{id}/delete")
    public String deleteParagraph(@PathVariable Long id) {
        paragraphRepository.deleteById(id);
        return "redirect:/paragraphs";
    }
}