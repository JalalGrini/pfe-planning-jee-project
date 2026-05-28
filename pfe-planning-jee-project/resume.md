# PFE Planning Project - Complete Team Documentation

## 1. Project Global Description

### Context
This project is a web application for managing PFE (Projet de Fin d'Études) defense planning at the engineering school. It handles the complete workflow from importing student data to scheduling defense sessions.

### Objective
Build a system that:
- Imports students, professors, and defense topics from Excel files
- Allows planning defense sessions (dates, times, rooms, jury members)
- Provides REST APIs for frontend consumption
- Displays statistics and planning results

### Business Rules (Non-Negotiable)
1. **Defense Planning Rules**:
   - Each defense has 1-2 students (binômes detected automatically)
   - Each defense needs: 1 president, 2-3 jury members, 1 room
   - No scheduling conflicts: same professor/room at same time
   - Defenses grouped by filière (Ingénierie des Données, Génie Informatique, Transformation Digitale & IA)

2. **Red Lines** (What NOT to do):
   - ❌ No automatic date/time assignment (Member 2's job)
   - ❌ No room assignment (Member 2's job)
   - ❌ No jury/president assignment (Member 2's job)
   - ❌ No PDF generation
   - ❌ No dashboard/graph creation (Member 3's job)
   - ❌ No file tree display (Member 4's job)

---

## 2. Technical Stack (Spring MVC Classic - NO Spring Boot)

### Core Framework
- **Java 17** (LTS version)
- **Spring MVC 6.0.x** (Classic, XML-free, JavaConfig only)
- **Spring ORM 6.0.x** (Hibernate integration)
- **Spring Test 6.0.x** (Unit/integration testing)

### Data Layer
- **Hibernate 6.3.1.Final** (JPA implementation via Spring ORM)
- **H2 Database 2.2.224** (Development, in-memory, create-drop)
- **MySQL 8.1.0** (Production, update mode)

### Utilities
- **Apache POI 5.2.5** (Excel file reading)
- **Jackson 2.16.0** (JSON serialization/deserialization)
- **JUnit 5.10.0** (Testing framework)
- **Spring Test** (Integration testing with Spring context)
- **Logback 1.4.11** (Logging)

### Infrastructure
- **Maven 3.9+** (Build tool)
- **Tomcat 10.x** (Servlet container, Jakarta EE 6)
- **Jakarta Servlet API 6.0** (Web layer)

### Key Point
This is **NOT Spring Boot**. It's classic Spring MVC with:
- JavaConfig (`@Configuration` classes) instead of XML
- `DispatcherServlet` configured via `web.xml`
- Manual `LocalSessionFactoryBean` and `HibernateTransactionManager` setup
- WAR packaging for Tomcat deployment

---

## 3. Detailed Structure - What EACH Member Should Do

### Member 1 (YOU - Complete ✅)
**Deliverables (All Done)**:
1. **Model Layer** (`com.pfe.model`): 5 JPA entities
   - `Filiere.java`, `Etudiant.java`, `Professeur.java`, `Salle.java`, `Soutenance.java`
2. **DAO Interfaces** (`com.pfe.dao.interfaces`): 6 stable interfaces
   - `IEtudiantDAO`, `IProfesseurDAO`, `ISalleDAO`, `ISoutenanceDAO`, `IFiliereDAO`, `IPlanningDAO`
3. **DTO Layer** (`com.pfe.dto`): 5 DTOs for JSON
   - `EtudiantDTO`, `ProfesseurDTO`, `SalleDTO`, `CreneauDTO`, `SoutenanceDTO`
4. **DAO Implementations** (`com.pfe.dao.impl`): 6 `@Repository` classes
   - All use `@Transactional`, injected `SessionFactory`
5. **REST Controllers** (`com.pfe.controller`): 4 `@RestController` classes
   - `SoutenanceController`, `ProfesseurController`, `SalleController`, `StatistiquesController`
6. **Service Layer** (`com.pfe.service`): `ExcelImportService` (`@Service`)
7. **Configuration** (`com.pfe.config`): Pure JavaConfig
   - `AppConfig`, `DatabaseConfig`, `WebConfig`, `DataInitializer`, `SecurityConfig`
8. **Tests** (`src/test/java/com/pfe/test`): 3 Spring Test classes
9. **Resources**: Excel files in `src/main/resources/data/`

---

### Member 2 (Planning Algorithm)
**What to Work On**: `com.pfe.dao.impl.PlanningDAOImpl.java`

**Available Interfaces** (DO NOT modify):
- `ISoutenanceDAO` - Get unplanned defenses, update planned ones
- `ISalleDAO` - Get available rooms
- `IProfesseurDAO` - Get jury members/presidents
- `IFiliereDAO` - Access filière data

**Task**: Implement `getDonneesPlanification()` method to:
1. Retrieve all unplanned defenses (date = null)
2. Assign dates/times based on business rules
3. Assign available rooms
4. Assign presidents and jury members (no conflicts)
5. Save planned defenses back to database

**Deliverable**: Functional planning algorithm that transforms unplanned defenses into fully scheduled sessions.

**No Changes Needed**: Model, DTOs, other DAO interfaces, controllers (all stable).

---

### Member 3 (Frontend - Angular)
**What to Work On**: New Angular project (separate repo/folder)

**Use**: All existing REST endpoints return JSON matching DTO structure exactly.

**Deliverables**:
- Angular 15+ app with routes for:
  1. **Home/Stats Dashboard**: Call `/api/statistiques`, display counts
  2. **Defenses List**: Call `/api/soutenances`, display table
  3. **Professors List**: Call `/api/professeurs`, display cards
  4. **Rooms List**: Call `/api/salles`, display grid
  5. **Excel Import Page**: Form with file upload to `/api/soutenances/importer`
  6. **Planning Results**: Display Member 2's algorithm output
- Clean UI with Bootstrap/Material for professor presentation
- HTTP client service to consume REST APIs

**No Backend Changes Needed**: All endpoints are stable and tested.

---

### Member 4 (Testing & Validation)
**What to Work On**: 
- Existing tests in `src/test/java/com/pfe/test/`
- New tests for Member 2's planning algorithm
- Validation of all REST endpoints

**Use**: 
- Spring Test context with `@ExtendWith(SpringExtension.class)`
- `application.properties` for H2 in-memory testing
- `mvn test` to run all tests

**Deliverables**:
1. **100% test coverage** for all backend functionality
2. **Integration tests** for REST endpoints (using `MockMvc`)
3. **Validation report** showing all endpoints return correct JSON
4. **Bug fixes** if any issues found in backend
5. **Production config** validation with MySQL

**No Core Changes Needed**: Only fix bugs if found during testing.

---

## 4. Weak Coupling Constraints (Explained Simply)

### What is Weak Coupling?
Weak coupling means each member works independently without needing to understand others' code internals.

### What's EXPOSED (Use These Only):
```
com.pfe.dao.interfaces.*  (6 interfaces)
com.pfe.dto.*              (5 DTOs)
```
- Member 2: Uses interfaces to access data, DTOs for data transfer
- Member 3: Consumes REST APIs returning DTOs as JSON
- Member 4: Tests using interfaces, validates DTO JSON format

### What's HIDDEN (Never Access Directly):
```
com.pfe.dao.impl.*    (Implementation details - HOW data is fetched)
com.pfe.model.*       (JPA entities - database structure)
com.pfe.config.*      (Spring configuration - setup details)
com.pfe.service.*      (Excel import logic - internal processing)
```

### Why This Matters:
- **Member 2** doesn't need to know HOW `EtudiantDAOImpl` fetches data (only uses `IEtudiantDAO` interface)
- **Member 3** doesn't need to know backend exists (only calls REST URLs)
- **Member 4** doesn't need to know Spring config (only writes tests against interfaces)

### Proof of Compliance:
All controllers inject interfaces:
```java
@Autowired private ISoutenanceDAO soutenanceDAO;  // Interface, not impl
@Autowired private IProfesseurDAO professeurDAO;    // Interface, not impl
```

---

## 5. Workflow Between Members (Dependencies)

### Data Flow Diagram:
```
Step 1: User uploads "Sujets de Soutenance" Excel via website
    ↓
[Member 1] ExcelImportService → Soutenance entities (unplanned, with topics/students)
    ↓
[Member 2] Planning Algorithm → Soutenance entities (planned, with date/room/jury)
    ↓
[Member 2] Generate Fiche d'Évaluation (PDF/Excel) for each soutenance
    ↓
[Member 2] Store Fiches in folders named after each President
    ↓
[Member 1] REST Controllers → JSON (DTOs)
    ↓
[Member 3] Angular Frontend ← Displays data + downloadable fiches
    ↓
[Member 4] Tests ← Validates everything
```

### Excel Upload Workflow (Important!):
- **NOT pre-loaded**: "Sujets de Soutenance" Excel files are NOT in `src/main/resources/data/`
- **Uploaded later**: User uploads via website at `/api/soutenances/importer`
- **Used for planning**: Member 2's algorithm uses this uploaded data to plan defenses
- **Contains**: CNE, NOM, PRENOM, FILIÈRE, SUJET_PFE columns

### Who Uses What:
| Member | Input | Output | Dependencies |
|--------|-------|--------|-------------|
| **Member 1** | Excel files, requirements | REST APIs, JavaConfig | None (done) |
| **Member 2** | `IPlanningDAO.getDonneesPlanification()` | Updates Soutenance entities | Uses interfaces from M1 |
| **Member 3** | REST endpoints (JSON) | Angular UI | Consumes APIs from M1 |
| **Member 4** | All backend code | Test reports, bug fixes | Tests work from M1 + M2 |

### Critical Rule:
**Member 2, 3, 4**: If you need to change something in `com.pfe.model.*` or `com.pfe.dao.impl.*`, ask Member 1 first!

---

## 6. Complete Business Rules (ALL 9 Categories)

### 1. Time Slot Rules (Créneaux Horaires)
| Rule | Value | Explanation |
|------|-------|-------------|
| Slots per day | 9h00, 10h00, 11h00, 14h00, 15h00, 16h00, 17h00 | 7 slots per room per day |
| Defense duration | Exactly 1 hour | From 9h00 to 10h00, etc. |
| Lunch break | 12h00 to 14h00 | 2-hour break (no defenses) |

**Implementation for Member 2**:
- Use ONLY these time slots: 9h, 10h, 11h, 14h, 15h, 16h, 17h
- Each defense occupies exactly 1 full hour (heureDebut = slot time, heureFin = slot time + 1h)
- Never schedule during 12h-14h

---

### 2. Professor Rules (Jurys)
| Rule | Value | Explanation |
|------|-------|-------------|
| Minimum rest | 1 hour minimum | Prof at 9h cannot be at 10h (too close) |
| Valid example | 9h and 11h | 1 hour rest between (10h free) |
| Invalid example | 9h and 10h | 0 hour rest → FORBIDDEN |
| President | 1 per soutenance | Designated among the 3 professors |
| Jury members | 2 per soutenance | Accompany the president |
| Jury composition | 1 president + 2 jury = 3 profs | Random draw (no intelligent model yet) |
| No dual location | Not at same hour | A prof can only be in one room at time T |

**Implementation for Member 2**:
- Check `findAvailableForDate()` to verify 1h rest between defenses
- Each soutenance MUST have exactly 3 professors (1 president + 2 jury)
- Use random assignment for now (Member 2 can improve later)

---

### 3. Room Rules (Salles)
| Rule | Value | Explanation |
|------|-------|-------------|
| Number of rooms | 4 | Salle 101, 102, 103, 104 (modifiable) |
| Capacity per room | 1 defense per slot | A room cannot have 2 defenses at same time |
| Unavailability | Configurable | Possibility to mark a room as unavailable |

**Implementation for Member 2**:
- Loop through 4 rooms in round-robin fashion
- Check `salle.isDisponible() == true` before assignment
- No double-booking: same room, same time slot

---

### 4. Student & Filière Rules
| Rule | Value | Explanation |
|------|-------|-------------|
| Existing filières | 3 | "Ingénierie des Données", "Génie Informatique", "Transformation Digitale & IA" |
| Possible binômes | Yes | If 2 students have same SUJET_PFE → ONE defense with 2 students |
| Binôme size | 2 students max | No more than 2 per soutenance |
| Equity between filières | SAME NUMBER per day | Each day, each filière must have same number of students defending |
| Equity example | 3 ID, 3 GI, 3 TD per day | Not 5 ID, 2 GI, 2 TD |

**Implementation for Member 2**:
- Count students per filière each day
- Alert if imbalance detected (e.g., 5 ID vs 2 GI)
- Distribute evenly: 3 ID, 3 GI, 3 TD per day maximum

---

### 5. Planning Capacity Rules
| Rule | Formula | Example |
|------|---------|---------|
| Capacity per day | nb_salles × 7 slots | 4 × 7 = 28 defenses max/day |
| Days needed | ceil(nb_soutenances_total / 28) | 100 soutenances → 4 days (100/28 = 3.57 → 4 days) |
| User modification | User can choose days | Remove weekends, holidays via calendar |

**Implementation for Member 2**:
- Maximum 28 defenses per day (4 rooms × 7 slots)
- Calculate minimum days: `ceil(total_soutenances / 28)`
- Allow user to select specific days (exclude weekends/holidays)

---

### 6. Excel Import Rules
| Rule | Behavior |
|------|------------|
| Mandatory columns | CNE, NOM, PRENOM, FILIERE, SUJET_PFE |
| Binômes | Same SUJET_PFE on 2 rows → create ONE defense with 2 students |
| Non-existent filière | Row ignored + error log (no blocking) |
| Non-existent student | Student is automatically created |
| Duplicate CNE | Handled by uniqueness constraint in database |

**Files to upload** (via website, NOT pre-loaded):
- Student files: `Ingénierie des données 3_Email.xlsx`, etc.
- Defense topics: `exemples_soutenances.xlsx` (or user-uploaded file)
- Cell positions (0-4) as described in Section 8

---

### 7. Export Rules (PDF Fiches)
| Rule | Explanation |
|------|-------------|
| Folder structure | Day 1/ → Prof X (president)/ → fiche_soutenance1.pdf |
| Template | Based on `Fiche_Evaluation_PFE_NomEtudiant_Prenom.docx` |
| Content | Students, subject, president, jury, date, time, room |

**Implementation for Member 2** (Post-Planning):
```
fiches_evaluation/
├── Jour_1/
│   ├── Dupont_Jean/
│   │   ├── fiche_soutenance1.pdf    ← Based on template
│   │   └── fiche_soutenance2.pdf
│   └── Martin_Marie/
│       └── fiche_soutenance3.pdf
├── Jour_2/
│   └── ...
```

- Use `Soutenance.getPresident().getNom()` + `getPrenom()` for folder name
- Generate PDF from `.docx` template (or create programmatically)
- Store in configurable path, make downloadable via: `GET /api/soutenances/{id}/evaluation`

---

### 8. Planning Algorithm Rules (Member 2 TO IMPLEMENT)
| Rule | Status | Explanation |
|------|--------|-------------|
| Date assignment | TO IMPLEMENT | Member 2 must assign a date to each soutenance |
| Time slot assignment | TO IMPLEMENT | 9h,10h,11h,14h,15h,16h,17h |
| Room assignment | TO IMPLEMENT | Loop through 4 rooms (round-robin) |
| Jury assignment | Random (for now) | 3 profs among 32 available |
| President | Random | One of the 3 assigned profs |
| 1h rest management | TO IMPLEMENT | Check via `findAvailableForDate()` |
| Filière equity | TO IMPLEMENT | Count students per filière each day |

**Input** (from `IPlanningDAO.getDonneesPlanification()`):
- List of `Soutenance` objects with:
  - `titre` (topic), `etudiants` (1-2 students), `filiere`
  - `date = null`, `heureDebut = null`, `heureFin = null`
  - `salle = null`, `president = null`, `jurys = []`

**Output**:
- Updated `Soutenance` objects with ALL fields populated
- Saved back via `ISoutenanceDAO.save()`
- Fiches d'évaluation generated and stored in president folders

---

### 9. Validation Rules (Member 3 - AFTER Member 2)
| Rule | Explanation |
|------|-------------|
| Check room overlap | A room cannot have 2 defenses per slot |
| Check prof rest | A prof cannot have 2 consecutive defenses |
| Filière equity | Count students per filière each day, alert if imbalance |
| PDF generation | Create one fiche per soutenance with Word template |

**To validate**:
1. Verify no room has 2 defenses at same time slot
2. Verify professors have ≥1h rest between defenses
3. Verify each day has equal students per filière (±1 tolerance)
4. Verify fiches d'évaluation are generated for each soutenance
5. Verify folder structure: `Jour_X/President_Nom/fiche.pdf`

---

### Quick Reference for Member 2:
✅ **MUST DO**: Assign date, time (7 slots), room (round-robin), jury (1+2=3), check 1h rest, ensure filière equity
❌ **MUST NOT DO**: Assign same prof to overlapping defenses, schedule during lunch (12h-14h), create uneven filière distribution

---

## 7. REST Endpoints for Member 4 (Testing)

### Base URL: `http://localhost:8080/pfe-planning/api`

### Endpoint List:

#### 1. Get All Defenses
```
GET /soutenances
Response: List<SoutenanceDTO> (JSON array)
```

#### 2. Get Unplanned Defenses
```
GET /soutenances?filter=/non-plannifiees
Response: List<SoutenanceDTO> where date is null
```

#### 3. Import Defenses from Excel
```
POST /soutenances/importer
Content-Type: multipart/form-data
Body: file=<excel-file>
Response: {"message": "Import réussi", "count": N}
```

#### 4. Get All Professors
```
GET /professeurs
Response: List<ProfesseurDTO> (JSON array)
```

#### 5. Get All Rooms
```
GET /salles
Response: List<SalleDTO> (JSON array)
```

#### 6. Get Statistics
```
GET /statistiques
Response: {
  "nbEtudiants": 79,
  "nbProfesseurs": 32,
  "nbSalles": 4,
  "nbFilieres": 3,
  "nbSoutenancesNonPlannifiees": 39
}
```

### JSON Format Examples:

**SoutenanceDTO**:
```json
{
  "id": 1,
  "titre": "Machine Learning Application",
  "date": "2026-06-15",
  "heureDebut": "09:00:00",
  "heureFin": "09:45:00",
  "salle": {"id": 1, "nom": "Salle 101", "capacite": 30, "disponible": true},
  "president": {"id": 5, "nom": "Dupont", "prenom": "Jean", "discipline": "IA"},
  "jurys": [{"id": 6, "nom": "Martin", "prenom": "Marie"}],
  "etudiants": [{"id": 1, "cne": "CNE001", "nom": "Nom1", "prenom": "Prenom1"}],
  "filiereId": 1,
  "filiereNom": "Ingénierie des Données"
}
```

---

## 8. Excel File Format for Upload (Explanations for Other Members)

### File 1: `Liste des Profs.xlsx` (Professor List)
**Location**: `src/main/resources/data/`
**Used by**: DataInitializer (auto-import on startup)
**Columns** (cell positions):
- Cell 0: Nom (Last name)
- Cell 1: Prenom (First name)
- Cell 2: Discipline (Department)

**Important**: Header row is ignored. Professors auto-saved to database on application startup.

---

### File 2: Student Files (3 files)
**Location**: `src/main/resources/data/`
- `Ingénierie des données 3_Email.xlsx`
- `Génie Informatique 3 Option GL_Email.xlsx`
- `Transformation Digitale & Intelligence Artificielle 3_Email.xlsx`

**Columns** (cell positions):
- Cell 0: CNE (Student ID)
- Cell 1: NOM (Last name)
- Cell 2: PRENOM (First name)
- Cell 3: EMAIL PERSONNEL (Personal email)
- Cell 4: EMAIL ACADEMIQUE (Academic email)

**Important**: Header row ignored. Students linked to their filière based on filename.

---

### File 3: `exemples_soutenances.xlsx` (Example Defenses)
**Location**: `src/main/resources/data/`
**Used by**: `ExcelImportTest.java` (test file)
**Columns** (cell positions):
- Cell 0: CNE (Student ID)
- Cell 1: NOM (Last name)
- Cell 2: PRENOM (First name)
- Cell 3: FILIERE (Filière name)
- Cell 4: SUJET_PFE (Defense topic/title)

**Import Logic**:
1. Groups students by `SUJET_PFE` (detects binômes with same topic)
2. Creates `Soutenance` object per topic
3. Links students to soutenance
4. Sets `date/heureDebut/heureFin` = null (unplanned)
5. Sets `salle/president/jurys` = null (unassigned)

**To Upload via API**:
```
POST /api/soutenances/importer
Form-data: file=<your-excel-file>
```
Excel must have columns at positions 0-4 as described above.

---

## Quick Reference for All Members

### Member 2 (Planning):
- **Read**: Section 6 (Business Rules)
- **Use**: `IPlanningDAO`, `ISoutenanceDAO`, `ISalleDAO`, `IProfesseurDAO`
- **Output**: Planned defenses with all fields set

### Member 3 (Frontend):
- **Read**: Section 7 (REST Endpoints)
- **Use**: Angular HTTP client to consume JSON APIs
- **Output**: Professional UI for professor

### Member 4 (Testing):
- **Read**: Section 7 (REST Endpoints), Section 4 (Coupling)
- **Use**: Spring Test, `mvn test`
- **Output**: Test reports, bug fixes

### All Members:
- **NEVER modify**: `com.pfe.dao.interfaces.*`, `com.pfe.dto.*`
- **ALWAYS use**: Interfaces for data access, DTOs for data transfer
- **ASK Member 1** if you need model/impl changes

---

## Project Validation Checklist (For Professor)

- [ ] Backend compiles: `mvn clean compile`
- [ ] Tests pass: `mvn test`
- [ ] H2 database auto-creates tables (dev profile)
- [ ] Excel files import correctly (professors, students, defenses)
- [ ] REST endpoints return valid JSON
- [ ] Member 2's algorithm assigns dates/rooms/jurys
- [ ] Member 3's frontend displays all data correctly
- [ ] No Spring Boot used (pure Spring MVC 6)
- [ ] Weak coupling respected (only interfaces exposed)
- [ ] Documentation clear for all team members
