package com.pfe.model;

import jakarta.persistence.*;

@Entity
@Table(name = "etudiant")
public class Etudiant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String cne;

    @Column(nullable = false)
    private String nom;

    @Column(nullable = false)
    private String prenom;

    @Column(name = "email_perso")
    private String emailPerso;

    @Column(name = "email_acad")
    private String emailAcad;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "filiere_id")
    private Filiere filiere;

    public Etudiant() {}

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
    public Filiere getFiliere() { return filiere; }
    public void setFiliere(Filiere filiere) { this.filiere = filiere; }
}