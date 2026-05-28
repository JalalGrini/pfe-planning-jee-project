package com.pfe.dto;

import java.io.Serializable;

public class EtudiantDTO implements Serializable {
    private Long id;
    private String cne;
    private String nom;
    private String prenom;
    private String emailPerso;
    private String emailAcad;
    private Long filiereId;
    private String filiereNom;

    public EtudiantDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getCne() { return cne; }
    public void setCne(String cne) { this.cne = cne; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public String getPrenom() { return prenom; }
    public void setPrenom(String prenom) { this.prenom = prenom; }
    public String getEmailPerso() { return emailPerso; }
    public void setEmailPerso(String emailPerso) { this.emailPerso = emailPerso; }
    public String getEmailAcad() { return emailAcad; }
    public void setEmailAcad(String emailAcad) { this.emailAcad = emailAcad; }
    public Long getFiliereId() { return filiereId; }
    public void setFiliereId(Long filiereId) { this.filiereId = filiereId; }
    public String getFiliereNom() { return filiereNom; }
    public void setFiliereNom(String filiereNom) { this.filiereNom = filiereNom; }
}