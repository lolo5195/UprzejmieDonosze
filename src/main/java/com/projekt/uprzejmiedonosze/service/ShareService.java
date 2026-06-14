package com.projekt.uprzejmiedonosze.service;

import com.projekt.uprzejmiedonosze.model.Report;
import com.projekt.uprzejmiedonosze.repository.ReportRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
@Transactional(readOnly = true)
public class ShareService {

    private final ReportRepository reportRepository;

    public ShareService(ReportRepository reportRepository) {
        this.reportRepository = reportRepository;
    }

    public Report findByShareToken(String token) {
        return reportRepository.findByShareToken(token)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Nie znaleziono udostępnionego donosu"));
    }
}