import sys

with open('src/main/java/com/pfe/controller/SoutenanceController.java', 'r', encoding='utf-8') as f:
    content = f.read()

new_endpoint = '''
    @PostMapping(value = "/importer-complet", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, Object>> importerComplet(@RequestParam("file") MultipartFile file) {
        Map<String, Object> response = new HashMap<>();
        try {
            byte[] fileBytes = file.getBytes();
            Map<String, List<?>> result = excelImportService.importerFichierComplet(new ByteArrayInputStream(fileBytes));
            
            @SuppressWarnings("unchecked")
            List<Professeur> professeurs = (List<Professeur>) result.get("professeurs");
            @SuppressWarnings("unchecked")
            List<Salle> salles = (List<Salle>) result.get("salles");
            @SuppressWarnings("unchecked")
            List<Soutenance> soutenances = (List<Soutenance>) result.get("etudiants");
            
            if (professeurs != null) {
                Map<String, String> existingProfesseurs = professeurDAO.findAll().stream()
                        .filter(p -> !isHeaderProfessor(p))
                        .collect(Collectors.toMap(this::professeurKey, p -> professeurKey(p), (a, b) -> a));
                professeurDAO.findAll().stream()
                        .filter(this::isHeaderProfessor)
                        .forEach(p -> professeurDAO.delete(p.getId()));
                for (Professeur professeur : professeurs) {
                    if (isHeaderProfessor(professeur)) continue;
                    String key = professeurKey(professeur);
                    if (!existingProfesseurs.containsKey(key)) {
                        professeurDAO.save(professeur);
                        existingProfesseurs.put(key, key);
                    }
                }
            }
            
            if (salles != null) {
                Map<String, String> existingSalles = salleDAO.findAll().stream()
                        .collect(Collectors.toMap(s -> normalizeText(s.getNom()), s -> normalizeText(s.getNom()), (a, b) -> a));
                for (Salle salle : salles) {
                    String key = normalizeText(salle.getNom());
                    if (!existingSalles.containsKey(key)) {
                        salleDAO.save(salle);
                        existingSalles.put(key, key);
                    }
                }
            }
            
            if (soutenances != null) {
                for (Soutenance soutenance : soutenances) {
                    if (soutenance.getEtudiants() != null) {
                        List<Etudiant> persistedEtudiants = soutenance.getEtudiants().stream()
                                .map(etudiant -> {
                                    Etudiant existing = etudiantDAO.findByCne(etudiant.getCne());
                                    if (existing != null) return existing;
                                    etudiantDAO.save(etudiant);
                                    return etudiant;
                                })
                                .collect(Collectors.toList());
                        soutenance.setEtudiants(persistedEtudiants);
                    }
                    soutenanceDAO.save(soutenance);
                }
            }

            response.put("message", "Import complet réussi");
            response.put("countSoutenances", soutenances != null ? soutenances.size() : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Erreur import fichier complet", e);
            response.put("message", "Import complet échoué");
            response.put("errors", List.of(e.getMessage()));
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
        }
    }
'''

target = '    @PostMapping(value = "/importer-multiple", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)'
idx = content.find(target)
if idx != -1:
    content = content[:idx] + new_endpoint + content[idx:]

with open('src/main/java/com/pfe/controller/SoutenanceController.java', 'w', encoding='utf-8') as f:
    f.write(content)
