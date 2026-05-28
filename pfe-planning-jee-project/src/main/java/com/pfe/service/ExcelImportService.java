package com.pfe.service;

import com.pfe.dao.interfaces.IFiliereDAO;
import com.pfe.model.Etudiant;
import com.pfe.model.Filiere;
import com.pfe.model.Professeur;
import com.pfe.model.Salle;
import com.pfe.model.Soutenance;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.IOException;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.*;

@Service
public class ExcelImportService {
    public Map<String, List<?>> importerFichierComplet(InputStream excelFile) throws IOException {
        Map<String, List<?>> result = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            // Find sheets
            Sheet etudiantSheet = workbook.getSheet("Etudiants");
            if (etudiantSheet == null) etudiantSheet = workbook.getSheetAt(0);
            
            Sheet profSheet = workbook.getSheet("Professeurs");
            if (profSheet == null && workbook.getNumberOfSheets() > 1) profSheet = workbook.getSheetAt(1);
            
            Sheet salleSheet = workbook.getSheet("Salles");
            if (salleSheet == null && workbook.getNumberOfSheets() > 2) salleSheet = workbook.getSheetAt(2);
            
            // Process Etudiants (binome detection integrated)
            List<Soutenance> soutenances = extractSoutenancesFromSheet(etudiantSheet, null);
            result.put("etudiants", soutenances);
            
            // Process Professeurs
            if (profSheet != null) {
                List<Professeur> profs = extractProfesseursFromSheet(profSheet);
                result.put("professeurs", profs);
            }
            
            // Process Salles
            if (salleSheet != null) {
                List<Salle> salles = extractSallesFromSheet(salleSheet);
                result.put("salles", salles);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture fichier complet", e);
            throw new IOException("Erreur lecture fichier complet", e);
        }
        return result;
    }

    public List<Soutenance> detecterBinomes(List<Soutenance> soutenances) {
        // Implementation provided just in case, but detection is handled above
        return soutenances;
    }

    private static final Logger logger = LoggerFactory.getLogger(ExcelImportService.class);
    
    @Autowired
    private IFiliereDAO filiereDAO;

    public List<Soutenance> importerSoutenances(InputStream excelFile) throws IOException {
        return importerSoutenances(excelFile, null);
    }

    public List<Soutenance> importerSoutenances(InputStream excelFile, String sourceFileName) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            return extractSoutenancesFromSheet(workbook.getSheetAt(0), sourceFileName);
        }
    }

    private List<Soutenance> extractSoutenancesFromSheet(Sheet sheet, String sourceFileName) {
        List<Soutenance> soutenances = new ArrayList<>();
        String inferredFiliere = inferFiliereFromFileName(sourceFileName);

        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try {
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                try {
                    String cne = getCellValueAsString(row.getCell(0));
                    String nom = getCellValueAsString(row.getCell(1));
                    String prenom = getCellValueAsString(row.getCell(2));
                    if (cne.isBlank() || nom.isBlank() || prenom.isBlank()) {
                        continue;
                    }
                    if (isHeaderLike(cne) || isHeaderLike(nom) || isHeaderLike(prenom)) {
                        continue;
                    }
                    String filiereNom = getCellValueAsString(row.getCell(3));
                    if (inferredFiliere != null && !inferredFiliere.isBlank()) {
                        filiereNom = inferredFiliere;
                    } else if (filiereNom.isBlank() || filiereNom.contains("@")) {
                        filiereNom = "Genie Informatique";
                    }
                    if (isHeaderLike(filiereNom)) {
                        continue;
                    }
                    String sujetPfe = getCellValueAsString(row.getCell(4));
                    if (sujetPfe.isBlank() || sujetPfe.contains("@")) {
                        sujetPfe = "Sujet-" + cne;
                    }
                    if (isHeaderLike(sujetPfe)) {
                        sujetPfe = "Sujet-" + cne;
                    }

                    Filiere filiere = findFiliere(filiereNom);
                    if (filiere == null) {
                        logger.error("Filière inexistante : {} (CNE {})", filiereNom, cne);
                        continue;
                    }

                    Etudiant etudiant = new Etudiant();
                    etudiant.setCne(cne);
                    etudiant.setNom(nom);
                    etudiant.setPrenom(prenom);
                    etudiant.setFiliere(filiere);

                    // Change: Disabling automatic grouping by subject. Each student gets their own Soutenance entry.
                    String uniqueKey = UUID.randomUUID().toString();
                    binomeMap.computeIfAbsent(uniqueKey, k -> new ArrayList<>()).add(etudiant);
                    filiereMap.putIfAbsent(uniqueKey, filiere);
                    originalSubjectMap.putIfAbsent(uniqueKey, sujetPfe);
                } catch (Exception e) {
                    logger.error("Erreur lecture ligne Excel", e);
                }
            }
            
            for (Map.Entry<String, List<Etudiant>> entry : binomeMap.entrySet()) {
                Soutenance soutenance = new Soutenance();
                soutenance.setTitre(originalSubjectMap.get(entry.getKey()));
                soutenance.setEtudiants(entry.getValue());
                soutenance.setFiliere(filiereMap.get(entry.getKey()));
                soutenance.setDate(null);
                soutenance.setHeureDebut(null);
                soutenance.setHeureFin(null);
                soutenance.setSalle(null);
                soutenance.setPresident(null);
                soutenance.setJurys(new ArrayList<>());
                soutenances.add(soutenance);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture sheet Excel", e);
        }
        return soutenances;
    }

    private String inferFiliereFromFileName(String sourceFileName) {
        if (sourceFileName == null || sourceFileName.isBlank()) {
            return null;
        }
        String normalized = normalize(sourceFileName);
        Map<String, String> mapping = new LinkedHashMap<>();
        mapping.put("transformation digitale", "Transformation Digitale et Intelligence Artificielle");
        mapping.put("intelligence artificielle", "Transformation Digitale et Intelligence Artificielle");
        mapping.put("genie civil", "Genie Civil");
        mapping.put("genie informatique", "Genie Informatique");
        mapping.put("ingenierie des donnees", "Ingenierie des donnees");
        mapping.put("genie energetique", "Genie energetique et energies renouvelables");
        mapping.put("energies renouvelables", "Genie energetique et energies renouvelables");
        mapping.put("genie de l eau", "Genie de l Eau et de l Environnement");
        mapping.put("environnement", "Genie de l Eau et de l Environnement");
        mapping.put("genie mecanique", "Genie Mecanique");

        for (Map.Entry<String, String> entry : mapping.entrySet()) {
            if (normalized.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        return null;
    }

    public List<Professeur> importerProfesseurs(InputStream excelFile) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            return extractProfesseursFromSheet(workbook.getSheetAt(0));
        }
    }

    private List<Professeur> extractProfesseursFromSheet(Sheet sheet) {
        List<Professeur> professeurs = new ArrayList<>();

        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try {
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String nom = getCellValueAsString(row.getCell(0));
                String prenom = getCellValueAsString(row.getCell(1));
                if (nom.isBlank() || prenom.isBlank() || isHeaderRowNomPrenom(nom, prenom)) {
                    continue;
                }

                Professeur professeur = new Professeur();
                professeur.setNom(nom);
                professeur.setPrenom(prenom);
                professeur.setDiscipline(getCellValueAsString(row.getCell(2)));
                String email = getCellValueAsString(row.getCell(4));
                if (!email.isBlank()) {
                    professeur.setEmail(email);
                }
                professeurs.add(professeur);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture sheet professeurs", e);
        }

        return professeurs;
    }

    public List<Salle> importerSalles(InputStream excelFile) throws IOException {
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            return extractSallesFromSheet(workbook.getSheetAt(0));
        }
    }

    private List<Salle> extractSallesFromSheet(Sheet sheet) {
        List<Salle> salles = new ArrayList<>();

        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try {
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();

            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String nom = getCellValueAsString(row.getCell(0));
                if (nom.isBlank() || isHeaderLike(nom)) {
                    continue;
                }

                Salle salle = new Salle();
                salle.setNom(nom);
                salle.setCapacite(parseInt(getCellValueAsString(row.getCell(1)), 1));
                salle.setDisponible(parseBoolean(getCellValueAsString(row.getCell(2))));
                salles.add(salle);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture sheet salles", e);
        }

        return salles;
    }

    private String getCellValueAsString(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private Filiere findFiliere(String filiereNom) {
        if (filiereNom == null || filiereNom.trim().isEmpty()) {
            return null;
        }
        Filiere exactMatch = filiereDAO.findByNom(filiereNom);
        if (exactMatch != null) {
            return exactMatch;
        }

        String normalizedInput = normalize(filiereNom);
        Filiere normalizedMatch = filiereDAO.findAll().stream()
                .filter(f -> normalize(f.getNom()).equals(normalizedInput))
                .findFirst()
                .orElse(null);
        if (normalizedMatch != null) {
            return normalizedMatch;
        }

        Filiere created = new Filiere();
        created.setNom(filiereNom.trim());
        created.setCode(generateUniqueFiliereCode(filiereNom));
        filiereDAO.save(created);
        return created;
    }

    private String generateUniqueFiliereCode(String filiereNom) {
        String base = normalize(filiereNom).replaceAll("[^a-z0-9]", "");
        if (base.isBlank()) {
            base = "FIL";
        } else if (base.length() > 3) {
            base = base.substring(0, 3).toUpperCase(Locale.ROOT);
        } else {
            base = base.toUpperCase(Locale.ROOT);
        }

        Set<String> existingCodes = new HashSet<>();
        for (Filiere filiere : filiereDAO.findAll()) {
            if (filiere.getCode() != null && !filiere.getCode().isBlank()) {
                existingCodes.add(filiere.getCode().trim().toUpperCase(Locale.ROOT));
            }
        }

        String candidate = base;
        int suffix = 2;
        while (existingCodes.contains(candidate)) {
            candidate = base + suffix;
            suffix++;
        }
        return candidate;
    }

    private String normalize(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private int parseInt(String value, int defaultValue) {
        try {
            return Integer.parseInt(value);
        } catch (Exception e) {
            return defaultValue;
        }
    }

    private boolean parseBoolean(String value) {
        String normalized = normalize(value);
        return normalized.isBlank()
                || "oui".equals(normalized)
                || "true".equals(normalized)
                || "1".equals(normalized)
                || "disponible".equals(normalized);
    }

    public int countEtudiantsRows(InputStream excelFile) throws IOException {
        int count = 0;
        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String cne = getCellValueAsString(row.getCell(0));
                String nom = getCellValueAsString(row.getCell(1));
                String prenom = getCellValueAsString(row.getCell(2));
                if (cne.isBlank() || nom.isBlank() || prenom.isBlank()) {
                    continue;
                }
                if (isHeaderLike(cne) || isHeaderLike(nom) || isHeaderLike(prenom)) {
                    continue;
                }
                count++;
            }
        } catch (Exception e) {
            throw new IOException("Erreur comptage lignes etudiants", e);
        }
        return count;
    }

    public int countProfesseursRows(InputStream excelFile) throws IOException {
        int count = 0;
        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String nom = getCellValueAsString(row.getCell(0));
                String prenom = getCellValueAsString(row.getCell(1));
                if (nom.isBlank() || prenom.isBlank() || isHeaderRowNomPrenom(nom, prenom)) {
                    continue;
                }
                count++;
            }
        } catch (Exception e) {
            throw new IOException("Erreur comptage lignes professeurs", e);
        }
        return count;
    }

    public int countSallesRows(InputStream excelFile) throws IOException {
        int count = 0;
        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();
            if (rowIterator.hasNext()) rowIterator.next();
            while (rowIterator.hasNext()) {
                Row row = rowIterator.next();
                String nom = getCellValueAsString(row.getCell(0));
                if (nom.isBlank() || isHeaderLike(nom)) {
                    continue;
                }
                count++;
            }
        } catch (Exception e) {
            throw new IOException("Erreur comptage lignes salles", e);
        }
        return count;
    }

    private boolean isHeaderLike(String value) {
        String n = normalize(value);
        return "nom".equals(n)
                || "prenom".equals(n)
                || "cne".equals(n)
                || "salle".equals(n)
                || "filiere".equals(n)
                || "capacite".equals(n)
                || "specialite".equals(n)
                || "sujet".equals(n)
                || "sujet pfe".equals(n)
                || "intitule".equals(n)
                || n.startsWith("nom ")
                || n.startsWith("prenom ")
                || n.contains("nom prenom")
                || n.contains("prenom nom");
    }

    private boolean isHeaderRowNomPrenom(String nom, String prenom) {
        String n1 = normalize(nom);
        String n2 = normalize(prenom);
        if (isHeaderLike(n1) || isHeaderLike(n2)) {
            return true;
        }
        String merged = (n1 + " " + n2).trim();
        return merged.contains("nom prenom")
                || merged.contains("prenom nom")
                || merged.contains("nom de famille")
                || merged.contains("nom complet");
    }
}
