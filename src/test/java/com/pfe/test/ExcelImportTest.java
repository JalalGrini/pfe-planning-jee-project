package com.pfe.test;

import com.pfe.config.AppConfig;
import com.pfe.service.ExcelImportService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
public class ExcelImportTest {
    
    @Autowired
    private ExcelImportService service;

    @Test
    void testImport() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("data/exemples_soutenances.xlsx");
        assertNotNull(is, "Fichier exemples_soutenances.xlsx introuvable");
        List<Soutenance> list = service.importerSoutenances(is);
        assertFalse(list.isEmpty(), "La liste des soutenances ne doit pas être vide");
        assertTrue(list.stream().anyMatch(s -> s.getEtudiants().size() > 1), "Un binôme doit être détecté");
    }
}