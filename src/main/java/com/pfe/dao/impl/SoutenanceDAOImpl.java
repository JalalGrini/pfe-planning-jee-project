package com.pfe.dao.impl;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Soutenance;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class SoutenanceDAOImpl implements ISoutenanceDAO {
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Soutenance soutenance) {
        sessionFactory.getCurrentSession().persist(soutenance);
    }

    @Override
    @Transactional(readOnly = true)
    public Soutenance findById(Long id) {
        return sessionFactory.getCurrentSession().get(Soutenance.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Soutenance> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Soutenance", Soutenance.class)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Soutenance> findNonPlannifiees() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Soutenance WHERE date IS NULL", Soutenance.class)
                .list();
    }

    @Override
    public void delete(Long id) {
        Soutenance s = findById(id);
        if (s != null) sessionFactory.getCurrentSession().remove(s);
    }

    @Override
    @Transactional(readOnly = true)
    public long countNonPlannifiees() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(s) FROM Soutenance s WHERE s.date IS NULL", Long.class)
                .uniqueResult();
    }
}