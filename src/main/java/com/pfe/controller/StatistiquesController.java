package com.pfe.controller;

import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/statistiques")
public class StatistiquesController {
    
    @Autowired
    private IEtudiantDAO etudiantDAO;
    @Autowired
    private IProfesseurDAO professeurDAO;
    @Autowired
    private ISalleDAO salleDAO;
    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    @GetMapping
    public Map<String, Object> getStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nbEtudiants", etudiantDAO.count());
        stats.put("nbProfesseurs", professeurDAO.count());
        stats.put("nbSalles", salleDAO.count());
        stats.put("nbFilieres", 3);
        stats.put("nbSoutenancesNonPlannifiees", soutenanceDAO.countNonPlannifiees());
        return stats;
    }
}