package com.projekt.uprzejmiedonosze.repository;

import com.projekt.uprzejmiedonosze.model.Report;
import com.projekt.uprzejmiedonosze.model.ReportStatus;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {

    List<Report> findByAuthorId(Long authorId);

    List<Report> findByStatusAndParagraphId(ReportStatus status, Long paragraphId, Sort sort);

    List<Report> findByStatus(ReportStatus status, Sort sort);

    List<Report> findByParagraphId(Long paragraphId, Sort sort);

    List<Report> findAll(Sort sort);
}