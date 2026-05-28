package com.pfe.controller;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.dao.interfaces.IEtudiantDAO;
import com.pfe.dao.interfaces.IFiliereDAO;
import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dto.*;
import com.pfe.model.Etudiant;
import com.pfe.model.Professeur;
import com.pfe.model.Salle;
import com.pfe.model.Soutenance;
import com.pfe.service.ExcelImportService;
import com.pfe.service.EvaluationDocumentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import java.text.Normalizer;
import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/soutenances")
public class SoutenanceController {
    private static final Logger logger = LoggerFactory.getLogger(SoutenanceController.class);

    @Autowired
    private ISoutenanceDAO soutenanceDAO;
    @Autowired
    private IEtudiantDAO etudiantDAO;
    @Autowired
    private IProfesseurDAO professeurDAO;
    @Autowired
    private ISalleDAO salleDAO;
    @Autowired
    private IFiliereDAO filiereDAO;

    @Autowired
    private ExcelImportService excelImportService;

    @Autowired
    private EvaluationDocumentService evaluationDocumentService;

    @GetMapping
    @Transactional(readOnly = true)
    public List<SoutenanceDTO> getAll(@RequestParam(required = false) String filter) {
        List<Soutenance> soutenances = "/non-plannifiees".equals(filter) 
            ? soutenanceDAO.findNonPlannifiees() 
            : soutenanceDAO.findAll();
        return soutenances.stream().map(this::toDTO).collect(Collectors.toList());
    }

    @PostMapping(value = "/importer", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importer(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            List<Soutenance> soutenances = excelImportService.importerSoutenances(file.getInputStream(), file.getOriginalFilename());
            soutenances.forEach(soutenanceDAO::save);
            response.put("message", "Import reussi");
            response.put("count", soutenances.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur import soutenances", e);
            response.put("error", e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }


    @PostMapping(value = "/importer-complet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importerComplet(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            byte[] fileBytes = file.getBytes();
            Map<String, List<?>> result = excelImportService.importerFichierComplet(new ByteArrayInputStream(fileBytes));
            
            @SuppressWarnings("unchecked")
            List<Professeur> professeurs = (List<Professeur>) result.get("professeurs");
            @SuppressWarnings("unchecked")
            List<Salle> salles = (List<Salle>) result.get("salles");
            @SuppressWarnings("unchecked")
            List<Soutenance> soutenances = (List<Soutenance>) result.get("etudiants");
            
            if (professeurs != null) {
                Map<String, String> existingProfesseurs = professeurDAO.findAll().stream()
                        .filter(p -> !isHeaderProfessor(p))
                        .collect(Collectors.toMap(this::professeurKey, p -> professeurKey(p), (a, b) -> a));
                professeurDAO.findAll().stream()
                        .filter(this::isHeaderProfessor)
                        .forEach(p -> professeurDAO.delete(p.getId()));
                for (Professeur professeur : professeurs) {
                    if (isHeaderProfessor(professeur)) continue;
                    String key = professeurKey(professeur);
                    if (!existingProfesseurs.containsKey(key)) {
                        professeurDAO.save(professeur);
                        existingProfesseurs.put(key, key);
                    }
                }
            }
            
            if (salles != null) {
                Map<String, String> existingSalles = salleDAO.findAll().stream()
                        .collect(Collectors.toMap(s -> normalizeText(s.getNom()), s -> normalizeText(s.getNom()), (a, b) -> a));
                for (Salle salle : salles) {
                    String key = normalizeText(salle.getNom());
                    if (!existingSalles.containsKey(key)) {
                        salleDAO.save(salle);
                        existingSalles.put(key, key);
                    }
                }
            }
            
            if (soutenances != null) {
                for (Soutenance soutenance : soutenances) {
                    if (soutenance.getEtudiants() != null) {
                        List<Etudiant> persistedEtudiants = soutenance.getEtudiants().stream()
                                .map(etudiant -> {
                                    Etudiant existing = etudiantDAO.findByCne(etudiant.getCne());
                                    if (existing != null) return existing;
                                    etudiantDAO.save(etudiant);
                                    return etudiant;
                                })
                                .collect(Collectors.toList());
                        soutenance.setEtudiants(persistedEtudiants);
                    }
                    soutenanceDAO.save(soutenance);
                }
            }

            Map<String, Object> stats = new HashMap<>();
            int nbEtudiants = 0;
            List<String> filieresList = new java.util.ArrayList<>();
            if (soutenances != null) {
                nbEtudiants = soutenances.stream().mapToInt(s -> s.getEtudiants() != null ? s.getEtudiants().size() : 0).sum();
                filieresList = soutenances.stream()
                        .filter(s -> s.getFiliere() != null && s.getFiliere().getNom() != null)
                        .map(s -> s.getFiliere().getNom())
                        .distinct()
                        .collect(Collectors.toList());
            }
            stats.put("nbEtudiants", nbEtudiants);
            
            List<String> specialitesList = new java.util.ArrayList<>();
            if (professeurs != null) {
                stats.put("nbProfesseurs", professeurs.size());
                specialitesList = professeurs.stream()
                        .filter(p -> p.getDiscipline() != null)
                        .map(Professeur::getDiscipline)
                        .distinct()
                        .collect(Collectors.toList());
            } else {
                stats.put("nbProfesseurs", 0);
            }
            
            if (salles != null) {
                stats.put("nbSalles", salles.size());
            } else {
                stats.put("nbSalles", 0);
            }
            
            stats.put("nbFilieres", filieresList.size());
            
            Map<String, Object> details = new HashMap<>();
            details.put("filieres", filieresList);
            details.put("specialites", specialitesList);
            stats.put("details", details);
            
            response.put("stats", stats);
            response.put("message", "Import complet réussi");
            response.put("countSoutenances", soutenances != null ? soutenances.size() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur import fichier complet", e);
            response.put("message", "Import complet échoué");
            response.put("errors", List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
    @PostMapping(value = "/importer-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importerMultiple(
            @RequestParam("etudiants") MultipartFile[] etudiantsFiles,
            @RequestParam("professeurs") MultipartFile professeursFile,
            @RequestParam("salles") MultipartFile sallesFile) {
        Map<String, Object> response = new HashMap<>();
        try {
            byte[] profBytes = professeursFile.getBytes();
            byte[] salleBytes = sallesFile.getBytes();
            int nbProfesseursRows = excelImportService.countProfesseursRows(new ByteArrayInputStream(profBytes));
            int nbSallesRows = excelImportService.countSallesRows(new ByteArrayInputStream(salleBytes));

            List<Professeur> professeurs = excelImportService.importerProfesseurs(new ByteArrayInputStream(profBytes));
            List<Salle> salles = excelImportService.importerSalles(new ByteArrayInputStream(salleBytes));
            List<Soutenance> soutenances = new java.util.ArrayList<>();
            int nbEtudiantsRows = 0;
            int nbFiliereFiles = 0;
            if (etudiantsFiles != null) {
                for (MultipartFile file : etudiantsFiles) {
                    if (file != null && !file.isEmpty()) {
                        byte[] studentBytes = file.getBytes();
                        nbEtudiantsRows += excelImportService.countEtudiantsRows(new ByteArrayInputStream(studentBytes));
                        soutenances.addAll(excelImportService.importerSoutenances(new ByteArrayInputStream(studentBytes), file.getOriginalFilename()));
                        nbFiliereFiles++;
                    }
                }
            }
            if (nbFiliereFiles == 0) {
                response.put("message", "Import échoué");
                response.put("errors", List.of("Aucun fichier étudiants reçu"));
                response.put("stats", buildImportStats(0, nbProfesseursRows, nbSallesRows, 0));
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
            }

            Map<String, String> existingProfesseurs = professeurDAO.findAll().stream()
                    .filter(p -> !isHeaderProfessor(p))
                    .collect(Collectors.toMap(this::professeurKey, p -> professeurKey(p), (a, b) -> a));
            professeurDAO.findAll().stream()
                    .filter(this::isHeaderProfessor)
                    .forEach(p -> professeurDAO.delete(p.getId()));
            for (Professeur professeur : professeurs) {
                if (isHeaderProfessor(professeur)) {
                    continue;
                }
                String key = professeurKey(professeur);
                if (!existingProfesseurs.containsKey(key)) {
                    professeurDAO.save(professeur);
                    existingProfesseurs.put(key, key);
                }
            }

            Map<String, String> existingSalles = salleDAO.findAll().stream()
                    .collect(Collectors.toMap(s -> normalizeText(s.getNom()), s -> normalizeText(s.getNom()), (a, b) -> a));
            for (Salle salle : salles) {
                String key = normalizeText(salle.getNom());
                if (!existingSalles.containsKey(key)) {
                    salleDAO.save(salle);
                    existingSalles.put(key, key);
                }
            }

            for (Soutenance soutenance : soutenances) {
                if (soutenance.getEtudiants() != null) {
                    List<Etudiant> persistedEtudiants = soutenance.getEtudiants().stream()
                            .map(etudiant -> {
                                Etudiant existing = etudiantDAO.findByCne(etudiant.getCne());
                                if (existing != null) {
                                    return existing;
                                }
                                etudiantDAO.save(etudiant);
                                return etudiant;
                            })
                            .collect(Collectors.toList());
                    soutenance.setEtudiants(persistedEtudiants);
                }
                soutenanceDAO.save(soutenance);
            }

            response.put("message", "Import réussi");
            response.put("count", soutenances.size());
            response.put("errors", List.of());
            response.put("stats", buildImportStats(
                    nbEtudiantsRows,
                    nbProfesseursRows,
                    nbSallesRows,
                    nbFiliereFiles
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur import multi-fichiers", e);
            response.put("message", "Import échoué");
            response.put("errors", List.of(e.getMessage()));
            response.put("stats", buildImportStats(
                    0,
                    0,
                    0,
                    0
            ));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }

    @GetMapping("/par-encadrant")
    @Transactional(readOnly = true)
    public List<SoutenanceDTO> getByEncadrant(@RequestParam(name = "q", required = false) String query) {
        String normalizedQuery = normalizeText(query);
        List<String> queryTokens = splitTokens(normalizedQuery);
        return soutenanceDAO.findAll().stream()
            .filter(s -> matchesSoutenanceSearch(s, normalizedQuery, queryTokens))
            .map(this::toDTO)
            .collect(Collectors.toList());
    }

    @GetMapping("/{id}/evaluation")
    public ResponseEntity<byte[]> downloadEvaluation(@PathVariable Long id) {
        EvaluationDocumentService.EvaluationPayload payload = evaluationDocumentService.generateEvaluationDoc(id);
        if (payload == null) {
            return ResponseEntity.notFound().build();
        }

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.parseMediaType(
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
        ));
        headers.setContentDispositionFormData("attachment", payload.getFileName());
        return ResponseEntity.ok().headers(headers).body(payload.getData());
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
            salleDTO.setNom(normalizeSalleName(s.getSalle().getNom()));
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

    private boolean matchesSoutenanceSearch(Soutenance s, String normalizedQuery, List<String> queryTokens) {
        if (normalizedQuery.isEmpty()) {
            return true;
        }
        if (s == null) {
            return false;
        }

        if (s.getPresident() != null && matchesPersonName(
                s.getPresident().getNom(),
                s.getPresident().getPrenom(),
                normalizedQuery,
                queryTokens
        )) {
            return true;
        }

        if (s.getEtudiants() != null) {
            for (Etudiant etudiant : s.getEtudiants()) {
                if (etudiant != null && matchesPersonName(
                        etudiant.getNom(),
                        etudiant.getPrenom(),
                        normalizedQuery,
                        queryTokens
                )) {
                    return true;
                }
            }
        }

        return false;
    }

    private boolean matchesPersonName(String nomValue, String prenomValue, String normalizedQuery, List<String> queryTokens) {
        String nom = normalizeText(safeString(nomValue));
        String prenom = normalizeText(safeString(prenomValue));
        if (nom.isBlank() && prenom.isBlank()) {
            return false;
        }

        String nomPrenom = (nom + " " + prenom).trim();
        String prenomNom = (prenom + " " + nom).trim();

        if (nom.contains(normalizedQuery)
                || prenom.contains(normalizedQuery)
                || nomPrenom.contains(normalizedQuery)
                || prenomNom.contains(normalizedQuery)) {
            return true;
        }

        if (queryTokens.isEmpty()) {
            return false;
        }

        for (String token : queryTokens) {
            boolean tokenMatched = nom.contains(token)
                    || prenom.contains(token)
                    || nomPrenom.contains(token)
                    || prenomNom.contains(token);
            if (!tokenMatched) {
                return false;
            }
        }
        return true;
    }

    private List<String> splitTokens(String normalizedQuery) {
        if (normalizedQuery == null || normalizedQuery.isBlank()) {
            return List.of();
        }
        return java.util.Arrays.stream(normalizedQuery.split("\\s+"))
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .collect(Collectors.toList());
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        String normalized = Normalizer.normalize(value, Normalizer.Form.NFD);
        String withoutMarks = normalized.replaceAll("\\p{M}", "");
        return withoutMarks.toLowerCase(Locale.ROOT).trim();
    }

    private String safeString(String value) {
        return value == null ? "" : value.trim();
    }

    private String professeurKey(Professeur professeur) {
        if (professeur == null) {
            return "";
        }
        return normalizeText(safeString(professeur.getNom()) + " " + safeString(professeur.getPrenom()));
    }

    private boolean isHeaderProfessor(Professeur professeur) {
        if (professeur == null) {
            return true;
        }
        String nom = normalizeText(safeString(professeur.getNom()));
        String prenom = normalizeText(safeString(professeur.getPrenom()));
        String merged = (nom + " " + prenom).trim();
        return nom.isBlank()
                || prenom.isBlank()
                || "nom".equals(nom)
                || "prenom".equals(prenom)
                || merged.contains("nom prenom")
                || merged.contains("prenom nom");
    }

    private String normalizeSalleName(String raw) {
        if (raw == null || raw.isBlank()) {
            return raw;
        }
        String text = raw.trim().toUpperCase(Locale.ROOT);
        java.util.regex.Matcher m1 = java.util.regex.Pattern.compile("\\b([AB])\\s*0*(\\d{1,2})\\b").matcher(text);
        if (m1.find()) {
            return m1.group(1) + String.format("%02d", Integer.parseInt(m1.group(2)));
        }
        java.util.regex.Matcher m2 = java.util.regex.Pattern.compile("\\b(\\d{3})\\b").matcher(text);
        if (m2.find()) {
            int n = Integer.parseInt(m2.group(1));
            char bloc = n >= 200 ? 'B' : 'A';
            return bloc + String.format("%02d", n % 100);
        }
        return text.replace("SALLE", "").trim();
    }

    private Map<String, Object> buildStats() {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nbEtudiants", etudiantDAO.count());
        stats.put("nbProfesseurs", professeurDAO.count());
        stats.put("nbSalles", salleDAO.count());
        stats.put("nbFilieres", filiereDAO.count());
        stats.put("nbSoutenancesNonPlannifiees", soutenanceDAO.countNonPlannifiees());
        return stats;
    }

    private Map<String, Object> buildImportStats(
            int nbEtudiantsRows,
            int nbProfesseursRows,
            int nbSallesRows,
            int nbFiliereFiles
    ) {
        Map<String, Object> stats = new HashMap<>();
        stats.put("nbEtudiants", nbEtudiantsRows);
        stats.put("nbProfesseurs", nbProfesseursRows);
        stats.put("nbSalles", nbSallesRows);
        stats.put("nbFilieres", nbFiliereFiles);
        stats.put("nbSoutenancesNonPlannifiees", soutenanceDAO.countNonPlannifiees());
        return stats;
    }
}
