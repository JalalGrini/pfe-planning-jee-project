package com.pfe.dao.impl;

import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.model.Salle;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class SalleDAOImpl implements ISalleDAO {
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Salle salle) {
        sessionFactory.getCurrentSession().persist(salle);
    }

    @Override
    @Transactional(readOnly = true)
    public Salle findById(Long id) {
        return sessionFactory.getCurrentSession().get(Salle.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Salle> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Salle", Salle.class)
                .list();
    }

    @Override
    public void delete(Long id) {
        Salle s = findById(id);
        if (s != null) sessionFactory.getCurrentSession().remove(s);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(s) FROM Salle s", Long.class)
                .uniqueResult();
    }
}