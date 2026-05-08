# PFE Planning Project - Member 1 Deliverables

## Project Overview
This is a fully functional Spring MVC 6 (classic, non-Boot) application for managing PFE defense planning. It uses:
- Java 17
- Spring MVC 6.0.x, Spring ORM, Spring Test
- Hibernate 6.3.1.Final (via Spring ORM)
- H2 (dev, create-drop) / MySQL 8.x (prod, update)
- Apache POI 5.2.5 for Excel imports
- Jackson 2.16.0 for JSON serialization
- JUnit 5.10.0 + Spring Test for testing
- Deploys as WAR on Tomcat 10+

## Current Project Structure (All Implemented)
All components are ready to use with stable, unchanged APIs for other team members:
1. **Model Layer** (`com.pfe.model`): 5 JPA entities (Filiere, Etudiant, Professeur, Salle, Soutenance) - unchanged
2. **DAO Interfaces** (`com.pfe.dao.interfaces`): 6 stable interfaces (IEtudiantDAO, IProfesseurDAO, ISalleDAO, ISoutenanceDAO, IFiliereDAO, IPlanningDAO) - unchanged, use these for all data access
3. **DTO Layer** (`com.pfe.dto`): 5 DTOs (EtudiantDTO, ProfesseurDTO, SalleDTO, CreneauDTO, SoutenanceDTO) - unchanged, for JSON serialization
4. **DAO Implementations** (`com.pfe.dao.impl`): 6 `@Repository` classes with `@Transactional` support, injected `SessionFactory`
5. **REST Controllers** (`com.pfe.controller`): 4 `@RestController` classes with endpoints:
   - `GET /api/soutenances?filter=/non-plannifiees` (list defenses)
   - `POST /api/soutenances/importer` (Excel import)
   - `GET /api/professeurs` (list professors)
   - `GET /api/salles` (list rooms)
   - `GET /api/statistiques` (counts)
6. **Service Layer** (`com.pfe.service`): `ExcelImportService` (`@Service`) for Excel imports, detects student pairs, handles missing filieres
7. **Configuration** (`com.pfe.config`): JavaConfig only (AppConfig, DatabaseConfig, WebConfig, DataInitializer, SecurityConfig) - zero XML
8. **Tests** (`src/test/java/com/pfe/test`): 3 Spring Test + JUnit 5 test classes
9. **Resources**: Excel files in `src/main/resources/data/`, profile-based property files

## Tasks for Next Members (Members 2, 3, 4)
Each task is isolated, use only the stable DAO interfaces (no need to modify low-level code):

### Member 2: Planning Algorithm Implementation
- **Work on**: `com.pfe.dao.impl.PlanningDAOImpl` (already exists, currently returns unplanned defenses)
- **Use**: `ISoutenanceDAO` (get unplanned defenses, update planned defenses), `ISalleDAO` (get available rooms), `IProfesseurDAO` (get jury members/presidents), `IFiliereDAO`
- **Deliver**: Functional algorithm that assigns dates, times, rooms, presidents and jury members to unplanned defenses
- **No changes needed**: Model, DTOs, other DAO interfaces, controllers

### Member 3: Frontend Integration (Angular)
- **Work on**: New Angular project (not in this repo)
- **Use**: All existing REST endpoints return JSON matching the DTO structure exactly
- **Deliver**: Angular app with views for:
  - Listing defenses, professors, rooms
  - Excel import for defenses
  - Statistics dashboard
  - Planning results display
- **No changes needed**: Backend code (all endpoints are stable and tested)

### Member 4: Testing & Deployment
- **Work on**: Existing test classes, deployment configuration
- **Use**: Spring Test context, `application-prod.properties` for MySQL, Tomcat 10+ deployment
- **Deliver**:
  - 100% passing tests
  - Production-ready WAR deployed on Tomcat 10+
  - Configured MySQL production database
- **No changes needed**: Core backend logic (only fix bugs if found)