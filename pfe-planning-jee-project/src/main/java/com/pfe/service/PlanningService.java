package com.pfe.service;

import com.pfe.dao.interfaces.IPlanningDAO;
import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dao.interfaces.IProfesseurIndisponibiliteDAO;
import com.pfe.dto.PlanningRequestDTO;
import com.pfe.model.ProfesseurIndisponibilite;
import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import java.lang.reflect.Method;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.text.Normalizer;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PlanningService {
    private static final Logger logger = LoggerFactory.getLogger(PlanningService.class);

    private static final int MAX_SOUTENANCES_PAR_JOUR = 28;
    private static final int MIN_REPOS_HEURES = 2;
    private static final int DEFAULT_START_HOUR = 9;
    private static final int DEFAULT_END_HOUR = 18;
    private static final int DEFAULT_MAX_PAR_CRENEAU = 3;

    @Autowired
    private IPlanningDAO planningDAO;

    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    @Autowired
    private ISalleDAO salleDAO;

    @Autowired
    private IProfesseurDAO professeurDAO;

    @Autowired
    private IProfesseurIndisponibiliteDAO professeurIndisponibiliteDAO;

    private final Random random = new Random();

    public static class PlanningResult {
        private boolean success;
        private int nbSoutenancesPlanifiees;
        private int nbJours;
        private String message;

        public PlanningResult(boolean success, int nbSoutenancesPlanifiees, int nbJours, String message) {
            this.success = success;
            this.nbSoutenancesPlanifiees = nbSoutenancesPlanifiees;
            this.nbJours = nbJours;
            this.message = message;
        }

        public boolean isSuccess() {
            return success;
        }

        public int getNbSoutenancesPlanifiees() {
            return nbSoutenancesPlanifiees;
        }

        public int getNbJours() {
            return nbJours;
        }

        public String getMessage() {
            return message;
        }
    }

    @Transactional
    public PlanningResult genererPlanning() {
        return genererPlanning(null, null, false, null, null, null, null, null, null);
    }

    @Transactional
    public PlanningResult genererPlanning(LocalDate startDate, LocalDate endDate, boolean excludeWeekends) {
        return genererPlanning(startDate, endDate, excludeWeekends, null, null, null, null, null, null);
    }

    @Transactional
    public PlanningResult genererPlanning(PlanningRequestDTO request) {
        if (request == null) {
            return genererPlanning();
        }
        return genererPlanning(
            request.getStartDate(),
            request.getEndDate(),
            request.isExcludeWeekends(),
            request.getStartHour(),
            request.getEndHour(),
            request.getMaxSoutenancesParCreneau(),
            request.getDaysOfWeek(),
            request.getSpecificDateConfigs(),
            request.getAllowedHours()
        );
    }

    @Transactional
    public PlanningResult genererPlanning(
        LocalDate startDate,
        LocalDate endDate,
        boolean excludeWeekends,
        Integer startHour,
        Integer endHour,
        Integer maxSoutenancesParCreneau,
        List<DayOfWeek> daysOfWeek,
        List<SpecificDateConfigDTO> specificDateConfigs,
        List<Integer> allowedHours
    ) {
        int validatedStartHour = sanitizeStartHour(startHour);
        int validatedEndHour = sanitizeEndHour(endHour, validatedStartHour);
        int validatedMaxParCreneau = sanitizeMaxParCreneau(maxSoutenancesParCreneau);
        List<LocalTime> globalCreneaux = buildCreneaux(validatedStartHour, validatedEndHour, allowedHours);
        if (globalCreneaux.isEmpty()) {
            return new PlanningResult(false, 0, 0, "Aucun créneau valide dans la plage horaire globale fournie");
        }

        List<?> donnees = safeList(planningDAO.getDonneesPlanification());
        List<Object> nonPlanifiees = new ArrayList<>();
        for (Object s : donnees) {
            if (getDate(s) == null) {
                nonPlanifiees.add(s);
            }
        }

        int nbSoutenances = nonPlanifiees.size();
        if (nbSoutenances == 0) {
            logger.info("Aucune soutenance a planifier.");
            return new PlanningResult(true, 0, 0, "Aucune soutenance a planifier");
        }

        if (startDate == null || endDate == null) {
            int nbJours = (int) Math.ceil(nbSoutenances / (double) MAX_SOUTENANCES_PAR_JOUR);
            startDate = LocalDate.now().plusDays(1);
            endDate = startDate.plusDays(Math.max(0, nbJours - 1));
            excludeWeekends = false;
        }

        if (endDate.isBefore(startDate)) {
            return new PlanningResult(false, 0, 0, "Periode invalide: la date de fin est avant la date de debut");
        }

        List<LocalDate> planningDates = buildPlanningDates(startDate, endDate, excludeWeekends, daysOfWeek, specificDateConfigs);
        if (planningDates.isEmpty()) {
            return new PlanningResult(false, 0, 0, "Aucune date disponible dans la periode choisie");
        }

        int capacity = planningDates.size() * MAX_SOUTENANCES_PAR_JOUR;
        if (capacity < nbSoutenances) {
            return new PlanningResult(
                false,
                0,
                planningDates.size(),
                "Periode insuffisante: capacite=" + capacity + " soutenances, besoin=" + nbSoutenances
            );
        }

        int nbJours = planningDates.size();

        List<Object> salles = filterSallesDisponibles(safeList(salleDAO.findAll()));
        List<Object> professeurs = filterProfesseursValides(safeList(professeurDAO.findAll()));

        Map<LocalDate, Map<LocalTime, Set<Long>>> usedRooms = new HashMap<>();
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes = new HashMap<>();
        initialiserContraintes(safeList(soutenanceDAO.findAll()), usedRooms, profTimes);
        
        List<ProfesseurIndisponibilite> indisponibilites = professeurIndisponibiliteDAO.findAll();
        Map<Long, List<ProfesseurIndisponibilite>> indispoMap = new HashMap<>();
        for (ProfesseurIndisponibilite pi : indisponibilites) {
            Long profId = safeId(pi.getProfesseur());
            indispoMap.computeIfAbsent(profId, k -> new ArrayList<>()).add(pi);
        }

        Map<String, Deque<Object>> parFiliere = grouperParFiliere(nonPlanifiees);
        List<String> filieres = new ArrayList<>(parFiliere.keySet());
        filieres.sort(Comparator.naturalOrder());

        int planifiees = 0;
        List<Object> reportees = new ArrayList<>();

        logger.info("Debut generation planning: {} soutenances, {} jours", nbSoutenances, nbJours);

        for (int dayIndex = 0; dayIndex < nbJours; dayIndex++) {
            LocalDate date = planningDates.get(dayIndex);
            int remainingDays = nbJours - dayIndex;
            int quota = calculerQuotaParFiliere(parFiliere, remainingDays);
            int remainingSoutenances = reportees.size() + countRemaining(parFiliere);
            int targetJour = Math.min(
                MAX_SOUTENANCES_PAR_JOUR,
                (int) Math.ceil(remainingSoutenances / (double) Math.max(1, remainingDays))
            );

            List<Object> ciblesJour = new ArrayList<>();
            if (!reportees.isEmpty()) {
                ciblesJour.addAll(reportees);
                reportees.clear();
            }

            for (String filiereKey : filieres) {
                Deque<Object> file = parFiliere.get(filiereKey);
                for (int i = 0; i < quota; i++) {
                    if (file != null && !file.isEmpty()) {
                        ciblesJour.add(file.pollFirst());
                    }
                }
            }
            // Ensure we still fill the day target with remaining soutenances.
            fillCiblesJourRoundRobin(ciblesJour, parFiliere, filieres, targetJour);

            logger.info("Jour {}: {} soutenances a planifier", date, ciblesJour.size());

            List<LocalTime> creneauxJour = globalCreneaux;
            if (specificDateConfigs != null) {
                for (SpecificDateConfigDTO config : specificDateConfigs) {
                    if (config.getDate() != null && config.getDate().equals(date)) {
                        int customStartHour = sanitizeStartHour(config.getStartHour() != null ? config.getStartHour() : startHour);
                        int customEndHour = sanitizeEndHour(config.getEndHour() != null ? config.getEndHour() : endHour, customStartHour);
                        creneauxJour = buildCreneaux(customStartHour, customEndHour, allowedHours);
                        break;
                    }
                }
            }

            for (Object soutenance : ciblesJour) {
                boolean ok = planifierSoutenance(
                    soutenance,
                    date,
                    salles,
                    professeurs,
                    usedRooms,
                    profTimes,
                    creneauxJour,
                    validatedMaxParCreneau,
                    indispoMap
                );
                if (ok) {
                    planifiees++;
                    appelerSave(soutenance);
                } else {
                    reportees.add(soutenance);
                }
            }

            if (planifiees == nbSoutenances) {
                break;
            }
        }

        if (!reportees.isEmpty()) {
            logger.warn("{} soutenances non planifiees apres {} jours", reportees.size(), nbJours);
        }

        boolean success = planifiees == nbSoutenances;
        String message = success
            ? "Planning genere avec succes"
            : "Planning genere partiellement";
        return new PlanningResult(success, planifiees, nbJours, message);
    }

    private int countRemaining(Map<String, Deque<Object>> parFiliere) {
        int total = 0;
        for (Deque<Object> queue : parFiliere.values()) {
            total += queue.size();
        }
        return total;
    }

    private void fillCiblesJourRoundRobin(
        List<Object> ciblesJour,
        Map<String, Deque<Object>> parFiliere,
        List<String> filieres,
        int targetJour
    ) {
        if (ciblesJour.size() >= targetJour) {
            return;
        }
        boolean added = true;
        while (ciblesJour.size() < targetJour && added) {
            added = false;
            for (String filiereKey : filieres) {
                if (ciblesJour.size() >= targetJour) {
                    break;
                }
                Deque<Object> queue = parFiliere.get(filiereKey);
                if (queue != null && !queue.isEmpty()) {
                    ciblesJour.add(queue.pollFirst());
                    added = true;
                }
            }
        }
    }

    private List<LocalDate> buildPlanningDates(LocalDate startDate, LocalDate endDate, boolean excludeWeekends, List<DayOfWeek> daysOfWeek, List<SpecificDateConfigDTO> specificDateConfigs) {
        if (specificDateConfigs != null && !specificDateConfigs.isEmpty()) {
            List<LocalDate> dates = new ArrayList<>();
            for (SpecificDateConfigDTO config : specificDateConfigs) {
                LocalDate d = config.getDate();
                if (d != null && !d.isBefore(startDate) && !d.isAfter(endDate)) {
                    dates.add(d);
                }
            }
            dates.sort(Comparator.naturalOrder());
            return dates;
        }
        List<LocalDate> dates = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(endDate); d = d.plusDays(1)) {
            if (excludeWeekends) {
                DayOfWeek day = d.getDayOfWeek();
                if (day == DayOfWeek.SATURDAY || day == DayOfWeek.SUNDAY) {
                    continue;
                }
            }
            if (daysOfWeek != null && !daysOfWeek.isEmpty()) {
                if (!daysOfWeek.contains(d.getDayOfWeek())) {
                    continue;
                }
            }
            dates.add(d);
        }
        return dates;
    }

    public List<String> validerContraintes() {
        List<String> anomalies = new ArrayList<>();
        List<?> soutenances = safeList(soutenanceDAO.findAll());

        Map<LocalDate, Map<LocalTime, Set<Long>>> usedRooms = new HashMap<>();
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes = new HashMap<>();
        Map<LocalDate, Integer> dayCounts = new HashMap<>();
        Map<LocalDate, Map<String, Integer>> dayFiliereCounts = new HashMap<>();

        for (Object s : soutenances) {
            LocalDate date = getDate(s);
            LocalTime time = getHeureDebut(s);
            Object salle = getSalle(s);

            verifierJury(s, anomalies);

            if (date == null || time == null || salle == null) {
                continue;
            }

            Long salleId = safeId(salle);
            Set<Long> roomsAtSlot = usedRooms
                .computeIfAbsent(date, d -> new HashMap<>())
                .computeIfAbsent(time, t -> new HashSet<>());
            if (roomsAtSlot.contains(salleId)) {
                anomalies.add("Chevauchement salle pour date=" + date + " heure=" + time + " salleId=" + salleId);
            } else {
                roomsAtSlot.add(salleId);
            }

            dayCounts.put(date, dayCounts.getOrDefault(date, 0) + 1);

            String filiereKey = getFiliereKey(s);
            Map<String, Integer> filiereCounts = dayFiliereCounts.computeIfAbsent(date, d -> new HashMap<>());
            filiereCounts.put(filiereKey, filiereCounts.getOrDefault(filiereKey, 0) + 1);

            enregistrerProfesseurs(s, date, time, profTimes);
        }

        for (Map.Entry<LocalDate, Integer> entry : dayCounts.entrySet()) {
            if (entry.getValue() > MAX_SOUTENANCES_PAR_JOUR) {
                anomalies.add("Capacite depassee pour date=" + entry.getKey() + " count=" + entry.getValue());
            }
        }

        for (Map.Entry<LocalDate, Map<String, Integer>> entry : dayFiliereCounts.entrySet()) {
            Set<Integer> counts = new HashSet<>(entry.getValue().values());
            if (counts.size() > 1) {
                anomalies.add("Inegalite filieres pour date=" + entry.getKey() + " details=" + entry.getValue());
            }
        }

        for (Map.Entry<LocalDate, Map<Long, List<LocalTime>>> dateEntry : profTimes.entrySet()) {
            for (Map.Entry<Long, List<LocalTime>> profEntry : dateEntry.getValue().entrySet()) {
                List<LocalTime> times = new ArrayList<>(profEntry.getValue());
                times.sort(Comparator.naturalOrder());
                for (int i = 1; i < times.size(); i++) {
                    long diff = Math.abs(ChronoUnit.HOURS.between(times.get(i - 1), times.get(i)));
                    if (diff < MIN_REPOS_HEURES) {
                        anomalies.add(
                            "Repos insuffisant pour profId=" + profEntry.getKey()
                                + " date=" + dateEntry.getKey()
                                + " heures=" + times
                        );
                        break;
                    }
                }
            }
        }

        return anomalies;
    }

    private boolean planifierSoutenance(
        Object soutenance,
        LocalDate date,
        List<Object> salles,
        List<Object> professeurs,
        Map<LocalDate, Map<LocalTime, Set<Long>>> usedRooms,
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes,
        List<LocalTime> creneaux,
        int maxParCreneau,
        Map<Long, List<ProfesseurIndisponibilite>> indispoMap
    ) {
        for (LocalTime start : creneaux) {
            Set<Long> roomsAtSlot = usedRooms
                .computeIfAbsent(date, d -> new HashMap<>())
                .computeIfAbsent(start, t -> new HashSet<>());
            if (roomsAtSlot.size() >= maxParCreneau) {
                continue;
            }
            for (Object salle : salles) {
                if (!isSalleDisponible(salle)) {
                    continue;
                }

                Long salleId = safeId(salle);
                if (roomsAtSlot.contains(salleId)) {
                    continue;
                }

                List<Object> disponibles = professeursDisponibles(date, start, professeurs, profTimes, indispoMap);
                if (disponibles.size() < 3) {
                    continue;
                }

                Collections.shuffle(disponibles, random);
                Object president = disponibles.get(0);
                Object jury1 = disponibles.get(1);
                Object jury2 = disponibles.get(2);

                setDate(soutenance, date);
                setHeureDebut(soutenance, start);
                setHeureFin(soutenance, start.plusHours(1));
                setSalle(soutenance, salle);
                setPresident(soutenance, president);
                List<Object> jurys = new ArrayList<>();
                jurys.add(jury1);
                jurys.add(jury2);
                setJurys(soutenance, jurys);

                roomsAtSlot.add(salleId);
                enregistrerProfesseurs(date, start, profTimes, president, jury1, jury2);
                return true;
            }
        }

        logger.warn("Aucun creneau disponible pour soutenance id={}", safeId(soutenance));
        return false;
    }

    private int calculerQuotaParFiliere(Map<String, Deque<Object>> parFiliere, int remainingDays) {
        int filiereCount = parFiliere.size();
        if (filiereCount == 0 || remainingDays <= 0) {
            return 0;
        }

        int minRemaining = Integer.MAX_VALUE;
        for (Deque<Object> file : parFiliere.values()) {
            minRemaining = Math.min(minRemaining, file.size());
        }
        if (minRemaining == Integer.MAX_VALUE) {
            return 0;
        }

        int quota = (int) Math.ceil(minRemaining / (double) remainingDays);
        int maxQuota = MAX_SOUTENANCES_PAR_JOUR / filiereCount;
        if (maxQuota <= 0) {
            maxQuota = 1;
        }
        int result = Math.min(quota, maxQuota);
        return Math.max(1, result);
    }

    private Map<String, Deque<Object>> grouperParFiliere(List<Object> soutenances) {
        Map<String, Deque<Object>> map = new HashMap<>();
        for (Object s : soutenances) {
            String key = getFiliereKey(s);
            map.computeIfAbsent(key, k -> new ArrayDeque<>()).add(s);
        }
        return map;
    }

    private void initialiserContraintes(
        List<?> soutenances,
        Map<LocalDate, Map<LocalTime, Set<Long>>> usedRooms,
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes
    ) {
        for (Object s : soutenances) {
            LocalDate date = getDate(s);
            LocalTime time = getHeureDebut(s);
            Object salle = getSalle(s);
            if (date == null || time == null || salle == null) {
                continue;
            }

            Long salleId = safeId(salle);
            usedRooms
                .computeIfAbsent(date, d -> new HashMap<>())
                .computeIfAbsent(time, t -> new HashSet<>())
                .add(salleId);

            enregistrerProfesseurs(s, date, time, profTimes);
        }
    }

    private void verifierJury(Object soutenance, List<String> anomalies) {
        Object president = getPresident(soutenance);
        List<?> jurys = safeList(getJurys(soutenance));

        if (president == null || jurys.size() != 2) {
            anomalies.add("Jury incomplet pour soutenance id=" + safeId(soutenance));
            return;
        }

        Object jury1 = jurys.get(0);
        Object jury2 = jurys.get(1);
        if (equalsById(president, jury1) || equalsById(president, jury2) || equalsById(jury1, jury2)) {
            anomalies.add("Jury non distinct pour soutenance id=" + safeId(soutenance));
        }
    }

    private List<Object> professeursDisponibles(
        LocalDate date,
        LocalTime time,
        List<Object> professeurs,
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes,
        Map<Long, List<ProfesseurIndisponibilite>> indispoMap
    ) {
        List<Object> disponibles = new ArrayList<>();
        for (Object prof : professeurs) {
            Long profId = safeId(prof);
            if (isProfDisponible(profId, date, time, profTimes, indispoMap)) {
                disponibles.add(prof);
            }
        }
        return disponibles;
    }

    private boolean isProfDisponible(
        Long profId,
        LocalDate date,
        LocalTime time,
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes,
        Map<Long, List<ProfesseurIndisponibilite>> indispoMap
    ) {
        Map<Long, List<LocalTime>> byProf = profTimes.get(date);
        if (byProf != null) {
            List<LocalTime> times = byProf.get(profId);
            if (times != null) {
                for (LocalTime existing : times) {
                    long diff = Math.abs(ChronoUnit.HOURS.between(existing, time));
                    if (diff < MIN_REPOS_HEURES) {
                        return false;
                    }
                }
            }
        }
        
        List<ProfesseurIndisponibilite> list = indispoMap.get(profId);
        if (list != null) {
            LocalTime timeFin = time.plusHours(1);
            for (ProfesseurIndisponibilite pi : list) {
                if (pi.getDate().equals(date)) {
                    LocalTime debut = pi.getHeureDebut();
                    LocalTime fin = pi.getHeureFin();
                    if (debut == null && fin == null) return false;
                    if (debut != null && fin == null && !time.isBefore(debut)) return false;
                    if (debut == null && fin != null && time.isBefore(fin)) return false;
                    if (debut != null && fin != null && time.isBefore(fin) && timeFin.isAfter(debut)) return false;
                }
            }
        }
        return true;
    }

    private void enregistrerProfesseurs(
        Object soutenance,
        LocalDate date,
        LocalTime time,
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes
    ) {
        Object president = getPresident(soutenance);
        List<?> jurys = safeList(getJurys(soutenance));

        if (president != null) {
            enregistrerProfesseurs(date, time, profTimes, president);
        }
        for (Object jury : jurys) {
            enregistrerProfesseurs(date, time, profTimes, jury);
        }
    }

    private void enregistrerProfesseurs(
        LocalDate date,
        LocalTime time,
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes,
        Object... professeurs
    ) {
        Map<Long, List<LocalTime>> byProf = profTimes.computeIfAbsent(date, d -> new HashMap<>());
        for (Object prof : professeurs) {
            if (prof == null) {
                continue;
            }
            Long profId = safeId(prof);
            byProf.computeIfAbsent(profId, p -> new ArrayList<>()).add(time);
        }
    }

    private List<Object> filterSallesDisponibles(List<?> salles) {
        List<Object> disponibles = new ArrayList<>();
        List<Object> styleExcel = new ArrayList<>();
        for (Object salle : salles) {
            if (isSalleDisponible(salle)) {
                disponibles.add(salle);
                String nom = getSalleNom(salle).toUpperCase();
                if (nom.matches(".*\\b[A-Z]\\d{2,3}\\b.*")) {
                    styleExcel.add(salle);
                }
            }
        }
        return styleExcel.isEmpty() ? disponibles : styleExcel;
    }

    private List<Object> filterProfesseursValides(List<?> profs) {
        List<Object> result = new ArrayList<>();
        for (Object prof : profs) {
            if (isValidProfesseur(prof)) {
                result.add(prof);
            }
        }
        return result;
    }

    private boolean isValidProfesseur(Object prof) {
        if (prof == null) {
            return false;
        }
        String nom = normalizeText(String.valueOf(tryInvokeGetter(prof, "getNom")));
        String prenom = normalizeText(String.valueOf(tryInvokeGetter(prof, "getPrenom")));
        if (nom.isBlank() || prenom.isBlank()) {
            return false;
        }
        String merged = (nom + " " + prenom).trim();
        return !nom.equals("nom")
                && !prenom.equals("prenom")
                && !merged.contains("nom prenom")
                && !merged.contains("prenom nom")
                && !merged.contains("nom de famille");
    }

    private String normalizeText(String value) {
        if (value == null) {
            return "";
        }
        return Normalizer.normalize(value, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .toLowerCase(Locale.ROOT)
                .trim();
    }

    private String getSalleNom(Object salle) {
        Object nom = tryInvokeGetter(salle, "getNom");
        return nom == null ? "" : String.valueOf(nom);
    }

    private int sanitizeStartHour(Integer startHour) {
        if (startHour == null) {
            return DEFAULT_START_HOUR;
        }
        return Math.max(0, Math.min(23, startHour));
    }

    private int sanitizeEndHour(Integer endHour, int startHour) {
        if (endHour == null) {
            return DEFAULT_END_HOUR;
        }
        int value = Math.max(1, Math.min(24, endHour));
        if (value <= startHour) {
            return startHour + 1;
        }
        return value;
    }

    private int sanitizeMaxParCreneau(Integer value) {
        if (value == null) {
            return DEFAULT_MAX_PAR_CRENEAU;
        }
        return Math.max(1, Math.min(3, value));
    }

    private List<LocalTime> buildCreneaux(int startHour, int endHour, List<Integer> allowedHours) {
        List<LocalTime> creneaux = new ArrayList<>();
        for (int hour = startHour; hour < endHour; hour++) {
            if (allowedHours != null && !allowedHours.isEmpty() && !allowedHours.contains(hour)) {
                continue;
            }
            creneaux.add(LocalTime.of(hour, 0));
        }
        return creneaux;
    }

    private boolean isSalleDisponible(Object salle) {
        if (salle == null) {
            return false;
        }
        Object value = tryInvokeGetter(salle, "isDisponible");
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        value = tryInvokeGetter(salle, "getDisponible");
        if (value instanceof Boolean) {
            return (Boolean) value;
        }
        return true;
    }

    private void appelerSave(Object soutenance) {
        Method saveMethod = findMethod(ISoutenanceDAO.class, "save", 1);
        if (!saveMethod.getParameterTypes()[0].isInstance(soutenance)) {
            return;
        }
        try {
            saveMethod.invoke(soutenanceDAO, soutenance);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur appel save sur soutenance", e);
        }
    }

    private boolean equalsById(Object a, Object b) {
        if (a == null || b == null) {
            return false;
        }
        return safeId(a).equals(safeId(b));
    }

    private String getFiliereKey(Object soutenance) {
        Object filiere = getFiliere(soutenance);
        if (filiere == null) {
            return "UNKNOWN";
        }
        Object nom = tryInvokeGetter(filiere, "getNom");
        if (nom instanceof String && !((String) nom).isEmpty()) {
            return (String) nom;
        }
        return String.valueOf(safeId(filiere));
    }

    private List<?> safeList(Object listValue) {
        if (listValue instanceof List<?>) {
            return (List<?>) listValue;
        }
        return Collections.emptyList();
    }

    private Long safeId(Object target) {
        if (target == null) {
            return 0L;
        }
        Object id = tryInvokeGetter(target, "getId");
        if (id instanceof Number) {
            return ((Number) id).longValue();
        }
        return (long) System.identityHashCode(target);
    }

    private LocalDate getDate(Object soutenance) {
        return (LocalDate) tryInvokeGetter(soutenance, "getDate");
    }

    private LocalTime getHeureDebut(Object soutenance) {
        return (LocalTime) tryInvokeGetter(soutenance, "getHeureDebut");
    }

    private Object getSalle(Object soutenance) {
        return tryInvokeGetter(soutenance, "getSalle");
    }

    private Object getPresident(Object soutenance) {
        return tryInvokeGetter(soutenance, "getPresident");
    }

    private Object getJurys(Object soutenance) {
        return tryInvokeGetter(soutenance, "getJurys");
    }

    private Object getFiliere(Object soutenance) {
        return tryInvokeGetter(soutenance, "getFiliere");
    }

    private void setDate(Object soutenance, LocalDate date) {
        invokeSetter(soutenance, "setDate", date);
    }

    private void setHeureDebut(Object soutenance, LocalTime time) {
        invokeSetter(soutenance, "setHeureDebut", time);
    }

    private void setHeureFin(Object soutenance, LocalTime time) {
        invokeSetter(soutenance, "setHeureFin", time);
    }

    private void setSalle(Object soutenance, Object salle) {
        invokeSetter(soutenance, "setSalle", salle);
    }

    private void setPresident(Object soutenance, Object president) {
        invokeSetter(soutenance, "setPresident", president);
    }

    private void setJurys(Object soutenance, List<Object> jurys) {
        invokeSetter(soutenance, "setJurys", jurys);
    }

    private Object tryInvokeGetter(Object target, String methodName) {
        if (target == null) {
            return null;
        }
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private void invokeSetter(Object target, String methodName, Object value) {
        if (target == null) {
            return;
        }
        Method method = findMethod(target.getClass(), methodName, 1);
        try {
            method.setAccessible(true);
            method.invoke(target, value);
        } catch (Exception e) {
            throw new IllegalStateException("Erreur setter " + methodName + " sur " + target.getClass().getName(), e);
        }
    }

    private Method findMethod(Class<?> type, String name, int paramCount) {
        for (Method method : type.getMethods()) {
            if (method.getName().equals(name) && method.getParameterCount() == paramCount) {
                return method;
            }
        }
        throw new IllegalStateException("Methode " + name + " introuvable sur " + type.getName());
    }
}
