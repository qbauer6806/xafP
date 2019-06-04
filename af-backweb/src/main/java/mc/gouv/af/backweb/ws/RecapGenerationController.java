package mc.gouv.af.backweb.ws;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mc.gouv.af.back.cache.PaysCacheImpl;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.DemandesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.xboot.config.web.annotation.GouvRestController;

/**
 * 
 * @author qdeme
 *
 */
@GouvRestController
@RequestMapping(value = "/ws/recap")
public class RecapGenerationController {

	private static final Logger LOGGER = LoggerFactory.getLogger(RecapGenerationController.class);
	
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");
	
	@Autowired
	private PaysCacheImpl paysCache;
	
	@Autowired
	private DemandesService demandesService;
	
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
	
    @RequestMapping(value = "/{pkDemande}", method = RequestMethod.GET, produces = "text/html")
    public @ResponseBody String getRecap(@PathVariable(value = "pkDemande") Integer pkDemande) throws Exception {

        LOGGER.info("======================= Appel de /ws/recap/" + pkDemande);

        DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), pkDemande);
        
        String ret = "";
        
        if (demande != null) {
        	ret = getHTML(demande);
        }

        LOGGER.info("======================= Fin appel de /ws/recap/" + pkDemande);

        return ret;

    }
	
	public String getHTML(DemandeDTO demande) throws IOException, ParseException, ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		
		LOGGER.info("Chargement du fichier recap...");
		InputStream inputStream = new ClassPathResource("/recaps/" + "recaps_" + demande.getBuildId() + ".json").getInputStream();
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArray = (JSONArray)jsonParser.parse(
		      new InputStreamReader(inputStream, "UTF-8"));
		
		LOGGER.info("Construction du recap HTML...");
		String html = "";
		
		for (int k = 0; k < jsonArray.size(); k++) {
			if ("projectDemandeRecap".equals(((JSONObject)jsonArray.get(k)).get("name"))) {
				JSONArray sections = (JSONArray)((JSONObject)jsonArray.get(k)).get("sections");
				for (int i = 0; i < sections.size(); i++) {
					JSONObject section = (JSONObject)sections.get(i);
					System.out.println(section);
					
					html += "<div class=\"sectiondemande\"><h3>" + section.get("titre") + "</h3><dl>";
					
					String sectionType = (String)section.get("type");
					if (sectionType.equals("champs")) {
						JSONArray champs = (JSONArray)section.get("champs");
						for (int j = 0; j < champs.size(); j++) {
							JSONObject champ = (JSONObject)champs.get(j);
							String type = (String)champ.get("type");
							if (type.equals("adresse")) {
								html += getHTML(demande.getContenu(), champ, demande.getBuildId());
							}
							else {
								String value = getHTML(demande.getContenu(), champ, demande.getBuildId());
								if (!StringUtils.isBlank(value)) {
									html += "<dd><span>"+ champ.get("label") + "</span></dd>";
									html += "<dt><span>" + value + "</span></dt>";
								}
							}
						}
					}
					else if (sectionType.equals("tableau")) {
						html += "<table id=\"datatable-demandes\" class=\"table table-striped\">";
						JSONArray columns = (JSONArray)section.get("columns");
						html += "<thead><tr>";
						for (Object column : columns.toArray()) {
							html += "<th>" + ((JSONObject)column).get("label") + "</th>";
						}
						html += "</tr></thead>";
						ArrayNode valeurs = (ArrayNode)getNode(demande.getContenu(),section,"path");
						Iterator<JsonNode> it = valeurs.elements();
						html += "<tbody>";
						while (it.hasNext()) {
							JsonNode valeur = it.next();
							html += "<tr>";
							for (Object column : columns.toArray()) {
								String value = getHTML(valeur,(JSONObject)column, demande.getBuildId());
								html += "<td>" + (value == null ? "" : value) + "</td>";
							}
							html += "</tr>";
						}
						html += "</tbody>";
						html += "</table>";
					}
					else if (sectionType.equals("adresse")) {
						html += "<dd><span>Adresse</span></dd>";
					}
					
					html += "</dl></div>";
				}
			}
		}
		
		return html;
	}
	
	private String getHTML(JsonNode node, JSONObject champ, String buildId) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {
		String type = (String)champ.get("type");
		if (type.equals("chaine") || type.equals("texte")) {
			JsonNode node0 = getNode(node,champ,"path");
			if (node0 == null || node0 instanceof NullNode) {
				return null;
			}
			return node0.asText();
		}
		else if (type.equals("choix")) {
			String mapping = champ.get("mapping").toString();
			if (mapping.equals("nationalites")) {
				JsonNode node0 = getNode(node,champ,"path");
				if (node0 == null || node0 instanceof NullNode) {
					return null;
				}
				return paysCache.get(node0.asText(), "fr").getNationalite();
			}
			else {
				String path = champ.get("path").toString().replace("contenu.", "/").replace(".", "/");
				if (path.charAt(0) != '/') {
					path = "/" + path;
				}
				JsonNode pathNode = node.at(path);
				if (pathNode instanceof MissingNode) {
					return "N/A";
				}
				
				// Prise en compte valeur/valeurExtra
				if (pathNode instanceof ObjectNode) {
					pathNode = node.at(path+"/valeur");
					if (pathNode instanceof MissingNode || pathNode instanceof NullNode || (pathNode instanceof TextNode && ((TextNode)pathNode).textValue().equals("AUTRE"))) {
						JsonNode node0 = node.at(path+"/valeurExtra");
						if (node0 == null || node0 instanceof NullNode) {
							return null;
						}
						return ((TextNode)node0).textValue();
					}
				}
				
				String enumField = pathNode.asText();
				if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField) || enumField.equals("null")) {
					return null;
				}
				
				mapping = mapping.substring(0,1).toUpperCase() + mapping.substring(1);
				Class<?> klass = Class.forName("mc.gouv." + gouvPropertiesResolver.getDemarcheId().toLowerCase() + ".shared.model.v" + buildId + "." + mapping + "Enum");
				Object value = klass.getMethod("forValue", String.class).invoke(klass,enumField);
				return value.toString();
			}
		}
		else if (type.equals("date")) {
			JsonNode node0 = getNode(node,champ,"path");
			if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
				return null;
			}
			Date date = new Date(OffsetDateTime.parse(node0.asText()).toInstant().toEpochMilli());
			return dateFormat.format(date);
		}
		else if (type.equals("choixMultiple")) {
			ObjectNode list = (ObjectNode)getNode(node,champ,"path");
			Iterator<Map.Entry<String, JsonNode>> it = list.fields();
			String ret = "";
			String mapping = champ.get("mapping").toString();
			while (it.hasNext()) {
				Map.Entry<String, JsonNode> entry = it.next();
				if (((BooleanNode)entry.getValue()).asBoolean()) {
					mapping = mapping.substring(0,1).toUpperCase() + mapping.substring(1);
					Class<?> klass = Class.forName("mc.gouv." + gouvPropertiesResolver.getDemarcheId().toLowerCase() + ".shared.model.v" + buildId + "." + mapping + "Enum");
					Object[] parameters = { entry.getKey().toUpperCase(), true };
					Object value = klass.getMethod("forValue", String.class, boolean.class).invoke(klass,parameters);
					if (!ret.equals("")) {
						ret += ", ";
					}
					ret += value.toString();
				}
			}
			return ret;
		}
		else if (type.equals("adresse")) {
			String ligne1 = getNode(node,champ,"ligne1").textValue();
			String ligne2 = getNode(node,champ,"ligne2").textValue();
			String ligne3 = getNode(node,champ,"ligne3").textValue();
			String ret = "<dd><span>Adresse</span></dd><dt><span>" + ligne1 + "</span>";
			if (StringUtils.isNotBlank(ligne2)) {
				ret += "<br><span>" + ligne2 + "</span>";
			}
			if (StringUtils.isNotBlank(ligne3)) {
				ret += "<br><span>" + ligne3 + "</span>";
			}
			ret += "</dt>";
			String codePostal = getNode(node,champ,"codePostal").textValue();
			String ville = getNode(node,champ,"ville").textValue();
			ret += "<dd><span>Ville</span></dd><dt><span>" + codePostal + " " + ville + "</span></dt>";
			String pays = getNode(node,champ,"pays").textValue();
			ret += "<dd><span>Pays</span></dd><dt><span>" + paysCache.get(pays, "fr").getNom() + "</span></dt>";
			return ret;
		}
		else {
			return type;
		}
	}
	
	private JsonNode getNode(JsonNode node, JSONObject champ, String ref) {
		String path = champ.get(ref).toString().replace("contenu.", "/").replace(".", "/");
		if (path.charAt(0) != '/') {
			path = "/" + path;
		}
		return node.at(path);
		
	}
}
