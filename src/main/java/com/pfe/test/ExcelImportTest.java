package com.pfe.test;

import com.pfe.model.Soutenance;
import com.pfe.service.ExcelImportService;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import java.io.InputStream;
import java.util.List;

public class ExcelImportTest {
    @Test
    void testImport() throws Exception {
        ExcelImportService service = new ExcelImportService();
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/exemples_soutenances.xlsx");
        assertNotNull(is, "Fichier exemples_soutenances.xlsx introuvable");
        List<Soutenance> list = service.importerSoutenances(is);
        assertFalse(list.isEmpty(), "La liste des soutenances ne doit pas être vide");
        assertTrue(list.stream().anyMatch(s -> s.getEtudiants().size() > 1), "Un binôme doit être détecté");
    }
}