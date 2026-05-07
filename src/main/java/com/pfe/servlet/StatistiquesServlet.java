package com.pfe.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.dao.impl.EtudiantDAOImpl;
import com.pfe.dao.impl.ProfesseurDAOImpl;
import com.pfe.dao.impl.SalleDAOImpl;
import com.pfe.dao.impl.SoutenanceDAOImpl;
import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@WebServlet("/api/statistiques")
public class StatistiquesServlet extends HttpServlet {
    private final IEtudiantDAO etudiantDAO = new EtudiantDAOImpl();
    private final IProfesseurDAO professeurDAO = new ProfesseurDAOImpl();
    private final ISalleDAO salleDAO = new SalleDAOImpl();
    private final ISoutenanceDAO soutenanceDAO = new SoutenanceDAOImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");
        Map<String, Object> stats = new HashMap<>();
        stats.put("nbEtudiants", etudiantDAO.count());
        stats.put("nbProfesseurs", professeurDAO.count());
        stats.put("nbSalles", salleDAO.count());
        stats.put("nbFilieres", 3);
        stats.put("nbSoutenancesNonPlannifiees", soutenanceDAO.countNonPlannifiees());
        objectMapper.writeValue(resp.getWriter(), stats);
    }
}