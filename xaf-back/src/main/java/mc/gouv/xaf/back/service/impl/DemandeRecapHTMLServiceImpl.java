package mc.gouv.xaf.back.service.impl;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
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

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.data.DemandesService;
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

/**
 * Service permettant de générer une page HTML contenant le récapitulatif d'une demande.
 *
 * @author qdeme
 * @author mboutelier.ext
 */
@Component
public class DemandeRecapHTMLServiceImpl implements DemandeRecapHTMLService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeRecapHTMLServiceImpl.class);
    private static final String CONTENU_DTO = "ContenuProjectDemandeDTO";
    private static final String SPAN_OPEN = "<span>";
    private static final String SPAN_DD = "</span></dd>";
    private static final String SPAN_CLOSE = "</span>";
    private static final String DD = "</dd>";
    private static final String ID = "id=\"";
    private static final String ADRESSE = "adresse";
    private static final String LABEL = "label";
    private static final String ID_PREFIX = "idPrefix";
    private static final String CONTENU = "contenu.";

    private final DateFormat sdf = new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_HOURS_FORMAT);

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

    @Autowired
    private DemandesService demandesService;

    @Override
    public String getHTMLDemandeGeneric(DemandeDTO demande) {
        StringBuilder htmlBuilder = new StringBuilder();

        // Numéro de la demande
        htmlBuilder.append("<dl><dt><span>Numéro de la demande</span></dt><dd><span>");
        htmlBuilder.append(escape(demande.getIdentifiant(), true));
        htmlBuilder.append(SPAN_DD);

        // Date de transmission/dépôt
        boolean isVirtuel = demande.getCanal() == DemandeCanalEnum.GUICHET_VIRTUEL;
        htmlBuilder.append("<dt><span>Date de ");
        htmlBuilder.append(isVirtuel ? "transmission" : "dépôt");
        htmlBuilder.append("</span></dt><dd><span>");
        Date dateCreation = isVirtuel ? demande.getDateCreation() : demande.getCourrierDateReception();
        htmlBuilder.append(sdf.format(dateCreation));
        htmlBuilder.append(SPAN_DD);

        // Etat de la demande
        htmlBuilder.append("<dt><span>État de la demande</span></dt><dd><span>");
        htmlBuilder.append(afBackUtils.getStatusLibelleFromName(demande.getDernierStatut().getLibelle()));
        htmlBuilder.append(" le ");
        htmlBuilder.append(sdf.format(demande.getDernierStatut().getDate()));
        htmlBuilder.append(SPAN_DD);

        // Langue
        htmlBuilder.append("<dt><span>Langue</span></dt><dd><span>");
        htmlBuilder.append(escape(demande.getLangue(), true));
        htmlBuilder.append(SPAN_DD);

        // Canal
        htmlBuilder.append("<dt><span>Canal</span></dt><dd><span>");
        htmlBuilder.append(demande.getCanal());
        htmlBuilder.append("</span></dd></dl>");

        return htmlBuilder.toString();
    }

    public String getHTMLDemandeComplements(DemandeDTO demande) {
        StringBuilder htmlBuilder = new StringBuilder();

        for (DemandeComplementsDTO complement : demande.getComplements()) {
            DemandeComplementsQuestionDTO question = complement.getQuestion();
            DemandeComplementsReponseDTO reponse = complement.getReponse();
            String date = sdf.format(question.getDate());

            htmlBuilder.append("<h3>Compléments du ");
            htmlBuilder.append(date);
            htmlBuilder.append("</h3>");

            htmlBuilder.append("<div class=\"dem-admin\">");
            htmlBuilder.append("<span>Demande de l'administration</span>");

            // Date de création
            htmlBuilder.append("<dl><dt><span>Date création</span></dt><dd><span>");
            htmlBuilder.append(date);
            htmlBuilder.append(SPAN_DD);

            // Motif
            htmlBuilder.append("<dt><span>Motif</span></dt><dd><span>");
            htmlBuilder.append(escape(motifsCache.getMotif(question.getCodeMotif(), "fr").getLibelle(), true));
            htmlBuilder.append(SPAN_DD);

            // Texte
            htmlBuilder.append("<dt><span>Texte</span></dt><dd><span>");
            htmlBuilder.append(escape(question.getTexte(), true));
            htmlBuilder.append(SPAN_DD);

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
                    htmlBuilder.append(sdf.format(reponseDate));
                }
                htmlBuilder.append(SPAN_DD);

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

        for (Object value : jsonArray) {
            if ("projectDemandeRecap".equals(((JSONObject) value).get("name"))) {
                JSONObject projectDemandeRecap = (JSONObject) value;
                JSONArray sections = (JSONArray) projectDemandeRecap.get("sections");
                String pojo = StringUtils.remove((String) projectDemandeRecap.get("pojo"), CONTENU_DTO);
                for (Object o : sections) {
                    JSONObject section = (JSONObject) o;
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
            boolean isPdfRecap, String pojo) throws ClassNotFoundException, IllegalAccessException,
            IllegalArgumentException, InvocationTargetException, NoSuchMethodException, SecurityException {

        String firstLevel = getFirstLevelHTML(demande, sectionType, section, isPdfRecap, pojo);
        if (StringUtils.isNotBlank(firstLevel)) {
            html.append("<div class=\"sectiondemande\"><h3>").append(section.get("titre")).append("</h3><dl>");
            html.append(firstLevel);
            if (StringUtils.equals(sectionType, ADRESSE)) {
                if (section.get(LABEL) != null) {
                    html.append("<dt><span>").append(section.get(LABEL)).append("</span></dt>");
                } else {
                    html.append("<dt><span>Adresse</span></dt>");
                }
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
                // span display:grid afin d'éviter que le <pre> reçu du fichier récap, ne fasse s'élargir toute la
                // partie
                // gauche de la page (si texte à afficher trop long, malgré l'ascenseur horizontal) !
                sousSectionBuilder.append(
                        StringUtils.isNotBlank(introHtml) ? "<span style='display:grid'>" + introHtml + SPAN_CLOSE
                                : "");
                String firstLevel = getFirstLevelHTML(demande, sousSectionType, (JSONObject) sousSection, isPdfRecap,
                        pojo);
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

    private String getFirstLevelHTML(DemandeDTO demande, String sectionType, JSONObject section, boolean isPdfRecap,
            String pojo) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        // On créé un nouveau SB de façon à ne pas générer la section si aucune donnée n'est renseignée.
        StringBuilder html = new StringBuilder();
        if (StringUtils.equals(sectionType, "champs")) {
            // Génération du code pour un champs HTML (titre / valeur)
            getFirstLevelChamps(demande, section, isPdfRecap, pojo, html);
        } else if (StringUtils.equals(sectionType, "tableau")) {
            // Génération du code pour un tableau
            getFirstLevelTableau(demande, section, isPdfRecap, pojo, html);
        }
        return html.toString();
    }

    /**
     * Génération du code pour un champs HTML (titre / valeur)
     */
    private void getFirstLevelChamps(DemandeDTO demande, JSONObject section, boolean isPdfRecap, String pojo,
            StringBuilder html)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        JSONArray champs = (JSONArray) section.get("champs");
        DemandeDTO demandeSource = null;

        if (demande.getContenuInitial() != null && !demande.getContenuInitial().isNull()) {
            ObjectMapper om = new ObjectMapper();
            try {
                demandeSource = om.treeToValue(demande.getContenuInitial(), DemandeDTO.class);
                demandeSource.setBuildId(demande.getBuildId());
            } catch (JsonProcessingException e) {
                LOGGER.error("Impossible de parser le contenu initial de la demande" + demande.getIdentifiant(), e);
            }
        } else if (demande.getPkDemandeSource() != null) {
            demandeSource = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(),
                    demande.getPkDemandeSource());
        }

        List<String> donneesCertififiees = AfBackUtils.donneesCertifieesJsonToList(demande.getDonneesCertifiees());
        for (Object o : champs) {
            JSONObject champ = (JSONObject) o;
            String value = getSecondLevelHTML(demande.getContenu(), champ, pojo, isPdfRecap, false);
            if (!StringUtils.isBlank(value)) {
                // Pour mettre une icône s'il s'agit d'une donnée certifiée
                boolean isDonneeCertifiee = donneesCertififiees.contains(champ.get("path"));
                buildHTML(html, demandeSource, value, isDonneeCertifiee, isPdfRecap, champ);
            }
        }
    }

    private void buildHTML(StringBuilder html, DemandeDTO demandeSource, String value, boolean isDonneeCertifiee,
            boolean isPdfRecap, JSONObject champ)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        String type = (String) champ.get("type");

        // Pour mettre l'ID HTML de la donnée, récupéré depuis le fichier Recap (pour les testeurs)
        String idPrefix = (String) champ.get(ID_PREFIX);
        String idTag1 = "";
        String idTag2 = "";
        // Si ce qui est retourné de getSecondLevelHTML est un champ composé (en HTML), comme l'adresse, alors les spans
        // et idTags sont déjà dedans
        if (StringUtils.isNotBlank(idPrefix) && !type.equals(ADRESSE) && !type.equals("adresseMc")) {
            idTag1 = StringUtils.isNotBlank(idPrefix) ? "<span id=\"" + idPrefix + "\">" : SPAN_OPEN;
            idTag2 = SPAN_CLOSE;
        }

        String imgTag = "<img src=\"../img/icone_identite_numerique_valide.svg\"></img>";
        if (isPdfRecap) {
            URL url = getClass().getClassLoader().getResource("static/img/icone_identite_numerique_valide.svg");
            imgTag = "<img src=\"" + url.toExternalForm() + "\"></img>";
        }

        String valueSource = getSourceValue(demandeSource, champ, isPdfRecap);
        if (demandeSource != null && !value.equals(valueSource)) {
            if (StringUtils.isBlank(valueSource)) {
                valueSource = "(vide)";
            }
            html.append("<dt class='nouvelledonnee-titre'>").append(champ.get(LABEL)).append("</dt>");
            html.append("<dd class='nouvelledonnee-titre'>").append(idTag1)
                    .append(value.replace(SPAN_OPEN, "<span class='nouvelledonnee-contenu'>")
                            .replace("<dt>", "<dt class='nouvelledonnee-titre'>")
                            .replace("<dd>", "<dd class='nouvelledonnee-contenu'>"))
                    .append(idTag2);
            if (isDonneeCertifiee) {
                html.append(" <span class=\"nouvelledonnee\" title=\"Donnée certifiée\">").append(imgTag)
                        .append(SPAN_CLOSE);
            }
            html.append(DD);

            html.append("<dt class='anciennedonnee-titre' title='Donnée modifiée'>").append(champ.get(LABEL))
                    .append("</dt>");
            html.append("<dd class='anciennedonnee-titre' title='Donnée modifiée'>")
                    .append(valueSource.replace(SPAN_OPEN, "<span class='anciennedonnee-contenu'>")
                            .replace("<dt>", "<dt class='anciennedonnee-titre' title='Donnée modifiée'>")
                            .replace("<dd>", "<dd class='anciennedonnee-contenu' title='Donnée modifiée'>"));
        } else {
            html.append("<dt><span>").append(champ.get(LABEL)).append("</span></dt>");
            html.append("<dd>").append(idTag1).append(value).append(idTag2);
            if (isDonneeCertifiee) {
                html.append(" <span title=\"Donnée certifiée\">").append(imgTag).append(SPAN_CLOSE);
            }
        }
        html.append(DD);
    }

    /**
     * Génération du code pour un tableau
     */
    private void getFirstLevelTableau(DemandeDTO demande, JSONObject section, boolean isPdfRecap, String pojo,
            StringBuilder html)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        ArrayNode valeurs = (ArrayNode) getNode(demande.getContenu(), section, "path");
        if (valeurs.size() > 0) {
            String classPdfRecap = isPdfRecap ? "pdf-recap" : "";
            html.append(
                    "<dd style=\"width: 100%\"><table id=\"datatable-demandes\" class=\"table table-striped recaptable")
                    .append(classPdfRecap).append("\">");
            JSONArray columns = (JSONArray) section.get("columns");
            html.append("<thead><tr>");
            for (Object column : columns.toArray()) {
                html.append("<th>").append(((JSONObject) column).get(LABEL)).append("</th>");
            }
            html.append("</tr></thead>");
            Iterator<JsonNode> it = valeurs.elements();
            html.append("<tbody>");
            while (it.hasNext()) {
                JsonNode valeur = it.next();
                html.append("<tr>");
                for (Object column : columns.toArray()) {
                    String value = getSecondLevelHTML(valeur, (JSONObject) column, pojo, isPdfRecap, true);
                    String result = StringUtils.isNoneBlank(value) ? value : "";
                    html.append("<td>").append(result).append("</td>");
                }
                html.append("</tr>");
            }
            html.append("</tbody></table></dd>");
        }
    }

    private String getSecondLevelHTML(JsonNode node, JSONObject champ, String pojo, boolean isPdfRecap,
            boolean pourTableau) throws ClassNotFoundException, IllegalAccessException, IllegalArgumentException,
            InvocationTargetException, NoSuchMethodException, SecurityException {

        String type = (String) champ.get("type");
        if (StringUtils.equals(type, "chaine") || StringUtils.equals(type, "texte")) {
            JsonNode node0 = getNode(node, champ, "path");
            if (node0 == null || node0 instanceof NullNode) {
                return "";
            }
            return escape(node0.asText(), isPdfRecap);
        } else if (StringUtils.equals(type, "choix")) {
            return buildChoixHTML(node, champ, pojo, isPdfRecap);
        } else if (StringUtils.equals(type, "date")) {
            return buildDateHTML(node, champ);
        } else if (StringUtils.equals(type, "choixMultiple")) {
            return buildChoixMultipleHTML(node, champ, pojo);
        } else if (StringUtils.equals(type, ADRESSE)) {
            StringBuilder adresseBuilder = new StringBuilder();
            buildAdresseHTML(adresseBuilder, node, champ, isPdfRecap);
            buildComplementAdresseHTML(adresseBuilder, node, champ, isPdfRecap, pourTableau);
            return adresseBuilder.toString();
        } else if (StringUtils.equals(type, "adresseMc")) {
            StringBuilder adresseBuilder = new StringBuilder();
            buildAdresseHTML(adresseBuilder, node, champ, isPdfRecap);
            return adresseBuilder.toString();
        } else if (StringUtils.equals(type, "iban")) {
            String titulaire = escape(getNode(node, champ, "titulaire").textValue(), isPdfRecap);
            String bic = escape(getNode(node, champ, "bic").textValue(), isPdfRecap);
            String iban = escape(getNode(node, champ, "iban").textValue(), isPdfRecap);
            return iban + " (Titulaire: " + titulaire + ", BIC: " + bic + ")";
        } else if (StringUtils.equals(type, "telephone")) {
            return buildTelephoneHTML(node, champ, isPdfRecap);
        } else {
            return type;
        }
    }

    private JsonNode getNode(JsonNode node, JSONObject champ, String ref) {
        String path = champ.get(ref).toString().replace(CONTENU, "/").replace(".", "/");
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

    private String buildChoixHTML(JsonNode node, JSONObject champ, String pojo, boolean isPdfRecap)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        String mapping = champ.get("mapping").toString();

        if (StringUtils.equals(mapping, "nationalites")) {
            JsonNode node0 = getNode(node, champ, "path");
            if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                return "";
            }
            return paysCache.get(node0.asText(), "fr").getNationalite();
        }

        if (StringUtils.equals(mapping, "pays")) {
            JsonNode node0 = getNode(node, champ, "path");
            if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
                return "";
            }
            return paysCache.get(node0.asText(), "fr").getNom();
        }

        String path = champ.get("path").toString().replace(CONTENU, "/").replace(".", "/");
        if (path.charAt(0) != '/') {
            path = "/" + path;
        }
        JsonNode pathNode = node.at(path);
        if (pathNode instanceof MissingNode) {
            return "N/A";
        }

        if (mapping.startsWith("properties_")) {
            String key = mapping.substring(11) + "_FR";
            return propertiesService.getPropertyPourRecap(key, pathNode, true);
        }

        return buildOtherHTML(node, pathNode, path, mapping, pojo, isPdfRecap);
    }

    private String buildOtherHTML(JsonNode node, JsonNode pathNode, String path, String mapping, String pojo,
            boolean isPdfRecap)
            throws ClassNotFoundException, NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        // Prise en compte valeur/valeurExtra
        if (pathNode instanceof ObjectNode) {
            pathNode = node.at(path + "/valeur");
            if (pathNode instanceof MissingNode || pathNode instanceof NullNode
                    || (pathNode instanceof TextNode && pathNode.textValue().equals("AUTRE"))) {
                JsonNode node0 = node.at(path + "/valeurExtra");
                if (node0 == null || node0 instanceof NullNode) {
                    return "";
                }
                return escape(node0.textValue(), isPdfRecap);
            }
        }

        String enumField = pathNode.asText();
        if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField)
                || enumField.equals("null")) {
            return "";
        }

        mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
        Class<?> klass = Class.forName(pojo + mapping + "Enum");
        Object value = klass.getMethod("forValue", String.class).invoke(klass, enumField);
        return value != null ? value.toString() : enumField;
    }

    private String buildDateHTML(JsonNode node, JSONObject champ) {
        JsonNode node0 = getNode(node, champ, "path");
        if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
            return "";
        }
        try {
            LocalDateTime dateTime = LocalDateTime.parse(node0.asText(), DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            // Si la date a un format d'affichage
            String format = (String) champ.get("displayJavaFormat");
            if (StringUtils.isBlank(format)) {
                format = AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT;
            }
            return dateTime.format(DateTimeFormatter.ofPattern(format));
        } catch (Exception e) {
            LOGGER.error("buildDateHTML exception: vérifier le format en entrée");
            return "date en erreur";
        }
    }

    private void buildAdresseHTML(StringBuilder adresseBuilder, JsonNode node, JSONObject champ, boolean isPdfRecap) {
        String idPrefix = (String) champ.get(ID_PREFIX);
        String ligne1 = escape(getNode(node, champ, "ligne1").textValue(), isPdfRecap);
        String ligne2 = escape(getNode(node, champ, "ligne2").textValue(), isPdfRecap);
        String ligne3 = escape(getNode(node, champ, "ligne3").textValue(), isPdfRecap);
        if (StringUtils.isNotEmpty(ligne1)) {
            adresseBuilder.append("<span ");
            if (StringUtils.isNotBlank(idPrefix)) {
                adresseBuilder.append(ID).append(idPrefix).append("-ligne1\" ");
            }
            adresseBuilder.append('>').append(ligne1).append(SPAN_CLOSE);
        }
        if (StringUtils.isNotBlank(ligne2)) {
            adresseBuilder.append("<br/><span ");
            if (StringUtils.isNotBlank(idPrefix)) {
                adresseBuilder.append(ID).append(idPrefix).append("-ligne2\" ");
            }
            adresseBuilder.append('>').append(ligne2).append(SPAN_CLOSE);
        }
        if (StringUtils.isNotBlank(ligne3)) {
            adresseBuilder.append("<br/><span ");
            if (StringUtils.isNotBlank(idPrefix)) {
                adresseBuilder.append(ID).append(idPrefix).append("-ligne3\" ");
            }
            adresseBuilder.append('>').append(ligne3).append(SPAN_CLOSE);
        }
    }

    private void buildComplementAdresseHTML(StringBuilder adresseBuilder, JsonNode node, JSONObject champ,
            boolean isPdfRecap, boolean pourTableau) {
        if (adresseBuilder.length() != 0) {
            String codePostal = escape(getNode(node, champ, "codePostal").textValue(), isPdfRecap);
            String ville = escape(getNode(node, champ, "ville").textValue(), isPdfRecap);
            String pays = getNode(node, champ, "pays").textValue();
            if (pourTableau) {
                adresseBuilder.append("<br/><span>").append(codePostal).append(' ').append(ville).append(SPAN_CLOSE);
                if (StringUtils.isNotBlank(pays)) {
                    adresseBuilder.append("<br/><span>").append(paysCache.get(pays, "fr").getNom()).append(SPAN_CLOSE);
                }
                return;
            }
            buildComplementAdressePageHTML(adresseBuilder, champ, codePostal, ville, pays);
        }
    }

    private void buildComplementAdressePageHTML(StringBuilder adresseBuilder, JSONObject champ, String codePostal,
            String ville, String pays) {
        String idPrefix = (String) champ.get(ID_PREFIX);
        if (StringUtils.isNotBlank(codePostal)) {
            adresseBuilder.append("</dd><dt><span>Code postal</span></dt><dd><span ");
            if (StringUtils.isNotBlank(idPrefix)) {
                adresseBuilder.append(ID).append(idPrefix).append("-cp\" ");
            }
            adresseBuilder.append('>').append(codePostal).append(SPAN_CLOSE);
        }
        if (StringUtils.isNotBlank(ville)) {
            adresseBuilder.append("</dd><dt><span>Ville</span></dt><dd><span ");
            if (StringUtils.isNotBlank(idPrefix)) {
                adresseBuilder.append(ID).append(idPrefix).append("-ville\" ");
            }
            adresseBuilder.append('>').append(ville).append(SPAN_CLOSE);
        }
        if (StringUtils.isNotBlank(pays)) {
            adresseBuilder.append("</dd><dt><span>Pays</span></dt><dd><span ");
            if (StringUtils.isNotBlank(idPrefix)) {
                adresseBuilder.append(ID).append(idPrefix).append("-pays\" ");
            }
            adresseBuilder.append('>').append(paysCache.get(pays, "fr").getNom()).append(SPAN_CLOSE);
        }
    }

    private String buildChoixMultipleHTML(JsonNode node, JSONObject champ, String pojo)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException, ClassNotFoundException {

        JsonNode n = getNode(node, champ, "path");
        if (n instanceof ObjectNode) {
            ObjectNode list = (ObjectNode) n;
            Iterator<Map.Entry<String, JsonNode>> it = list.fields();
            StringBuilder retBuilder = new StringBuilder();
            String mapping = champ.get("mapping").toString();
            mapping = mapping.substring(0, 1).toUpperCase() + mapping.substring(1);
            Class<?> klass = Class.forName(pojo + mapping + "Enum");
            LOGGER.debug("n={}, path={}, klass={}", n, champ.get("path"), klass);
            while (it.hasNext()) {
                Map.Entry<String, JsonNode> entry = it.next();
                appendChoixHTML(entry, klass, retBuilder);
            }
            return retBuilder.toString();
        }
        return "";
    }

    private void appendChoixHTML(Map.Entry<String, JsonNode> entry, Class<?> klass, StringBuilder retBuilder)
            throws NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        if (entry.getValue().asBoolean()) {
            Object[] parameters = { entry.getKey().toUpperCase(), true };
            Object value = klass.getMethod("forValue", String.class, boolean.class).invoke(klass, parameters);
            LOGGER.debug("parameters={}, value={}", parameters, value);
            if (retBuilder.length() != 0) {
                retBuilder.append(", ");
            }
            retBuilder.append(value);
        }
        if (StringUtils.equals("autre", entry.getKey())) {
            if (retBuilder.length() != 0) {
                retBuilder.append(", ");
            }
            retBuilder.append("Autre: ").append(entry.getValue());
        }
    }

    private String buildTelephoneHTML(JsonNode node, JSONObject champ, boolean isPdfRecap) {
        String indicatif = getNode(node, champ, "indicatif").textValue();
        String numero = escape(getNode(node, champ, "numero").textValue(), isPdfRecap);
        StringBuilder indicteurBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(indicatif)) {
            indicteurBuilder.append("(").append(AfBackUtils.convertTelIndicateur(indicatif)).append(") ");
        }
        if (StringUtils.isNotBlank(numero)) {
            indicteurBuilder.append(numero);
        }
        return indicteurBuilder.toString();
    }

    private String getChampPojoFromRecap(String buildId) throws IOException, ParseException {
        LOGGER.info("getChampPojoFromRecap : chargement du fichier recap...");
        InputStream inputStream = new ClassPathResource("/recaps/recaps_" + buildId + ".json").getInputStream();
        JSONParser jsonParser = new JSONParser();
        JSONArray jsonArray = (JSONArray) jsonParser.parse(new InputStreamReader(inputStream, StandardCharsets.UTF_8));

        for (Object o : jsonArray) {
            if ("projectDemandeRecap".equals(((JSONObject) o).get("name"))) {
                JSONObject projectDemandeRecap = (JSONObject) o;
                return StringUtils.remove((String) projectDemandeRecap.get("pojo"), CONTENU_DTO);
            }
        }
        return null;
    }

    /**
     * Mise en valeur des données modifiées par rapport à la demande source, si cette demande est issue d'un
     * renouvellement
     */
    private String getSourceValue(DemandeDTO demandeSource, JSONObject champ, boolean isPdfRecap)
            throws ClassNotFoundException, InvocationTargetException, IllegalAccessException, NoSuchMethodException {

        // Pas possible de faire ce qui suit car c'est pas toujours "path" qu'il faut récupérer... ça peut être
        // "indicatif", "bic", etc. Impossible à savoir à l'avance
        // JsonNode donneeCourante = getNode(demande.getContenu(), champ, "path");
        // JsonNode donneeSource = getNode(demandeSource.getContenu(), champ, "path");
        // if (champ != null && donneeCourante != null && donneeSource != null && donneeCourante.textValue() != null &&
        // donneeSource.textValue() != null && !donneeCourante.textValue().equals(donneeSource.textValue())) {
        // // Signaler la diff ici
        // }

        // Donc on fait la différence sur la comparaison du résultat formatté en HTML :
        String valueSource = null;
        String pojoSource = null;
        if (demandeSource != null) {
            // Récupération du champ "pojo" du fichier Recap de ce buildId là
            try {
                pojoSource = getChampPojoFromRecap(demandeSource.getBuildId());
            } catch (IOException | ParseException e) {
                LOGGER.error("Impossible de récupérer le pojoSource", e);
            }
        }
        if (pojoSource != null) {
            valueSource = getSecondLevelHTML(demandeSource.getContenu(), champ, pojoSource, isPdfRecap, false);
        }
        return valueSource;
    }
}

