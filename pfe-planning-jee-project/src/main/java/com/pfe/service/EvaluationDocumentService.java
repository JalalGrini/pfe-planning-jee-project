package com.pfe.service;

import com.pfe.dao.interfaces.ISoutenanceDAO;
import com.pfe.model.Etudiant;
import com.pfe.model.Professeur;
import com.pfe.model.Soutenance;
import org.apache.poi.xwpf.usermodel.IBodyElement;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.apache.poi.xwpf.usermodel.XWPFTable;
import org.apache.poi.xwpf.usermodel.XWPFTableCell;
import org.apache.poi.xwpf.usermodel.XWPFTableRow;
import org.apache.poi.xwpf.usermodel.UnderlinePatterns;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.Normalizer;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class EvaluationDocumentService {
    private static final String TEMPLATE_PATH = "templates/Fiche_Evaluation_PFE_NomEtudiant_Prenom.docx";
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final List<String> FILIERES = List.of(
            "Transformation Digitale et Intelligence Artificielle",
            "Genie Civil",
            "Genie Informatique",
            "Ingenierie des donnees",
            "Genie energetique et energies renouvelables",
            "Genie de l Eau et de l Environnement",
            "Genie Mecanique"
    );

    @Autowired
    private ISoutenanceDAO soutenanceDAO;

    public static class EvaluationPayload {
        private final byte[] data;
        private final String fileName;

        public EvaluationPayload(byte[] data, String fileName) {
            this.data = data;
            this.fileName = fileName;
        }

        public byte[] getData() {
            return data;
        }

        public String getFileName() {
            return fileName;
        }
    }

    @Transactional(readOnly = true)
    public EvaluationPayload generateEvaluationDoc(Long id) {
        Soutenance soutenance = soutenanceDAO.findById(id);
        if (soutenance == null) {
            return null;
        }
        return new EvaluationPayload(generateDoc(soutenance), buildFileName(soutenance));
    }

    private byte[] generateDoc(Soutenance soutenance) {
        try (InputStream is = new ClassPathResource(TEMPLATE_PATH).getInputStream();
             XWPFDocument doc = new XWPFDocument(is);
             ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            applyTemplateData(doc, soutenance);
            doc.write(out);
            return out.toByteArray();
        } catch (Exception e) {
            throw new IllegalStateException("Erreur generation document evaluation", e);
        }
    }

    public String buildFileName(Soutenance soutenance) {
        String etudiant = formatEtudiants(soutenance);
        String base = "Fiche_Evaluation_PFE_" + (etudiant.isEmpty() ? "Etudiant" : etudiant);
        return base.replaceAll("[\\\\/:*?\"<>|]", "_").replaceAll("\\s+", "_") + ".docx";
    }

    private void applyTemplateData(XWPFDocument doc, Soutenance soutenance) {
        String nomEtudiant = formatEtudiants(soutenance);
        XWPFParagraph studentNameParagraph = replaceNextDottedLine(doc, "Nom - Prenom", nomEtudiant);

        String titre = safeText(soutenance.getTitre());
        if (titre.startsWith("Sujet-")) {
            titre = "";
        }
        replaceNextDottedLine(doc, "Intitule du rapport", titre);
        replaceNextDottedLine(doc, "encadrant", formatProfesseur(soutenance.getPresident()));

        fillJuryTable(doc, soutenance);
        forceFiliereBlock(doc, resolveFiliereLabel(soutenance), studentNameParagraph);
        appendPlanningInfo(doc, soutenance);
    }

    private void forceFiliereBlock(XWPFDocument doc, String selectedFiliere, XWPFParagraph studentNameParagraph) {
        String filiereDetails = buildFiliereDetailsLine(selectedFiliere);

        clearLegacyFiliereParagraphs(doc);

        XWPFParagraph targetParagraph = resolveFiliereTargetParagraph(doc, studentNameParagraph);
        Integer titleFontSize = resolveLabelFontSize(doc, "Nom - Prenom");
        setFiliereLineUnderName(targetParagraph, filiereDetails, titleFontSize);
    }

    private void clearLegacyFiliereParagraphs(XWPFDocument doc) {
        List<XWPFParagraph> legacyFiliereParagraphs = new ArrayList<>();
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            if (isLegacyFiliereParagraph(paragraph)) {
                legacyFiliereParagraphs.add(paragraph);
            }
        }
        for (XWPFParagraph paragraph : legacyFiliereParagraphs) {
            replaceParagraphText(paragraph, "");
        }
    }

    private boolean isLegacyFiliereParagraph(XWPFParagraph paragraph) {
        String raw = paragraph.getText() == null ? "" : paragraph.getText();
        String normalized = normalize(raw);
        return normalized.contains("filiere")
                || normalized.contains("transformation digitale")
                || normalized.contains("genie civil")
                || normalized.contains("genie informatique")
                || normalized.contains("ingenierie des donnees")
                || normalized.contains("genie energetique")
                || normalized.contains("genie de l eau")
                || normalized.contains("genie mecanique")
                || raw.contains("\u2610")
                || raw.contains("\u2611")
                || raw.contains("\u2022");
    }

    private XWPFParagraph resolveFiliereTargetParagraph(XWPFDocument doc, XWPFParagraph studentNameParagraph) {
        if (studentNameParagraph != null) {
            return studentNameParagraph;
        }
        for (int i = 0; i < doc.getBodyElements().size(); i++) {
            IBodyElement element = doc.getBodyElements().get(i);
            if (!(element instanceof XWPFParagraph paragraph) || !containsLabel(paragraph.getText(), "Nom - Prenom")) {
                continue;
            }
            for (int j = i + 1; j < doc.getBodyElements().size(); j++) {
                IBodyElement next = doc.getBodyElements().get(j);
                if (next instanceof XWPFParagraph p && !normalize(p.getText()).isBlank()) {
                    return p;
                }
            }
            return paragraph;
        }
        return doc.createParagraph();
    }

    private String buildFiliereDetailsLine(String selectedFiliere) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < FILIERES.size(); i++) {
            String filiere = FILIERES.get(i);
            String mark = normalize(filiere).equals(normalize(selectedFiliere)) ? "\u2611 " : "\u2610 ";
            sb.append(mark).append(filiere);
            if (i < FILIERES.size() - 1) {
                sb.append("   ");
            }
        }
        return sb.toString();
    }

    private void setFiliereLineUnderName(XWPFParagraph paragraph, String filiereDetails, Integer titleFontSize) {
        String studentName = safeText(paragraph.getText());
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
        if (!studentName.isBlank()) {
            XWPFRun nameRun = paragraph.createRun();
            nameRun.setText(studentName);
            nameRun.addBreak();
            nameRun.addBreak();
        }
        XWPFRun titleRun = paragraph.createRun();
        titleRun.setBold(true);
        titleRun.setUnderline(UnderlinePatterns.SINGLE);
        titleRun.setFontSize((titleFontSize != null && titleFontSize > 0) ? titleFontSize + 1 : 14);
        titleRun.setText("Filiere :");
        titleRun.addBreak();
        XWPFRun detailsRun = paragraph.createRun();
        detailsRun.setText(filiereDetails);
    }

    private Integer resolveLabelFontSize(XWPFDocument doc, String label) {
        for (XWPFParagraph paragraph : doc.getParagraphs()) {
            if (!containsLabel(paragraph.getText(), label)) {
                continue;
            }
            for (XWPFRun run : paragraph.getRuns()) {
                if (run != null && run.getFontSize() > 0) {
                    return run.getFontSize();
                }
            }
        }
        return null;
    }

    private String resolveFiliereLabel(Soutenance soutenance) {
        if (soutenance.getFiliere() != null && soutenance.getFiliere().getNom() != null) {
            return soutenance.getFiliere().getNom();
        }
        List<Etudiant> etudiants = soutenance.getEtudiants();
        if (etudiants != null && !etudiants.isEmpty() && etudiants.get(0).getFiliere() != null) {
            return safeText(etudiants.get(0).getFiliere().getNom());
        }
        return "";
    }

    private XWPFParagraph replaceNextDottedLine(XWPFDocument doc, String label, String value) {
        boolean awaitingValue = false;
        for (IBodyElement element : doc.getBodyElements()) {
            if (element instanceof XWPFParagraph paragraph) {
                String text = paragraph.getText();
                if (awaitingValue && text != null && text.matches(".*[.\\u2026]{5,}.*")) {
                    replaceParagraphText(paragraph, value);
                    return paragraph;
                }
                if (containsLabel(text, label)) {
                    awaitingValue = true;
                }
            }
        }
        return null;
    }

    private void fillJuryTable(XWPFDocument doc, Soutenance soutenance) {
        List<Professeur> jurys = soutenance.getJurys();
        String jury1 = jurys != null && jurys.size() > 0 ? formatProfesseur(jurys.get(0)) : "N/A";
        String jury2 = jurys != null && jurys.size() > 1 ? formatProfesseur(jurys.get(1)) : "N/A";
        for (int i = 0; i < doc.getBodyElements().size(); i++) {
            IBodyElement element = doc.getBodyElements().get(i);
            if (element instanceof XWPFParagraph p && containsLabel(p.getText(), "membres du jury")) {
                for (int j = i + 1; j < doc.getBodyElements().size(); j++) {
                    IBodyElement next = doc.getBodyElements().get(j);
                    if (next instanceof XWPFTable table) {
                        fillJuryRow(table, 0, jury1);
                        fillJuryRow(table, 1, jury2);
                        return;
                    }
                }
            }
        }
    }

    private void fillJuryRow(XWPFTable table, int rowIndex, String juryName) {
        if (table.getNumberOfRows() <= rowIndex) {
            return;
        }
        XWPFTableRow row = table.getRow(rowIndex);
        if (row.getTableCells().isEmpty()) {
            return;
        }
        XWPFTableCell cell = row.getCell(0);
        String suffix = cell.getText().contains("Rapporteur") ? " Rapporteur" : "";
        int count = cell.getParagraphs().size();
        for (int i = count - 1; i >= 0; i--) {
            cell.removeParagraph(i);
        }
        cell.setText("Pr. " + juryName + suffix);
    }

    private void appendPlanningInfo(XWPFDocument doc, Soutenance soutenance) {
        LocalDate date = soutenance.getDate();
        if (date == null) {
            return;
        }
        String formattedDate = date.format(DATE_FORMAT);
        if (replaceLeDateLine(doc, formattedDate)) {
            return;
        }
        XWPFParagraph paragraph = doc.createParagraph();
        paragraph.createRun().setText("Le : " + formattedDate);
    }

    private boolean replaceLeDateLine(XWPFDocument doc, String formattedDate) {
        for (IBodyElement element : doc.getBodyElements()) {
            if (!(element instanceof XWPFParagraph paragraph)) {
                continue;
            }
            String text = paragraph.getText();
            if (text == null) {
                continue;
            }
            String normalized = normalize(text).trim();
            boolean isLeLine = normalized.matches("^le\\s*:.*");
            boolean hasDots = text.matches(".*[.\\u2026]{3,}.*");
            if (isLeLine || (normalized.startsWith("le") && hasDots)) {
                replaceParagraphText(paragraph, "Le : " + formattedDate);
                return true;
            }
        }
        return false;
    }

    private void replaceParagraphText(XWPFParagraph paragraph, String value) {
        for (int i = paragraph.getRuns().size() - 1; i >= 0; i--) {
            paragraph.removeRun(i);
        }
        paragraph.createRun().setText(value);
    }

    private boolean containsLabel(String text, String label) {
        if (text == null) {
            return false;
        }
        return normalize(text).contains(normalize(label));
    }

    private String formatEtudiants(Soutenance soutenance) {
        List<Etudiant> etudiants = soutenance.getEtudiants();
        if (etudiants == null || etudiants.isEmpty()) {
            return "";
        }
        return etudiants.stream()
                .map(e -> safeText(e.getNom()) + " " + safeText(e.getPrenom()))
                .map(String::trim)
                .collect(Collectors.joining(" & "));
    }

    private String formatProfesseur(Professeur professeur) {
        if (professeur == null) {
            return "N/A";
        }
        String full = (safeText(professeur.getNom()) + " " + safeText(professeur.getPrenom())).trim();
        return full.isEmpty() ? "N/A" : full;
    }

    private String safeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalize(String value) {
        String normalized = Normalizer.normalize(value == null ? "" : value, Normalizer.Form.NFD);
        return normalized.replaceAll("\\p{M}", "").toLowerCase(Locale.ROOT);
    }
}
