package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IProfesseurIndisponibiliteDAO;
import com.pfe.model.ProfesseurIndisponibilite;
import org.hibernate.SessionFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Repository
@Transactional
public class ProfesseurIndisponibiliteDAOImpl implements IProfesseurIndisponibiliteDAO {

    @Autowired
    private SessionFactory sessionFactory;

    @Override
    public void save(ProfesseurIndisponibilite indisponibilite) {
        sessionFactory.getCurrentSession().persist(indisponibilite);
    }

    @Override
    @Transactional(readOnly = true)
    public ProfesseurIndisponibilite findById(Long id) {
        return sessionFactory.getCurrentSession().get(ProfesseurIndisponibilite.class, id);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesseurIndisponibilite> findAll() {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM ProfesseurIndisponibilite", ProfesseurIndisponibilite.class)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesseurIndisponibilite> findByProfesseurId(Long professeurId) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM ProfesseurIndisponibilite p WHERE p.professeur.id = :profId", ProfesseurIndisponibilite.class)
                .setParameter("profId", professeurId)
                .list();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProfesseurIndisponibilite> findByProfesseurIdAndDate(Long professeurId, LocalDate date) {
        return sessionFactory.getCurrentSession()
                .createQuery("FROM ProfesseurIndisponibilite p WHERE p.professeur.id = :profId AND p.date = :date", ProfesseurIndisponibilite.class)
                .setParameter("profId", professeurId)
                .setParameter("date", date)
                .list();
    }

    @Override
    public void delete(Long id) {
        ProfesseurIndisponibilite p = findById(id);
        if (p != null) {
            sessionFactory.getCurrentSession().remove(p);
        }
    }
}
