package com.pfe.service;

import com.itextpdf.io.image.ImageDataFactory;
import com.itextpdf.kernel.colors.Color;
import com.itextpdf.kernel.colors.ColorConstants;
import com.itextpdf.kernel.colors.DeviceRgb;
import com.itextpdf.kernel.events.Event;
import com.itextpdf.kernel.events.IEventHandler;
import com.itextpdf.kernel.events.PdfDocumentEvent;
import com.itextpdf.kernel.font.PdfFont;
import com.itextpdf.kernel.font.PdfFontFactory;
import com.itextpdf.kernel.geom.PageSize;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfPage;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.kernel.pdf.canvas.PdfCanvas;
import com.itextpdf.layout.Canvas;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Image;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Table;
import com.itextpdf.layout.properties.TextAlignment;
import com.itextpdf.layout.properties.UnitValue;
import com.pfe.dao.interfaces.ISoutenanceDAO;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.Font;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

@Service
public class ExportService {

    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm'H'");
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");

    public static class PlanningRow {
        public LocalDate date;
        public LocalTime heure;
        public String salle;
        public String etudiants;
        public String president;
        public String jury1;
        public String jury2;
        public String filiere;
        public String sujetPfe;
    }

    public static class AffectationStudent {
        public String nom;
        public String filiere;
    }

    public static class AffectationGroup {
        public String encadrant;
        public List<AffectationStudent> etudiants = new ArrayList<>();
    }

    public boolean hasPlannedData() {
        return !retrieveData().isEmpty();
    }

    public boolean hasAffectationData() {
        return !retrieveAffectationData().isEmpty();
    }

    private List<PlanningRow> retrieveData() {
        List<?> soutenances = soutenanceDAO.findAll();
        List<PlanningRow> data = new ArrayList<>();
        if (soutenances == null) return data;

        for (Object s : soutenances) {
            LocalDate d = (LocalDate) tryInvokeGetter(s, "getDate");
            if (d == null) continue;
            data.add(mapToRow(s));
        }

        data.sort(Comparator.comparing((PlanningRow r) -> r.date)
                .thenComparing(r -> r.heure == null ? LocalTime.MIN : r.heure)
                .thenComparing(r -> r.salle == null ? "" : r.salle));

        return data;
    }

    private PlanningRow mapToRow(Object soutenance) {
        PlanningRow item = new PlanningRow();
        item.date = (LocalDate) tryInvokeGetter(soutenance, "getDate");
        item.heure = (LocalTime) tryInvokeGetter(soutenance, "getHeureDebut");
        Object salleObj = tryInvokeGetter(soutenance, "getSalle");
        item.salle = salleObj != null ? normalizeRoomName(asString(tryInvokeGetter(salleObj, "getNom"))) : "N/A";

        List<?> etuds = (List<?>) tryInvokeGetter(soutenance, "getEtudiants");
        if (etuds != null && !etuds.isEmpty()) {
            item.etudiants = etuds.stream().map(e -> {
                String nom = asString(tryInvokeGetter(e, "getNom"));
                String prenom = asString(tryInvokeGetter(e, "getPrenom"));
                String fullName = (nom + " " + prenom).trim();
                return fullName;
            }).collect(Collectors.joining(" | "));
        } else {
            item.etudiants = "N/A";
        }

        Object presObj = tryInvokeGetter(soutenance, "getPresident");
        item.president = presObj != null ? (asString(tryInvokeGetter(presObj, "getNom")) + " " + asString(tryInvokeGetter(presObj, "getPrenom"))).trim() : "N/A";
        
        Object filiereObj = tryInvokeGetter(soutenance, "getFiliere");
        item.filiere = filiereObj != null ? asString(tryInvokeGetter(filiereObj, "getNom")) : "N/A";
        item.sujetPfe = asString(tryInvokeGetter(soutenance, "getTitre"));

        List<?> jurysList = (List<?>) tryInvokeGetter(soutenance, "getJurys");
        if (jurysList != null && jurysList.size() >= 2) {
            Object j1 = jurysList.get(0);
            Object j2 = jurysList.get(1);
            item.jury1 = (asString(tryInvokeGetter(j1, "getNom")) + " " + asString(tryInvokeGetter(j1, "getPrenom"))).trim();
            item.jury2 = (asString(tryInvokeGetter(j2, "getNom")) + " " + asString(tryInvokeGetter(j2, "getPrenom"))).trim();
        } else {
            item.jury1 = "N/A";
            item.jury2 = "N/A";
        }

        return item;
    }

    private Object tryInvokeGetter(Object target, String methodName) {
        if (target == null) return null;
        try {
            Method method = target.getClass().getMethod(methodName);
            return method.invoke(target);
        } catch (Exception e) {
            return null;
        }
    }

    private String asString(Object value) {
        return value == null ? "" : String.valueOf(value).trim();
    }

    private String normalizeRoomName(String value) {
        if (value == null || value.isBlank()) return "N/A";
        String text = value.trim().toUpperCase();
        java.util.regex.Matcher block = java.util.regex.Pattern.compile("\\b([AB])\\s*0*(\\d{1,2})\\b").matcher(text);
        if (block.find()) {
            return block.group(1) + String.format("%02d", Integer.parseInt(block.group(2)));
        }
        java.util.regex.Matcher number = java.util.regex.Pattern.compile("\\b(\\d{3})\\b").matcher(text);
        if (number.find()) {
            int n = Integer.parseInt(number.group(1));
            char bloc = n >= 200 ? 'B' : 'A';
            int local = n % 100;
            return bloc + String.format("%02d", local);
        }
        return text.replace("SALLE", "").trim();
    }

    private String formatTime(LocalTime time) {
        return time == null ? "" : time.format(TIME_FORMAT);
    }

    private String formatDate(LocalDate date) {
        return date == null ? "" : date.format(DATE_FORMAT);
    }

    private List<AffectationGroup> retrieveAffectationData() {
        List<?> soutenances = soutenanceDAO.findAll();
        List<AffectationGroup> rows = new ArrayList<>();
        if (soutenances == null) return rows;

        Map<String, Map<String, AffectationStudent>> grouped = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (Object soutenance : soutenances) {
            Object presidentObj = tryInvokeGetter(soutenance, "getPresident");
            String encadrant = presidentObj != null
                    ? (asString(tryInvokeGetter(presidentObj, "getNom")) + " " + asString(tryInvokeGetter(presidentObj, "getPrenom"))).trim()
                    : "N/A";
            String encadrantKey = encadrant.isBlank() ? "N/A" : encadrant;

            List<?> etudiants = (List<?>) tryInvokeGetter(soutenance, "getEtudiants");
            if (etudiants == null || etudiants.isEmpty()) {
                continue;
            }

            Map<String, AffectationStudent> etudiantsParNom = grouped.computeIfAbsent(
                    encadrantKey,
                    key -> new TreeMap<>(String.CASE_INSENSITIVE_ORDER)
            );

            for (Object etu : etudiants) {
                String etudiant = (asString(tryInvokeGetter(etu, "getNom")) + " " + asString(tryInvokeGetter(etu, "getPrenom"))).trim();
                if (etudiant.isBlank()) {
                    continue;
                }
                String filiere = resolveFiliereName(etu, soutenance);
                AffectationStudent student = new AffectationStudent();
                student.nom = etudiant;
                student.filiere = filiere;
                etudiantsParNom.putIfAbsent(etudiant, student);
            }
        }

        for (Map.Entry<String, Map<String, AffectationStudent>> entry : grouped.entrySet()) {
            if (entry.getValue().isEmpty()) {
                continue;
            }
            AffectationGroup group = new AffectationGroup();
            group.encadrant = entry.getKey();
            group.etudiants = new ArrayList<>(entry.getValue().values());
            rows.add(group);
        }

        return rows;
    }

    private String resolveFiliereName(Object etudiant, Object soutenance) {
        Object filiereObj = tryInvokeGetter(etudiant, "getFiliere");
        String filiere = asString(tryInvokeGetter(filiereObj, "getNom"));
        if (!filiere.isBlank()) {
            return filiere;
        }
        Object soutenanceFiliereObj = tryInvokeGetter(soutenance, "getFiliere");
        String soutenanceFiliere = asString(tryInvokeGetter(soutenanceFiliereObj, "getNom"));
        return soutenanceFiliere.isBlank() ? "N/A" : soutenanceFiliere;
    }

    private int computeMaxStudentsPerEncadrant(List<AffectationGroup> data) {
        int maxStudents = 0;
        for (AffectationGroup group : data) {
            maxStudents = Math.max(maxStudents, group.etudiants == null ? 0 : group.etudiants.size());
        }
        return Math.max(maxStudents, 1);
    }

    public byte[] exporterPlanningExcel() {
        List<PlanningRow> data = retrieveData();
        data.sort(Comparator.comparing((PlanningRow r) -> r.date == null ? LocalDate.MIN : r.date).thenComparing(r -> r.heure == null ? LocalTime.MIN : r.heure));
        
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

    public byte[] exporterPlanningPDF() {
        List<PlanningRow> data = retrieveData();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        pdf.addEventHandler(PdfDocumentEvent.END_PAGE, new IEventHandler() {
            @Override
            public void handleEvent(Event event) {
                PdfDocumentEvent docEvent = (PdfDocumentEvent) event;
                PdfDocument pdfDoc = docEvent.getDocument();
                PdfPage page = docEvent.getPage();
                int pageNumber = pdfDoc.getPageNumber(page);
                PdfCanvas canvas = new PdfCanvas(page.newContentStreamAfter(), page.getResources(), pdfDoc);
                try {
                    PdfFont font = PdfFontFactory.createFont();
                    new Canvas(canvas, page.getPageSize()).setFont(font).setFontSize(10)
                            .showTextAligned("Page " + pageNumber + " - Genere le " + LocalDate.now(),
                                    page.getPageSize().getWidth() / 2, 20, TextAlignment.CENTER);
                } catch (IOException ignored) {
                }
            }
        });

        Table logoTable = new Table(UnitValue.createPercentArray(new float[]{1, 1})).useAllAvailableWidth();
        logoTable.setBorder(null);
        logoTable.addCell(buildLogoCell("ensah.png", 55));
        logoTable.addCell(buildLogoCell("uae.png", 55));
        document.add(logoTable);

        document.add(new Paragraph("Planning des Soutenances PFE")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16)
                .setMarginBottom(20));

        float[] columnWidths = {1.9f, 1.8f, 1.8f, 1.2f, 1.2f, 1.1f, 3.2f};
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();
        Color purple = new DeviceRgb(124, 58, 237);
        Color green = new DeviceRgb(14, 140, 123);
        String[] headers = {"Encadrant", "Jury 1", "Jury 2", "Salle", "Date", "Heure", "Etudiant"};
        for (String header : headers) {
            com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(header).setBold().setFontSize(10))
                    .setBackgroundColor(purple)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER);
            table.addHeaderCell(cell);
        }

        boolean alt = false;
        for (PlanningRow item : data) {
            Color bg = alt ? new DeviceRgb(238, 246, 244) : ColorConstants.WHITE;
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(item.president).setFontSize(8.5f)));
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(item.jury1).setFontSize(8.5f)));
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(item.jury2).setFontSize(8.5f)));
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(item.salle).setFontSize(8.5f)));
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(formatDate(item.date)).setFontSize(8.5f)));
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(formatTime(item.heure)).setFontSize(8.5f).setFontColor(green)));
            table.addCell(new com.itextpdf.layout.element.Cell().setBackgroundColor(bg).add(new Paragraph(item.etudiants).setFontSize(8.5f)));
            alt = !alt;
        }

        document.add(table);
        document.close();
        return baos.toByteArray();
    }

    public byte[] exporterAffectationExcel() {
        List<AffectationGroup> data = retrieveAffectationData();
        try (Workbook workbook = new XSSFWorkbook(); ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Sheet sheet = workbook.createSheet("Affectation");

            int maxStudents = computeMaxStudentsPerEncadrant(data);
            Row header = sheet.createRow(0);
            header.createCell(0).setCellValue("Encadrant");
            for (int i = 1; i <= maxStudents; i++) {
                header.createCell(i).setCellValue("Etudiant " + i);
            }

            int rowIndex = 1;
            for (AffectationGroup item : data) {
                Row row = sheet.createRow(rowIndex++);
                row.createCell(0).setCellValue(item.encadrant == null ? "" : item.encadrant);
                for (int i = 0; i < item.etudiants.size(); i++) {
                    AffectationStudent student = item.etudiants.get(i);
                    row.createCell(i + 1).setCellValue(student.nom == null ? "" : student.nom);
                }
            }

            for (int i = 0; i <= maxStudents; i++) {
                sheet.autoSizeColumn(i);
            }
            workbook.write(out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new RuntimeException("Erreur generation Excel affectation", e);
        }
    }

    public byte[] exporterAffectationPDF() {
        List<AffectationGroup> data = retrieveAffectationData();
        ByteArrayOutputStream baos = new ByteArrayOutputStream();

        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf, PageSize.A4.rotate());

        document.add(new Paragraph("Affectation Encadrant - Etudiant")
                .setTextAlignment(TextAlignment.CENTER)
                .setBold()
                .setFontSize(16)
                .setMarginBottom(16));

        Color blue = new DeviceRgb(37, 99, 235);
        Color rowAlt = new DeviceRgb(246, 249, 255);
        Map<String, Color> filiereColors = buildFiliereColors(data);
        int maxStudents = computeMaxStudentsPerEncadrant(data);

        float[] columnWidths = new float[maxStudents + 1];
        columnWidths[0] = 2.8f;
        for (int i = 1; i < columnWidths.length; i++) {
            columnWidths[i] = 2.2f;
        }
        Table table = new Table(UnitValue.createPercentArray(columnWidths)).useAllAvailableWidth();

        table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                .add(new Paragraph("Encadrant").setBold().setFontSize(10))
                .setBackgroundColor(blue)
                .setFontColor(ColorConstants.WHITE)
                .setTextAlignment(TextAlignment.CENTER));

        for (int i = 1; i <= maxStudents; i++) {
            String header = "Etudiant " + i;
            table.addHeaderCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(header).setBold().setFontSize(10))
                    .setBackgroundColor(blue)
                    .setFontColor(ColorConstants.WHITE)
                    .setTextAlignment(TextAlignment.CENTER));
        }

        boolean alt = false;
        for (AffectationGroup item : data) {
            Color bg = alt ? rowAlt : ColorConstants.WHITE;
            table.addCell(new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(bg)
                    .add(new Paragraph(item.encadrant).setFontSize(9)));

            for (int i = 0; i < maxStudents; i++) {
                if (i < item.etudiants.size()) {
                    AffectationStudent student = item.etudiants.get(i);
                    String filiere = student.filiere == null || student.filiere.isBlank() ? "N/A" : student.filiere;
                    Color filiereColor = filiereColors.getOrDefault(filiere, new DeviceRgb(229, 231, 235));
                    table.addCell(new com.itextpdf.layout.element.Cell()
                            .setBackgroundColor(filiereColor)
                            .add(new Paragraph(student.nom).setFontSize(9)));
                } else {
                    table.addCell(new com.itextpdf.layout.element.Cell()
                            .setBackgroundColor(bg)
                            .add(new Paragraph("")));
                }
            }
            alt = !alt;
        }

        document.add(table);
        document.add(new Paragraph("Legende des filieres").setBold().setFontSize(11).setMarginTop(14).setMarginBottom(6));
        Table legend = new Table(UnitValue.createPercentArray(new float[]{0.5f, 4.5f}))
                .setWidth(UnitValue.createPercentValue(40));
        for (Map.Entry<String, Color> entry : filiereColors.entrySet()) {
            legend.addCell(new com.itextpdf.layout.element.Cell()
                    .setBackgroundColor(entry.getValue())
                    .setMinHeight(12f));
            legend.addCell(new com.itextpdf.layout.element.Cell()
                    .add(new Paragraph(entry.getKey()).setFontSize(9)));
        }
        document.add(legend);

        document.close();
        return baos.toByteArray();
    }

    private Map<String, Color> buildFiliereColors(List<AffectationGroup> data) {
        Map<String, Color> filiereColors = new LinkedHashMap<>();
        TreeMap<String, String> sortedFilieres = new TreeMap<>(String.CASE_INSENSITIVE_ORDER);

        for (AffectationGroup group : data) {
            for (AffectationStudent student : group.etudiants) {
                String filiere = (student.filiere == null || student.filiere.isBlank()) ? "N/A" : student.filiere;
                sortedFilieres.putIfAbsent(filiere, filiere);
            }
        }

        Color[] palette = new Color[]{
                new DeviceRgb(255, 224, 178),
                new DeviceRgb(197, 225, 165),
                new DeviceRgb(179, 229, 252),
                new DeviceRgb(225, 190, 231),
                new DeviceRgb(255, 245, 157),
                new DeviceRgb(178, 235, 242),
                new DeviceRgb(215, 204, 200),
                new DeviceRgb(207, 216, 220)
        };

        int index = 0;
        for (String filiere : sortedFilieres.values()) {
            filiereColors.put(filiere, palette[index % palette.length]);
            index++;
        }
        return filiereColors;
    }

    private com.itextpdf.layout.element.Cell buildLogoCell(String resource, float width) {
        com.itextpdf.layout.element.Cell cell = new com.itextpdf.layout.element.Cell().setBorder(null);
        try (InputStream is = getClass().getClassLoader().getResourceAsStream(resource)) {
            if (is == null) return cell;
            byte[] logoBytes = is.readAllBytes();
            Image logo = new Image(ImageDataFactory.create(logoBytes));
            logo.setAutoScale(false);
            logo.setWidth(width);
            logo.setHorizontalAlignment(com.itextpdf.layout.properties.HorizontalAlignment.CENTER);
            cell.add(logo);
        } catch (Exception ignored) {
        }
        return cell;
    }
}
