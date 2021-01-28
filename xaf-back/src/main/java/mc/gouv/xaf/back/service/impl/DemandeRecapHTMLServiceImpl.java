package mc.gouv.xaf.back.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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
import org.springframework.web.util.HtmlUtils;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mc.gouv.logon.apiclient.RestException;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.rest.PaysCache;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * Service permettant de générer une page HTML contenant le récapitulatif d'une
 * demande.
 *
 * @author qdeme
 * @author mboutelier.ext
 */
@Component
public class DemandeRecapHTMLServiceImpl implements DemandeRecapHTMLService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeRecapHTMLServiceImpl.class);

    private static final String CONTENU_DTO = "ContenuProjectDemandeDTO";

    @Autowired
    private PaysCache paysCache;

    @Autowired
    private AfBackUtils afBackUtils;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    private MotifsCache motifsCache;
    
    @Autowired
    private PropertiesService propertiesService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public String getHTMLDemandeGeneric(DemandeDTO demande) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Numéro de la demande
        htmlBuilder.append("<dl><dt><span>Numéro de la demande</span></dt><dd><span>");
        htmlBuilder.append(escape(demande.getIdentifiant(), true));
        htmlBuilder.append("</span></dd>");

        // Date de transmission/dépôt
        boolean isVirtuel = demande.getCanal() == DemandeCanalEnum.GUICHET_VIRTUEL;
        htmlBuilder.append("<dt><span>Date de ");
        htmlBuilder.append(isVirtuel ? "transmission" : "dépôt");
        htmlBuilder.append("</span></dt><dd><span>");
        Date dateCreation = isVirtuel ? demande.getDateCreation() : demande.getCourrierDateReception();
        htmlBuilder.append(AfBackUtils.SDF_JJ_MM_AAAA_HH_MM.format(dateCreation));
        htmlBuilder.append("</span></dd>");

        // Etat de la demande
        htmlBuilder.append("<dt><span>État de la demande</span></dt><dd><span>");
        htmlBuilder.append(afBackUtils.getStatusLibelleFromName(demande.getDernierStatut().getLibelle()));
        htmlBuilder.append(" le ");
        htmlBuilder.append(AfBackUtils.SDF_JJ_MM_AAAA_HH_MM.format(demande.getDernierStatut().getDate()));
        htmlBuilder.append("</span></dd>");

        // Langue
        htmlBuilder.append("<dt><span>Langue</span></dt><dd><span>");
        htmlBuilder.append(escape(demande.getLangue(), true));
        htmlBuilder.append("</span></dd>");

        // Canal
        htmlBuilder.append("<dt><span>Canal</span></dt><dd><span>");
        htmlBuilder.append(demande.getCanal());
        htmlBuilder.append("</span></dd></dl>");

        return htmlBuilder.toString();
    }

    public String getHTMLDemandeComplements(DemandeDTO demande) throws RestException {
        StringBuilder htmlBuilder = new StringBuilder();

        for (DemandeComplementsDTO complement : demande.getComplements()) {
            DemandeComplementsQuestionDTO question = complement.getQuestion();
            DemandeComplementsReponseDTO reponse = complement.getReponse();
            String date = AfBackUtils.SDF_JJ_MM_AAAA_HH_MM.format(question.getDate());

            htmlBuilder.append("<h3>Compléments du ");
            htmlBuilder.append(date);
            htmlBuilder.append("</h3>");

            htmlBuilder.append("<div class=\"dem-admin\">");
            htmlBuilder.append("<span>Demande de l'administration</span>");

            // Date de création
            htmlBuilder.append("<dl><dt><span>Date création</span></dt><dd><span>");
            htmlBuilder.append(date);
            htmlBuilder.append("</span></dd>");

            // Motif
            htmlBuilder.append("<dt><span>Motif</span></dt><dd><span>");
            htmlBuilder.append(escape(motifsCache.getMotif(question.getCodeMotif(), "fr").getLibelle(), true));
            htmlBuilder.append("</span></dd>");

            // Texte
            htmlBuilder.append("<dt><span>Texte</span></dt><dd><span>");
            htmlBuilder.append(escape(question.getTexte(), true));
            htmlBuilder.append("</span></dd>");

            // Agent
            htmlBuilder.append("<dt><span>Agent</span></dt><dd><span>");
            htmlBuilder.append(escape(utilisateursUtils.getUserNameFromID(question.getAgentId()), true));
            htmlBuilder.append("</span></dd></dl></div>");

            if (null != reponse) {
                htmlBuilder.append("<div class=\"rep-usager\">");
                htmlBuilder.append("<span>Réponse de l'usager</span>");
                // Date
                Date reponseDate = reponse.getDate();
                htmlBuilder.append("<dl><dt><span>Date</span></dt><dd><span>");
                if (null != reponseDate) {
                    htmlBuilder.append(AfBackUtils.SDF_JJ_MM_AAAA_HH_MM.format(reponseDate));
                }
                htmlBuilder.append("</span></dd>");

                // Texte
                htmlBuilder.append("<dt><span>Texte</span></dt><dd><span>");
                htmlBuilder.append(escape(reponse.getTexte(), true));
                htmlBuilder.append("</span></dd></dl></div>");
            }
        }

        return htmlBuilder.toString();
    }

    @Override
    public String getHTMLDemandeContenuRecap(DemandeDTO demande, boolean isPdfRecap)
            throws IOException, ParseException, ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        LOGGER.info("Chargement du fichier recap...");
        InputStream inputStream = new ClassPathResource("/recaps/recaps_" + demande.getBuildId() + ".json")
                .getInputStream();
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        LOGGER.info("Construction du recap HTML...");
        StringBuilder html = new StringBuilder();

        for (int k = 0; k < jsonArray.size(); k++) {
            if ("projectDemandeRecap".equals(((JSONObject) jsonArray.get(k)).get("name"))) {
                JSONObject projectDemandeRecap = (JSONObject) jsonArray.get(k);
                JSONArray sections = (JSONArray) projectDemandeRecap.get("sections");
                String pojo = StringUtils.remove((String) projectDemandeRecap.get("pojo"), CONTENU_DTO);
                for (int i = 0; i < sections.size(); i++) {
                    JSONObject section = (JSONObject) sections.get(i);
                    String sectionType = (String) section.get("type");

                    if (!StringUtils.equals(sectionType, "sousSections")) {
                        generateSectionHTML(html, section, sectionType, demande, isPdfRecap, pojo);
                    } else {
                        generateSectionAndSousSection(html, section, sectionType, demande, isPdfRecap, pojo);
                    }
                }
            }
        }

        return html.toString();
    }

    private void generateSectionHTML(StringBuilder html, JSONObject section, String sectionType, DemandeDTO demande,
                                     boolean isPdfRecap, String pojo) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        String firstLevel = getFirstLevelHTML(demande, sectionType, section, isPdfRecap, pojo);
        if (StringUtils.isNotBlank(firstLevel)) {
            html.append("<div class=\"sectiondemande\"><h3>").append(section.get("titre")).append("</h3><dl>");
            html.append(firstLevel);
            if (StringUtils.equals(sectionType, "adresse")) {
                html.append("<dt><span>Adresse</span></dt>");
            }
            html.append("</dl></div>");
        }
    }

    private void generateSectionAndSousSection(StringBuilder html, JSONObject section, String sectionType,
                                               DemandeDTO demande, boolean isPdfRecap, String pojo) throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        JSONArray sousSections = (JSONArray) section.get("sousSections");
        if (sousSections.toArray().length > 0) {
            StringBuilder sousSectionBuilder = new StringBuilder();
            sousSectionBuilder.append(getFirstLevelHTML(demande, sectionType, section, isPdfRecap, pojo));
            for (Object sousSection : sousSections.toArray()) {
                String sousSectionType = (String) ((JSONObject) sousSection).get("type");
                String introHtml = (String) ((JSONObject) sousSection).get("introHtml");
                // span display:grid afin d'éviter que le <pre> reçu du fichier récap, ne fasse s'élargir toute la partie
                // gauche de la page (si texte à afficher trop long, malgré l'ascenseur horizontal) !
                sousSectionBuilder.append(StringUtils.isNotBlank(introHtml) ? "<span style='display:grid'>"+introHtml+"</span>" : "");
                String firstLevel = getFirstLevelHTML(demande, sousSectionType, (JSONObject) sousSection, isPdfRecap, pojo);
                if (StringUtils.isNotBlank(firstLevel)) {
                    sousSectionBuilder.append(firstLevel);
                }
            }
            String generatedHtml = sousSectionBuilder.toString();
            if (StringUtils.isNotBlank(generatedHtml)) {
                html.append("<div class=\"sectiondemande\"><h3>").append(section.get("titre")).append("</h3><dl>");
                html.append(generatedHtml);
                html.append("</dl></div>");
            }
        }
    }

    private String getFirstLevelHTML(DemandeDTO demande, String sectionType, JSONObject section, boolean isPdfRecap, String pojo)
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {

        // On créé un nouveau SB de façon à ne pas générer la section si aucune donnée n'est renseignée.
        StringBuilder html = new StringBuilder();

        // Génération du code pour un champs HTML (titre / valeur)
        if (StringUtils.equals(sectionType, "champs")) {
            JSONArray champs = (JSONArray) section.get("champs");
            for (int j = 0; j < champs.size(); j++) {
                JSONObject champ = (JSONObject) champs.get(j);
                String type = (String) champ.get("type");
                if (StringUtils.equals(type, "adresse") || StringUtils.equals(type, "adresseMc")) {
                    html.append(getSecondLevelHTML(demande.getContenu(), champ, pojo, isPdfRecap));
                } else {
                    String value = getSecondLevelHTML(demande.getContenu(), champ, pojo, isPdfRecap);
                    if (!StringUtils.isBlank(value)) {
                        html.append("<dt><span>").append(champ.get("label")).append("</span></dt>");
                        html.append("<dd><span>").append(value).append("</span></dd>");
                    }
                }
            }

            // Génération du code pour un tableau
        } else if (StringUtils.equals(sectionType, "tableau")) {
            ArrayNode valeurs = (ArrayNode) getNode(demande.getContenu(), section, "path");
            if (valeurs.size() > 0) {
                html.append("<table id=\"datatable-demandes\" class=\"table table-striped\">");
                JSONArray columns = (JSONArray) section.get("columns");
                html.append("<thead><tr>");
                for (Object column : columns.toArray()) {
                    html.append("<th>").append(((JSONObject) column).get("label")).append("</th>");
                }
                html.append("</tr></thead>");
                Iterator<JsonNode> it = valeurs.elements();
                html.append("<tbody>");
                while (it.hasNext()) {
                    JsonNode valeur = it.next();
                    html.append("<tr>");
                    for (Object column : columns.toArray()) {
                        String value = getSecondLevelHTML(valeur, (JSONObject) column, pojo, isPdfRecap);
                        html.append("<td>").append(value).append("</td>");
                    }
                    html.append("</tr>");
                }
                html.append("</tbody></table>");
            }
        }

        return html.toString();
    }

    private String getSecondLevelHTML(JsonNode node, JSONObject champ, String pojo, boolean isPdfRecap)
            throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException, InvocationTargetException,
            NoSuchMethodException, SecurityException {
        String type = (String) champ.get("type");
        if (StringUtils.equals(type, "chaine") || StringUtils.equals(type, "texte")) {
            JsonNode node0 = getNode(node, champ, "path");
            if (node0 == null || node0 instanceof NullNode) {
                return null;
            }
            return escape(node0.asText(), isPdfRecap);
        } else if (StringUtils.equals(type, "choix")) {
            String mapping = champ.get("mapping").toString();
            if (StringUtils.equals(mapping, "nationalites")) {
                JsonNode node0 = getNode(node, champ, "path");
                if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                    return null;
                }
                return paysCache.get(node0.asText(), "fr").getNationalite();
            }
            if (StringUtils.equals(mapping, "pays")) {
                JsonNode node0 = getNode(node, champ, "path");
                if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                    return null;
                }
                return paysCache.get(node0.asText(), "fr").getLibelleCourt();
            } else if (mapping.startsWith("properties_")) {
                String path = champ.get("path").toString().replace("contenu.", "/").replace(".", "/");
                if (path.charAt(0) != '/') {
                    path = "/" + path;
                }
                JsonNode pathNode = node.at(path);
                if (pathNode instanceof MissingNode) {
                    return "N/A";
                }
            	String key = mapping.substring(11, mapping.length()) + "_FR";
            	PropertiesDTO prop = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), key);
            	if (prop != null) {
            		Map<String, String> map = AfBackUtils.getListFromDemProperty(prop.getValue());
	            	if (map != null) {
	            		return map.get(pathNode.asText());
	            	} else {
	            		LOGGER.warn("Impossible de transformer la valeur de la dem_property (key=" + key + ") en map");
	            		return "ERREUR";
	            	}
            	} else {
            		LOGGER.warn("Impossible de récupérer la dem_property requise par le fichier récap (key=" + key + ")");
            		return "ERREUR";
            	}
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
                            || (pathNode instanceof TextNode && pathNode.textValue().equals("AUTRE"))) {
                        JsonNode node0 = node.at(path + "/valeurExtra");
                        if (node0 == null || node0 instanceof NullNode) {
                            return null;
                        }
                        return escape(node0.textValue(), isPdfRecap);
                    }
                }

                String enumField = pathNode.asText();
                if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField)
                        || enumField.equals("null")) {
                    return null;
                }

                mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
                Class<?> klass = Class.forName(pojo + mapping + "Enum");
                Object value = klass.getMethod("forValue", String.class).invoke(klass, enumField);
                return value != null ? value.toString() : enumField;
            }
        } else if (StringUtils.equals(type, "date")) {
            JsonNode node0 = getNode(node, champ, "path");
            if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                return null;
            }
            LocalDateTime dateTime = LocalDateTime.parse(node0.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            // Si la date a un format d'affichage
            String format = (String) champ.get("displayJavaFormat");
            if (StringUtils.isBlank(format)) {
                format = AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT;
            }
            return dateTime.format(DateTimeFormatter.ofPattern(format));
        } else if (StringUtils.equals(type, "choixMultiple")) {
            JsonNode n = getNode(node, champ, "path");
            if (n instanceof ObjectNode) {
                ObjectNode list = (ObjectNode) n;
                Iterator<Map.Entry<String, JsonNode>> it = list.fields();
                String ret = "";
                String mapping = champ.get("mapping").toString();
                while (it.hasNext()) {
                    Map.Entry<String, JsonNode> entry = it.next();
                    if (entry.getValue().asBoolean()) {
                        mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
                        Class<?> klass = Class.forName(pojo + mapping + "Enum");
                        Object[] parameters = {entry.getKey().toUpperCase(), true};
                        Object value = klass.getMethod("forValue", String.class, boolean.class).invoke(klass, parameters);
                        if (!ret.equals("")) {
                            ret += ", ";
                        }
                        ret += value.toString();
                    }
                }
                return ret;
            }
            return "";
        } else if (StringUtils.equals(type, "adresse")) {
            String ret = buildAdresseHTML(node, champ, isPdfRecap);
            if (StringUtils.isNotEmpty(ret)) {
                String codePostal = escape(getNode(node, champ, "codePostal").textValue(), isPdfRecap);
                String ville = escape(getNode(node, champ, "ville").textValue(), isPdfRecap);
                ret += "<dt><span>Ville</span></dt><dd><span>" + codePostal + " " + ville + "</span></dd>";
                String pays = getNode(node, champ, "pays").textValue();
                if (StringUtils.isNotBlank(pays)) {
                    ret += "<dt><span>Pays</span></dt><dd><span>" + paysCache.get(pays, "fr").getNom() + "</span></dd>";
                }
            }
            return ret;
        } else if (StringUtils.equals(type, "adresseMc")) {
            return buildAdresseHTML(node, champ, isPdfRecap);
        } else if (StringUtils.equals(type, "iban")) {
            String titulaire = escape(getNode(node, champ, "titulaire").textValue(), isPdfRecap);
            String bic = escape(getNode(node, champ, "bic").textValue(), isPdfRecap);
            String iban = escape(getNode(node, champ, "iban").textValue(), isPdfRecap);
            return iban + " (Titulaire: " + titulaire + ", BIC: " + bic + ")";
        } else if (StringUtils.equals(type, "telephone")) {
            String indicatif = AfBackUtils.convertTelIndicateur(getNode(node, champ, "indicatif").textValue());
            String numero = escape(getNode(node, champ, "numero").textValue(), isPdfRecap);
            return "(" + indicatif + ") " + numero;
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

    private String escape(String str, boolean isPdfRecap) {
        String result = "";
        if (null != str) {
            str = AfBackUtils.escapeChars(str);
            result = isPdfRecap ? HtmlUtils.htmlEscapeDecimal(str) : StringEscapeUtils.escapeHtml4(str);
        }
        return result;
    }

    private String buildAdresseHTML(JsonNode node, JSONObject champ, boolean isPdfRecap) {
        String ligne1 = escape(getNode(node, champ, "ligne1").textValue(), isPdfRecap);
        String ligne2 = escape(getNode(node, champ, "ligne2").textValue(), isPdfRecap);
        String ligne3 = escape(getNode(node, champ, "ligne3").textValue(), isPdfRecap);
        String ret = "";
        if (StringUtils.isNotEmpty(ligne1)) {
            ret = "<dt><span>Adresse</span></dt><dd><span>" + ligne1 + "</span>";
            if (StringUtils.isNotBlank(ligne2)) {
                ret += "<br/><span>" + ligne2 + "</span>";
            }
            if (StringUtils.isNotBlank(ligne3)) {
                ret += "<br/><span>" + ligne3 + "</span>";
            }
            ret += "</dd>";
        }
        return ret;
    }

}
