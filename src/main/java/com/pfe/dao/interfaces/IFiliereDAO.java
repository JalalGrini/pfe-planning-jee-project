package com.pfe.dao.interfaces;

import com.pfe.model.Filiere;
import java.util.List;

/**
 * Interface DAO pour l'entité Filiere.
 */
public interface IFiliereDAO {
    /**
     * Sauvegarde ou met à jour une filière.
     * @param filiere filière à sauvegarder
     */
    void save(Filiere filiere);

    /**
     * Recherche une filière par identifiant.
     * @param id identifiant
     * @return filière ou null
     */
    Filiere findById(Long id);

    /**
     * Recherche une filière par nom.
     * @param nom nom de la filière
     * @return filière ou null
     */
    Filiere findByNom(String nom);

    /**
     * Retourne toutes les filières.
     * @return liste de filières
     */
    List<Filiere> findAll();

    /**
     * Supprime une filière par identifiant.
     * @param id identifiant
     */
    void delete(Long id);

    /**
     * Compte le nombre total de filières.
     * @return nombre de filières
     */
    long count();
}