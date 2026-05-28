package com.pfe.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalTime;

public class CreneauDTO implements Serializable {
    private LocalDate date;
    private LocalTime heureDebut;
    private LocalTime heureFin;

    public CreneauDTO() {}

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }
    public LocalTime getHeureDebut() { return heureDebut; }
    public void setHeureDebut(LocalTime heureDebut) { this.heureDebut = heureDebut; }
    public LocalTime getHeureFin() { return heureFin; }
    public void setHeureFin(LocalTime heureFin) { this.heureFin = heureFin; }
}