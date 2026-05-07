package com.pfe.dao.impl;

import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.model.Salle;
import com.pfe.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class SalleDAOImpl implements ISalleDAO {
    @Override
    public void save(Salle salle) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(salle);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur sauvegarde salle", e);
            }
        }
    }

    @Override
    public Salle findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Salle.class, id);
        }
    }

    @Override
    public List<Salle> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Salle", Salle.class).list();
        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Salle s = session.get(Salle.class, id);
                if (s != null) session.remove(s);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur suppression salle", e);
            }
        }
    }

    @Override
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(s) FROM Salle s", Long.class);
            return query.uniqueResult();
        }
    }
}