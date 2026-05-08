package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.model.Professeur;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class ProfesseurDAOImpl implements IProfesseurDAO {
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Professeur professeur) {
        sessionFactory.getCurrentSession().persist(professeur);
    }

    @Override
    @Transactional(readOnly = true)
    public Professeur findById(Long id) {
        return sessionFactory.getCurrentSession().get(Professeur.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Professeur> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Professeur", Professeur.class)
                .list();
    }

    @Override
    public void delete(Long id) {
        Professeur p = findById(id);
        if (p != null) sessionFactory.getCurrentSession().remove(p);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(p) FROM Professeur p", Long.class)
                .uniqueResult();
    }
}