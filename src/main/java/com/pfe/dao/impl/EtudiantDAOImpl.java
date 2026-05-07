package com.pfe.dao.impl;

import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.model.Etudiant;
import com.pfe.config.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;

public class EtudiantDAOImpl implements IEtudiantDAO {
    @Override
    public void save(Etudiant etudiant) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                session.persist(etudiant);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur sauvegarde étudiant", e);
            }
        }
    }

    @Override
    public Etudiant findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.get(Etudiant.class, id);
        }
    }

    @Override
    public Etudiant findByCne(String cne) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Etudiant> query = session.createQuery("FROM Etudiant WHERE cne = :cne", Etudiant.class);
            query.setParameter("cne", cne);
            return query.uniqueResult();
        }
    }

    @Override
    public List<Etudiant> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery("FROM Etudiant", Etudiant.class).list();
        }
    }

    @Override
    public void delete(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            try {
                Etudiant e = session.get(Etudiant.class, id);
                if (e != null) session.remove(e);
                tx.commit();
            } catch (Exception e) {
                tx.rollback();
                throw new RuntimeException("Erreur suppression étudiant", e);
            }
        }
    }

    @Override
    public long count() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Query<Long> query = session.createQuery("SELECT COUNT(e) FROM Etudiant e", Long.class);
            return query.uniqueResult();
        }
    }
}