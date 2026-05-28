package com.pfe.dto;

import java.io.Serializable;
import java.util.List;

public class BinomeDTO implements Serializable {
    private String sujet;
    private List<EtudiantDTO> etudiants;
    private boolean confirmed;

    public BinomeDTO() {}

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }

    public List<EtudiantDTO> getEtudiants() { return etudiants; }
    public void setEtudiants(List<EtudiantDTO> etudiants) { this.etudiants = etudiants; }

    public boolean isConfirmed() { return confirmed; }
    public void setConfirmed(boolean confirmed) { this.confirmed = confirmed; }
}
