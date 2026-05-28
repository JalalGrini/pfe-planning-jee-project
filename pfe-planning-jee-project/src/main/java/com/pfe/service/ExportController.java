package com.pfe.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/export")
public class ExportController {

    @Autowired
    private ExportService exportService;

    @GetMapping("/excel")
    public ResponseEntity<byte[]> exportExcel() {
        if (!exportService.hasPlannedData()) {
            return ResponseEntity.status(409).build();
        }
        byte[] data = exportService.exporterPlanningExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "planning_soutenances.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/pdf")
    public ResponseEntity<byte[]> exportPdf() {
        if (!exportService.hasPlannedData()) {
            return ResponseEntity.status(409).build();
        }
        byte[] data = exportService.exporterPlanningPDF();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "planning_soutenances.pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/affectation-excel")
    public ResponseEntity<byte[]> exportAffectationExcel() {
        if (!exportService.hasAffectationData()) {
            return ResponseEntity.status(409).build();
        }
        byte[] data = exportService.exporterAffectationExcel();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"));
        headers.setContentDispositionFormData("attachment", "affectation_encadrants_etudiants.xlsx");
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }

    @GetMapping("/affectation-pdf")
    public ResponseEntity<byte[]> exportAffectationPdf() {
        if (!exportService.hasAffectationData()) {
            return ResponseEntity.status(409).build();
        }
        byte[] data = exportService.exporterAffectationPDF();
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_PDF);
        headers.setContentDispositionFormData("attachment", "affectation_encadrants_etudiants.pdf");
        return ResponseEntity.ok()
                .headers(headers)
                .body(data);
    }
}
