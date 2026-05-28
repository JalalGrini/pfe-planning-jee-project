import sys

with open('src/main/java/com/pfe/service/ExportService.java', 'r', encoding='utf-8') as f:
    content = f.read()

# Add filiere and sujetPfe to PlanningRow
row_old = '''    public static class PlanningRow {
        public LocalDate date;
        public LocalTime heure;
        public String salle;
        public String etudiants;
        public String president;
        public String jury1;
        public String jury2;
    }'''
row_new = '''    public static class PlanningRow {
        public LocalDate date;
        public LocalTime heure;
        public String salle;
        public String etudiants;
        public String president;
        public String jury1;
        public String jury2;
        public String filiere;
        public String sujetPfe;
    }'''
content = content.replace(row_old, row_new)

# Update mapToRow to populate filiere and sujetPfe
map_old = '''        Object presObj = tryInvokeGetter(soutenance, "getPresident");
        item.president = presObj != null ? (asString(tryInvokeGetter(presObj, "getNom")) + " " + asString(tryInvokeGetter(presObj, "getPrenom"))).trim() : "N/A";'''
map_new = '''        Object presObj = tryInvokeGetter(soutenance, "getPresident");
        item.president = presObj != null ? (asString(tryInvokeGetter(presObj, "getNom")) + " " + asString(tryInvokeGetter(presObj, "getPrenom"))).trim() : "N/A";
        
        Object filiereObj = tryInvokeGetter(soutenance, "getFiliere");
        item.filiere = filiereObj != null ? asString(tryInvokeGetter(filiereObj, "getNom")) : "N/A";
        item.sujetPfe = asString(tryInvokeGetter(soutenance, "getTitre"));'''
content = content.replace(map_old, map_new)

# Update exporterPlanningExcel
idx_start = content.find('    public byte[] exporterPlanningExcel() {')
idx_end = content.find('    public byte[] exporterPlanningPDF() {')

new_exporter = '''    public byte[] exporterPlanningExcel() {
        List<PlanningRow> data = retrieveData();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            CellStyle headerStyle = workbook.createCellStyle();
            Font headerFont = workbook.createFont();
            headerFont.setBold(true);
            headerStyle.setFont(headerFont);
            
            // 1. Planning Global
            Sheet sheet1 = workbook.createSheet("Planning Global");
            createExcelHeader(sheet1, 0, headerStyle);
            fillExcelData(sheet1, data, 1, null);
            for (int i = 0; i < 9; i++) sheet1.autoSizeColumn(i);
            
            // 2. Par Filière
            Sheet sheet2 = workbook.createSheet("Par Filière");
            createExcelHeader(sheet2, 0, headerStyle);
            List<PlanningRow> parFiliere = new ArrayList<>(data);
            parFiliere.sort(Comparator.comparing((PlanningRow r) -> r.filiere == null ? "" : r.filiere).thenComparing(r -> r.date == null ? LocalDate.MIN : r.date).thenComparing(r -> r.heure == null ? LocalTime.MIN : r.heure));
            fillExcelData(sheet2, parFiliere, 1, null);
            for (int i = 0; i < 9; i++) sheet2.autoSizeColumn(i);
            
            // 3. Par Professeur
            Sheet sheet3 = workbook.createSheet("Par Professeur");
            createExcelHeader(sheet3, 0, headerStyle);
            List<PlanningRow> parProf = new ArrayList<>(data);
            parProf.sort(Comparator.comparing((PlanningRow r) -> r.president == null ? "" : r.president).thenComparing(r -> r.date == null ? LocalDate.MIN : r.date).thenComparing(r -> r.heure == null ? LocalTime.MIN : r.heure));
            fillExcelData(sheet3, parProf, 1, null);
            for (int i = 0; i < 9; i++) sheet3.autoSizeColumn(i);

            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur generation Excel", e);
        }
    }

    private int createExcelHeader(Sheet sheet, int rowNum, CellStyle style) {
        Row row = sheet.createRow(rowNum);
        String[] headers = {"Date", "Heure", "Salle", "Encadrant", "Jury 1", "Jury 2", "Etudiant", "Filière", "Sujet PFE"};
        for (int i = 0; i < headers.length; i++) {
            org.apache.poi.ss.usermodel.Cell cell = row.createCell(i);
            cell.setCellValue(headers[i]);
            if (style != null) cell.setCellStyle(style);
        }
        return rowNum + 1;
    }

    private int fillExcelData(Sheet sheet, List<PlanningRow> rows, int startRow, CellStyle style) {
        int rowIndex = startRow;
        for (PlanningRow item : rows) {
            Row row = sheet.createRow(rowIndex++);
            createCell(row, 0, formatDate(item.date), style);
            createCell(row, 1, formatTime(item.heure), style);
            createCell(row, 2, item.salle, style);
            createCell(row, 3, item.president, style);
            createCell(row, 4, item.jury1, style);
            createCell(row, 5, item.jury2, style);
            createCell(row, 6, item.etudiants, style);
            createCell(row, 7, item.filiere, style);
            createCell(row, 8, item.sujetPfe, style);
        }
        return rowIndex;
    }

    private void createCell(Row row, int col, String value, CellStyle style) {
        org.apache.poi.ss.usermodel.Cell cell = row.createCell(col);
        cell.setCellValue(value != null ? value : "");
        if (style != null) cell.setCellStyle(style);
    }
'''

if idx_start != -1 and idx_end != -1:
    content = content[:idx_start] + new_exporter + '\n' + content[idx_end:]

# We need to remove the old createCell if it's there
# It was already replaced in the block if it was between idx_start and idx_end, but wait, createCell might be AFTER idx_end?
# Let's check original file.
# createCell is at line 328, exporterPlanningPDF is at line 334. So createCell was BEFORE exporterPlanningPDF.
# Meaning our replacement of the whole block from exporterPlanningExcel to exporterPlanningPDF wiped out the old createCell and we redefined it in new_exporter. That's perfect.

with open('src/main/java/com/pfe/service/ExportService.java', 'w', encoding='utf-8') as f:
    f.write(content)
