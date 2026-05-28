package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IFiliereDAO;
import com.pfe.model.Filiere;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class FiliereDAOImpl implements IFiliereDAO {
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Filiere filiere) {
        sessionFactory.getCurrentSession().persist(filiere);
    }

    @Override
    @Transactional(readOnly = true)
    public Filiere findById(Long id) {
        return sessionFactory.getCurrentSession().get(Filiere.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Filiere findByNom(String nom) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Filiere WHERE nom = :nom", Filiere.class)
                .setParameter("nom", nom)
                .uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Filiere> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Filiere", Filiere.class)
                .list();
    }

    @Override
    public void delete(Long id) {
        Filiere f = findById(id);
        if (f != null) sessionFactory.getCurrentSession().remove(f);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(f) FROM Filiere f", Long.class)
                .uniqueResult();
    }
}