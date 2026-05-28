package com.pfe.dto;

import java.io.Serializable;
import java.util.List;

public class BinomeRequestDTO implements Serializable {
    private List<Long> etudiantIds;
    private String sujet;

    public BinomeRequestDTO() {}

    public List<Long> getEtudiantIds() { return etudiantIds; }
    public void setEtudiantIds(List<Long> etudiantIds) { this.etudiantIds = etudiantIds; }

    public String getSujet() { return sujet; }
    public void setSujet(String sujet) { this.sujet = sujet; }
}
