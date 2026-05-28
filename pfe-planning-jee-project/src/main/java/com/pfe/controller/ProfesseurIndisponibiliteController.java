package com.pfe.controller;

import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dao.interfaces.IProfesseurIndisponibiliteDAO;
import com.pfe.model.Professeur;
import com.pfe.model.ProfesseurIndisponibilite;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/professeurs")
public class ProfesseurIndisponibiliteController {

    @Autowired
    private IProfesseurIndisponibiliteDAO indisponibiliteDAO;

    @Autowired
    private IProfesseurDAO professeurDAO;

    @GetMapping("/{id}/indisponibilites")
    @Transactional(readOnly = true)
    public List<ProfesseurIndisponibilite> getIndisponibilites(@PathVariable Long id) {
        return indisponibiliteDAO.findByProfesseurId(id);
    }

    @PostMapping("/{id}/indisponibilites")
    @Transactional
    public ResponseEntity<Map<String, Object>> addIndisponibilite(
            @PathVariable Long id,
            @RequestBody ProfesseurIndisponibilite indisponibilite) {
        Map<String, Object> response = new HashMap<>();
        Professeur professeur = professeurDAO.findById(id);
        if (professeur == null) {
            response.put("error", "Professeur non trouvé");
            return ResponseEntity.badRequest().body(response);
        }
        
        indisponibilite.setProfesseur(professeur);
        indisponibiliteDAO.save(indisponibilite);
        
        response.put("message", "Indisponibilité ajoutée avec succès");
        response.put("id", indisponibilite.getId());
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/indisponibilites/{id}")
    @Transactional
    public ResponseEntity<Map<String, Object>> deleteIndisponibilite(@PathVariable Long id) {
        Map<String, Object> response = new HashMap<>();
        indisponibiliteDAO.delete(id);
        response.put("message", "Indisponibilité supprimée avec succès");
        return ResponseEntity.ok(response);
    }
}
