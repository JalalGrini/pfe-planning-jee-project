package com.pfe.dao.interfaces;

import com.pfe.model.Salle;
import java.util.List;

/**
 * Interface DAO pour l'entité Salle.
 */
public interface ISalleDAO {
    /**
     * Sauvegarde ou met à jour une salle.
     * @param salle salle à sauvegarder
     */
    void save(Salle salle);

    /**
     * Recherche une salle par identifiant.
     * @param id identifiant
     * @return salle ou null
     */
    Salle findById(Long id);

    /**
     * Retourne toutes les salles.
     * @return liste de salles
     */
    List<Salle> findAll();

    /**
     * Supprime une salle par identifiant.
     * @param id identifiant
     */
    void delete(Long id);

    /**
     * Compte le nombre total de salles.
     * @return nombre de salles
     */
    long count();
}