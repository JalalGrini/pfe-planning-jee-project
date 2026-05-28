package com.pfe.dao.impl;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Etudiant;
import com.pfe.model.Soutenance;
import org.hibernate.Hibernate;
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
        List<Soutenance> soutenances = sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT DISTINCT s FROM Soutenance s " +
                        "LEFT JOIN FETCH s.salle " +
                        "LEFT JOIN FETCH s.president " +
                        "LEFT JOIN FETCH s.filiere",
                        Soutenance.class
                )
                .list();
        initializeForDto(soutenances);
        return soutenances;
    }

    @Override
    @Transactional(readOnly = true)
    public List<Soutenance> findNonPlannifiees() {
        List<Soutenance> soutenances = sessionFactory.getCurrentSession()
                .createQuery(
                        "SELECT DISTINCT s FROM Soutenance s " +
                        "LEFT JOIN FETCH s.salle " +
                        "LEFT JOIN FETCH s.president " +
                        "LEFT JOIN FETCH s.filiere " +
                        "WHERE s.date IS NULL",
                        Soutenance.class
                )
                .list();
        initializeForDto(soutenances);
        return soutenances;
    }

    private void initializeForDto(List<Soutenance> soutenances) {
        for (Soutenance soutenance : soutenances) {
            Hibernate.initialize(soutenance.getJurys());
            Hibernate.initialize(soutenance.getEtudiants());
            if (soutenance.getEtudiants() != null) {
                for (Etudiant etudiant : soutenance.getEtudiants()) {
                    if (etudiant != null) {
                        Hibernate.initialize(etudiant.getFiliere());
                    }
                }
            }
        }
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
