package com.pfe.test;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.service.ExportService;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;

import java.io.ByteArrayInputStream;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class ExportServiceTest {

    @Mock
    private ISoutenanceDAO soutenanceDAO;

    @InjectMocks
    private ExportService exportService;

    public static class DummyProf {
        private String nom, prenom;
        public DummyProf(String nom, String prenom) { this.nom = nom; this.prenom = prenom; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
    }

    public static class DummyEtudiant {
        private String nom, prenom;
        public DummyEtudiant(String nom, String prenom) { this.nom = nom; this.prenom = prenom; }
        public String getNom() { return nom; }
        public String getPrenom() { return prenom; }
    }

    public static class DummySalle {
        private String nom;
        public DummySalle(String nom) { this.nom = nom; }
        public String getNom() { return nom; }
    }

    public static class DummyFiliere {
        private String nom;
        public DummyFiliere(String nom) { this.nom = nom; }
        public String getNom() { return nom; }
    }

    public static class DummySoutenance {
        public LocalDate date;
        public LocalTime heureDebut;
        public DummySalle salle;
        public DummyFiliere filiere;
        public String titre;
        public DummyProf president;
        public List<DummyProf> jurys = new ArrayList<>();
        public List<DummyEtudiant> etudiants = new ArrayList<>();

        public LocalDate getDate() { return date; }
        public LocalTime getHeureDebut() { return heureDebut; }
        public Object getSalle() { return salle; }
        public Object getFiliere() { return filiere; }
        public String getTitre() { return titre; }
        public Object getPresident() { return president; }
        public Object getJurys() { return jurys; }
        public Object getEtudiants() { return etudiants; }
    }

    private List<Object> createMockSoutenances() {
        List<Object> list = new ArrayList<>();

        DummySoutenance s1 = new DummySoutenance();
        s1.date = LocalDate.of(2026, 6, 12);
        s1.heureDebut = LocalTime.of(9, 0);
        s1.salle = new DummySalle("Salle 101");
        s1.filiere = new DummyFiliere("GI");
        s1.titre = "Application E-Commerce";
        s1.president = new DummyProf("El Fassi", "Ahmed");
        s1.jurys.add(new DummyProf("Naciri", "Hassan"));
        s1.jurys.add(new DummyProf("Amrani", "Sara"));
        s1.etudiants.add(new DummyEtudiant("Bouzit", "Ali"));

        DummySoutenance s2 = new DummySoutenance();
        s2.date = LocalDate.of(2026, 6, 10); // Plus tôt pour tester le tri
        s2.heureDebut = LocalTime.of(14, 0);
        s2.salle = new DummySalle("Salle 102");
        s2.filiere = new DummyFiliere("GSTR");
        s2.titre = "Sécurité Réseau";
        s2.president = new DummyProf("Rami", "Kamal");
        s2.jurys.add(new DummyProf("Saidi", "Ayoub"));
        s2.jurys.add(new DummyProf("Touhami", "Nour"));

        list.add(s1);
        list.add(s2);
        return list;
    }

    @Test
    public void testExcelNonVide() {
        Mockito.doReturn(createMockSoutenances()).when(soutenanceDAO).findAll();
        byte[] excelData = exportService.exporterPlanningExcel();
        assertNotNull(excelData, "Le fichier Excel ne doit pas être null");
        assertTrue(excelData.length > 0, "Le fichier Excel ne doit pas être vide");
    }

    @Test
    public void testExcelContientTroisFeuilles() throws Exception {
        Mockito.doReturn(createMockSoutenances()).when(soutenanceDAO).findAll();
        byte[] excelData = exportService.exporterPlanningExcel();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            assertEquals(3, workbook.getNumberOfSheets(), "Le workbook doit contenir exactement 3 feuilles");
            assertEquals("Planning Global", workbook.getSheetName(0));
            assertEquals("Par Filière", workbook.getSheetName(1));
            assertEquals("Par Professeur", workbook.getSheetName(2));
        }
    }

    @Test
    public void testExcelColonnesCorrectes() throws Exception {
        Mockito.doReturn(createMockSoutenances()).when(soutenanceDAO).findAll();
        byte[] excelData = exportService.exporterPlanningExcel();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheet("Planning Global");
            Row headerRow = sheet.getRow(0);
            assertNotNull(headerRow, "La ligne d'en-tête doit exister");
            assertEquals(9, headerRow.getLastCellNum(), "Il doit y avoir 9 colonnes");
            assertEquals("Date", headerRow.getCell(0).getStringCellValue());
            assertEquals("Sujet PFE", headerRow.getCell(8).getStringCellValue());
        }
    }

    @Test
    public void testExcelTriParDateEtHeure() throws Exception {
        Mockito.doReturn(createMockSoutenances()).when(soutenanceDAO).findAll();
        byte[] excelData = exportService.exporterPlanningExcel();

        try (Workbook workbook = new XSSFWorkbook(new ByteArrayInputStream(excelData))) {
            Sheet sheet = workbook.getSheet("Planning Global");
            // Ligne 0 : header
            // Ligne 1 : "2026-06-10" car s2 (10 juin) doit être avant s1 (12 juin)
            Row row1 = sheet.getRow(1);
            Row row2 = sheet.getRow(2);

            assertEquals("10/06/2026", row1.getCell(0).getStringCellValue());
            assertEquals("12/06/2026", row2.getCell(0).getStringCellValue());
        }
    }

    @Test
    public void testPDFNonVide() {
        Mockito.doReturn(createMockSoutenances()).when(soutenanceDAO).findAll();
        byte[] pdfData = exportService.exporterPlanningPDF();

        assertNotNull(pdfData, "Le fichier PDF ne doit pas être null");
        assertTrue(pdfData.length > 0, "Le fichier PDF ne doit pas être vide");
    }
}
