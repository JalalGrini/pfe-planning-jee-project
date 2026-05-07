package com.pfe.servlet;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.pfe.dao.impl.SoutenanceDAOImpl;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.dto.EtudiantDTO;
import com.pfe.dto.ProfesseurDTO;
import com.pfe.dto.SalleDTO;
import com.pfe.dto.SoutenanceDTO;
import com.pfe.model.Soutenance;
import com.pfe.service.ExcelImportService;
import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.MultipartConfig;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@WebServlet("/api/soutenances/*")
@MultipartConfig
public class SoutenanceServlet extends HttpServlet {
    private static final Logger logger = LoggerFactory.getLogger(SoutenanceServlet.class);
    private final ISoutenanceDAO soutenanceDAO = new SoutenanceDAOImpl();
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ExcelImportService excelImportService = new ExcelImportService();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        String pathInfo = req.getPathInfo();
        List<Soutenance> soutenances = "/non-plannifiees".equals(pathInfo)
            ? soutenanceDAO.findNonPlannifiees()
            : soutenanceDAO.findAll();

        List<SoutenanceDTO> dtoList = soutenances.stream().map(this::toDTO).collect(Collectors.toList());
        objectMapper.writeValue(resp.getWriter(), dtoList);
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json");
        resp.setCharacterEncoding("UTF-8");

        if (!"/importer".equals(req.getPathInfo())) {
            resp.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        try {
            Part filePart = req.getPart("file");
            if (filePart == null) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                objectMapper.writeValue(resp.getWriter(), Map.of("error", "Champ 'file' manquant"));
                return;
            }

            try (InputStream is = filePart.getInputStream()) {
                List<Soutenance> soutenances = excelImportService.importerSoutenances(is);
                soutenances.forEach(soutenanceDAO::save);
                resp.setStatus(HttpServletResponse.SC_OK);
                objectMapper.writeValue(resp.getWriter(), Map.of(
                    "message", "Import réussi",
                    "count", soutenances.size()
                ));
            }
        } catch (Exception e) {
            logger.error("Erreur import soutenances", e);
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            objectMapper.writeValue(resp.getWriter(), Map.of("error", e.getMessage()));
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