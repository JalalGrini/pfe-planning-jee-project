package com.pfe.test;

import com.pfe.dao.interfaces.IPlanningDAO;
import com.pfe.dao.interfaces.IProfesseurDAO;
import com.pfe.dao.interfaces.ISalleDAO;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.service.PlanningService;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class PlanningServiceTest {

    @Mock
    private IPlanningDAO planningDAO;

    @Mock
    private ISoutenanceDAO soutenanceDAO;

    @Mock
    private ISalleDAO salleDAO;

    @Mock
    private IProfesseurDAO professeurDAO;

    @Mock
    private com.pfe.dao.interfaces.IProfesseurIndisponibiliteDAO professeurIndisponibiliteDAO;

    @InjectMocks
    private PlanningService planningService;

    @Test
    void testEquiteFilieresParJour() {
        AtomicLong id = new AtomicLong(1);
        DummyFiliere f1 = new DummyFiliere(1L, "F1");
        DummyFiliere f2 = new DummyFiliere(2L, "F2");
        DummyFiliere f3 = new DummyFiliere(3L, "F3");

        List<DummySoutenance> soutenances = new ArrayList<>();
        soutenances.addAll(buildSoutenances(f1, 3, id));
        soutenances.addAll(buildSoutenances(f2, 3, id));
        soutenances.addAll(buildSoutenances(f3, 3, id));

        stubDaos(soutenances, buildSalles(4), buildProfesseurs(12));
        planningService.genererPlanning();

        Map<LocalDate, Map<Long, Integer>> counts = new HashMap<>();
        for (DummySoutenance s : soutenances) {
            assertNotNull(s.getDate());
            Map<Long, Integer> byFiliere = counts.computeIfAbsent(s.getDate(), d -> new HashMap<>());
            Long filiereId = s.getFiliere().getId();
            byFiliere.put(filiereId, byFiliere.getOrDefault(filiereId, 0) + 1);
        }

        for (Map<Long, Integer> byFiliere : counts.values()) {
            Set<Integer> unique = new HashSet<>(byFiliere.values());
            assertEquals(1, unique.size());
        }
    }

    @Test
    void testPasDeChevauchementSalle() {
        AtomicLong id = new AtomicLong(1);
        DummyFiliere f1 = new DummyFiliere(1L, "F1");
        DummyFiliere f2 = new DummyFiliere(2L, "F2");
        DummyFiliere f3 = new DummyFiliere(3L, "F3");

        List<DummySoutenance> soutenances = new ArrayList<>();
        soutenances.addAll(buildSoutenances(f1, 4, id));
        soutenances.addAll(buildSoutenances(f2, 4, id));
        soutenances.addAll(buildSoutenances(f3, 4, id));

        stubDaos(soutenances, buildSalles(4), buildProfesseurs(16));
        planningService.genererPlanning();

        Set<String> used = new HashSet<>();
        for (DummySoutenance s : soutenances) {
            String key = s.getDate() + "|" + s.getHeureDebut() + "|" + s.getSalle().getId();
            assertTrue(used.add(key));
        }
    }

    @Test
    void testReposMinimumProfesseur() {
        AtomicLong id = new AtomicLong(1);
        DummyFiliere f1 = new DummyFiliere(1L, "F1");
        DummyFiliere f2 = new DummyFiliere(2L, "F2");
        DummyFiliere f3 = new DummyFiliere(3L, "F3");

        List<DummySoutenance> soutenances = new ArrayList<>();
        soutenances.addAll(buildSoutenances(f1, 6, id));
        soutenances.addAll(buildSoutenances(f2, 6, id));
        soutenances.addAll(buildSoutenances(f3, 6, id));

        stubDaos(soutenances, buildSalles(4), buildProfesseurs(20));
        planningService.genererPlanning();

        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes = new HashMap<>();
        for (DummySoutenance s : soutenances) {
            LocalDate date = s.getDate();
            LocalTime time = s.getHeureDebut();
            enregistrerProf(profTimes, date, time, s.getPresident());
            for (DummyProfesseur p : s.getJurys()) {
                enregistrerProf(profTimes, date, time, p);
            }
        }

        for (Map<Long, List<LocalTime>> byProf : profTimes.values()) {
            for (List<LocalTime> times : byProf.values()) {
                times.sort(LocalTime::compareTo);
                for (int i = 1; i < times.size(); i++) {
                    long diff = Math.abs(ChronoUnit.HOURS.between(times.get(i - 1), times.get(i)));
                    assertTrue(diff >= 2);
                }
            }
        }
    }

    @Test
    void testCapaciteMaxParJour() {
        AtomicLong id = new AtomicLong(1);
        DummyFiliere f1 = new DummyFiliere(1L, "F1");
        DummyFiliere f2 = new DummyFiliere(2L, "F2");
        DummyFiliere f3 = new DummyFiliere(3L, "F3");

        List<DummySoutenance> soutenances = new ArrayList<>();
        soutenances.addAll(buildSoutenances(f1, 10, id));
        soutenances.addAll(buildSoutenances(f2, 10, id));
        soutenances.addAll(buildSoutenances(f3, 10, id));

        stubDaos(soutenances, buildSalles(4), buildProfesseurs(24));
        planningService.genererPlanning();

        Map<LocalDate, Integer> dayCounts = new HashMap<>();
        for (DummySoutenance s : soutenances) {
            dayCounts.put(s.getDate(), dayCounts.getOrDefault(s.getDate(), 0) + 1);
        }

        for (int count : dayCounts.values()) {
            assertTrue(count <= 28);
        }
    }

    @Test
    void testJuryComplet() {
        AtomicLong id = new AtomicLong(1);
        DummyFiliere f1 = new DummyFiliere(1L, "F1");
        DummyFiliere f2 = new DummyFiliere(2L, "F2");
        DummyFiliere f3 = new DummyFiliere(3L, "F3");

        List<DummySoutenance> soutenances = new ArrayList<>();
        soutenances.addAll(buildSoutenances(f1, 4, id));
        soutenances.addAll(buildSoutenances(f2, 4, id));
        soutenances.addAll(buildSoutenances(f3, 4, id));

        stubDaos(soutenances, buildSalles(4), buildProfesseurs(16));
        planningService.genererPlanning();

        for (DummySoutenance s : soutenances) {
            assertNotNull(s.getPresident());
            assertNotNull(s.getJurys());
            assertEquals(2, s.getJurys().size());
            Long presId = s.getPresident().getId();
            Long j1 = s.getJurys().get(0).getId();
            Long j2 = s.getJurys().get(1).getId();
            assertTrue(!presId.equals(j1));
            assertTrue(!presId.equals(j2));
            assertTrue(!j1.equals(j2));
        }
    }

    private void stubDaos(
        List<DummySoutenance> soutenances,
        List<DummySalle> salles,
        List<DummyProfesseur> professeurs
    ) {
        when(planningDAO.getDonneesPlanification()).thenReturn((List) soutenances);
        when(salleDAO.findAll()).thenReturn((List) salles);
        when(professeurDAO.findAll()).thenReturn((List) professeurs);
        when(soutenanceDAO.findAll()).thenReturn((List) Collections.emptyList());
        when(professeurIndisponibiliteDAO.findAll()).thenReturn((List) Collections.emptyList());
    }

    private List<DummySoutenance> buildSoutenances(DummyFiliere filiere, int count, AtomicLong id) {
        List<DummySoutenance> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DummySoutenance s = new DummySoutenance();
            s.setId(id.getAndIncrement());
            s.setTitre("S" + s.getId());
            s.setFiliere(filiere);
            s.setJurys(new ArrayList<>());
            list.add(s);
        }
        return list;
    }

    private List<DummySalle> buildSalles(int count) {
        List<DummySalle> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DummySalle s = new DummySalle();
            s.setId((long) (i + 1));
            s.setNom("S" + (i + 1));
            s.setCapacite(30);
            s.setDisponible(true);
            list.add(s);
        }
        return list;
    }

    private List<DummyProfesseur> buildProfesseurs(int count) {
        List<DummyProfesseur> list = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            DummyProfesseur p = new DummyProfesseur();
            p.setId((long) (i + 1));
            p.setNom("P" + (i + 1));
            p.setPrenom("Prenom" + (i + 1));
            p.setEmail("p" + (i + 1) + "@example.com");
            list.add(p);
        }
        return list;
    }

    private void enregistrerProf(
        Map<LocalDate, Map<Long, List<LocalTime>>> profTimes,
        LocalDate date,
        LocalTime time,
        DummyProfesseur prof
    ) {
        profTimes
            .computeIfAbsent(date, d -> new HashMap<>())
            .computeIfAbsent(prof.getId(), p -> new ArrayList<>())
            .add(time);
    }

    static class DummyFiliere {
        private Long id;
        private String nom;

        DummyFiliere(Long id, String nom) {
            this.id = id;
            this.nom = nom;
        }

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }
    }

    static class DummyProfesseur {
        private Long id;
        private String nom;
        private String prenom;
        private String email;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getPrenom() {
            return prenom;
        }

        public void setPrenom(String prenom) {
            this.prenom = prenom;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }
    }

    static class DummySalle {
        private Long id;
        private String nom;
        private int capacite;
        private boolean disponible;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public int getCapacite() {
            return capacite;
        }

        public void setCapacite(int capacite) {
            this.capacite = capacite;
        }

        public boolean isDisponible() {
            return disponible;
        }

        public void setDisponible(boolean disponible) {
            this.disponible = disponible;
        }
    }

    static class DummySoutenance {
        private Long id;
        private String titre;
        private LocalDate date;
        private LocalTime heureDebut;
        private LocalTime heureFin;
        private DummySalle salle;
        private DummyProfesseur president;
        private List<DummyProfesseur> jurys;
        private DummyFiliere filiere;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitre() {
            return titre;
        }

        public void setTitre(String titre) {
            this.titre = titre;
        }

        public LocalDate getDate() {
            return date;
        }

        public void setDate(LocalDate date) {
            this.date = date;
        }

        public LocalTime getHeureDebut() {
            return heureDebut;
        }

        public void setHeureDebut(LocalTime heureDebut) {
            this.heureDebut = heureDebut;
        }

        public LocalTime getHeureFin() {
            return heureFin;
        }

        public void setHeureFin(LocalTime heureFin) {
            this.heureFin = heureFin;
        }

        public DummySalle getSalle() {
            return salle;
        }

        public void setSalle(DummySalle salle) {
            this.salle = salle;
        }

        public DummyProfesseur getPresident() {
            return president;
        }

        public void setPresident(DummyProfesseur president) {
            this.president = president;
        }

        public List<DummyProfesseur> getJurys() {
            return jurys;
        }

        public void setJurys(List<DummyProfesseur> jurys) {
            this.jurys = jurys;
        }

        public DummyFiliere getFiliere() {
            return filiere;
        }

        public void setFiliere(DummyFiliere filiere) {
            this.filiere = filiere;
        }
    }
}
