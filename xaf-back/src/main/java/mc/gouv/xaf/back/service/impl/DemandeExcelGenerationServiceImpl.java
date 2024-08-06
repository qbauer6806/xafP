package mc.gouv.xaf.back.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.io.IOException;
import java.io.OutputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import mc.gouv.xaf.back.data.entity.DemandeConfigBO;
import mc.gouv.xaf.back.service.DemandeExcelGenerationService;
import mc.gouv.xaf.back.service.DemandeExcelRechercheProvider;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesConfigService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeExcelGenerationDTO;
import mc.gouv.xaf.shared.dto.ExcelRechercheDTO;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.BorderStyle;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.FillPatternType;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.VerticalAlignment;
import org.apache.poi.xssf.usermodel.DefaultIndexedColorMap;
import org.apache.poi.xssf.usermodel.XSSFCellStyle;
import org.apache.poi.xssf.usermodel.XSSFColor;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * 
 * Classe permettant de générer un fichier Excel à partir des fichiers Recap de la démarche, avec
 * une feuille Excel par buildId.
 * 
 * @author qdeme
 *
 */
@Component
public class DemandeExcelGenerationServiceImpl implements DemandeExcelGenerationService {
	
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeExcelGenerationServiceImpl.class);

    private static final String LABEL = "label";
    
    private static final String CONTENU = "contenu.";

    @Autowired
    private PaysCache paysCache;

    @Autowired
    private AfBackUtils afBackUtils;
    
    @Autowired
    private PropertiesService propertiesService;
    
    @Autowired
    private DemandesConfigService demandesConfigService;
    
    @Autowired
    private DemarchesDataProvider demarchesDataProvider;
    
    private static final List<byte[]> colorList = new ArrayList<>();
    
    static {
    	colorList.add(new byte[] { (byte)253, (byte)233, (byte)217 });
    	colorList.add(new byte[] { (byte)235, (byte)241, (byte)222 });
    	colorList.add(new byte[] { (byte)220, (byte)230, (byte)241 });
    	colorList.add(new byte[] { (byte)242, (byte)220, (byte)219 });
    	colorList.add(new byte[] { (byte)221, (byte)217, (byte)196 });
    	colorList.add(new byte[] { (byte)216, (byte)228, (byte)188 });
    	colorList.add(new byte[] { (byte)218, (byte)238, (byte)243 });
    }

	@Override
	public void generateExcel(ExcelRechercheDTO excelRechercheDto, DemandeExcelRechercheProvider demandesByDateRangeProvider, OutputStream outputStream)
            throws IOException, ParseException {

        LOGGER.info("Début de la génération de l'export Excel des demandes...");
        
        XSSFWorkbook workbook = new XSSFWorkbook();

		List<DemandeConfigBO> demandeConfigs = demandesConfigService.getConfigsBO();
        List<DemandeDTO> demandes = demandesByDateRangeProvider.getDemandes(excelRechercheDto);

		for (DemandeConfigBO demandeConfig : demandeConfigs) {
			String buildId = demandeConfig.getBuildId();
	        LOGGER.info("Chargement du fichier recap {}...", buildId);
			JsonNode sectionsNode = demandeConfig.getContenu().get("recap").get("sections");
			JSONParser jsonParser = new JSONParser();
			JSONArray sections = (JSONArray) jsonParser.parse(sectionsNode.toString());
	        String sheetName = buildId;
	        DemandeExcelGenerationDTO demandeExcelGenerationDto = demarchesDataProvider.getDemandeExcelGenerationDTO();
	        if (demandeExcelGenerationDto.getBuildIdNameMap() != null && demandeExcelGenerationDto.getBuildIdNameMap().get(buildId) != null) {
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
	        	if (buildId.equals(demande.getConfig().get("buildId").asText())) {
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
	        	row.setHeight((short)500);
	        }
	        headerRow.setHeight((short)900);
		}

		LOGGER.info("Ecriture du workbook...");
		workbook.write(outputStream);
		workbook.close();
	}
	
	private void writeRow(XSSFWorkbook workbook, JSONArray sections, Row row, DemandeDTO demande, boolean header) {
		// 2 premières colonnes, qui ne concernent pas le contenu de la demande
		if (header) {
    		Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
    		XSSFCellStyle cellStyle = workbook.createCellStyle();
    		setCellStyle(cellStyle, header, 0);
    		cell.setCellValue("Identifiant de la demande");
    		cell.setCellStyle(cellStyle);
    		cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
    		cell.setCellValue("Statut de la demande");
    		cell.setCellStyle(cellStyle);
		}
		else {
    		Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
    		XSSFCellStyle cellStyle = workbook.createCellStyle();
    		setCellStyle(cellStyle, header, 0);
    		cell.setCellValue(demande.getIdentifiant());
    		cell.setCellStyle(cellStyle);
    		cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
    		cell.setCellValue(afBackUtils.getStatutPublicOuInterne(demande).getLibelle());
    		cell.setCellStyle(cellStyle);
		}
		
		// Reste des colonnes, qui sont générées à partir du fichier Recap BO
        genererColonnes(workbook, sections, row, demande, header);
	}
	
	private void genererColonnes(XSSFWorkbook workbook, JSONArray sections, Row row, DemandeDTO demande, boolean header) {
		// Reste des colonnes, qui sont générées à partir du fichier Recap BO
		for (int i = 0; i < sections.size(); i++) {
			JSONObject section = (JSONObject) sections.get(i);
			String sectionType = (String) section.get("type");

			XSSFCellStyle cellStyle = workbook.createCellStyle();
			setCellStyle(cellStyle, header, i+1);

			if (!StringUtils.equals(sectionType, "sousSections")) {
				generateBasicField((String)section.get("titre"),
						section, row, cellStyle, demande == null ? null : demande.getContenu(), header);
			}
			else {
				JSONArray sousSections = ((JSONArray)section.get("sousSections"));
				for (Object sect : sousSections) {
					generateBasicField((String)section.get("titre"), (JSONObject) sect,  row, cellStyle, demande == null ? null : demande.getContenu(), header);
				}
			}
		}
	}
	
	private void generateBasicField(String nomSection, JSONObject jsonObject, Row row, CellStyle cellStyle, JsonNode node, boolean header) {
		String type = (String)jsonObject.get("type");
        if (StringUtils.equals(type, "champs")) {
            JSONArray champs = (JSONArray) jsonObject.get("champs");
            for (Object o : champs) {
                JSONObject champ = (JSONObject) o;

                Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
                cell.setCellStyle(cellStyle);
                String cellValue = getFieldValue(champ, node, header);
                if (header) {
                    cellValue = nomSection + " - " + cellValue;
                }
                cell.setCellValue(cellValue);
            }
        }
        else if ("tableau".equals(type)) {
        	JSONArray columns = ((JSONArray)jsonObject.get("columns"));
        	for (Object column : columns) {
        		Cell cell = row.createCell(row.getLastCellNum() == -1 ? 0 : row.getLastCellNum());
        		cell.setCellStyle(cellStyle);
        		if (header) {
    	    		cell.setCellValue(nomSection + " - " + ((JSONObject)column).get(LABEL));
        		}
        		else {
	        		JsonNode node0 = getNode(node, jsonObject, "path");
                    StringBuilder valeurColonne = new StringBuilder();
	        		if (node0 instanceof ArrayNode) {
                        for (JsonNode elem : node0) {
                            String val = getFieldValue((JSONObject) column, elem, header);
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
	
    private JsonNode getNode(JsonNode node, JSONObject champ, String ref) {
        String path = champ.get(ref).toString().replace(CONTENU, "/").replace(".", "/");
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        return node.at(path);
    }
    
    private String buildAdresseHTML(JsonNode node, JSONObject champ) {
        String ligne1 = getNode(node, champ, "ligne1").textValue();
        String ligne2 = getNode(node, champ, "ligne2").textValue();
        String ligne3 = getNode(node, champ, "ligne3").textValue();
        String ret = "";
        if (StringUtils.isNotEmpty(ligne1)) {
            ret = ligne1;
        }
        if (StringUtils.isNotBlank(ligne2)) {
            ret += "\n" + ligne2;
        }
        if (StringUtils.isNotBlank(ligne3)) {
            ret += "\n" + ligne3;
        }
        return ret;
    }
    
    private String getFieldValue(JSONObject jsonObject, JsonNode node, boolean header) {
		String type = (String)jsonObject.get("type");
        if ("chaine".equals(type) || "texte".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
    			JsonNode node0 = getNode(node, jsonObject, "path");
                if (node0 == null || node0 instanceof NullNode) {
                	return "";
                }
                else {
                	return node0.asText();
                }
    		}
        }
        else if ("choix".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
                String mapping = jsonObject.get("mapping").toString();
                if (StringUtils.equals(mapping.toLowerCase(), "nationalites")) {
                    JsonNode node0 = getNode(node, jsonObject, "path");
                    if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                    	return "";
                    }
                    else {
                    	return paysCache.get(node0.asText(), "fr").getNationalite();
                    }
                }
                else if (StringUtils.equals(mapping.toLowerCase(), "pays")) {
                    JsonNode node0 = getNode(node, jsonObject, "path");
                    if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                    	return "";
                    }
                    else {
                    	return paysCache.get(node0.asText(), "fr").getNom();
                    }
                } else if (mapping.toLowerCase().startsWith("properties_")) {
                    String path = jsonObject.get("path").toString().replace(CONTENU, "/").replace(".", "/");
                    if (path.charAt(0) != '/') {
                        path = "/" + path;
                    }
                    JsonNode pathNode = node.at(path);
                    if (pathNode instanceof MissingNode) {
                    	return "N/A";
                    }
                	String key = mapping.substring(11) + "_FR";
					return propertiesService.getPropertyPourRecap(key, pathNode, true);
                } else {
                    String path = jsonObject.get("path").toString().replace(CONTENU, "/").replace(".", "/");
                    if (path.charAt(0) != '/') {
                        path = "/" + path;
                    }
                    JsonNode pathNode = node.at(path);
                    if (pathNode instanceof MissingNode) {
                    	return "N/A";
                    }
                    else {
	                    // Prise en compte valeur/valeurExtra
	                    if (pathNode instanceof ObjectNode) {
	                        pathNode = node.at(path + "/valeur");
	                        if (pathNode instanceof MissingNode || pathNode instanceof NullNode
	                                || (pathNode instanceof TextNode && pathNode.textValue().equals("AUTRE"))) {
	                            JsonNode node0 = node.at(path + "/valeurExtra");
	                            if (node0 == null || node0 instanceof NullNode) {
	                            	return "";
	                            }
	                            else {
	                            	return node0.textValue();
	                            }
	                        }
	                    }
	
	                    String enumField = pathNode.asText();
	                    if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField)
	                            || enumField.equals("null")) {
	                    	return "";
	                    }
	                    else {
		                    mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
		                    Class<?> klass;
							try {
								// todo enum
								return "";
//								klass = Class.forName(pojo + mapping + "Enum");
//			                    Object value = klass.getMethod("forValue", String.class).invoke(klass, enumField);
//			                    return value != null ? value.toString() : enumField;
							} catch (Exception e) {
								LOGGER.error("Erreur lors de la récupération d'un champ d'Enum", e);
								return "ERREUR";
							}
	                    }
                    }
                }
    		}
        }
        else if ("date".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
                JsonNode node0 = getNode(node, jsonObject, "path");
                if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                	return "";
                }
                else {
	                LocalDateTime dateTime = LocalDateTime.parse(node0.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
	                // Si la date a un format d'affichage
	                String format = (String) jsonObject.get("displayJavaFormat");
	                if (StringUtils.isBlank(format)) {
	                    format = AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT;
	                }
	                return dateTime.format(DateTimeFormatter.ofPattern(format));
                }
    		}
        }
        else if ("choixMultiple".equals(type)) {
        	if (header) {
        		return (String)jsonObject.get(LABEL);
        	}
        	else {
                JsonNode n = getNode(node, jsonObject, "path");
                if (n instanceof ObjectNode list) {
                    Iterator<Map.Entry<String, JsonNode>> it = list.fields();
                    StringBuilder ret = new StringBuilder();
                    String mapping = jsonObject.get("mapping").toString();
                    while (it.hasNext()) {
                        Map.Entry<String, JsonNode> entry = it.next();
                        if (entry.getValue().asBoolean()) {
                            mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
                            try {
								// todo enum
								//ret = " ";
//								klass = Class.forName(pojo + mapping + "Enum");
//	                            Object[] parameters = {entry.getKey().toUpperCase(), true};
//	                            Object value = klass.getMethod("forValue", String.class, boolean.class).invoke(klass, parameters);
//	                            LOGGER.debug("n={}, path={}, klass={}, parameters={}, value={}", n, jsonObject.get("path"), klass, parameters, value);
//	                            if (!ret.isEmpty()) {
//	                                ret += ", ";
//	                            }
//	                            ret += value.toString();
							} catch (Exception e) {
								ret.append("ERREUR");
								LOGGER.error("Erreur lors de la récupération d'un champ d'Enum", e);
							}
                        }
                    }
                    return ret.toString();
                }
                else {
                	return "";
                }
        	}
        }
        else if ("adresse".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
	           String ret = buildAdresseHTML(node, jsonObject);
	            if (StringUtils.isNotEmpty(ret)) {
	                String codePostal = getNode(node, jsonObject, "codePostal").textValue();
	                String ville = getNode(node, jsonObject, "ville").textValue();
	                String pays = getNode(node, jsonObject, "pays").textValue();
	                ret += "\n" + codePostal + " " + ville;
	                if (StringUtils.isNotBlank(pays)) {
	                    ret += "\n" + paysCache.get(pays, "fr").getNom();
	                }
	            }
	            return ret;
    		}
        }
        else if ("adresseMc".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
    			return buildAdresseHTML(node, jsonObject);
    		}
        }
        else if ("iban".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
                String titulaire = getNode(node, jsonObject, "titulaire").textValue();
                String bic = getNode(node, jsonObject, "bic").textValue();
                String iban = getNode(node, jsonObject, "iban").textValue();
                return iban + " (Titulaire: " + titulaire + ", BIC: " + bic + ")";
    		}
        }
        else if ("telephone".equals(type)) {
    		if (header) {
	    		return (String)jsonObject.get(LABEL);
    		}
    		else {
                String indicatif = getNode(node, jsonObject, "indicatif").textValue();
                String numero = getNode(node, jsonObject, "numero").textValue();
                StringBuilder indicateurBuilder = new StringBuilder();
                if (StringUtils.isNotBlank(indicatif)) {
                    indicateurBuilder.append("(").append(AfBackUtils.convertTelIndicateur(indicatif)).append(") ");
                }
                if (StringUtils.isNotBlank(numero)) {
                    indicateurBuilder.append(numero);
                }
                return indicateurBuilder.toString();
    		}
        }
        else {
        	LOGGER.error("ERREUR Type non pris en charge");
        	return "ERREUR Type non pris en charge";
        }
	}
    
	private void setCellStyle(XSSFCellStyle cellStyle, boolean header, int sectionIndex) {
		if (header) {
        	cellStyle.setFillForegroundColor(getColorFromSectionIndex(sectionIndex+1));
        	cellStyle.setFillPattern(FillPatternType.SOLID_FOREGROUND);
        	cellStyle.setWrapText(true);
        	cellStyle.setBorderTop(BorderStyle.THIN);
        	cellStyle.setBorderBottom(BorderStyle.THIN);
        	cellStyle.setBorderLeft(BorderStyle.THIN);
        	cellStyle.setBorderRight(BorderStyle.THIN);
        	cellStyle.setAlignment(HorizontalAlignment.CENTER);
        	cellStyle.setVerticalAlignment(VerticalAlignment.CENTER);
		}
		else {
			cellStyle.setWrapText(true);
		}
	}
	
	private XSSFColor getColorFromSectionIndex(int sectionIndex) {
		return new XSSFColor(colorList.get(sectionIndex % colorList.size()), new DefaultIndexedColorMap());
	}

}
