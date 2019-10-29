package mc.gouv.xaf.back.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.BooleanNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.logon.apiclient.RestException;

/**
 * Service permettant de générer une page HTML contenant le récapitulatif d'une
 * demande.
 *
 * @author qdeme
 * @author mboutelier.ext
 *
 */
@Component
public class DemandeRecapHTMLServiceImpl implements DemandeRecapHTMLService {

	private static final Logger LOGGER = LoggerFactory.getLogger(DemandeRecapHTMLServiceImpl.class);

	private static SimpleDateFormat dateHeureFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
	private static SimpleDateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy");

	@Autowired
	private PaysCache paysCache;

	@Autowired
	private GouvPropertiesResolver gouvPropertiesResolver;

	@Autowired
	private AfBackUtils afBackUtils;

	@Autowired
	private MotifsCache motifsCache;

	@Override
	public String getHTMLDemandeGeneric(DemandeDTO demande) {
		StringBuilder htmlBuilder = new StringBuilder();

		// Numéro de la demande
		htmlBuilder.append("<dl><dd><span>Numéro de la demande</span></dd><dt><span>");
		htmlBuilder.append(demande.getIdentifiant());
		htmlBuilder.append("</span></dt></dl>");

		// Date de transmission/dépôt
		boolean isVirtuel = demande.getCanal() == DemandeCanalEnum.GUICHET_VIRTUEL;
		htmlBuilder.append("<dl><dd><span>Date de ");
		htmlBuilder.append(isVirtuel ? "transmission" : "dépôt");
		htmlBuilder.append("</span></dd><dt><span>");
		Date dateCreation = isVirtuel ? demande.getDateCreation() : demande.getCourrierDateReception();
		htmlBuilder.append(dateHeureFormat.format(dateCreation));
		htmlBuilder.append("</span></dt></dl>");

		// Etat de la demande
		htmlBuilder.append("<dl><dd><span>Etat de la demande</span></dd><dt><span>");
		htmlBuilder.append(afBackUtils.getStatusLibelleFromName(demande.getDernierStatut().getLibelle()));
		htmlBuilder.append(" le ");
		htmlBuilder.append(dateHeureFormat.format(demande.getDernierStatut().getDate()));
		htmlBuilder.append("</span></dt></dl>");

		// Langue
		htmlBuilder.append("<dl><dd><span>Langue</span></dd><dt><span>");
		htmlBuilder.append(demande.getLangue());
		htmlBuilder.append("</span></dt></dl>");

		// Canal
		htmlBuilder.append("<dl><dd><span>Canal</span></dd><dt><span>");
		htmlBuilder.append(demande.getCanal());
		htmlBuilder.append("</span></dt></dl>");

		return htmlBuilder.toString();
	}

	public String getHTMLDemandeComplements(DemandeDTO demande) throws RestException {
		StringBuilder htmlBuilder = new StringBuilder();

		for (DemandeComplementsDTO complement : demande.getComplements()) {
			DemandeComplementsQuestionDTO question = complement.getQuestion();
			DemandeComplementsReponseDTO reponse = complement.getReponse();
			String date = dateHeureFormat.format(question.getDate());

			htmlBuilder.append("<h3>Compléments du ");
			htmlBuilder.append(date);
			htmlBuilder.append("</h3>");

			htmlBuilder.append("<div class=\"dem-admin\">");
			htmlBuilder.append("<span>Demande de l'administration</span>");

			// Date de création
			htmlBuilder.append("<dl><dd><span>Date création</span></dd><dt><span>");
			htmlBuilder.append(date);
			htmlBuilder.append("</span></dt></dl>");

			// Motif
			htmlBuilder.append("<dl><dd><span>Motif</span></dd><dt><span>");
			htmlBuilder.append(motifsCache.getMotif(question.getCodeMotif(), "fr").getLibelle());
			htmlBuilder.append("</span></dt></dl>");

			// Texte
			htmlBuilder.append("<dl><dd><span>Texte</span></dd><dt><span class=\"display-commentaire\">");
			htmlBuilder.append(question.getTexte());
			htmlBuilder.append("</span></dt></dl>");

			// Agent
			htmlBuilder.append("<dl><dd><span>Agent</span></dd><dt><span>");
			htmlBuilder.append(afBackUtils.getUserNameFromID(question.getAgentId()));
			htmlBuilder.append("</span></dt></dl>");

			htmlBuilder.append("</div><div class=\"rep-usager\">");
			htmlBuilder.append("<span>Réponse de l'usager</span>");

			// Date
			htmlBuilder.append("<dl><dd><span>Date</span></dd><dt><span>");
			htmlBuilder.append(dateHeureFormat.format(reponse.getDate()));
			htmlBuilder.append("</span></dt></dl>");

			// Texte
			htmlBuilder.append("<dl><dd><span>Texte</span></dd><dt><span class=\"display-commentaire\">");
			htmlBuilder.append(reponse.getTexte());
			htmlBuilder.append("</span></dt></dl></div>");
		}

		return htmlBuilder.toString();
	}

	@Override
	public String getHTMLDemandeContenuRecap(DemandeDTO demande, boolean isPdfRecap)
			throws IOException, ParseException, ClassNotFoundException, IllegalAccessException,
			IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

		LOGGER.info("Chargement du fichier recap...");
		InputStream inputStream = new ClassPathResource("/recaps/" + "recaps_" + demande.getBuildId() + ".json")
				.getInputStream();
		JSONParser jsonParser = new JSONParser();
		JSONArray jsonArray = (JSONArray) jsonParser.parse(new InputStreamReader(inputStream, "UTF-8"));

		LOGGER.info("Construction du recap HTML...");
		StringBuilder html = new StringBuilder();

		for (int k = 0; k < jsonArray.size(); k++) {
			if ("projectDemandeRecap".equals(((JSONObject) jsonArray.get(k)).get("name"))) {
				JSONArray sections = (JSONArray) ((JSONObject) jsonArray.get(k)).get("sections");
				for (int i = 0; i < sections.size(); i++) {
					JSONObject section = (JSONObject) sections.get(i);
					String sectionType = (String) section.get("type");

					if (!sectionType.equals("sousSections")) {
						html.append(generateSectionHTML(section, sectionType, demande, isPdfRecap));
					} else {
						html.append(generateSectionAndSousSection(section, sectionType, demande, isPdfRecap));
					}
					html.append("</dl></div>");
				}
			}
		}

		return html.toString();
	}

	private String generateSectionHTML(JSONObject section, String sectionType, DemandeDTO demande, boolean isPdfRecap)
			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		String html = "<div class=\"sectiondemande\"><h3>" + section.get("titre") + "</h3><dl>";
		html = getFirstLevelHTML(html, demande, sectionType, section, isPdfRecap);

		if (sectionType.equals("adresse")) {
			html += "<dd><span>Adresse</span></dd>";
		}
		return html;
	}

	private String generateSectionAndSousSection(JSONObject section, String sectionType , DemandeDTO demande, boolean isPdfRecap)
			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		String html = "";
		JSONArray sousSections = (JSONArray) section.get("sousSections");
		if (sousSections.toArray().length > 0) {
			html += "<div class=\"sectiondemande\"><h3>" + section.get("titre") + "</h3><dl>";
			html = getFirstLevelHTML(html, demande, sectionType, section, isPdfRecap);
			for (Object sousSection : sousSections.toArray()) {
				String sousSectionType = (String) ((JSONObject) sousSection).get("type");
				String introHtml = (String) ((JSONObject) sousSection).get("introHtml");
				html += (introHtml != null)? introHtml : "";
				html = getFirstLevelHTML(html, demande, sousSectionType, (JSONObject) sousSection, isPdfRecap);
			}
		}
		return html;
	}

	private String getFirstLevelHTML(String html, DemandeDTO demande, String sectionType, JSONObject section, boolean isPdfRecap)
			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		if (sectionType.equals("champs")) {
			JSONArray champs = (JSONArray) section.get("champs");
			for (int j = 0; j < champs.size(); j++) {
				JSONObject champ = (JSONObject) champs.get(j);
				String type = (String) champ.get("type");
				if (type.equals("adresse")) {
					html += getSecondLevelHTML(demande.getContenu(), champ, demande.getBuildId());
				} else {
					String value = getSecondLevelHTML(demande.getContenu(), champ, demande.getBuildId());
					if (!StringUtils.isBlank(value)) {
						if (isPdfRecap && value.contains("\n")) {
							html += "<div class=\"long-text\"><p class=\"long-text-title\">" + champ.get("label")
									+ "</p>";
							html += "<p class=\"long-text-content\">" + value + "</p></div>";
						} else {
							html += "<dd><span>" + champ.get("label") + "</span></dd>";
							html += "<dt><span>" + value + "</span></dt>";
						}
					}
				}
			}
		} else if (sectionType.equals("tableau")) {
			ArrayNode valeurs = (ArrayNode) getNode(demande.getContenu(), section, "path");
			if (valeurs.size() > 0) {
				html += "<table id=\"datatable-demandes\" class=\"table table-striped\">";
				JSONArray columns = (JSONArray) section.get("columns");
				html += "<thead><tr>";
				for (Object column : columns.toArray()) {
					html += "<th>" + ((JSONObject) column).get("label") + "</th>";
				}
				html += "</tr></thead>";
				Iterator<JsonNode> it = valeurs.elements();
				html += "<tbody>";
				while (it.hasNext()) {
					JsonNode valeur = it.next();
					html += "<tr>";
					for (Object column : columns.toArray()) {
						String value = getSecondLevelHTML(valeur, (JSONObject) column, demande.getBuildId());
						html += "<td>" + (value == null ? "" : value) + "</td>";
					}
					html += "</tr>";
				}
				html += "</tbody>";
				html += "</table>";
			}
		}
		return html;
	}

	private String getSecondLevelHTML(JsonNode node, JSONObject champ, String buildId)
			throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
			NoSuchMethodException, SecurityException {
		String type = (String) champ.get("type");
		if (type.equals("chaine") || type.equals("texte")) {
			JsonNode node0 = getNode(node, champ, "path");
			if (node0 == null || node0 instanceof NullNode) {
				return null;
			}
			return escape(node0.asText());
		} else if (type.equals("choix")) {
			String mapping = champ.get("mapping").toString();
			if (mapping.equals("nationalites")) {
				JsonNode node0 = getNode(node, champ, "path");
				if (node0 == null || node0 instanceof NullNode) {
					return null;
				}
				return paysCache.get(node0.asText(), "fr").getNationalite();
			} else {
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
					pathNode = node.at(path + "/valeur");
					if (pathNode instanceof MissingNode || pathNode instanceof NullNode
							|| (pathNode instanceof TextNode && ((TextNode) pathNode).textValue().equals("AUTRE"))) {
						JsonNode node0 = node.at(path + "/valeurExtra");
						if (node0 == null || node0 instanceof NullNode) {
							return null;
						}
						return escape(((TextNode) node0).textValue());
					}
				}

				String enumField = pathNode.asText();
				if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField)
						|| enumField.equals("null")) {
					return null;
				}

				mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
				Class<?> klass = Class.forName("mc.gouv." + gouvPropertiesResolver.getDemarcheId().toLowerCase()
						+ ".shared.model.v" + buildId + "." + mapping + "Enum");
				Object value = klass.getMethod("forValue", String.class).invoke(klass, enumField);
				return value.toString();
			}
		} else if (type.equals("date")) {
			JsonNode node0 = getNode(node, champ, "path");
			if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
				return null;
			}
			Date date = new Date(OffsetDateTime.parse(node0.asText()).toInstant().toEpochMilli());
			return dateFormat.format(date);
		} else if (type.equals("choixMultiple")) {
			ObjectNode list = (ObjectNode) getNode(node, champ, "path");
			Iterator<Map.Entry<String, JsonNode>> it = list.fields();
			String ret = "";
			String mapping = champ.get("mapping").toString();
			while (it.hasNext()) {
				Map.Entry<String, JsonNode> entry = it.next();
				if (((BooleanNode) entry.getValue()).asBoolean()) {
					mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
					Class<?> klass = Class.forName("mc.gouv." + gouvPropertiesResolver.getDemarcheId().toLowerCase()
							+ ".shared.model.v" + buildId + "." + mapping + "Enum");
					Object[] parameters = { entry.getKey().toUpperCase(), true };
					Object value = klass.getMethod("forValue", String.class, boolean.class).invoke(klass, parameters);
					if (!ret.equals("")) {
						ret += ", ";
					}
					ret += value.toString();
				}
			}
			return ret;
		} else if (type.equals("adresse")) {
			String ligne1 = escape(getNode(node, champ, "ligne1").textValue());
			String ligne2 = escape(getNode(node, champ, "ligne2").textValue());
			String ligne3 = escape(getNode(node, champ, "ligne3").textValue());
			String ret = "";
			if (StringUtils.isNotEmpty(ligne1)) {
				ret = "<dd><span>Adresse</span></dd><dt><span>" + ligne1 + "</span>";
				if (StringUtils.isNotBlank(ligne2)) {
					ret += "<br><span>" + ligne2 + "</span>";
				}
				if (StringUtils.isNotBlank(ligne3)) {
					ret += "<br><span>" + ligne3 + "</span>";
				}
				ret += "</dt>";
				String codePostal = escape(getNode(node, champ, "codePostal").textValue());
				String ville = escape(getNode(node, champ, "ville").textValue());
				ret += "<dd><span>Ville</span></dd><dt><span>" + codePostal + " " + ville + "</span></dt>";
				String pays = getNode(node, champ, "pays").textValue();
				ret += "<dd><span>Pays</span></dd><dt><span>" + paysCache.get(pays, "fr").getNom() + "</span></dt>";
			}
			return ret;
		} else if (type.equals("iban")) {
			String titulaire = escape(getNode(node, champ, "titulaire").textValue());
			String bic = escape(getNode(node, champ, "bic").textValue());
			String iban = escape(getNode(node, champ, "iban").textValue());
			String ret = iban + " (Titulaire: " + titulaire + ", BIC: " + bic + ")";
			return ret;
		} else {
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

	private String escape(String str) {
		return StringEscapeUtils.escapeHtml4(str);
	}

}
