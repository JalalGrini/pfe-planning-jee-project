package com.pfe.test;

import com.pfe.config.AppConfig;
import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.model.Etudiant;
import com.pfe.model.Filiere;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = AppConfig.class)
@Transactional
public class EtudiantDAOTest {
    
    @Autowired
    private IEtudiantDAO dao;

    @Test
    void testSave() {
        Filiere filiere = new Filiere();
        filiere.setNom("Test Filiere");
        filiere.setCode("TF");
        
        Etudiant e = new Etudiant();
        e.setCne("CNE001");
        e.setNom("Nom");
        e.setPrenom("Prenom");
        e.setFiliere(filiere);
        dao.save(e);
        assertNotNull(dao.findByCne("CNE001"));
    }

    @Test
    void testCount() {
        long count = dao.count();
        assertTrue(count >= 0);
    }
}