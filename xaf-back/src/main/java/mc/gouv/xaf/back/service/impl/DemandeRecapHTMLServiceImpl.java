package mc.gouv.xaf.back.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.NullNode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.function.Predicate;
import mc.gouv.xaf.back.service.DemandeRecapHTMLService;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
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
            throws IllegalArgumentException, SecurityException {

        LOGGER.info("Chargement du fichier recap...");
        JsonNode sectionsNode = demande.getConfig().get("recap").get("sections");

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

        if (sectionsNode != null && sectionsNode.isArray()) {
            for (JsonNode section : sectionsNode) {
                String sectionType = section.has("type") ? section.get("type").asText() : null;
                if (!StringUtils.equals(sectionType, "sousSections")) {
                    generateSectionHTML(html, section, sectionType, demande, isPdfRecap, donneesCertifiees,
                            contenuSource);
                } else {
                    generateSectionAndSousSection(html, section, sectionType, demande, isPdfRecap, donneesCertifiees,
                            contenuSource);
                }
            }

            if (CollectionUtils.isNotEmpty(donneesCertifiees) && isPdfRecap) {
                this.ajouterSectionDonneesSourceFiable(donneesCertifiees, html, sectionsNode);
            }
        }
        return html.toString();
    }

    private void ajouterSectionDonneesSourceFiable(List<SourceFiableDTO> donneesCertifiees, StringBuilder html,
            JsonNode sections) {

        List<JsonNode> liste = new ArrayList<>();
        for (JsonNode e : sections) {
            if (e.has(CHAMPS)) {
                JsonNode champs = e.get(CHAMPS);
                if (champs.isArray()) {
                    champs.forEach(liste::add);
                }
            }
        }
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
                    String champ = liste.stream()
                            .filter(e -> (e.has("path") && value.equals(e.get("path").asText())) || containsValue(e,
                                    value)).map(e -> getLabel(value, e)).findFirst().orElse("");
                    if (StringUtils.isNotBlank(champ) && !listeChamps.contains(champ)) {
                        listeChamps.add(champ);
                    }
                }
                html.append(String.join("; ", listeChamps)).append("</span></dd></dl>");
            }
        }
        html.append("</div>");
    }

    private boolean containsValue(JsonNode node, String searched) {
        Iterator<String> it = node.fieldNames();
        while (it.hasNext()) {
            String key = it.next();
            if (node.get(key).asText().equals(searched)) {
                return true;
            }
        }
        return false;
    }

    private String getLabel(String value, JsonNode e) {
        if (ADRESSE.equals(e.get("type").asText()) || ADRESSE_MC.equals(e.get("type").asText())) {
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
        return e.has(LABEL) ? e.get(LABEL).asText() : "";
    }

    private void generateSectionHTML(StringBuilder html, JsonNode section, String sectionType, DemandeDTO demande,
            boolean isPdfRecap, List<SourceFiableDTO> donneesCertifiees, JsonNode contenuSource) {
        String firstLevel = getFirstLevelHTML(demande, sectionType, section, isPdfRecap, donneesCertifiees,
                contenuSource);

        if (StringUtils.isNotBlank(firstLevel)) {
            html.append("<div class=\"sectiondemande\"><h3>").append(section.get("titre").asText()).append("</h3><dl>");
            html.append(firstLevel);
            if (StringUtils.equals(sectionType, ADRESSE)) {
                if (section.has(LABEL)) {
                    html.append("<dt><span>").append(section.get(LABEL).asText()).append("</span></dt>");
                } else {
                    html.append("<dt><span>Adresse</span></dt>");
                }
            }
            html.append("</dl></div>");
        }
    }

    private void generateSectionAndSousSection(StringBuilder html, JsonNode section, String sectionType,
            DemandeDTO demande, boolean isPdfRecap, List<SourceFiableDTO> donneesCertifiees, JsonNode contenuSource) {
        JsonNode sousSections = section.get("sousSections");
        if (sousSections != null && sousSections.isArray() && !sousSections.isEmpty()) {
            StringBuilder sousSectionBuilder = new StringBuilder();
            sousSectionBuilder.append(
                    getFirstLevelHTML(demande, sectionType, section, isPdfRecap, donneesCertifiees, contenuSource));
            for (JsonNode sousSection : sousSections) {
                String sousSectionType = sousSection.has("type") ? sousSection.get("type").asText() : null;
                String introHtml = sousSection.has("introHtml") ? sousSection.get("introHtml").asText() : null;
                sousSectionBuilder.append(StringUtils.isNotBlank(introHtml)
                        ? "<span style='display:grid'>" + introHtml + SPAN_CLOSE
                        : "");
                String firstLevel = getFirstLevelHTML(demande, sousSectionType, sousSection, isPdfRecap,
                        donneesCertifiees, contenuSource);
                if (StringUtils.isNotBlank(firstLevel)) {
                    sousSectionBuilder.append(firstLevel);
                }
            }
            String generatedHtml = sousSectionBuilder.toString();
            if (StringUtils.isNotBlank(generatedHtml)) {
                html.append("<div class=\"sectiondemande\"><h3>").append(section.get("titre").asText())
                        .append("</h3><dl>");
                html.append(generatedHtml);
                html.append("</dl></div>");
            }
        }
    }

    private String getFirstLevelHTML(DemandeDTO demande, String sectionType, JsonNode section, boolean isPdfRecap,
            List<SourceFiableDTO> donneesCertifiees, JsonNode contenuSource) {
        StringBuilder html = new StringBuilder();
        if (StringUtils.equals(sectionType, CHAMPS)) {
            this.getFirstLevelChamps(demande, contenuSource, section, isPdfRecap, html, donneesCertifiees);
        } else if (StringUtils.equals(TABLEAU, sectionType)) {
            this.getFirstLevelTableau(demande, contenuSource, section, isPdfRecap, html, donneesCertifiees);
        }
        return html.toString();
    }

    private void getFirstLevelChamps(DemandeDTO demande, JsonNode contenuSource, JsonNode section, boolean isPdfRecap,
            StringBuilder html, List<SourceFiableDTO> donneesCertifiees) {

        JsonNode champs = section.get(CHAMPS);

        if (champs != null && champs.isArray()) {
            for (JsonNode champ : champs) {
                String type = champ.has("type") ? champ.get("type").asText() : "";
                if (StringUtils.equals(TABLEAU, type)) {
                    this.getFirstLevelTableau(demande, contenuSource, champ, isPdfRecap, html, donneesCertifiees);
                } else {
                    String value = this.getSecondLevelHTML(demande.getContenuTrad(), champ, isPdfRecap, false,
                            donneesCertifiees);
                    if (StringUtils.isNotBlank(value)) {
                        buildHTML(html, contenuSource, value, isPdfRecap, champ, demande, donneesCertifiees);
                    }
                }
            }
        }
    }

    private void buildHTML(StringBuilder html, JsonNode contenuSource, String value, boolean isPdfRecap, JsonNode champ,
            DemandeDTO demande, List<SourceFiableDTO> donneesCertifiees) {
        String type = champ.has("type") ? champ.get("type").asText() : "";
        List<String> spansIdAMarquer = demarchesDataProvider.getSpansIdAMarquer(demande);

        String path = champ.has("path") ? champ.get("path").asText() : "";
        String idPrefix = champ.has(ID_PREFIX) ? champ.get(ID_PREFIX).asText() : "";
        String sourceDonneesFiable = this.getSourceDonneesFiable(champ, demande, donneesCertifiees, type, path);

        // Pour mettre l'ID HTML de la donnée, récupéré depuis le fichier Recap (pour les testeurs)
        boolean champAMarquer = spansIdAMarquer.contains(idPrefix);
        String idTag1 = "";
        String idTag2 = "";
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
            html.append("<dt class='nouvelledonnee-titre'>").append(champ.get(LABEL).asText()).append(DT);

            String newValue = value.replace(SPAN_OPEN, "<span class='nouvelledonnee-contenu'>")
                    .replace("<dt>", "<dt class='nouvelledonnee-titre'>")
                    .replace("<dd>", "<dd class='nouvelledonnee-contenu'>");
            html.append("<dd class='nouvelledonnee-titre'>").append(idTag1)
                    .append(this.getValue(champAMarquer, newValue)).append(idTag2);

            html.append(DD);

            html.append("<dt class='anciennedonnee-titre' title='Donnée modifiée'>").append(champ.get(LABEL).asText())
                    .append(DT);
            String newValueSource = valueSource.replace(SPAN_OPEN, "<span class='anciennedonnee-contenu'>")
                    .replace("<dt>", "<dt class='anciennedonnee-titre' title='Donnée modifiée'>")
                    .replace("<dd>", "<dd class='anciennedonnee-contenu' title='Donnée modifiée'>");
            html.append("<dd class='anciennedonnee-titre' title='Donnée modifiée'>")
                    .append(this.getValue(champAMarquer, newValueSource));
        } else {
            Object obj = champ.has(LABEL) ? champ.get(LABEL).asText() : "";
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

    private String getSourceDonneesFiable(JsonNode champ, DemandeDTO demande, List<SourceFiableDTO> donneesCertifiees,
            String type, String path) {
        return donneesCertifiees.stream().filter(this.filtrer(type, demande, champ, path))
                .map(SourceFiableDTO::getSourceFiable).map(SourceFiablesEnum::toString).findFirst()
                .orElse(StringUtils.EMPTY);
    }

    private Predicate<SourceFiableDTO> filtrer(String type, DemandeDTO demande, JsonNode champ, String path) {
        return sourceFiableDTO ->
                (type.equals(ADRESSE) && isAdresseCertifiee(demande, champ, sourceFiableDTO.getModelPath()))
                        || sourceFiableDTO.getModelPath().equals(path);
    }

    private boolean isAdresseCertifiee(DemandeDTO demande, JsonNode champ, String modelPath) {
        String ligne1 = escape(getNode(demande.getContenuTrad(), champ, LIGNE1).textValue(), false);
        if (StringUtils.isNotEmpty(ligne1)) {
            return modelPath.equals(champ.get("path").asText());
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

    private void getFirstLevelTableau(DemandeDTO demande, JsonNode contenuSource, JsonNode section,
            boolean isPdfRecap, StringBuilder html, List<SourceFiableDTO> donneesCertifiees) {

        JsonNode jsonNode = this.getNode(demande.getContenuTrad(), section);
        if (jsonNode instanceof ArrayNode newValeurs && !newValeurs.isEmpty()) {
            String classPdfRecap = isPdfRecap ? "pdf-recap" : "";
            html.append(
                            "<dd style=\"width: 100%\"><table id=\"datatable-demandes-recap\" class=\"table table-striped recaptable")
                    .append(classPdfRecap).append("\">");
            JsonNode columns = section.get(COLUMNS);
            String style = isPdfRecap ? String.format(" style=\"font-size: %spx\"",
                    demarchesDataProvider.getTaileTexteEnteteTableauxRecapPdf()) : "";
            html.append("<thead><tr onclick=\"switchTS()\"").append(style).append(">");
            for (JsonNode column : columns) {
                html.append("<th>").append(column.get(LABEL).asText()).append("</th>");
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

    private void contructSimpleTableau(DemandeDTO demande, JsonNode section, boolean isPdfRecap, StringBuilder html,
            List<SourceFiableDTO> donneesCertifiees) throws IllegalArgumentException, SecurityException {
        JsonNode jsonNode = this.getNode(demande.getContenuTrad(), section);
        if (jsonNode instanceof ArrayNode valeurs && !valeurs.isEmpty()) {
            JsonNode columns = section.get(COLUMNS);
            Iterator<JsonNode> it = valeurs.elements();
            html.append("<tbody>");
            while (it.hasNext()) {
                JsonNode valeur = it.next();
                html.append("<tr>");
                for (JsonNode column : columns) {
                    String value = getSecondLevelHTML(valeur, column, isPdfRecap, true, donneesCertifiees);
                    String result = StringUtils.isNoneBlank(value) ? value : "";
                    html.append("<td>").append(result).append(CLOSING_TD);
                }
                html.append(CLOSING_TR);
            }
            html.append("</tbody></table></dd>");
        }
    }

    private void contructTableauWithDiff(JsonNode contenuSource, JsonNode section, boolean isPdfRecap,
            StringBuilder html, Iterator<JsonNode> itNew, List<SourceFiableDTO> donneesCertifiees) {
        JsonNode jsonNode = this.getNode(contenuSource, section);
        html.append("<tbody>");
        JsonNode columns = section.get(COLUMNS);
        if (jsonNode instanceof ArrayNode demandeSourceValeurs && !demandeSourceValeurs.isEmpty()) {
            Iterator<JsonNode> itDemandeSource = demandeSourceValeurs.elements();
            while (itNew.hasNext() && itDemandeSource.hasNext()) {
                JsonNode newValeur = itNew.next();
                JsonNode demandeSourceValeur = itDemandeSource.next();
                html.append("<tr>");
                for (JsonNode column : columns) {
                    String valueSource = getSecondLevelHTML(demandeSourceValeur, column, isPdfRecap, true,
                            donneesCertifiees);
                    String value = getSecondLevelHTML(newValeur, column, isPdfRecap, true, donneesCertifiees);
                    this.completeTd(html, valueSource, value, isPdfRecap);
                }
                html.append(CLOSING_TR);
            }
        }
        if (itNew.hasNext()) {
            while (itNew.hasNext()) {
                JsonNode newValeur = itNew.next();
                html.append("<tr>");
                for (JsonNode column : columns) {
                    String value = getSecondLevelHTML(newValeur, column, isPdfRecap, true, new ArrayList<>());
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

    private String getSecondLevelHTML(JsonNode node, JsonNode champ, boolean isPdfRecap, boolean pourTableau,
            List<SourceFiableDTO> donneesCertifiees) throws IllegalArgumentException, SecurityException {
        String type = champ.has("type") ? champ.get("type").asText() : "";
        if (StringUtils.equals(type, "chaine") || StringUtils.equals(type, "texte") || StringUtils.equals(type,
                "choix")) {
            JsonNode node0 = getNode(node, champ);
            if (node0 == null || node0 instanceof NullNode) {
                return "";
            }
            return escape(node0.asText(), isPdfRecap);
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

    private JsonNode getNode(JsonNode node, JsonNode champ) {
        return getNode(node, champ, null);
    }

    private JsonNode getNode(JsonNode node, JsonNode champ, String complementChemin) {
        String chemin = getChemin(champ);
        if (complementChemin != null && !complementChemin.isEmpty()) {
            chemin += "/" + complementChemin;
        }
        return node.at(chemin);
    }

    private String getChemin(JsonNode champ) {
        String chemin = champ.get("path").asText().replace(CONTENU, "/").replace(".", "/");
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

    private String buildDateHTML(JsonNode node, JsonNode champ) {
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

    private void buildAdresseHTML(StringBuilder adresseBuilder, JsonNode node, JsonNode champ, boolean isPdfRecap) {
        String idPrefix = champ.has(ID_PREFIX) ? champ.get(ID_PREFIX).asText() : "";
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

    private void buildComplementAdresseHTML(StringBuilder adresseBuilder, JsonNode node, JsonNode champ,
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

    private void buildComplementAdressePageHTML(StringBuilder adresseBuilder, JsonNode champ, String codePostal,
            String ville, String pays, List<SourceFiableDTO> donneesCertifiees, boolean isPdfRecap) {
        String imgTag = this.getImgTag(isPdfRecap);
        String idPrefix = champ.has(ID_PREFIX) ? champ.get(ID_PREFIX).asText() : "";
        if (StringUtils.isNotBlank(codePostal)) {
            String path = champ.has(CODE_POSTAL) ? champ.get(CODE_POSTAL).asText() : "";
            this.completeSpan(idPrefix + "-cp", adresseBuilder, donneesCertifiees, "Code postal", codePostal, path,
                    imgTag);
        }
        if (StringUtils.isNotBlank(ville)) {
            String path = champ.has(VILLE) ? champ.get(VILLE).asText() : "";
            this.completeSpan(idPrefix + "-ville", adresseBuilder, donneesCertifiees, "Ville", ville, path, imgTag);
        }
        if (StringUtils.isNotBlank(pays)) {
            String path = champ.has("pays") ? champ.get("pays").asText() : "";
            this.completeSpan(idPrefix + "-pays", adresseBuilder, donneesCertifiees, "Pays", pays, path, imgTag);
        }
    }

    private String buildChoixMultipleHTML(JsonNode node, JsonNode champ) {
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

    private String buildTelephoneHTML(JsonNode node, JsonNode champ, boolean isPdfRecap) {
        String indicatif = getNode(node, champ, "indicatif").textValue();
        String numero = escape(getNode(node, champ, "numero").textValue(), isPdfRecap);
        return AfBackUtils.genererTelephone(indicatif, numero);
    }

    private String getSourceValue(JsonNode contenuSource, JsonNode champ, boolean isPdfRecap,
            List<SourceFiableDTO> donneesCertifiees) {
        if (contenuSource != null) {
            return getSecondLevelHTML(contenuSource, champ, isPdfRecap, false, donneesCertifiees);
        }
        return null;
    }
}
