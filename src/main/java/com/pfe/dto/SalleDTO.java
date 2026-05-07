package com.pfe.dto;

import java.io.Serializable;

public class SalleDTO implements Serializable {
    private Long id;
    private String nom;
    private int capacite;
    private boolean disponible;

    public SalleDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getNom() { return nom; }
    public void setNom(String nom) { this.nom = nom; }
    public int getCapacite() { return capacite; }
    public void setCapacite(int capacite) { this.capacite = capacite; }
    public boolean isDisponible() { return disponible; }
    public void setDisponible(boolean disponible) { this.disponible = disponible; }
}