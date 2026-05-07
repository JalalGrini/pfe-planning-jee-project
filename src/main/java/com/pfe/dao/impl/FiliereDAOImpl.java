package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IFiliereDAO;
import com.pfe.model.Filiere;
import com.pfe.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class FiliereDAOImpl implements IFiliereDAO {
    @Override
    public void save(Filiere filiere) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(filiere);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur sauvegarde filière", e);
            }
        }
    }

    @Override
    public Filiere findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Filiere.class, id);
        }
    }

    @Override
    public Filiere findByNom(String nom) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Filiere> query = session.createQuery("FROM Filiere WHERE nom = :nom", Filiere.class);
            query.setParameter("nom", nom);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Filiere> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Filiere", Filiere.class).list();
        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Filiere f = session.get(Filiere.class, id);
                if (f != null) session.remove(f);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur suppression filière", e);
            }
        }
    }

    @Override
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(f) FROM Filiere f", Long.class);
            return query.uniqueResult();
        }
    }
}