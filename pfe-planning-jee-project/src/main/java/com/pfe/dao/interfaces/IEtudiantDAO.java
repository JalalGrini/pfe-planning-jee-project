package com.pfe.dao.interfaces;

import com.pfe.model.Etudiant;
import java.util.List;

/**
 * Interface DAO pour l'entité Etudiant.
 */
public interface IEtudiantDAO {
    /**
     * Sauvegarde ou met à jour un étudiant.
     * @param etudiant l'étudiant à sauvegarder
     */
    void save(Etudiant etudiant);

    /**
     * Recherche un étudiant par identifiant.
     * @param id identifiant
     * @return étudiant ou null
     */
    Etudiant findById(Long id);

    /**
     * Recherche un étudiant par CNE.
     * @param cne CNE unique
     * @return étudiant ou null
     */
    Etudiant findByCne(String cne);

    /**
     * Retourne tous les étudiants.
     * @return liste d'étudiants
     */
    List<Etudiant> findAll();

    /**
     * Supprime un étudiant par identifiant.
     * @param id identifiant
     */
    void delete(Long id);

    /**
     * Compte le nombre total d'étudiants.
     * @return nombre d'étudiants
     */
    long count();
}