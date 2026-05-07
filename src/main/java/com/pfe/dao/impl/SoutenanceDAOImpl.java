package com.pfe.dao.impl;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Soutenance;
import com.pfe.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class SoutenanceDAOImpl implements ISoutenanceDAO {
    @Override
    public void save(Soutenance soutenance) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(soutenance);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur sauvegarde soutenance", e);
            }
        }
    }

    @Override
    public Soutenance findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Soutenance.class, id);
        }
    }

    @Override
    public List<Soutenance> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Soutenance", Soutenance.class).list();
        }
    }

    @Override
    public List<Soutenance> findNonPlannifiees() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Soutenance WHERE date IS NULL", Soutenance.class).list();
        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Soutenance s = session.get(Soutenance.class, id);
                if (s != null) session.remove(s);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur suppression soutenance", e);
            }
        }
    }

    @Override
    public long countNonPlannifiees() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(s) FROM Soutenance s WHERE s.date IS NULL", Long.class);
            return query.uniqueResult();
        }
    }
}