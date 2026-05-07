package com.pfe.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.dao.impl.SalleDAOImpl;
import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dto.SalleDTO;
import com.pfe.model.Salle;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;

@WebServlet("/api/salles")
public class SalleServlet extends HttpServlet {
    private final ISalleDAO salleDAO = new SalleDAOImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        List<Salle> salles = salleDAO.findAll();
        List<SalleDTO> dtos = salles.stream().map(s -> {
            SalleDTO dto = new SalleDTO();
            dto.setId(s.getId());
            dto.setNom(s.getNom());
            dto.setCapacite(s.getCapacite());
            dto.setDisponible(s.isDisponible());
            return dto;
        }).collect(Collectors.toList());
        objectMapper.writeValue(resp.getWriter(), dtos);
    }
}