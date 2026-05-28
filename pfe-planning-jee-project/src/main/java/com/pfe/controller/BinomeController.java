package com.pfe.controller;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.dto.BinomeDTO;
import com.pfe.dto.EtudiantDTO;
import com.pfe.model.Etudiant;
import com.pfe.model.Soutenance;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import com.pfe.dto.BinomeRequestDTO;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/binomes")
public class BinomeController {

    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    @GetMapping
    @Transactional(readOnly = true)
    public List<BinomeDTO> getAllBinomes() {
        List<Soutenance> soutenances = soutenanceDAO.findAll();
        List<BinomeDTO> binomes = new ArrayList<>();
        
        for (Soutenance s : soutenances) {
            if (s.getEtudiants() != null && s.getEtudiants().size() > 1) {
                BinomeDTO dto = new BinomeDTO();
                dto.setSujet(s.getTitre());
                dto.setConfirmed(true);
                
                List<EtudiantDTO> etuDTOs = s.getEtudiants().stream().map(e -> {
                    EtudiantDTO eDTO = new EtudiantDTO();
                    eDTO.setId(e.getId());
                    eDTO.setCne(e.getCne());
                    eDTO.setNom(e.getNom());
                    eDTO.setPrenom(e.getPrenom());
                    eDTO.setEmailPerso(e.getEmailPerso());
                    eDTO.setEmailAcad(e.getEmailAcad());
                    return eDTO;
                }).collect(Collectors.toList());
                
                dto.setEtudiants(etuDTOs);
                binomes.add(dto);
            }
        }
        return binomes;
    }

    @PostMapping("/confirmer")
    @Transactional
    public ResponseEntity<Map<String, Object>> confirmerBinome(@RequestBody BinomeDTO binomeDTO) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Binome confirmé avec succès");
        return ResponseEntity.ok(response);
    }

    @GetMapping("/etudiants")
    @Transactional(readOnly = true)
    public List<EtudiantDTO> getEtudiantsDisponibles() {
        List<Soutenance> soutenances = soutenanceDAO.findAll();
        List<EtudiantDTO> disponibles = new ArrayList<>();
        
        for (Soutenance s : soutenances) {
            if (s.getEtudiants() != null && s.getEtudiants().size() == 1) {
                Etudiant e = s.getEtudiants().get(0);
                EtudiantDTO eDTO = new EtudiantDTO();
                eDTO.setId(e.getId());
                eDTO.setCne(e.getCne());
                eDTO.setNom(e.getNom());
                eDTO.setPrenom(e.getPrenom());
                eDTO.setEmailPerso(e.getEmailPerso());
                eDTO.setEmailAcad(e.getEmailAcad());
                disponibles.add(eDTO);
            }
        }
        return disponibles;
    }

    @PostMapping("/manual")
    @Transactional
    public ResponseEntity<?> creerBinomeManuel(@RequestBody BinomeRequestDTO request) {
        if (request.getEtudiantIds() == null || request.getEtudiantIds().size() != 2) {
            return ResponseEntity.badRequest().body(Map.of("message", "Il faut exactement 2 étudiants pour un binôme."));
        }

        List<Soutenance> soutenances = soutenanceDAO.findAll();
        
        Soutenance s1 = null;
        Soutenance s2 = null;
        
        for (Soutenance s : soutenances) {
            if (s.getEtudiants() != null && s.getEtudiants().size() == 1) {
                Long etuId = s.getEtudiants().get(0).getId();
                if (etuId.equals(request.getEtudiantIds().get(0))) {
                    s1 = s;
                } else if (etuId.equals(request.getEtudiantIds().get(1))) {
                    s2 = s;
                }
            }
        }

        if (s1 == null || s2 == null) {
            return ResponseEntity.badRequest().body(Map.of("message", "Un ou plusieurs étudiants sont introuvables ou déjà dans un binôme."));
        }

        // Merge s2 into s1
        s1.getEtudiants().add(s2.getEtudiants().get(0));
        s1.setTitre(request.getSujet());
        
        soutenanceDAO.save(s1);
        soutenanceDAO.delete(s2.getId());

        return ResponseEntity.ok(Map.of("message", "Binôme créé avec succès"));
    }
}
