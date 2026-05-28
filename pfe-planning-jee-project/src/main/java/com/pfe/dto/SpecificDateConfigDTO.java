package com.pfe.dto;

import java.io.Serializable;
import java.time.LocalDate;

public class SpecificDateConfigDTO implements Serializable {
    private LocalDate date;
    private Integer startHour;
    private Integer endHour;

    public SpecificDateConfigDTO() {}

    public SpecificDateConfigDTO(LocalDate date, Integer startHour, Integer endHour) {
        this.date = date;
        this.startHour = startHour;
        this.endHour = endHour;
    }

    public LocalDate getDate() { return date; }
    public void setDate(LocalDate date) { this.date = date; }

    public Integer getStartHour() { return startHour; }
    public void setStartHour(Integer startHour) { this.startHour = startHour; }

    public Integer getEndHour() { return endHour; }
    public void setEndHour(Integer endHour) { this.endHour = endHour; }
}
