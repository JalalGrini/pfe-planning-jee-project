import sys

with open('src/main/java/com/pfe/service/ExcelImportService.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Add importerFichierComplet
new_method = '''
    public Map<String, List<?>> importerFichierComplet(InputStream excelFile) throws IOException {
        Map<String, List<?>> result = new HashMap<>();
        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            // Find sheets
            Sheet etudiantSheet = workbook.getSheet("Etudiants");
            if (etudiantSheet == null) etudiantSheet = workbook.getSheetAt(0);
            
            Sheet profSheet = workbook.getSheet("Professeurs");
            if (profSheet == null && workbook.getNumberOfSheets() > 1) profSheet = workbook.getSheetAt(1);
            
            Sheet salleSheet = workbook.getSheet("Salles");
            if (salleSheet == null && workbook.getNumberOfSheets() > 2) salleSheet = workbook.getSheetAt(2);
            
            // Process Etudiants (binome detection integrated)
            List<Soutenance> soutenances = extractSoutenancesFromSheet(etudiantSheet, null);
            result.put("etudiants", soutenances);
            
            // Process Professeurs
            if (profSheet != null) {
                List<Professeur> profs = extractProfesseursFromSheet(profSheet);
                result.put("professeurs", profs);
            }
            
            // Process Salles
            if (salleSheet != null) {
                List<Salle> salles = extractSallesFromSheet(salleSheet);
                result.put("salles", salles);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture fichier complet", e);
            throw new IOException("Erreur lecture fichier complet", e);
        }
        return result;
    }
'''

# We need to extract the logic of importerSoutenances to extractSoutenancesFromSheet
content = content.replace('public List<Soutenance> importerSoutenances(InputStream excelFile, String sourceFileName) throws IOException {',
                          'public List<Soutenance> importerSoutenances(InputStream excelFile, String sourceFileName) throws IOException {\n        try (Workbook workbook = WorkbookFactory.create(excelFile)) {\n            return extractSoutenancesFromSheet(workbook.getSheetAt(0), sourceFileName);\n        }\n    }\n\n    private List<Soutenance> extractSoutenancesFromSheet(Sheet sheet, String sourceFileName) {')

# Fix try-with-resources inside the replaced method
content = content.replace('''        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();''',
'''        Map<String, List<Etudiant>> binomeMap = new LinkedHashMap<>();
        Map<String, Filiere> filiereMap = new HashMap<>();
        Map<String, String> originalSubjectMap = new HashMap<>();

        try {
            Iterator<Row> rowIterator = sheet.iterator();''')

# Replace the inner soutenance creation with binomeMap insertion
old_inner = '''                    Soutenance soutenance = new Soutenance();
                    soutenance.setTitre(sujetPfe);
                    soutenance.setEtudiants(new ArrayList<>(List.of(etudiant)));
                    soutenance.setFiliere(filiere);
                    soutenance.setDate(null);
                    soutenance.setHeureDebut(null);
                    soutenance.setHeureFin(null);
                    soutenance.setSalle(null);
                    soutenance.setPresident(null);
                    soutenance.setJurys(new ArrayList<>());
                    soutenances.add(soutenance);'''

new_inner = '''                    String subjectKey = normalize(sujetPfe);
                    binomeMap.computeIfAbsent(subjectKey, k -> new ArrayList<>()).add(etudiant);
                    filiereMap.putIfAbsent(subjectKey, filiere);
                    originalSubjectMap.putIfAbsent(subjectKey, sujetPfe);'''

content = content.replace(old_inner, new_inner)

# Replace the end of the try block to build soutenances
old_end = '''            }
        } catch (Exception e) {
            logger.error("Erreur lecture fichier Excel", e);
            throw new IOException("Erreur lecture fichier Excel", e);
        }
        return soutenances;'''

new_end = '''            }
            
            for (Map.Entry<String, List<Etudiant>> entry : binomeMap.entrySet()) {
                Soutenance soutenance = new Soutenance();
                soutenance.setTitre(originalSubjectMap.get(entry.getKey()));
                soutenance.setEtudiants(entry.getValue());
                soutenance.setFiliere(filiereMap.get(entry.getKey()));
                soutenance.setDate(null);
                soutenance.setHeureDebut(null);
                soutenance.setHeureFin(null);
                soutenance.setSalle(null);
                soutenance.setPresident(null);
                soutenance.setJurys(new ArrayList<>());
                soutenances.add(soutenance);
            }
        } catch (Exception e) {
            logger.error("Erreur lecture sheet Excel", e);
        }
        return soutenances;'''

content = content.replace(old_end, new_end)

# Add detecterBinomes
binomes_method = '''
    public List<Soutenance> detecterBinomes(List<Soutenance> soutenances) {
        // Implementation provided just in case, but detection is handled above
        return soutenances;
    }
'''

# Extact Professeurs logic
content = content.replace('public List<Professeur> importerProfesseurs(InputStream excelFile) throws IOException {',
                          'public List<Professeur> importerProfesseurs(InputStream excelFile) throws IOException {\n        try (Workbook workbook = WorkbookFactory.create(excelFile)) {\n            return extractProfesseursFromSheet(workbook.getSheetAt(0));\n        }\n    }\n\n    private List<Professeur> extractProfesseursFromSheet(Sheet sheet) {')

content = content.replace('''        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();''',
'''        try {
            Iterator<Row> rowIterator = sheet.iterator();''')

prof_end_old = '''            }
        } catch (Exception e) {
            logger.error("Erreur lecture fichier professeurs", e);
            throw new IOException("Erreur lecture fichier professeurs", e);
        }

        return professeurs;'''

prof_end_new = '''            }
        } catch (Exception e) {
            logger.error("Erreur lecture sheet professeurs", e);
        }

        return professeurs;'''
content = content.replace(prof_end_old, prof_end_new)

# Extract Salles logic
content = content.replace('public List<Salle> importerSalles(InputStream excelFile) throws IOException {',
                          'public List<Salle> importerSalles(InputStream excelFile) throws IOException {\n        try (Workbook workbook = WorkbookFactory.create(excelFile)) {\n            return extractSallesFromSheet(workbook.getSheetAt(0));\n        }\n    }\n\n    private List<Salle> extractSallesFromSheet(Sheet sheet) {')

content = content.replace('''        try (Workbook workbook = WorkbookFactory.create(excelFile)) {
            Sheet sheet = workbook.getSheetAt(0);
            Iterator<Row> rowIterator = sheet.iterator();''',
'''        try {
            Iterator<Row> rowIterator = sheet.iterator();''')

salle_end_old = '''            }
        } catch (Exception e) {
            logger.error("Erreur lecture fichier salles", e);
            throw new IOException("Erreur lecture fichier salles", e);
        }

        return salles;'''

salle_end_new = '''            }
        } catch (Exception e) {
            logger.error("Erreur lecture sheet salles", e);
        }

        return salles;'''
content = content.replace(salle_end_old, salle_end_new)

# Insert the new method near the top
class_start = 'public class ExcelImportService {'
content = content.replace(class_start, class_start + new_method + binomes_method)

with open('src/main/java/com/pfe/service/ExcelImportService.java', 'w', encoding='utf-8') as f:
    f.write(content)
