package com.pfe.test;

import com.pfe.config.HibernateUtil;
import com.pfe.dao.impl.EtudiantDAOImpl;
import com.pfe.dao.impl.FiliereDAOImpl;
import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.model.Etudiant;
import com.pfe.model.Filiere;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class EtudiantDAOTest {
    private static IEtudiantDAO dao;
    private static Filiere filiere;

    @BeforeAll
    static void setup() {
        dao = new EtudiantDAOImpl();
        FiliereDAOImpl fDao = new FiliereDAOImpl();
        filiere = new Filiere();
        filiere.setNom("Test Filiere");
        filiere.setCode("TF");
        fDao.save(filiere);
    }

    @Test
    void testSave() {
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

    @AfterAll
    static void tearDown() { HibernateUtil.shutdown(); }
}