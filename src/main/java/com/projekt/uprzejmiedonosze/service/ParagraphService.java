package com.projekt.uprzejmiedonosze.service;

import com.projekt.uprzejmiedonosze.dto.ParagraphForm;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class ParagraphService {

    private final ParagraphRepository paragraphRepository;

    public ParagraphService(ParagraphRepository paragraphRepository) {
        this.paragraphRepository = paragraphRepository;
    }

    public List<Paragraph> findAll() {
        return paragraphRepository.findAll(Sort.by(Sort.Direction.ASC, "title"));
    }

    public List<Paragraph> findActive() {
        return paragraphRepository.findByActiveTrue();
    }

    public ParagraphForm createForm() {
        ParagraphForm form = new ParagraphForm();
        form.setSeverity(1);
        return form;
    }

    public ParagraphForm findFormById(Long id) {
        Paragraph paragraph = findById(id);
        return toForm(paragraph);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public Paragraph save(ParagraphForm form) {
        Paragraph paragraph = form.getId() == null ? new Paragraph() : findById(form.getId());

        paragraph.setTitle(form.getTitle());
        paragraph.setDescription(form.getDescription());
        paragraph.setSeverity(form.getSeverity());
        paragraph.setActive(form.isActive());

        return paragraphRepository.save(paragraph);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @Transactional
    public void delete(Long id) {
        paragraphRepository.deleteById(id);
    }

    private Paragraph findById(Long id) {
        return paragraphRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Nie znaleziono paragrafu o id: " + id));
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
}
