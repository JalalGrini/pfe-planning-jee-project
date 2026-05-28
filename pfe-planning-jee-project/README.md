# PFE Planning - Member 1 Module

## 📋 Project Overview
Backend module for PFE defense planning (Member 1 of 4). Exposes REST APIs, DAO interfaces, and DTOs for other team members.

## 🔴 Critical Coupling Rules (READ BEFORE CODING!)
**Member 2 (Planning Algorithm) MUST:**
- ✅ ONLY use: `com.pfe.dao.interfaces.*` and `com.pfe.dto.*`
- ❌ NEVER import: `com.pfe.dao.impl.*`, `com.pfe.model.*`, `com.pfe.config.*`, `com.pfe.service.*`

**Member 2's Task:** Implement planning algorithm using ONLY the exposed interfaces/DTOs.

## 🛠️ Setup Instructions

### Prerequisites
- Java 17
- Maven 3.8+
- Tomcat 10+
- Git
- Node.js 18+ (for React frontend)

### Build & Run
```bash
# Clone the repo
git clone https://github.com/JalalGrini/pfe-planning-jee-project.git
cd pfe-planning-jee-project

# Download dependencies & compile
mvn clean install

# Deploy to Tomcat
copy target\pfe-planning.war \path\to\tomcat\webapps\
```

### Frontend (React)
```bash
cd frontend
npm install
npm run dev
```

The frontend uses `VITE_API_BASE_URL` from [frontend/.env](frontend/.env).
By default it points to `http://localhost:8080/pfe-planning/api`.

### Test Endpoints (after Tomcat starts)
```bash
# Verify salles (should return 4)
curl http://localhost:8080/pfe-planning/api/salles

# Verify professors (should return 32)
curl http://localhost:8080/pfe-planning/api/professeurs

# Check stats
curl http://localhost:8080/pfe-planning/api/statistiques

# Search by encadrant (president)
curl "http://localhost:8080/pfe-planning/api/soutenances/par-encadrant?q=Benali"

# Download evaluation PV (DOCX)
curl -o fiche.docx http://localhost:8080/pfe-planning/api/soutenances/1/evaluation
```

## 📂 Project Structure (What Member 2 Can Use)
```
src/main/java/com/pfe/
├── dao/interfaces/  ← USE THESE (6 DAO interfaces)
│   ├── IEtudiantDAO.java
│   ├── IProfesseurDAO.java
│   ├── ISalleDAO.java
│   ├── ISoutenanceDAO.java
│   ├── IFiliereDAO.java
│   └── IPlanningDAO.java
└── dto/  ← USE THESE (5 DTOs)
    ├── EtudiantDTO.java
    ├── ProfesseurDTO.java
    ├── SalleDTO.java
    ├── CreneauDTO.java
    └── SoutenanceDTO.java
```

## 📄 Member 2 Full Prompt
See `resume.md` for complete Member 1 deliverables, then use the **Member 2 prompt** (provided separately) to start your work.

## 📄 PV Template
The evaluation template used for PV generation is stored in:
`src/main/resources/templates/Fiche_Evaluation_PFE_NomEtudiant_Prenom.docx`

## 🔧 Database
- **Dev mode (default)**: H2 in-memory (auto-created)
- **Prod mode**: Set `DB_PROFILE=prod` for MySQL

## ⚠️ Red Lines (DO NOT IMPLEMENT)
- No date/time/salle/president/jury assignment (Member 2's job)
- No PDF generation (Member 3)
- No dashboards/graphs (Member 4)
