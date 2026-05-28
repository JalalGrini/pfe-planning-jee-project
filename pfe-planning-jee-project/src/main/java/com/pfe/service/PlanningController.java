package com.pfe.service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.pfe.dto.PlanningRequestDTO;
import java.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/planning")
public class PlanningController {

    @Autowired
    private PlanningService planningService;

    @PostMapping("/generer")
    public Map<String, Object> genererPlanning(@RequestBody(required = false) PlanningRequestDTO request) {
        PlanningService.PlanningResult result = planningService.genererPlanning(request);
        Map<String, Object> response = new HashMap<>();
        response.put("success", result.isSuccess());
        response.put("nbSoutenancesPlanifiees", result.getNbSoutenancesPlanifiees());
        response.put("nbJours", result.getNbJours());
        response.put("message", result.getMessage());
        if (request != null) {
            response.put("startDate", request.getStartDate());
            response.put("endDate", request.getEndDate());
            response.put("startHour", request.getStartHour());
            response.put("endHour", request.getEndHour());
            response.put("maxSoutenancesParCreneau", request.getMaxSoutenancesParCreneau());
            response.put("excludeWeekends", request.isExcludeWeekends());
            response.put("daysOfWeek", request.getDaysOfWeek());
            response.put("specificDates", request.getSpecificDates());
            response.put("allowedHours", request.getAllowedHours());
        }
        return response;
    }

    @GetMapping("/valider")
    public List<String> validerContraintes() {
        return planningService.validerContraintes();
    }

    @PostMapping("/valider-avant")
    public List<String> validerAvantPlanning(@RequestBody(required = false) PlanningRequestDTO request) {
        return planningService.validerContraintes();
    }
}
