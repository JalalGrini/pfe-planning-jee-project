package com.pfe.dto;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.DayOfWeek;
import java.util.List;

public class PlanningRequestDTO implements Serializable {
    private LocalDate startDate;
    private LocalDate endDate;
    private boolean excludeWeekends;
    private Integer startHour;
    private Integer endHour;
    private Integer maxSoutenancesParCreneau;
    private List<DayOfWeek> daysOfWeek;
    private List<SpecificDateConfigDTO> specificDateConfigs;
    private List<Integer> allowedHours;

    public PlanningRequestDTO() {}

    public LocalDate getStartDate() { return startDate; }
    public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
    public LocalDate getEndDate() { return endDate; }
    public void setEndDate(LocalDate endDate) { this.endDate = endDate; }
    public boolean isExcludeWeekends() { return excludeWeekends; }
    public void setExcludeWeekends(boolean excludeWeekends) { this.excludeWeekends = excludeWeekends; }
    public Integer getStartHour() { return startHour; }
    public void setStartHour(Integer startHour) { this.startHour = startHour; }
    public Integer getEndHour() { return endHour; }
    public void setEndHour(Integer endHour) { this.endHour = endHour; }
    public Integer getMaxSoutenancesParCreneau() { return maxSoutenancesParCreneau; }
    public void setMaxSoutenancesParCreneau(Integer maxSoutenancesParCreneau) { this.maxSoutenancesParCreneau = maxSoutenancesParCreneau; }
    public List<DayOfWeek> getDaysOfWeek() { return daysOfWeek; }
    public void setDaysOfWeek(List<DayOfWeek> daysOfWeek) { this.daysOfWeek = daysOfWeek; }
    public List<SpecificDateConfigDTO> getSpecificDateConfigs() { return specificDateConfigs; }
    public void setSpecificDateConfigs(List<SpecificDateConfigDTO> specificDateConfigs) { this.specificDateConfigs = specificDateConfigs; }
    public List<Integer> getAllowedHours() { return allowedHours; }
    public void setAllowedHours(List<Integer> allowedHours) { this.allowedHours = allowedHours; }
}
