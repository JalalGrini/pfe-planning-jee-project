package com.pfe.config;

import com.pfe.dao.impl.*;
import com.pfe.dao.interfaces.*;
import com.pfe.model.*;
import jakarta.servlet.ServletContextEvent;
import jakarta.servlet.ServletContextListener;
import jakarta.servlet.annotation.WebListener;
import org.apache.poi.ss.usermodel.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;

@WebListener
public class DataInitializer implements ServletContextListener {
    private static final Logger logger = LoggerFactory.getLogger(DataInitializer.class);
    private final IFiliereDAO filiereDAO = new FiliereDAOImpl();
    private final ISalleDAO salleDAO = new SalleDAOImpl();
    private final IProfesseurDAO professeurDAO = new ProfesseurDAOImpl();
    private final IEtudiantDAO etudiantDAO = new EtudiantDAOImpl();

    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("Initialisation des données...");
        initFilieres();
        initSalles();
        initProfesseurs();
        initEtudiants();
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
                    Professeur p = new Professeur();
                    p.setNom(getCellValue(row.getCell(0)));
                    p.setPrenom(getCellValue(row.getCell(1)));
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
                        Etudiant e = new Etudiant();
                        e.setCne(getCellValue(row.getCell(0)));
                        e.setNom(getCellValue(row.getCell(1)));
                        e.setPrenom(getCellValue(row.getCell(2)));
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

    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        HibernateUtil.shutdown();
    }
}