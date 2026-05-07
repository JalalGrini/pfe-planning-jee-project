package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.model.Professeur;
import com.pfe.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class ProfesseurDAOImpl implements IProfesseurDAO {
    @Override
    public void save(Professeur professeur) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(professeur);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur sauvegarde professeur", e);
            }
        }
    }

    @Override
    public Professeur findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Professeur.class, id);
        }
    }

    @Override
    public List<Professeur> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Professeur", Professeur.class).list();
        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Professeur p = session.get(Professeur.class, id);
                if (p != null) session.remove(p);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur suppression professeur", e);
            }
        }
    }

    @Override
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(p) FROM Professeur p", Long.class);
            return query.uniqueResult();
        }
    }
}