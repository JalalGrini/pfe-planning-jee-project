package com.pfe.dao.interfaces;

import com.pfe.model.Professeur;
import java.util.List;

/**
 * Interface DAO pour l'entité Professeur.
 */
public interface IProfesseurDAO {
    /**
     * Sauvegarde ou met à jour un professeur.
     * @param professeur professeur à sauvegarder
     */
    void save(Professeur professeur);

    /**
     * Recherche un professeur par identifiant.
     * @param id identifiant
     * @return professeur ou null
     */
    Professeur findById(Long id);

    /**
     * Retourne tous les professeurs.
     * @return liste de professeurs
     */
    List<Professeur> findAll();

    /**
     * Supprime un professeur par identifiant.
     * @param id identifiant
     */
    void delete(Long id);

    /**
     * Compte le nombre total de professeurs.
     * @return nombre de professeurs
     */
    long count();
}