package com.pfe.test;

import com.pfe.dao.impl.SoutenanceDAOImpl;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Soutenance;
import org.junit.jupiter.api.*;
import static org.junit.jupiter.api.Assertions.*;

public class SoutenanceDAOTest {
    private static ISoutenanceDAO dao;

    @BeforeAll
    static void setup() { dao = new SoutenanceDAOImpl(); }

    @Test
    void testSave() {
        Soutenance s = new Soutenance();
        s.setTitre("Test");
        dao.save(s);
        assertNotNull(dao.findById(s.getId()));
    }

    @Test
    void testNonPlannifiees() {
        Soutenance s = new Soutenance();
        s.setTitre("Non Plan");
        dao.save(s);
        assertTrue(dao.findNonPlannifiees().size() >= 1);
    }

    @AfterAll
    static void tearDown() { com.pfe.config.HibernateUtil.shutdown(); }
}