package com.projekt.uprzejmiedonosze.repository;

import com.projekt.uprzejmiedonosze.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    List<Report> findByAuthorId(Long authorId);
}