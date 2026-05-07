package com.pfe.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.dao.impl.ProfesseurDAOImpl;
import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dto.ProfesseurDTO;
import com.pfe.model.Professeur;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/professeurs")
public class ProfesseurServlet extends HttpServlet {
    private final IProfesseurDAO professeurDAO = new ProfesseurDAOImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        List<Professeur> profs = professeurDAO.findAll();
        List<ProfesseurDTO> dtos = profs.stream().map(p -> {
            ProfesseurDTO dto = new ProfesseurDTO();
            dto.setId(p.getId());
            dto.setNom(p.getNom());
            dto.setPrenom(p.getPrenom());
            dto.setDiscipline(p.getDiscipline());
            dto.setEmail(p.getEmail());
            return dto;
        }).collect(Collectors.toList());
        objectMapper.writeValue(resp.getWriter(), dtos);
    }
}