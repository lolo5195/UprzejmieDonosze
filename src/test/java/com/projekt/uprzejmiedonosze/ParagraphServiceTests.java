package com.projekt.uprzejmiedonosze;

import com.projekt.uprzejmiedonosze.dto.ParagraphForm;
import com.projekt.uprzejmiedonosze.model.Paragraph;
import com.projekt.uprzejmiedonosze.repository.ParagraphRepository;
import com.projekt.uprzejmiedonosze.service.ParagraphService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ParagraphServiceTests {

    private final ParagraphRepository paragraphRepository = mock(ParagraphRepository.class);
    private final ParagraphService paragraphService = new ParagraphService(paragraphRepository);

    @Test
    void saveCreatesParagraphFromForm() {
        ParagraphForm form = paragraphForm(null, "Spóźnienie", "Spóźnienie na zajęcia bez usprawiedliwienia.", 3, true);
        when(paragraphRepository.save(any(Paragraph.class))).thenAnswer(invocation -> invocation.getArgument(0));

        paragraphService.save(form);

        ArgumentCaptor<Paragraph> captor = ArgumentCaptor.forClass(Paragraph.class);
        verify(paragraphRepository).save(captor.capture());

        Paragraph saved = captor.getValue();
        assertThat(saved.getTitle()).isEqualTo("Spóźnienie");
        assertThat(saved.getDescription()).isEqualTo("Spóźnienie na zajęcia bez usprawiedliwienia.");
        assertThat(saved.getSeverity()).isEqualTo(3);
        assertThat(saved.isActive()).isTrue();
    }

    @Test
    void saveUpdatesExistingParagraphFromForm() {
        Paragraph existing = new Paragraph();
        existing.setTitle("Stary tytuł");
        existing.setDescription("Stary opis paragrafu.");
        existing.setSeverity(2);
        existing.setActive(true);

        ParagraphForm form = paragraphForm(7L, "Nowy tytuł", "Nowy opis paragrafu po aktualizacji.", 8, false);
        when(paragraphRepository.findById(7L)).thenReturn(Optional.of(existing));
        when(paragraphRepository.save(existing)).thenReturn(existing);

        paragraphService.save(form);

        assertThat(existing.getTitle()).isEqualTo("Nowy tytuł");
        assertThat(existing.getDescription()).isEqualTo("Nowy opis paragrafu po aktualizacji.");
        assertThat(existing.getSeverity()).isEqualTo(8);
        assertThat(existing.isActive()).isFalse();
        verify(paragraphRepository).save(existing);
    }

    @Test
    void findActiveDelegatesToRepositoryQuery() {
        Paragraph active = new Paragraph();
        active.setTitle("Aktywny paragraf");
        when(paragraphRepository.findByActiveTrue()).thenReturn(List.of(active));

        List<Paragraph> paragraphs = paragraphService.findActive();

        assertThat(paragraphs).containsExactly(active);
        verify(paragraphRepository).findByActiveTrue();
    }

    @Test
    void findFormByIdMapsEntityToDto() {
        Paragraph paragraph = new Paragraph();
        paragraph.setTitle("Hałas");
        paragraph.setDescription("Hałasowanie w czytelni podczas ciszy.");
        paragraph.setSeverity(4);
        paragraph.setActive(false);
        when(paragraphRepository.findById(9L)).thenReturn(Optional.of(paragraph));

        ParagraphForm form = paragraphService.findFormById(9L);

        assertThat(form.getTitle()).isEqualTo("Hałas");
        assertThat(form.getDescription()).isEqualTo("Hałasowanie w czytelni podczas ciszy.");
        assertThat(form.getSeverity()).isEqualTo(4);
        assertThat(form.isActive()).isFalse();
    }

    private ParagraphForm paragraphForm(Long id, String title, String description, int severity, boolean active) {
        ParagraphForm form = new ParagraphForm();
        form.setId(id);
        form.setTitle(title);
        form.setDescription(description);
        form.setSeverity(severity);
        form.setActive(active);
        return form;
    }
}
