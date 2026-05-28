package com.pfe.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

public class SoutenanceDTO implements Serializable {
    private Long id;
    private String titre;
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;
    private SalleDTO salle;
    private ProfesseurDTO president;
    private List<ProfesseurDTO> jurys;
    private List<EtudiantDTO> etudiants;
    private Long filiereId;
    private String filiereNom;

    public SoutenanceDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getTitre() { return titre; }
    public void setTitre(String titre) { this.titre = titre; }
    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
    public SalleDTO getSalle() { return salle; }
    public void setSalle(SalleDTO salle) { this.salle = salle; }
    public ProfesseurDTO getPresident() { return president; }
    public void setPresident(ProfesseurDTO president) { this.president = president; }
    public List<ProfesseurDTO> getJurys() { return jurys; }
    public void setJurys(List<ProfesseurDTO> jurys) { this.jurys = jurys; }
    public List<EtudiantDTO> getEtudiants() { return etudiants; }
    public void setEtudiants(List<EtudiantDTO> etudiants) { this.etudiants = etudiants; }
    public Long getFiliereId() { return filiereId; }
    public void setFiliereId(Long filiereId) { this.filiereId = filiereId; }
    public String getFiliereNom() { return filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }
}