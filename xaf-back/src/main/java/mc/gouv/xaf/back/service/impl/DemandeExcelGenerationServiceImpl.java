package mc.gouv.xaf.back.service.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.service.DemandeExcelGenerationService;
import mc.gouv.xaf.back.service.DemandeExcelRechercheProvider;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.MissingNode;
import tools.jackson.databind.node.NullNode;
import tools.jackson.databind.node.ObjectNode;
import tools.jackson.databind.node.StringNode;

/**
 * Classe permettant de générer un fichier Excel à partir des fichiers Recap de la démarche, avec une feuille Excel par
 * buildId.
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class DemandeExcelGenerationServiceImpl implements DemandeExcelGenerationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeExcelGenerationServiceImpl.class);

    private static final String LABEL = "label";

    private static final String CONTENU = "contenu.";

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final PaysCache paysCache;
    private final PropertiesService propertiesService;
    private final DemandesConfigService demandesConfigService;
    private final DemarchesDataProvider demarchesDataProvider;

    private static final List<Short> colorListStr = new ArrayList<>();

    static {
        colorListStr.add(IndexedColors.TAN.index);
        colorListStr.add(IndexedColors.LIGHT_GREEN.index);
        colorListStr.add(IndexedColors.LIGHT_CORNFLOWER_BLUE.index);
        colorListStr.add(IndexedColors.ROSE.index);
        colorListStr.add(IndexedColors.LEMON_CHIFFON.index);
        colorListStr.add(IndexedColors.LIME.index);
        colorListStr.add(IndexedColors.PALE_BLUE.index);
    }

    @Override
    public void generateExcel(ExcelRechercheDTO excelRechercheDto,
            DemandeExcelRechercheProvider demandesByDateRangeProvider, OutputStream outputStream) throws IOException {

        LOGGER.info("Début de la génération de l'export Excel des demandes...");

        XSSFWorkbook workbook = new XSSFWorkbook();

        List<DemandeConfigBO> demandeConfigs = demandesConfigService.getConfigsBO();
        List<DemandeDTO> demandes = demandesByDateRangeProvider.getDemandes(excelRechercheDto);

        for (DemandeConfigBO demandeConfig : demandeConfigs) {
            String buildId = demandeConfig.getBuildId();
            LOGGER.info("Chargement du fichier recap {}...", buildId);
            JsonNode sectionsNode = demandeConfig.getContenu().get("recap").get("sections");
            ArrayNode sections = (sectionsNode != null && sectionsNode.isArray())
                    ? (ArrayNode) sectionsNode
                    : MAPPER.createArrayNode();

            String sheetName = buildId;
            DemandeExcelGenerationDTO demandeExcelGenerationDto = demarchesDataProvider.getDemandeExcelGenerationDTO();
            if (demandeExcelGenerationDto.getBuildIdNameMap() != null
                    && demandeExcelGenerationDto.getBuildIdNameMap().get(buildId) != null) {
                sheetName = demandeExcelGenerationDto.getBuildIdNameMap().get(buildId);
            }
            Sheet sheet = workbook.createSheet(sheetName);
            sheet.setColumnWidth(0, 6000);
            sheet.setColumnWidth(1, 4000);

            Row headerRow = sheet.createRow(0);

            // Écriture de la ligne de header
            LOGGER.info("Ecriture de la ligne de header...");
            writeRow(workbook, sections, headerRow, null, true);
            List<Row> rows = new ArrayList<>();
            int n = 1;
            for (DemandeDTO demande : demandes) {
                if (buildId.equals(demande.getConfigBuildId())) {
                    LOGGER.info("Ecriture de la ligne de la demande {}...", demande.getPkDemandes());
                    Row demRow = sheet.createRow(n);
                    writeRow(workbook, sections, demRow, demande, false);
                    rows.add(demRow);
                    n++;
                }
            }

            LOGGER.info("Redimensionnement des colonnes et des lignes...");
            // Elargissement des colonnes
            for (int i = 0; i < headerRow.getLastCellNum(); i++) {
                sheet.setColumnWidth(i, 7000);
            }
            // Définition de la hauteur des lignes
            for (Row row : rows) {
                row.setHeight((short) 500);
            }
            headerRow.setHeight((short) 900);
        }

        LOGGER.info("Ecriture du workbook...");
        workbook.write(outputStream);
        workbook.close();
    }

    private void writeRow(XSSFWorkbook workbook, ArrayNode sections, Row row, DemandeDTO demande, boolean header) {
        // 2 premières colonnes, qui ne concernent pas le contenu de la demande
        if (header) {
            Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
            CellStyle cellStyle = workbook.createCellStyle();
            setCellStyle(cellStyle, header, 0);
            cell.setCellValue("Identifiant de la demande");
            cell.setCellStyle(cellStyle);
            cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
            cell.setCellValue("Statut de la demande");
            cell.setCellStyle(cellStyle);
        } else {
            Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
            cell.setCellValue(demande.getIdentifiant());
            cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
            cell.setCellValue(demande.getDernierStatut().getLibelle());
        }
        // Reste des colonnes, qui sont générées à partir du fichier Recap BO
        genererColonnes(workbook, sections, row, demande, header);
    }

    private void genererColonnes(XSSFWorkbook workbook, ArrayNode sections, Row row, DemandeDTO demande,
            boolean header) {
        for (int i = 0; i < sections.size(); i++) {
            JsonNode section = sections.get(i);
            String sectionType = section.has("type") ? section.get("type").asString() : "";

            XSSFCellStyle cellStyle = workbook.createCellStyle();
            setCellStyle(cellStyle, header, i + 1);

            if (!"sousSections".equals(sectionType)) {
                generateBasicField(section.has("titre") ? section.get("titre").asString() : "", section, row, cellStyle,
                        demande == null ? null : demande.getContenu(), header);
            } else if (section.has("sousSections") && section.get("sousSections").isArray()) {
                ArrayNode sousSections = (ArrayNode) section.get("sousSections");
                for (JsonNode sect : sousSections) {
                    generateBasicField(section.has("titre") ? section.get("titre").asString() : "", sect, row,
                            cellStyle,
                            demande == null ? null : demande.getContenu(), header);
                }
            }
        }
    }

    private void generateBasicField(String nomSection, JsonNode jsonObject, Row row, CellStyle cellStyle,
            JsonNode node, boolean header) {
        String type = jsonObject.has("type") ? jsonObject.get("type").asString() : "";

        if ("champs".equals(type)) {
            ArrayNode champs =
                    jsonObject.has("champs") && jsonObject.get("champs").isArray() ? (ArrayNode) jsonObject.get(
                            "champs") : MAPPER.createArrayNode();
            for (JsonNode champ : champs) {
                Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
                cell.setCellStyle(cellStyle);
                String cellValue = getFieldValue(champ, node, header);
                if (header) {
                    cellValue = nomSection + " - " + cellValue;
                }
                cell.setCellValue(cellValue);
            }
        } else if ("tableau".equals(type)) {
            ArrayNode columns =
                    jsonObject.has("columns") && jsonObject.get("columns").isArray() ? (ArrayNode) jsonObject.get(
                            "columns") : MAPPER.createArrayNode();
            for (JsonNode column : columns) {
                Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
                cell.setCellStyle(cellStyle);
                if (header) {
                    cell.setCellValue(nomSection + " - " + column.get(LABEL).asString());
                } else {
                    JsonNode node0 = getNode(node, jsonObject, "path");
                    StringBuilder valeurColonne = new StringBuilder();
                    if (node0 instanceof ArrayNode) {
                        for (JsonNode elem : node0) {
                            String val = getFieldValue(column, elem, header);
                            if (StringUtils.isNotBlank(val)) {
                                if (!valeurColonne.isEmpty()) {
                                    valeurColonne.append(", ");
                                }
                                valeurColonne.append(val);
                            }
                        }
                    }
                    cell.setCellValue(valeurColonne.toString());
                }
            }
        }
    }


    private JsonNode getNode(JsonNode node, JsonNode champ, String ref) {
        if (node == null || champ == null || !champ.has(ref)) {
            return NullNode.getInstance();
        }

        String path = champ.get(ref).asString().replace(CONTENU, "/").replace(".", "/");
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return node.at(path);
    }


    private String buildAdresseHTML(JsonNode node, JsonNode  champ) {
        String ligne1 = getNode(node, champ, "ligne1").stringValue();
        String ligne2 = getNode(node, champ, "ligne2").stringValue();
        String ligne3 = getNode(node, champ, "ligne3").stringValue();
        StringBuilder ret = new StringBuilder();
        if (StringUtils.isNotEmpty(ligne1)) {
            ret.append(ligne1);
        }
        if (StringUtils.isNotBlank(ligne2)) {
            ret.append("\n").append(ligne2);
        }
        if (StringUtils.isNotBlank(ligne3)) {
            ret.append("\n").append(ligne3);
        }
        return ret.toString();
    }

    private String getFieldValue(JsonNode jsonObject, JsonNode node, boolean header) {
        String type = jsonObject.has("type") ? jsonObject.get("type").asString() : "";
        switch (type) {
            case "chaine", "texte" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    JsonNode node0 = getNode(node, jsonObject, "path");
                    if (node0 == null || node0 instanceof NullNode) {
                        return "";
                    } else {
                        return node0.asString();
                    }
                }
            }
            case "choix" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    String mapping = jsonObject.has("mapping") ? jsonObject.get("mapping").asString() : "";
                    if (Strings.CS.equals(mapping.toLowerCase(), "nationalites")) {
                        JsonNode node0 = getNode(node, jsonObject, "path");
                        if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asString())) {
                            return "";
                        } else {
                            return paysCache.get(node0.asString()).getNationalite();
                        }
                    } else if (Strings.CS.equals(mapping.toLowerCase(), "pays")) {
                        JsonNode node0 = getNode(node, jsonObject, "path");
                        if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asString())) {
                            return "";
                        } else {
                            return paysCache.get(node0.asString()).getLibelle();
                        }
                    } else if (mapping.toLowerCase().startsWith("properties_")) {
                        String path = jsonObject.has("path") ? jsonObject.get("path").asString().replace(CONTENU, "/")
                                                               .replace(".", "/") : "";
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                        JsonNode pathNode = node.at(path);
                        if (pathNode instanceof MissingNode) {
                            return "N/A";
                        }
                        String key = mapping.substring(11) + "_FR";
                        return propertiesService.getPropertyPourRecap(key, pathNode, true);
                    } else {
                        String path = jsonObject.has("path") ? jsonObject.get("path").asString().replace(CONTENU, "/")
                                                               .replace(".", "/") : "";
                        if (!path.startsWith("/")) {
                            path = "/" + path;
                        }
                        JsonNode pathNode = node.at(path);
                        if (pathNode instanceof MissingNode) {
                            return "N/A";
                        } else {
                            // Prise en compte valeur/valeurExtra
                            if (pathNode instanceof ObjectNode) {
                                JsonNode valeurNode = node.at(path + "/valeur");
                                if (valeurNode instanceof MissingNode || valeurNode instanceof NullNode || (
                                        valeurNode instanceof StringNode && valeurNode.stringValue().equals("AUTRE"))) {
                                    JsonNode node0 = node.at(path + "/valeurExtra");
                                    if (node0 == null || node0 instanceof NullNode) {
                                        return "";
                                    } else {
                                        return node0.stringValue();
                                    }
                                }
                            }

                            String enumField = pathNode.asString();
                            if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField)
                                    || enumField.equals("null")) {
                                return "";
                            }
                            return enumField;
                        }
                    }
                }
            }
            case "date" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    JsonNode node0 = getNode(node, jsonObject, "path");
                    if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asString())) {
                        return "";
                    } else {
                        LocalDateTime dateTime = LocalDateTime.parse(node0.asString(),
                                DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                        // Si la date a un format d'affichage
                        String format = jsonObject.has("displayJavaFormat") ? jsonObject.get("displayJavaFormat")
                                                                              .asString() : "";
                        if (StringUtils.isBlank(format)) {
                            format = AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT;
                        }
                        return dateTime.format(DateTimeFormatter.ofPattern(format));
                    }
                }
            }
            case "choixMultiple" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    JsonNode n = getNode(node, jsonObject, "path");
                    if (n instanceof ObjectNode) {
                        Iterator<Map.Entry<String, JsonNode>> it = n.properties().iterator();
                        StringBuilder ret = new StringBuilder();
                        while (it.hasNext()) {
                            Map.Entry<String, JsonNode> entry = it.next();
                            if (entry.getValue().asBoolean()) {
                                if (!ret.isEmpty()) {
                                    ret.append(", ");
                                }
                                ret.append(entry.getValue().asString());
                            }
                        }
                        return ret.toString();
                    }
                    return "";
                }
            }
            case "adresse" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    String ret = buildAdresseHTML(node, jsonObject);
                    if (StringUtils.isNotEmpty(ret)) {
                        String codePostal = getNode(node, jsonObject, "codePostal").stringValue();
                        String ville = getNode(node, jsonObject, "ville").stringValue();
                        String pays = getNode(node, jsonObject, "pays").stringValue();
                        ret += "\n" + codePostal + " " + ville;
                        if (StringUtils.isNotBlank(pays)) {
                            ret += "\n" + paysCache.get(pays).getLibelle();
                        }
                    }
                    return ret;
                }
            }
            case "adresseMc" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    return buildAdresseHTML(node, jsonObject);
                }
            }
            case "iban" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    String titulaire = getNode(node, jsonObject, "titulaire").stringValue();
                    String bic = getNode(node, jsonObject, "bic").stringValue();
                    String iban = getNode(node, jsonObject, "iban").stringValue();
                    return iban + " (Titulaire: " + titulaire + ", BIC: " + bic + ")";
                }
            }
            case "telephone" -> {
                if (header) {
                    return jsonObject.has(LABEL) ? jsonObject.get(LABEL).asString() : "";
                } else {
                    String indicatif = getNode(node, jsonObject, "indicatif").stringValue();
                    String numero = getNode(node, jsonObject, "numero").stringValue();
                    return AfBackUtils.genererTelephone(indicatif, numero);
                }
            }
            case null, default -> {
                LOGGER.error("ERREUR Type non pris en charge");
                return "ERREUR Type non pris en charge";
            }
        }
    }

    private void setCellStyle(CellStyle cellStyle, boolean header, int sectionIndex) {
        if (header) {
            cellStyle.setFillForegroundColor(colorListStr.get(sectionIndex % colorListStr.size()));
            cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
            cellStyle.setWrapText(true);
            cellStyle.setBorderTop(BorderStyle.THIN);
            cellStyle.setBorderBottom(BorderStyle.THIN);
            cellStyle.setBorderLeft(BorderStyle.THIN);
            cellStyle.setBorderRight(BorderStyle.THIN);
            cellStyle.setAlignment(HorizontalAlignment.CENTER);
            cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
        } else {
            cellStyle.setWrapText(true);
        }
    }
}
