package com.pfe.controller;

import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dto.SalleDTO;
import com.pfe.model.Salle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/salles")
public class SalleController {
    
    @Autowired
    private ISalleDAO salleDAO;

    @GetMapping
    public List<SalleDTO> getAll() {
        return salleDAO.findAll().stream().map(s -> {
            SalleDTO dto = new SalleDTO();
            dto.setId(s.getId());
            dto.setNom(s.getNom());
            dto.setCapacite(s.getCapacite());
            dto.setDisponible(s.isDisponible());
            return dto;
        }).collect(Collectors.toList());
    }
}