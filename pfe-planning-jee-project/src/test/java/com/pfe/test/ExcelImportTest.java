package com.pfe.test;

import com.pfe.model.Soutenance;
import com.pfe.service.ExcelImportService;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(locations = "file:src/main/webapp/WEB-INF/Spring-beans.xml")
public class ExcelImportTest {

    @Autowired
    private ExcelImportService service;

    @Test
    void testImport() throws Exception {
        List<Soutenance> list = service.importerSoutenances(createSampleWorkbook());
        assertFalse(list.isEmpty(), "La liste des soutenances ne doit pas etre vide");
        assertTrue(list.size() == 2, "Chaque etudiant doit avoir sa propre soutenance");
    }

    private InputStream createSampleWorkbook() throws Exception {
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Soutenances");
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("CNE");
            header.createCell(1).setCellValue("NOM");
            header.createCell(2).setCellValue("PRENOM");
            header.createCell(3).setCellValue("FILIERE");
            header.createCell(4).setCellValue("SUJET_PFE");

            Row first = sheet.createRow(1);
            first.createCell(0).setCellValue("CNE100");
            first.createCell(1).setCellValue("NomA");
            first.createCell(2).setCellValue("PrenomA");
            first.createCell(3).setCellValue("Genie Informatique");
            first.createCell(4).setCellValue("Sujet Binome");

            Row second = sheet.createRow(2);
            second.createCell(0).setCellValue("CNE101");
            second.createCell(1).setCellValue("NomB");
            second.createCell(2).setCellValue("PrenomB");
            second.createCell(3).setCellValue("Genie Informatique");
            second.createCell(4).setCellValue("Sujet Binome");

            workbook.write(out);
            return new ByteArrayInputStream(out.toByteArray());
        }
    }
}
