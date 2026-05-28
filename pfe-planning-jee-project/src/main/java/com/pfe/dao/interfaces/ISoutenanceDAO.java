package com.pfe.dao.interfaces;

import com.pfe.model.Soutenance;
import java.util.List;

/**
 * Interface DAO pour l'entité Soutenance.
 */
public interface ISoutenanceDAO {
    /**
     * Sauvegarde ou met à jour une soutenance.
     * @param soutenance soutenance à sauvegarder
     */
    void save(Soutenance soutenance);

    /**
     * Recherche une soutenance par identifiant.
     * @param id identifiant
     * @return soutenance ou null
     */
    Soutenance findById(Long id);

    /**
     * Retourne toutes les soutenances.
     * @return liste de soutenances
     */
    List<Soutenance> findAll();

    /**
     * Retourne les soutenances non plannifiées (date = null).
     * @return liste de soutenances non plannifiées
     */
    List<Soutenance> findNonPlannifiees();

    /**
     * Supprime une soutenance par identifiant.
     * @param id identifiant
     */
    void delete(Long id);

    /**
     * Compte les soutenances non plannifiées.
     * @return nombre de soutenances non plannifiées
     */
    long countNonPlannifiees();
}