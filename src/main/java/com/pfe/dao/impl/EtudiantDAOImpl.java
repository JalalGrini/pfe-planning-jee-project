package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.model.Etudiant;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Repository
@Transactional
public class EtudiantDAOImpl implements IEtudiantDAO {
    
    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(Etudiant etudiant) {
        sessionFactory.getCurrentSession().persist(etudiant);
    }

    @Override
    @Transactional(readOnly = true)
    public Etudiant findById(Long id) {
        return sessionFactory.getCurrentSession().get(Etudiant.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public Etudiant findByCne(String cne) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Etudiant WHERE cne = :cne", Etudiant.class)
                .setParameter("cne", cne)
                .uniqueResult();
    }

    @Override
    @Transactional(readOnly = true)
    public List<Etudiant> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM Etudiant", Etudiant.class)
                .list();
    }

    @Override
    public void delete(Long id) {
        Etudiant e = findById(id);
        if (e != null) sessionFactory.getCurrentSession().remove(e);
    }

    @Override
    @Transactional(readOnly = true)
    public long count() {
        return sessionFactory.getCurrentSession()
                .createQuery("SELECT COUNT(e) FROM Etudiant e", Long.class)
                .uniqueResult();
    }
}