package com.pfe.test;

import com.pfe.config.AppConfig;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Soutenance;
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
public class SoutenanceDAOTest {
    
    @Autowired
    private ISoutenanceDAO dao;

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
}