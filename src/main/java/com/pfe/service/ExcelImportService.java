package com.pfe.service;

import com.pfe.dao.interfaces.IFiliereDAO;
import com.pfe.model.Etudiant;
import com.pfe.model.Filiere;
import com.pfe.model.Soutenance;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

@Service
public class ExcelImportService {
    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);
    
    @Autowired
    private IFiliereDAO filiereDAO;

    public List<Soutenance> importerSoutenances(InputStream excelFile) throws IOException {
        List<Soutenance> soutenances = new ArrayList<>();
        Map<String, List<Etudiant>> sujetToEtudiants = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String cne = getCellValueAsString(row.getCell(0));
                    String nom = getCellValueAsString(row.getCell(1));
                    String prenom = getCellValueAsString(row.getCell(2));
                    String filiereNom = getCellValueAsString(row.getCell(3));
                    String sujetPfe = getCellValueAsString(row.getCell(4));

                    Filiere filiere = filiereDAO.findByNom(filiereNom);
                    if (filiere == null) {
                        logger.error("Filière inexistante : {} (CNE {})", filiereNom, cne);
                        continue;
                    }

                    Etudiant etudiant = new Etudiant();
                    etudiant.setCne(cne);
                    etudiant.setNom(nom);
                    etudiant.setPrenom(prenom);
                    etudiant.setFiliere(filiere);

                    sujetToEtudiants.computeIfAbsent(sujetPfe, k -> new ArrayList<>()).add(etudiant);
                } catch (Exception e) {
                    logger.error("Erreur lecture ligne Excel", e);
                }
            }

            for (Map.Entry<String, List<Etudiant>> entry : sujetToEtudiants.entrySet()) {
                Soutenance soutenance = new Soutenance();
                soutenance.setTitre(entry.getKey());
                soutenance.setEtudiants(entry.getValue());
                soutenance.setFiliere(entry.getValue().get(0).getFiliere());
                soutenance.setDate(null);
                soutenance.setHeureDebut(null);
                soutenance.setHeureFin(null);
                soutenance.setSalle(null);
                soutenance.setPresident(null);
                soutenance.setJurys(new ArrayList<>());
                soutenances.add(soutenance);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture fichier Excel", e);
            throw new IOException("Erreur lecture fichier Excel", e);
        }
        return soutenances;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }
}