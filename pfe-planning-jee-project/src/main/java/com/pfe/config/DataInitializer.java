package com.pfe.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import com.pfe.dao.interfaces.*;
import com.pfe.model.*;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.InputStream;
import java.text.Normalizer;
import java.util.Locale;

@Component
public class DataInitializer {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    
    @Autowired
    private IFiliereDAO filiereDAO;
    @Autowired
    private ISalleDAO salleDAO;
    @Autowired
    private IProfesseurDAO professeurDAO;
    @Autowired
    private IEtudiantDAO etudiantDAO;

    @EventListener(ContextRefreshedEvent.class)
    public void onApplicationEvent(ContextRefreshedEvent event) {
        logger.info("Initialisation des données...");
        initFilieres();
        // initSalles();
        // initProfesseurs();
        // initEtudiants();
        logger.info("Initialisation terminée.");
    }

    private void initFilieres() {
        if (filiereDAO.count() == 0) {
            String[] filieres = {"Ingénierie des Données", "Génie Informatique", "Transformation Digitale & IA"};
            for (String nom : filieres) {
                Filiere f = new Filiere();
                f.setNom(nom);
                f.setCode(nom.substring(0, 3).toUpperCase());
                filiereDAO.save(f);
            }
        }
    }

    private void initSalles() {
        if (salleDAO.count() == 0) {
            for (int i = 1; i <= 4; i++) {
                Salle s = new Salle();
                s.setNom("Salle 10" + i);
                s.setCapacite(30);
                s.setDisponible(true);
                salleDAO.save(s);
            }
        }
    }

    private void initProfesseurs() {
        if (professeurDAO.count() == 0) {
            try (InputStream is = getClass().getClassLoader().getResourceAsStream("data/Liste des Profs.xlsx")) {
                if (is == null) { logger.error("Liste des Profs.xlsx introuvable"); return; }
                Workbook wb = WorkbookFactory.create(is);
                Sheet sheet = wb.getSheetAt(0);
                boolean first = true;
                for (Row row : sheet) {
                    if (first) { first = false; continue; }
                    String nom = getCellValue(row.getCell(0));
                    String prenom = getCellValue(row.getCell(1));
                    if (nom.isBlank() || prenom.isBlank() || isHeaderRowNomPrenom(nom, prenom)) {
                        continue;
                    }
                    Professeur p = new Professeur();
                    p.setNom(nom);
                    p.setPrenom(prenom);
                    p.setDiscipline(getCellValue(row.getCell(2)));
                    professeurDAO.save(p);
                }
                wb.close();
            } catch (Exception e) { logger.error("Erreur lecture profs", e); }
        }
    }

    private void initEtudiants() {
        if (etudiantDAO.count() == 0) {
            String[][] files = {
                {"data/Ingénierie des données 3_Email.xlsx", "Ingénierie des Données"},
                {"data/Génie Informatique 3 Option GL_Email.xlsx", "Génie Informatique"},
                {"data/Transformation Digitale & Intelligence Artificielle 3_Email.xlsx", "Transformation Digitale & IA"}
            };
            for (String[] file : files) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(file[0])) {
                    if (is == null) { logger.error("Fichier introuvable: {}", file[0]); continue; }
                    Filiere f = filiereDAO.findByNom(file[1]);
                    if (f == null) continue;
                    Workbook wb = WorkbookFactory.create(is);
                    Sheet sheet = wb.getSheetAt(0);
                    boolean first = true;
                    for (Row row : sheet) {
                        if (first) { first = false; continue; }
                        String cne = getCellValue(row.getCell(0));
                        String nom = getCellValue(row.getCell(1));
                        String prenom = getCellValue(row.getCell(2));
                        if (cne.isBlank() || nom.isBlank() || prenom.isBlank() || isHeaderRowNomPrenom(nom, prenom) || isHeaderLike(cne)) {
                            continue;
                        }
                        Etudiant e = new Etudiant();
                        e.setCne(cne);
                        e.setNom(nom);
                        e.setPrenom(prenom);
                        e.setEmailPerso(getCellValue(row.getCell(3)));
                        e.setEmailAcad(getCellValue(row.getCell(4)));
                        e.setFiliere(f);
                        etudiantDAO.save(e);
                    }
                    wb.close();
                } catch (Exception e) { logger.error("Erreur lecture étudiants", e); }
            }
        }
    }

    private String getCellValue(Cell cell) {
        if (cell == null) return "";
        return switch (cell.getCellType()) {
            case STRING -> cell.getStringCellValue().trim();
            case NUMERIC -> String.valueOf((long) cell.getNumericCellValue());
            default -> "";
        };
    }

    private String normalize(String value) {
        if (value == null) return "";
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private boolean isHeaderLike(String value) {
        String n = normalize(value);
        return "nom".equals(n)
                || "prenom".equals(n)
                || "cne".equals(n)
                || "specialite".equals(n)
                || "filiere".equals(n)
                || n.contains("nom prenom")
                || n.contains("prenom nom");
    }

    private boolean isHeaderRowNomPrenom(String nom, String prenom) {
        String n1 = normalize(nom);
        String n2 = normalize(prenom);
        String merged = (n1 + " " + n2).trim();
        return isHeaderLike(n1) || isHeaderLike(n2)
                || merged.contains("nom prenom")
                || merged.contains("prenom nom");
    }
}
