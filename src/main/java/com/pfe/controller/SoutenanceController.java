package com.pfe.controller;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.dto.*;
import com.pfe.model.Soutenance;
import com.pfe.service.ExcelImportService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/soutenances")
public class SoutenanceController {
    private static final Logger logger = LoggerFactory.getLogger(SoutenanceController.class);

    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    @Autowired
    private ExcelImportService excelImportService;

    @GetMapping
    public List<SoutenanceDTO> getAll(@RequestParam(required = false) String filter) {
        List<Soutenance> soutenances = "/non-plannifiees".equals(filter) 
            ? soutenanceDAO.findNonPlannifiees() 
            : soutenanceDAO.findAll();
        return soutenances.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @PostMapping(value = "/importer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public String importer(@RequestParam("file") MultipartFile file) {
        try {
            List<Soutenance> soutenances = excelImportService.importerSoutenances(file.getInputStream());
            soutenances.forEach(soutenanceDAO::save);
            return "{\"message\": \"Import réussi\", \"count\": " + soutenances.size() + "}";
        } catch (Exception e) {
            logger.error("Erreur import soutenances", e);
            return "{\"error\": \"" + e.getMessage() + "\"}";
        }
    }

    private SoutenanceDTO toDTO(Soutenance s) {
        SoutenanceDTO dto = new SoutenanceDTO();
        dto.setId(s.getId());
        dto.setTitre(s.getTitre());
        dto.setDate(s.getDate());
        dto.setHeureDebut(s.getHeureDebut());
        dto.setHeureFin(s.getHeureFin());

        if (s.getSalle() != null) {
            SalleDTO salleDTO = new SalleDTO();
            salleDTO.setId(s.getSalle().getId());
            salleDTO.setNom(s.getSalle().getNom());
            salleDTO.setCapacite(s.getSalle().getCapacite());
            salleDTO.setDisponible(s.getSalle().isDisponible());
            dto.setSalle(salleDTO);
        }

        if (s.getPresident() != null) {
            ProfesseurDTO presDTO = new ProfesseurDTO();
            presDTO.setId(s.getPresident().getId());
            presDTO.setNom(s.getPresident().getNom());
            presDTO.setPrenom(s.getPresident().getPrenom());
            presDTO.setDiscipline(s.getPresident().getDiscipline());
            presDTO.setEmail(s.getPresident().getEmail());
            dto.setPresident(presDTO);
        }

        if (s.getJurys() != null) {
            List<ProfesseurDTO> juryDTOs = s.getJurys().stream().map(p -> {
                ProfesseurDTO pDTO = new ProfesseurDTO();
                pDTO.setId(p.getId());
                pDTO.setNom(p.getNom());
                pDTO.setPrenom(p.getPrenom());
                pDTO.setDiscipline(p.getDiscipline());
                pDTO.setEmail(p.getEmail());
                return pDTO;
            }).collect(Collectors.toList());
            dto.setJurys(juryDTOs);
        }

        if (s.getEtudiants() != null) {
            List<EtudiantDTO> etuDTOs = s.getEtudiants().stream().map(e -> {
                EtudiantDTO eDTO = new EtudiantDTO();
                eDTO.setId(e.getId());
                eDTO.setCne(e.getCne());
                eDTO.setNom(e.getNom());
                eDTO.setPrenom(e.getPrenom());
                eDTO.setEmailPerso(e.getEmailPerso());
                eDTO.setEmailAcad(e.getEmailAcad());
                if (e.getFiliere() != null) {
                    eDTO.setFiliereId(e.getFiliere().getId());
                    eDTO.setFiliereNom(e.getFiliere().getNom());
                }
                return eDTO;
            }).collect(Collectors.toList());
            dto.setEtudiants(etuDTOs);
        }

        if (s.getFiliere() != null) {
            dto.setFiliereId(s.getFiliere().getId());
            dto.setFiliereNom(s.getFiliere().getNom());
        }

        return dto;
    }
}