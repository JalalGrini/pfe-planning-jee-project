# Member 2 Task Prompt (Planning Algorithm)

## 📋 Project Context
You are **Member 2** of a 4-developer team building a **PFE Defense Planning** web application. This is a Java 17 web app using:
- Tomcat 10+ (Jakarta EE)
- Hibernate 6.x (no Spring Data)
- Maven for build
- H2 (dev) / MySQL (prod)
- Apache POI 5.x for Excel
- REST endpoints via Servlets + Jackson JSON

### 🔴 Non-Negotiable Coupling Rule
**Member 1 has exposed ONLY interfaces and DTOs. You (Member 2) must NEVER:**
- Import or use any class in `com.pfe.dao.impl.*`
- Import or use any class in `com.pfe.model.*` (entities)
- Import or use `com.pfe.config.*`, `com.pfe.service.ExcelImportService`, or `com.pfe.servlet.*`

**You may ONLY use:**
- `com.pfe.dao.interfaces.*` (DAO interfaces)
- `com.pfe.dto.*` (DTOs)

---

## 📁 Repository Structure (What You Get)
Clone: `git clone https://github.com/JalalGrini/pfe-planning-jee-project.git`

```
src/main/java/com/pfe/
├── dao/interfaces/  ← USE THESE (6 DAO interfaces)
│   ├── IEtudiantDAO.java
│   ├── IProfesseurDAO.java
│   ├── ISalleDAO.java
│   ├── ISoutenanceDAO.java
│   ├── IFiliereDAO.java
│   └── IPlanningDAO.java
├── dto/  ← USE THESE (5 DTOs)
│   ├── EtudiantDTO.java
│   ├── ProfesseurDTO.java
│   ├── SalleDTO.java
│   ├── CreneauDTO.java
│   └── SoutenanceDTO.java
├── model/ (DO NOT USE - entities)
├── dao/impl/ (DO NOT USE - implementations)
├── service/ (DO NOT USE)
├── servlet/ (DO NOT MODIFY)
├── config/ (DO NOT USE)
└── test/ (DO NOT MODIFY)
```

---

## 📄 Key Interfaces for Member 2

### ISoutenanceDAO
- `findNonPlannifiees()` → Get soutenances with `date = null`
- `save(Soutenance soutenance)` → Persist updated soutenance
- `findAll()` → Get all soutenances

### ISalleDAO
- `findAll()` → Get all rooms (4 salles: Salle 101-104)
- `findById(Long id)` → Get room by ID

### IProfesseurDAO
- `findAll()` → Get all professors (32 professors available)
- `findById(Long id)` → Get professor by ID

### IPlanningDAO
- `getDonneesPlanification()` → Returns unplanned soutenances

---

## ✅ Your Task: Implement Planning Algorithm

You must create **new classes** (e.g., in `com.pfe.planning.*`) that:

1. **Fetch unplanned soutenances** via `ISoutenanceDAO.findNonPlannifiees()`
2. **Assign to each soutenance**:
   - `date` (LocalDate)
   - `heureDebut` (LocalTime)
   - `heureFin` (LocalTime)
   - `salle` (Salle object via `ISalleDAO`)
   - `president` (Professeur object via `IProfesseurDAO`)
   - `jurys` (List<Professeur> via `IProfesseurDAO`)
3. **Avoid conflicts**:
   - No overlapping time slots for the same room
   - No professor (president/jury) assigned to two soutenances at same time
   - Respect room capacity (30 people)
4. **Save updated soutenances** via `ISoutenanceDAO.save()`

### Constraints
- 4 salles available
- 32 professors total
- Professors shouldn't work two consecutive hours
- Each soutenance needs: 1 president + 2 jury members + students

---

## 🛠️ Technical Implementation

### Package Structure (suggested)
```
src/main/java/com/pfe/planning/
├── PlanningAlgorithm.java   # Core algorithm
├── ConflictChecker.java    # Overlap detection
└── PlanningServlet.java   # REST endpoint (POST /api/planning/execute)
```

### How to Instantiate DAOs
Since this is a non-Spring project, you'll need to instantiate the impl classes:
```java
package com.pfe.planning;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.dao.impl.SoutenanceDAOImpl; // Only for instantiation!

public class PlanningAlgorithm {
    private final ISoutenanceDAO soutenanceDAO = new SoutenanceDAOImpl();
    // Other DAOs similarly...
}
```

**Note**: You may instantiate the impl classes, but NEVER depend on their internal implementation details. Only use the interface methods.

---

## 🔴 Your Strict Prohibitions (RED LINES)
- **NEVER modify** any of Member 1's existing files
- **NEVER generate PDFs** (Member 3's job)
- **NEVER create dashboards/graphs** (Member 4's job)
- **NEVER write code that depends on Member 3 or 4's work**

---

## 🧪 Build & Test
```bash
# Build project
mvn clean install

# Deploy to Tomcat
copy target\pfe-planning.war \path\to\tomcat\webapps\

# Test Member 1 endpoints
curl http://localhost:8080/pfe-planning/api/salles  # Should return 4 salles
curl http://localhost:8080/pfe-planning/api/professeurs  # Should return 32 professors
curl http://localhost:8080/pfe-planning/api/statistiques
```

---

## 📝 Deliverables Expected from Member 2
1. Planning algorithm implementation
2. New REST endpoint(s) for triggering planning
3. Updated `resume.md` with your progress
4. No modifications to Member 1's code

---

## ❓ Questions?
Refer to `README.md` and `resume.md` in the repository for Member 1's complete documentation.
