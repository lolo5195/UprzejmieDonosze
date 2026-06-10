package com.projekt.uprzejmiedonosze.controller;

import com.projekt.uprzejmiedonosze.dto.ParagraphForm;
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
        model.addAttribute("paragraphForm", new ParagraphForm());
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

        Paragraph paragraph = toEntity(paragraphForm);
        paragraphRepository.save(paragraph);
        return "redirect:/paragraphs";
    }

    @GetMapping("/{id}/edit")
    public String showEditForm(@PathVariable Long id, Model model) {
        Paragraph paragraph = paragraphRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono paragrafu o id: " + id));

        model.addAttribute("paragraphForm", toForm(paragraph));
        return "paragraphs/form";
    }

    @GetMapping("/{id}/delete")
    public String deleteParagraph(@PathVariable Long id) {
        paragraphRepository.deleteById(id);
        return "redirect:/paragraphs";
    }

    private ParagraphForm toForm(Paragraph paragraph) {
        ParagraphForm form = new ParagraphForm();
        form.setId(paragraph.getId());
        form.setTitle(paragraph.getTitle());
        form.setDescription(paragraph.getDescription());
        form.setSeverity(paragraph.getSeverity());
        form.setActive(paragraph.isActive());
        return form;
    }

    private Paragraph toEntity(ParagraphForm form) {
        Paragraph paragraph;

        if (form.getId() != null) {
            paragraph = paragraphRepository.findById(form.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono paragrafu o id: " + form.getId()));
        } else {
            paragraph = new Paragraph();
        }

        paragraph.setTitle(form.getTitle());
        paragraph.setDescription(form.getDescription());
        paragraph.setSeverity(form.getSeverity());
        paragraph.setActive(form.isActive());

        return paragraph;
    }
}
