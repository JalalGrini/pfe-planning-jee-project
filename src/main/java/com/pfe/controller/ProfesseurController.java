package com.pfe.controller;

import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dto.ProfesseurDTO;
import com.pfe.model.Professeur;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/professeurs")
public class ProfesseurController {
    
    @Autowired
    private IProfesseurDAO professeurDAO;

    @GetMapping
    public List<ProfesseurDTO> getAll() {
        return professeurDAO.findAll().stream().map(p -> {
            ProfesseurDTO dto = new ProfesseurDTO();
            dto.setId(p.getId());
            dto.setNom(p.getNom());
            dto.setPrenom(p.getPrenom());
            dto.setDiscipline(p.getDiscipline());
            dto.setEmail(p.getEmail());
            return dto;
        }).collect(Collectors.toList());
    }
}