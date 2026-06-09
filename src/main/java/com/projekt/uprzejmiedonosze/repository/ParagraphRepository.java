package com.projekt.uprzejmiedonosze.repository;

import com.projekt.uprzejmiedonosze.model.Paragraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ParagraphRepository extends JpaRepository<Paragraph, Long> {
}