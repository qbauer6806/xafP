package mc.gouv.xaf.back.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.MissingNode;
import com.fasterxml.jackson.databind.node.NullNode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.Predicate;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.back.service.utils.UtilisateursUtils;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.SourceFiableDTO;
import mc.gouv.xaf.shared.dto.sourcefiable.enums.SourceFiablesEnum;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.util.HtmlUtils;

/**
 * Service permettant de générer une page HTML contenant le récapitulatif d'une demande.
 *
 * @author qdeme
 * @author mboutelier.ext
 */
@Component
public class DemandeRecapHTMLServiceImpl implements DemandeRecapHTMLService {

    public static final String DT = "</dt>";
    private static final String LIGNE1 = "ligne1";
    private static final String CLOSING_DD_OPENING_DT = "</dd><dt>";
    private static final String CLOSING_TR = "</tr>";
    private static final String CLOSING_TD = "</td>";
    private static final String BOLT_UDERLINE_END = "</u></b>";
    private static final String BOLT_UDERLINE_START = "<b><u>";
    private static final Logger LOGGER = LoggerFactory.getLogger(DemandeRecapHTMLServiceImpl.class);
    private static final String SPAN_OPEN = "<span>";
    private static final String SPAN_DD = "</span></dd>";
    private static final String SPAN_CLOSE = "</span>";
    private static final String DD = "</dd>";
    private static final String ID = "id=\"";
    private static final String ADRESSE = "adresse";
    private static final String LABEL = "label";
    private static final String ID_PREFIX = "idPrefix";
    private static final String CONTENU = "contenu.";
    private static final String COLUMNS = "columns";
    private static final String CHAMPS = "champs";
    private static final String CODE_POSTAL = "codePostal";
    private static final String VILLE = "ville";
    private static final String ADRESSE_MC = "adresseMc";
    private static final String TABLEAU = "tableau";

    private final DateFormat sdf = new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_HOURS_FORMAT);

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    private MotifsCache motifsCache;

    @Autowired
    private PropertiesService propertiesService;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

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
        htmlBuilder.append(demande.getDernierStatut().getLibelle());
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
            throws ParseException, IllegalArgumentException, SecurityException {

        LOGGER.info("Chargement du fichier recap...");
        JsonNode sectionsNode = demande.getConfig().get("recap").get("sections");
        JSONParser jsonParser = new JSONParser();

        LOGGER.info("Construction du recap HTML...");
        StringBuilder html = new StringBuilder();
        List<SourceFiableDTO> donneesCertifiees = demande.getDonneesCertifiees() != null
                ? Arrays.asList(demande.getDonneesCertifiees())
                : new ArrayList<>();

        JsonNode contenuSource = null;
        if (demande.getContenuInitial() != null && !demande.getContenuInitial().isNull()) {
            // récupérer le contenu de la demandeInitial et traduire
            contenuSource = demande.getContenuInitial().get("contenu").deepCopy();
            demandesService.setContenuTrad(contenuSource, demande.getConfig());
        } else if (demande.getPkDemandeSource() != null) {
            DemandeDTO d = demandesService.getDemande(demande.getPkDemandeSource());
            contenuSource = d != null ? d.getContenuTrad() : null;
        }

        JSONArray sections = (JSONArray) jsonParser.parse(sectionsNode.toString());
        for (Object o : sections) {
            JSONObject section = (JSONObject) o;
            String sectionType = (String) section.get("type");

            if (!StringUtils.equals(sectionType, "sousSections")) {
                generateSectionHTML(html, section, sectionType, demande, isPdfRecap, donneesCertifiees, contenuSource);
            } else {
                generateSectionAndSousSection(html, section, sectionType, demande, isPdfRecap, donneesCertifiees,
                        contenuSource);
            }
        }

        if (CollectionUtils.isNotEmpty(donneesCertifiees) && isPdfRecap) {
            this.ajouterSectionDonneesSourceFiable(donneesCertifiees, html, sections);
        }

        return html.toString();
    }

    private void ajouterSectionDonneesSourceFiable(List<SourceFiableDTO> donneesCertifiees, StringBuilder html,
            JSONArray sections) {

        List<JSONObject> liste = sections.stream().map(e -> ((JSONObject) e).get(CHAMPS)).filter(Objects::nonNull)
                .flatMap(e -> ((JSONArray) e).stream()).toList();
        if (CollectionUtils.isEmpty(liste)) {
            return;
        }

        html.append("<div class=\"sectiondemande\">").append("<h3>").append("Informations provenant de sources fiables")
                .append("</h3>");
        for (SourceFiablesEnum sourceFiablesEnum : SourceFiablesEnum.values()) {
            List<String> sources = donneesCertifiees.stream()
                    .filter(element -> sourceFiablesEnum.equals(element.getSourceFiable()))
                    .map(SourceFiableDTO::getModelPath).toList();
            if (CollectionUtils.isNotEmpty(sources)) {
                html.append("<dl><dt><span>").append("Source des informations suivantes :").append("</span></dt><br/>");
                html.append("<dt><span>Service : </span></dt><dd><span>").append(sourceFiablesEnum.getService())
                        .append(SPAN_DD);
                html.append("<dt><span>Application : </span></dt><dd><span>").append(sourceFiablesEnum.getApplication())
                        .append(SPAN_DD);
                html.append("<dt><span>Informations : </span></dt><dd><span>");
                List<String> listeChamps = new ArrayList<>();
                for (String value : sources) {
                    String champ = liste.stream().filter(e -> value.equals(e.get("path")) || e.containsValue(value))
                            .map(e -> getLabel(value, e)).findFirst().orElse("");
                    if (StringUtils.isNotBlank(champ) && !listeChamps.contains(champ)) {
                        listeChamps.add(champ);
                    }
                }
                html.append(String.join("; ", listeChamps)).append("</span></dd></dl>");
            }
        }
        html.append("</div>");
    }

    private String getLabel(String value, JSONObject e) {
        if (ADRESSE.equals(e.get("type")) || ADRESSE_MC.equals(e.get("type"))) {
            if (value.endsWith(CODE_POSTAL)) {
                return "Code postal";
            }
            if (value.endsWith(VILLE)) {
                return "Ville";
            }
            if (value.endsWith("pays")) {
                return "Pays";
            }
            return "Adresse";
        }
        return (String) e.get(LABEL);
    }

    private void generateSectionHTML(StringBuilder html, JSONObject section, String sectionType, DemandeDTO demande,
            boolean isPdfRecap, List<SourceFiableDTO> donneesCertifiees, JsonNode contenuSource)
            throws IllegalArgumentException, SecurityException {

        String firstLevel = getFirstLevelHTML(demande, sectionType, section, isPdfRecap, donneesCertifiees,
                contenuSource);
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
            DemandeDTO demande, boolean isPdfRecap, List<SourceFiableDTO> donneesCertifiees, JsonNode contenuSource)
            throws IllegalArgumentException, SecurityException {

        JSONArray sousSections = (JSONArray) section.get("sousSections");
        if (sousSections.toArray().length > 0) {
            StringBuilder sousSectionBuilder = new StringBuilder();
            sousSectionBuilder.append(
                    getFirstLevelHTML(demande, sectionType, section, isPdfRecap, donneesCertifiees, contenuSource));
            for (Object sousSection : sousSections.toArray()) {
                String sousSectionType = (String) ((JSONObject) sousSection).get("type");
                String introHtml = (String) ((JSONObject) sousSection).get("introHtml");
                // span display:grid afin d'éviter que le <pre> reçu du fichier récap, ne fasse s'élargir toute la
                // partie
                // gauche de la page (si texte à afficher trop long, malgré l'ascenseur horizontal) !
                sousSectionBuilder.append(StringUtils.isNotBlank(introHtml)
                        ? "<span style='display:grid'>" + introHtml + SPAN_CLOSE
                        : "");
                String firstLevel = getFirstLevelHTML(demande, sousSectionType, (JSONObject) sousSection, isPdfRecap,
                        donneesCertifiees, contenuSource);
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
            List<SourceFiableDTO> donneesCertifiees, JsonNode contenuSource)
            throws IllegalArgumentException, SecurityException {

        // On créé un nouveau SB de façon à ne pas générer la section si aucune donnée n'est renseignée.
        StringBuilder html = new StringBuilder();
        if (StringUtils.equals(sectionType, CHAMPS)) {
            // Génération du code pour un champs HTML (titre / valeur)
            this.getFirstLevelChamps(demande, contenuSource, section, isPdfRecap, html, donneesCertifiees);
        } else if (StringUtils.equals(TABLEAU, sectionType)) {
            // Génération du code pour un tableau
            this.getFirstLevelTableau(demande, contenuSource, section, isPdfRecap, html, donneesCertifiees);
        }
        return html.toString();
    }

    /**
     * Génération du code pour un champs HTML (titre / valeur)
     *
     */
    private void getFirstLevelChamps(DemandeDTO demande, JsonNode contenuSource, JSONObject section,
            boolean isPdfRecap, StringBuilder html, List<SourceFiableDTO> donneesCertifiees) {

        JSONArray champs = (JSONArray) section.get(CHAMPS);

        for (Object o : champs) {
            JSONObject champ = (JSONObject) o;
            String type = (String) champ.get("type");
            if (StringUtils.equals(TABLEAU, type)) {
                // Génération du code pour un tableau
                this.getFirstLevelTableau(demande, contenuSource, champ, isPdfRecap, html, donneesCertifiees);
            } else {
                String value = this.getSecondLevelHTML(demande.getContenuTrad(), champ, isPdfRecap, false, donneesCertifiees);
                if (StringUtils.isNotBlank(value)) {
                    buildHTML(html, contenuSource, value, isPdfRecap, champ, demande, donneesCertifiees);
                }
            }
        }
    }

    private void buildHTML(StringBuilder html, JsonNode contenuSource, String value, boolean isPdfRecap,
            JSONObject champ, DemandeDTO demande, List<SourceFiableDTO> donneesCertifiees) {

        String type = (String) champ.get("type");
        List<String> spansIdAMarquer = demarchesDataProvider.getSpansIdAMarquer(demande);

        // Pour mettre une icône s'il s'agit d'une donnée certifiée
        String path = (String) champ.get("path");
        String idPrefix = (String) champ.get(ID_PREFIX);
        String sourceDonneesFiable = this.getSourceDonneesFiable(champ, demande, donneesCertifiees, type, path);

        // Pour mettre l'ID HTML de la donnée, récupéré depuis le fichier Recap (pour les testeurs)
        boolean champAMarquer = spansIdAMarquer.contains(idPrefix);
        String idTag1 = "";
        String idTag2 = "";
        // Si ce qui est retourné de getSecondLevelHTML est un champ composé (en HTML), comme l'adresse, alors les spans
        // et idTags sont déjà dedans
        if (StringUtils.isNotBlank(idPrefix) && !type.equals(ADRESSE) && !type.equals(ADRESSE_MC)) {
            idTag1 = StringUtils.isNotBlank(idPrefix) ? "<span id=\"" + idPrefix + "\">" : SPAN_OPEN;
            idTag2 = SPAN_CLOSE;
        }

        String imgTag = this.getImgTag(isPdfRecap);

        String valueSource = getSourceValue(contenuSource, champ, isPdfRecap, donneesCertifiees);
        if (contenuSource != null && !value.equalsIgnoreCase(valueSource)
                && demarchesDataProvider.isAfficheDemandeSource() && StringUtils.isBlank(sourceDonneesFiable)) {
            if (StringUtils.isBlank(valueSource)) {
                valueSource = "N/A";
            }
            html.append("<dt class='nouvelledonnee-titre'>").append(champ.get(LABEL)).append(DT);

            String newValue = value.replace(SPAN_OPEN, "<span class='nouvelledonnee-contenu'>")
                    .replace("<dt>", "<dt class='nouvelledonnee-titre'>")
                    .replace("<dd>", "<dd class='nouvelledonnee-contenu'>");
            html.append("<dd class='nouvelledonnee-titre'>").append(idTag1)
                    .append(this.getValue(champAMarquer, newValue)).append(idTag2);

            html.append(DD);

            html.append("<dt class='anciennedonnee-titre' title='Donnée modifiée'>").append(champ.get(LABEL))
                    .append(DT);
            String newValueSource = valueSource.replace(SPAN_OPEN, "<span class='anciennedonnee-contenu'>")
                    .replace("<dt>", "<dt class='anciennedonnee-titre' title='Donnée modifiée'>")
                    .replace("<dd>", "<dd class='anciennedonnee-contenu' title='Donnée modifiée'>");
            html.append("<dd class='anciennedonnee-titre' title='Donnée modifiée'>")
                    .append(this.getValue(champAMarquer, newValueSource));
        } else {
            Object obj = champ.get(LABEL);
            html.append("<dt><span>").append(obj).append(SPAN_CLOSE);
            this.addImmageDonneesSourceFiable(html, sourceDonneesFiable, imgTag);
            html.append(DT);
            html.append("<dd>").append(idTag1).append(this.getValue(champAMarquer, value)).append(idTag2);
        }
        html.append(DD);
    }

    private void addImmageDonneesSourceFiable(StringBuilder html, String source, String imgTag) {
        if (StringUtils.isNotBlank(source) && StringUtils.isNotBlank(imgTag)) {
            html.append("<span class='img-source-fiable' title='").append(source).append("'>").append(imgTag)
                    .append(SPAN_CLOSE);
        }
    }

    private String getSourceDonneesFiable(JSONObject champ, DemandeDTO demande, List<SourceFiableDTO> donneesCertifiees,
            String type, String path) {
        return donneesCertifiees.stream().filter(this.filtrer(type, demande, champ, path))
                .map(SourceFiableDTO::getSourceFiable).map(SourceFiablesEnum::toString).findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private Predicate<SourceFiableDTO> filtrer(String type, DemandeDTO demande, JSONObject champ, String path) {
        return sourceFiableDTO ->
                (type.equals(ADRESSE) && isAdresseCertifiee(demande, champ, sourceFiableDTO.getModelPath()))
                        || sourceFiableDTO.getModelPath().equals(path);
    }

    private boolean isAdresseCertifiee(DemandeDTO demande, JSONObject champ, String modelPath) {
        String ligne1 = escape(getNode(demande.getContenuTrad(), champ, LIGNE1).textValue(), false);
        if (StringUtils.isNotEmpty(ligne1)) {
            return modelPath.equals(champ.get("path"));
        }
        return false;
    }

    private String getValue(boolean champAMarquer, String newValue) {
        return champAMarquer ? BOLT_UDERLINE_START + newValue + BOLT_UDERLINE_END : newValue;
    }

    private String getImgTag(boolean isPdfRecap) {
        if (isPdfRecap) {
            return StringUtils.EMPTY;
        }
        return "<img src=\"../img/icone_identite_numerique_valide.svg\"></img>";
    }

    /**
     * Génération du code pour un tableau
     *
     */
    private void getFirstLevelTableau(DemandeDTO demande, JsonNode contenuSource, JSONObject section,
            boolean isPdfRecap, StringBuilder html, List<SourceFiableDTO> donneesCertifiees) {

        JsonNode jsonNode = this.getNode(demande.getContenuTrad(), section);
        if (jsonNode instanceof ArrayNode newValeurs && !newValeurs.isEmpty()) {
            String classPdfRecap = isPdfRecap ? "pdf-recap" : "";
            html.append(
                            "<dd style=\"width: 100%\"><table id=\"datatable-demandes-recap\" class=\"table table-striped recaptable")
                    .append(classPdfRecap).append("\">");
            JSONArray columns = (JSONArray) section.get(COLUMNS);
            String style = isPdfRecap ? String.format(" style=\"font-size: %spx\"",
                    demarchesDataProvider.getTaileTexteEnteteTableauxRecapPdf()) : "";
            html.append("<thead><tr onclick=\"switchTS()\"").append(style).append(">");
            for (Object column : columns.toArray()) {
                html.append("<th>").append(((JSONObject) column).get(LABEL)).append("</th>");
            }
            html.append("</tr></thead>");
            Iterator<JsonNode> itNew = newValeurs.elements();
            if (contenuSource != null) {
                contructTableauWithDiff(contenuSource, section, isPdfRecap, html, itNew, donneesCertifiees);
            } else {
                contructSimpleTableau(demande, section, isPdfRecap, html, donneesCertifiees);
            }
        }
    }

    private void contructSimpleTableau(DemandeDTO demande, JSONObject section, boolean isPdfRecap, StringBuilder html,
            List<SourceFiableDTO> donneesCertifiees) throws IllegalArgumentException, SecurityException {
        JsonNode jsonNode = this.getNode(demande.getContenuTrad(), section);
        if (jsonNode instanceof ArrayNode valeurs && !valeurs.isEmpty()) {
            JSONArray columns = (JSONArray) section.get(COLUMNS);
            Iterator<JsonNode> it = valeurs.elements();
            html.append("<tbody>");
            while (it.hasNext()) {
                JsonNode valeur = it.next();
                html.append("<tr>");
                for (Object column : columns.toArray()) {
                    String value = getSecondLevelHTML(valeur, (JSONObject) column, isPdfRecap, true, donneesCertifiees);
                    String result = StringUtils.isNoneBlank(value) ? value : "";
                    html.append("<td>").append(result).append(CLOSING_TD);
                }
                html.append(CLOSING_TR);
            }
            html.append("</tbody></table></dd>");
        }
    }

    private void contructTableauWithDiff(JsonNode contenuSource, JSONObject section, boolean isPdfRecap,
            StringBuilder html, Iterator<JsonNode> itNew, List<SourceFiableDTO> donneesCertifiees) {
        JsonNode jsonNode = this.getNode(contenuSource, section);
        html.append("<tbody>");
        JSONArray columns = (JSONArray) section.get(COLUMNS);
        if(jsonNode instanceof ArrayNode demandeSourceValeurs && !demandeSourceValeurs.isEmpty()) {
            Iterator<JsonNode> itDemandeSource = demandeSourceValeurs.elements();
            while (itNew.hasNext() && itDemandeSource.hasNext()) {
                JsonNode newValeur = itNew.next();
                JsonNode demandeSourceValeur = itDemandeSource.next();
                html.append("<tr>");
                for (Object column : columns.toArray()) {
                    String valueSource = getSecondLevelHTML(demandeSourceValeur, (JSONObject) column, isPdfRecap, true,
                            donneesCertifiees);
                    String value = getSecondLevelHTML(newValeur, (JSONObject) column, isPdfRecap, true, donneesCertifiees);
                    this.completeTd(html, valueSource, value, isPdfRecap);
                }
                html.append(CLOSING_TR);
            }
        }
        // On fini de remplir le tableau avec les nouvelles valeurs (un nouvel enfant
        // par exemple)
        if (itNew.hasNext()) {
            while (itNew.hasNext()) {
                JsonNode newValeur = itNew.next();
                html.append("<tr>");
                for (Object column : columns.toArray()) {
                    String value = getSecondLevelHTML(newValeur, (JSONObject) column, isPdfRecap, true,
                            new ArrayList<>());
                    html.append("<td onclick=\"switchTS()\" class='nouvelledonnee-contenu'>")
                            .append(StringUtils.isNoneBlank(value) ? value : "").append(CLOSING_TD);
                    if (!isPdfRecap) {
                        html.append("<td class='anciennedonnee-contenu' title='Donnée modifiée'>").append("N/A")
                                .append(CLOSING_TD);
                    }
                }
                html.append(CLOSING_TR);
            }
        }
        html.append("</tbody></table></dd>");
    }

    private void completeTd(StringBuilder html, String valueSource, String value, boolean isPdfRecap) {
        if (!value.equalsIgnoreCase(valueSource)) {
            if (StringUtils.isBlank(valueSource)) {
                valueSource = "N/A";
            }
            String newValue = StringUtils.isNoneBlank(value) ? value : "";
            html.append("<td  onclick=\"switchTS()\" class='nouvelledonnee-contenu'>").append(newValue)
                    .append(CLOSING_TD);
            if (!isPdfRecap) {
                String newValueSource = StringUtils.isNoneBlank(valueSource) ? valueSource : "";
                html.append("<td class='anciennedonnee-contenu' title='Donnée modifiée'>").append(newValueSource)
                        .append(CLOSING_TD);
            }
        } else {
            html.append("<td>").append(StringUtils.isNoneBlank(value) ? value : "").append(CLOSING_TD);
        }
    }

    private String getSecondLevelHTML(JsonNode node, JSONObject champ, boolean isPdfRecap, boolean pourTableau,
            List<SourceFiableDTO> donneesCertifiees) throws IllegalArgumentException, SecurityException {
        String type = (String) champ.get("type");
        if (StringUtils.equals(type, "chaine") || StringUtils.equals(type, "texte")) {
            JsonNode node0 = getNode(node, champ);
            if (node0 == null || node0 instanceof NullNode) {
                return "";
            }
            return escape(node0.asText(), isPdfRecap);
        } else if (StringUtils.equals(type, "choix")) {
            return buildChoixHTML(node, champ);
        } else if (StringUtils.equals(type, "date")) {
            return buildDateHTML(node, champ);
        } else if (StringUtils.equals(type, "choixMultiple")) {
            return buildChoixMultipleHTML(node, champ);
        } else if (StringUtils.equals(type, ADRESSE)) {
            StringBuilder adresseBuilder = new StringBuilder();
            buildAdresseHTML(adresseBuilder, node, champ, isPdfRecap);
            buildComplementAdresseHTML(adresseBuilder, node, champ, isPdfRecap, pourTableau, donneesCertifiees);
            return adresseBuilder.toString();
        } else if (StringUtils.equals(type, ADRESSE_MC)) {
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

    private JsonNode getNode(JsonNode node, JSONObject champ) {
        return getNode(node, champ, null);
    }

    private JsonNode getNode(JsonNode node, JSONObject champ, String complementChemin) {
        String chemin = getChemin(champ);
        if (complementChemin != null && !complementChemin.isEmpty()) {
            chemin += "/" + complementChemin;
        }
        return node.at(chemin);
    }

    private String getChemin(JSONObject champ) {
        String chemin = champ.get("path").toString().replace(CONTENU, "/").replace(".", "/");
        if (chemin.charAt(0) != '/') {
            chemin = "/" + chemin;
        }
        return chemin;
    }

    private String escape(String str, boolean isPdfRecap) {
        String result = "";
        if (null != str) {
            str = AfBackUtils.escapeChars(str);
            result = isPdfRecap ? HtmlUtils.htmlEscapeDecimal(str) : StringEscapeUtils.escapeHtml4(str);
        }
        return result;
    }

    private String buildChoixHTML(JsonNode node, JSONObject champ) {

        String mapping = champ.get("mapping").toString();

        String chemin = getChemin(champ);
        JsonNode pathNode = node.at(chemin);
        if (pathNode instanceof MissingNode) {
            return "N/A";
        }

        if (mapping.startsWith("properties_")) {
            String key = mapping.substring(11) + "_FR";
            return propertiesService.getPropertyPourRecap(key, pathNode, true);
        }

        String enumField = pathNode.asText();
        if (enumField == null || pathNode instanceof NullNode || StringUtils.isBlank(enumField) || enumField.equals(
                "null")) {
            return "";
        }
        return enumField;
    }

    private String buildDateHTML(JsonNode node, JSONObject champ) {
        JsonNode node0 = getNode(node, champ);
        if (node0 == null || node0 instanceof NullNode || StringUtils.isBlank(node0.asText())) {
            return "";
        }
        try {
            return node0.asText();
        } catch (Exception e) {
            LOGGER.error("buildDateHTML exception: vérifier le format en entrée");
            return "date en erreur";
        }
    }

    private void buildAdresseHTML(StringBuilder adresseBuilder, JsonNode node, JSONObject champ, boolean isPdfRecap) {
        String idPrefix = (String) champ.get(ID_PREFIX);
        String ligne1 = escape(getNode(node, champ, LIGNE1).textValue(), isPdfRecap);
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

    private void completeSpan(String id, StringBuilder adresseBuilder, List<SourceFiableDTO> donneesCertifiees,
            String sectionKey, String value, String path, String imgTag) {
        // Valeur par défaut de la source
        String source = donneesCertifiees.stream()
                .filter(sourceFiableDTO -> sourceFiableDTO.getModelPath().equals(path))
                .map(SourceFiableDTO::getSourceFiable).map(SourceFiablesEnum::toString).findFirst()
                .orElse(StringUtils.EMPTY);

        adresseBuilder.append(CLOSING_DD_OPENING_DT).append(SPAN_OPEN).append(sectionKey).append(SPAN_CLOSE);
        this.addImmageDonneesSourceFiable(adresseBuilder, source, imgTag);
        adresseBuilder.append(DT);

        adresseBuilder.append("<dd>").append("<span");
        if (StringUtils.isNotBlank(id)) {
            adresseBuilder.append(StringUtils.SPACE).append(ID).append(id).append("\"");
        }
        adresseBuilder.append('>').append(value).append(SPAN_CLOSE);
    }

    private void buildComplementAdresseHTML(StringBuilder adresseBuilder, JsonNode node, JSONObject champ,
            boolean isPdfRecap, boolean pourTableau, List<SourceFiableDTO> donneesCertifiees) {
        if (!adresseBuilder.isEmpty()) {
            String codePostal = escape(getNode(node, champ, CODE_POSTAL).textValue(), isPdfRecap);
            String ville = escape(getNode(node, champ, VILLE).textValue(), isPdfRecap);
            String pays = getNode(node, champ, "pays").textValue();
            if (pourTableau) {
                adresseBuilder.append("<br/><span>").append(codePostal).append(' ').append(ville).append(SPAN_CLOSE);
                if (StringUtils.isNotBlank(pays)) {
                    adresseBuilder.append("<br/><span>").append(pays).append(SPAN_CLOSE);
                }
            } else {
                buildComplementAdressePageHTML(adresseBuilder, champ, codePostal, ville, pays, donneesCertifiees,
                        isPdfRecap);
            }
        }
    }

    private void buildComplementAdressePageHTML(StringBuilder adresseBuilder, JSONObject champ, String codePostal,
            String ville, String pays, List<SourceFiableDTO> donneesCertifiees, boolean isPdfRecap) {
        String imgTag = this.getImgTag(isPdfRecap);
        String idPrefix = (String) champ.get(ID_PREFIX);
        if (StringUtils.isNotBlank(codePostal)) {
            String path = (String) champ.get(CODE_POSTAL);
            this.completeSpan(idPrefix + "-cp", adresseBuilder, donneesCertifiees, "Code postal", codePostal, path,
                    imgTag);
        }
        if (StringUtils.isNotBlank(ville)) {
            String path = (String) champ.get(VILLE);
            this.completeSpan(idPrefix + "-ville", adresseBuilder, donneesCertifiees, "Ville", ville, path, imgTag);
        }

        if (StringUtils.isNotBlank(pays)) {
            String path = (String) champ.get("pays");
            this.completeSpan(idPrefix + "-pays", adresseBuilder, donneesCertifiees, "Pays", pays, path, imgTag);
        }
    }

    private String buildChoixMultipleHTML(JsonNode node, JSONObject champ) {
        JsonNode n = getNode(node, champ);
        if (n instanceof ArrayNode list) {
            StringBuilder retBuilder = new StringBuilder();
            for (JsonNode value : list) {
                if (!retBuilder.isEmpty()) {
                    retBuilder.append(", ");
                }
                retBuilder.append(value.asText());
            }
            return retBuilder.toString();
        }
        return "";
    }

    private String buildTelephoneHTML(JsonNode node, JSONObject champ, boolean isPdfRecap) {
        String indicatif = getNode(node, champ, "indicatif").textValue();
        String numero = escape(getNode(node, champ, "numero").textValue(), isPdfRecap);
        StringBuilder indicateurBuilder = new StringBuilder();
        if (StringUtils.isNotBlank(indicatif)) {
            indicateurBuilder.append("(").append(AfBackUtils.convertTelIndicateur(indicatif)).append(") ");
        }
        if (StringUtils.isNotBlank(numero)) {
            indicateurBuilder.append(numero);
        }
        return indicateurBuilder.toString();
    }

    /**
     * Mise en valeur des données modifiées par rapport à la demande source, si cette demande est issue d'un
     * renouvellement
     */
    private String getSourceValue(JsonNode contenuSource, JSONObject champ, boolean isPdfRecap,
            List<SourceFiableDTO> donneesCertifiees) {
        if (contenuSource != null) {
            return getSecondLevelHTML(contenuSource, champ, isPdfRecap, false, donneesCertifiees);
        }
        return null;
    }
}


