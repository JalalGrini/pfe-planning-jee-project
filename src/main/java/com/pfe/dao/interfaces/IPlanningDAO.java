package com.pfe.dao.interfaces;

import com.pfe.model.Soutenance;
import java.util.List;

/**
 * Interface DAO pour les données de planification (Membre 1 n'implémente aucun algorithme).
 */
public interface IPlanningDAO {
    /**
     * Retourne les soutenances non plannifiées (données de base pour planification).
     * @return liste de soutenances non plannifiées
     */
    List<Soutenance> getDonneesPlanification();
}