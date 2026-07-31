package mc.gouv.xaf.back.service.utils;

import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.COURRIER;
import static mc.gouv.xaf.shared.enums.DemandeCanalEnum.GUICHET_PHYSIQUE;

import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.apiclient.AfApiClient;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.service.itg.file.service.FileClient;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.logon.dto.Droit;
import mc.gouv.xaf.back.service.itg.logon.dto.Role;
import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.back.service.itg.sms.impl.SmsClient;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import mc.gouv.xaf.shared.dto.DemandeFlatDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueAffichageDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueContenuDTO;
import mc.gouv.xaf.shared.dto.DemandeHistoriqueDTO;
import mc.gouv.xaf.shared.dto.DemandeUsagerDTO;
import mc.gouv.xaf.shared.dto.MarqueurDTO;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.shared.dto.PaysDTO;
import mc.gouv.xaf.shared.dto.PropertiesListEntityDTO;
import mc.gouv.xaf.shared.dto.TypedocDTO;
import mc.gouv.xaf.shared.enums.TypeConnexionUsagerEnum;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.Strings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

/**
 * Classe utilitaire pour le projet xaf-back
 *
 * @author qdeme
 */
@Component
@RequiredArgsConstructor
public class AfBackUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBackUtils.class);

    public static final String SMS_ENVOYE_STATUT = "Envoyé";

    public static final String MAIL_METADATA_DEMANDEID = "MC_DEMANDEID";

    public static final String SMS_METADATA_DEMANDEID = "MC_DEMANDEID";

    public static final String STATUT_PUBLIC_SUPPRIMEE = "SUPPRIMEE";

    public static final String DEFAULT_FRENCH_DATE_FORMAT = "dd/MM/yyyy";

    // 24 hours time format
    public static final String DEFAULT_FRENCH_TIME_FORMAT = "HH:mm";

    // 24 hours time format with seconds
    public static final String DEFAULT_FRENCH_TIME_FORMAT_SECONDS = "HH:mm:ss";

    // French date format with 24 hours
    public static final String DEFAULT_FRENCH_DATE_HOURS_FORMAT = "dd/MM/yyyy HH:mm";

    // French date format with 24 hours
    public static final String DEFAULT_FRENCH_DATE_HOURS_MINUTES_SECONDS_FORMAT = "dd/MM/yyyy HH:mm:ss";

    // Format de date en Anglais
    public static final String DEFAULT_ENGLISH_DATE_FORMAT = "MM/dd/yyyy";

    // Suffix pour l'unicité des fichiers
    public static final String FILE_DATE_SUFFIX_FORMAT = "HHmmssSSS";
    public static final String FILE_DATE_AND_TIME_SUFFIX_FORMAT = "yyyyMMddHHmmssSS";

    // Pattern pour les dates MCONNECT
    public static final String MCONNECT_DATE_AND_TIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss.SSSXXX";

    public static final DateTimeFormatter DTF_AAAA_MM_JJ = DateTimeFormatter.ofPattern("yyyy/MM/dd");

    public static final String MESSAGE_ERREURS_FORMULAIRE = "Le formulaire contient des erreurs.";

    // Préfix de la meta d'un fichier indiquant l'ID de la section correspondante
    public static final String META_FICHIER_SECTION_PREFIX = "SECTION_ID_";

    public static final String CODE_ALPHA2_APATRIDE = "SP";
    public static final String CODE_ALPHA2_APATRIDE_NOMEN = "XX";

    public static final String CODE_ALPHA2_NONCONNU = "ZZ";

    public static final String CODE_ALPHA3_NATIONALITEE_NONCONNU = "XXX";

    public static final String CODE_ALPHA3_PAYS_NONCONNU = "000";

    public static final String CODE_ALPHA3_APATRIDE = "XXA";

    @Lazy
    private final GouvPropertiesResolver gouvPropertiesResolver;

    private FileClient fileClient = null;

    private SmsClient smsClient = null;

    @Lazy
    private final UtilisateursCache utilisateursCache;

    @Lazy
    private final DemarchesDataProvider demarchesDataProvider;

    @Lazy
    private final MessageSource messageSource;

    @Lazy
    private final UtilisateursUtils utilisateursUtils;

    @Lazy
    private final MotifsService motifsService;

    @Lazy
    private final PaysCache paysCache;

    private AfApiClient afApiClient2Tiers = null;

    public static final short GENDER_MR_INDEX = 0;
    public static final short GENDER_MME_INDEX = 1;
    public static final short GENDER_MLLE_INDEX = 2;

    static final JsonMapper mapper = JsonMapper.builder().disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();

    public static String getAuthenticatedAgentId() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object o = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (o instanceof User user) {
                return user.getMatricule();
            }
        }

        return null;
    }

    /**
     * Retourne le code alpha2 de la nationalitée en fonction du code alpha3 donné en paramètre
     *
     * @param alpha3Code
     * @return
     */
    public static String getAlpha2Code(String alpha3Code) {
        Map<String, String> isoCodeMap = new HashMap<>();
        for (String currentCountry : Locale.getISOCountries()) {
            Locale currentCountryLocaleFr = Locale.of("fr", currentCountry);
            isoCodeMap.put(currentCountryLocaleFr.getISO3Country().toUpperCase(), currentCountry);
        }
        return isoCodeMap.get(alpha3Code);
    }

    /**
     * Convertit un code ISO Alpha-2 en code ISO Alpha-3.
     *
     * @param alpha2
     *         Code ISO Alpha-2 à convertir.
     * @param fromPays
     *         Indique si la conversion concerne un pays (true) ou une nationalité (false).
     * @return Le code ISO Alpha-3 correspondant, ou null si le code Alpha-2 est invalide ou inconnu.
     */
    public String getAlpha3IsoCodeFromAlpha2(String alpha2, boolean fromPays) {
        if (StringUtils.isBlank(alpha2)) {
            return null;
        }
        if (alpha2.equals(CODE_ALPHA2_APATRIDE) || alpha2.equals(CODE_ALPHA2_APATRIDE_NOMEN)) {
            return fromPays ? CODE_ALPHA3_PAYS_NONCONNU : CODE_ALPHA3_APATRIDE;
        }
        if (alpha2.equals(CODE_ALPHA2_NONCONNU)) {
            return fromPays ? CODE_ALPHA3_PAYS_NONCONNU : CODE_ALPHA3_NATIONALITEE_NONCONNU;
        }
        PaysDTO paysDTO = paysCache.get(alpha2);
        return paysDTO != null ? paysDTO.getCodeAlpha3() : null;
    }

    /**
     * Convertit un code ISO Alpha-3 en code ISO Alpha-2.
     *
     * @param alpha3
     *         Code ISO Alpha-3 à convertir.
     * @param fromPays
     *         Indique si la conversion concerne un pays (true) ou une nationalité (false).
     * @return Le code ISO Alpha-2 correspondant, ou null si le code Alpha-3 est invalide ou inconnu.
     */
    public String getAlpha2IsoCodeFromAlpha3(String alpha3, boolean fromPays) {
        if (StringUtils.isBlank(alpha3)) {
            return null;
        }
        if (alpha3.equals(CODE_ALPHA3_APATRIDE) || alpha3.equals(CODE_ALPHA3_PAYS_NONCONNU)) {
            return fromPays ? CODE_ALPHA2_NONCONNU : CODE_ALPHA2_APATRIDE_NOMEN;
        }
        Collection<PaysDTO> values = paysCache.getValues();
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.stream().filter(paysDTO -> Strings.CS.equals(paysDTO.getCodeAlpha3(), alpha3))
                .findFirst().map(PaysDTO::getCode).orElse(null);
    }

    /**
     * Utilisé dans les fichiers html/thymeleaf
     */
    public String getLogonUrl() {
        return gouvPropertiesResolver.getGouvSharedLogonUrl();
    }

    /**
     * Génère un suffixe de fichier en fonction de la date de génération conformément au pattern suivant: HHmmssSSS
     */
    public static String generateFileDateSuffix() {
        return new SimpleDateFormat(FILE_DATE_SUFFIX_FORMAT).format(new Date());
    }

    /**
     * Génère un suffixe de fichier en fonction de la date de génération conformément au pattern suivant:
     * YYYYMMDDHHmmssSS
     */
    public static String generateFileDateAndTimeSuffix() {
        return new SimpleDateFormat(FILE_DATE_AND_TIME_SUFFIX_FORMAT).format(new Date());
    }

    public FileClient getFileClient() {
        if (fileClient == null) {
            fileClient = new FileClient(gouvPropertiesResolver.getFileUrl(), gouvPropertiesResolver.getFileJwt());
        }
        return fileClient;
    }

    public SmsClient getSmsClient() {
        if (smsClient == null) {
            String smsUrl = gouvPropertiesResolver.getSmsUrl();
            String smsJwt = gouvPropertiesResolver.getSmsJwt();
            smsClient = new SmsClient(smsUrl, smsJwt);
        }
        return smsClient;
    }


    /**
     * Permet de récupérer une donnée d'une demande
     */
    public static String getDemandeData(DemandeDTO demande, String key) {
        if (demande.getData() != null) {
            for (DemandeDataDTO demandeData : demande.getData()) {
                if (demandeData.getKey().equals(key)) {
                    return demandeData.getValue();
                }
            }
        }
        return null;
    }

    public static String getYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    /**
     * Permet de générer un DemandeFlatDTO à partir d'un DemandeDTO
     *
     * @param demande
     * @return
     */
    public DemandeFlatDTO demandeDTOToDemandeFlatDTO(DemandeDTO demande) {
        DemandeFlatDTO flat = new DemandeFlatDTO();
        flat.setAgentAffecteId(demande.getAgentAffecteId());
        String agent = demande.getAgent() != null ? demande.getAgent().getNomAffichage() : StringUtils.EMPTY;
        flat.setAgentAffecteNom(getSafeString(agent));
        flat.setCanal(demande.getCanal() != null ? demande.getCanal().getLibelle() : StringUtils.EMPTY);
        flat.setCourrierDateReception(convertDateToString(demande.getCourrierDateReception()));
        flat.setCourrierRefInterne(getSafeString(demande.getCourrierRefInterne()));
        flat.setDateCreation(convertDateToString(demande.getDateCreation()));
        flat.setDernierStatut(demande.getDernierStatut().getLibelle());
        flat.setIdentifiant(getSafeString(demande.getIdentifiant()));
        flat.setLangue(getSafeString(demande.getLangue()));
        flat.setObservations(getSafeString(demande.getObservations()));
        flat.setPkDemandes(demande.getPkDemandes());
        DemandeUsagerDTO usager = demande.getUsager();
        if (usager != null) {
            flat.setUsagerNom(getSafeString(usager.getNom()));
            flat.setUsagerPrenom(getSafeString(usager.getPrenom()));
            flat.setUsagerEmail(getSafeString(usager.getEmail()));
            flat.setUsagerId(usager.getId());
        }
        // motif
        if (demande.getDernierStatut() != null && demande.getDernierStatut().getCodeMotif() != null) {
            MotifDTO motif = motifsService.getMotif(demande.getDernierStatut().getCodeMotif(), "fr");
            flat.setMotif(motif != null ? motif.getLibelle() : null);
        }
        // marqueurs
        flat.setMarqueurs(demande.getMarqueursTrad());
        return flat;
    }

    public static String getTitreStr(Short titre) {
        if (titre == null) {
            return "";
        } else if (titre == 0) {
            return "Monsieur";
        } else if (titre == 1) {
            return "Madame";
        } else if (titre == 2) {
            return "Mademoiselle";
        } else {
            return "";
        }
    }

    /**
     * Utilisé dans les fichiers html/thymeleaf
     */
    public String getStatusLibelleFromName(String status) {
        return demarchesDataProvider.getStatusLibelle(status);
    }

    public String getExportLibelle() {
        return demarchesDataProvider.getExportLibelle();
    }

    public String getRecapOrientation() {
        return demarchesDataProvider.getRecapOrientation();
    }

    /**
     * Retourne la classe CSS de la couleur associée à un statut Utilisé dans les fichiers html/thymeleaf
     *
     * @param statutName
     * @return
     */
    public String getStatusColorClass(String statutName) {
        if (statutName == null || statutName.isEmpty()) {
            return "default-status-color";
        }
        return statutName.toLowerCase().replace("_", "-");
    }

    public String getCivilite(Short titre, String locale) {
        return messageSource.getMessage("civilite." + titre, null, Locale.of(locale));
    }

    /**
     * Permet de retourner la liste des agents ayant un certain rôle
     *
     * @param
     * @return
     */
    public Set<User> getAgentsWithRoles(String[] rolesList) {
        Set<User> destinataires = new HashSet<>();
        String codeAppli = gouvPropertiesResolver.getDemarcheId();
        List<User> agents = new ArrayList<>(utilisateursCache.getAll().values());
        for (User agent : agents) {
            boolean toAdd = this.isToAdd(rolesList, codeAppli, agent);
            if (toAdd) {
                destinataires.add(agent);
            }
        }
        return destinataires;
    }

    private boolean isToAdd(String[] rolesList, String codeAppli, User agent) {
        boolean toAdd = false;
        Set<Role> agentRoles = agent.getRoles();
        for (Role role : agentRoles) {
            if (role.getAppli().getCode().equals(codeAppli)) {
                for (Droit droit : role.getDroits()) {
                    for (String roleFromList : rolesList) {
                        if (roleFromList.trim().equals(droit.getCode())) {
                            toAdd = true;
                            break;
                        }
                    }
                }

            }
        }
        return toAdd;
    }

    public String convertDateToString(final Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DEFAULT_FRENCH_DATE_FORMAT).format(date);
    }

    public String convertDateToTimeString(final Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DEFAULT_FRENCH_TIME_FORMAT).format(date);
    }

    public String convertDateTimeToString(final Date date) {
        if (date == null) {
            return "";
        }
        return new SimpleDateFormat(DEFAULT_FRENCH_DATE_HOURS_FORMAT).format(date);
    }

    public static String genererTelephone(String indicatif, String numero) {
        String indic = StringUtils.isNotBlank(indicatif) ? "(" + convertTelIndicateur(indicatif) + ") " : "";
        String num = StringUtils.isNotBlank(numero) ? numero : "";
        return indic + num;
    }

    /**
     * Utilisé dans certains exports excel
     */
    public static String choixMultipleToString(final List<String> choixMultiple) {
        return String.join(", ", choixMultiple);
    }

    /**
     * Utilisé dans certains exports excel
     */
    public static String tableauToString(final List<Map<String, String>> tableau, final String marqueur) {
        return tableau.stream().map(map -> map.get(marqueur)) // extrait la valeur associée à la clé "marqueur"
                .filter(Objects::nonNull)      // ignore les valeurs nulles
                .collect(Collectors.joining(", "));
    }

    public static String changeDateStringFormat(final String dateString) {
        return changeDateStringFormat(DEFAULT_FRENCH_DATE_FORMAT, dateString);
    }

    public static String changeDateStringFormat(final String format, final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return "";
        }
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                    .format(DateTimeFormatter.ofPattern(format));
        } catch (DateTimeParseException e) {
            // impossible de parser la date, elle est sûrement déjà au bon format
            return dateString;
        }
    }

    public static String changeTimeStringFormat(final String dateString) {
        return changeDateStringFormat(DEFAULT_FRENCH_TIME_FORMAT, dateString);
    }

    public static String changeDateTimeStringFormat(final String dateString) {
        return changeDateStringFormat(DEFAULT_FRENCH_DATE_HOURS_FORMAT, dateString);
    }

    public static String getSafeString(final String value) {
        return StringUtils.isBlank(value) ? StringUtils.EMPTY : value;
    }

    /**
     * Permet de récupérer le flag indiquant que la démarche peut générer des courriers
     *
     * @return
     */
    public boolean getDemarcheCanGenerateCourriers() {
        return demarchesDataProvider.getDemarcheCanGenerateCourriers();
    }

    /**
     * Permet de savoir si la démarche prend en charge les périodes d'ouverture
     *
     * @return
     */
    public boolean getDemarcheCanHandlePeriodesOuverture() {
        return demarchesDataProvider.getDemarcheCanHandlePeriodesOuverture();
    }

    /**
     * Permet de savoir si la démarche prend en charge des propriétés
     *
     * @return
     */
    public boolean getDemarcheCanHandleProperties() {
        return demarchesDataProvider.getDemarcheCanHandleProperties();
    }

    /**
     * Permet de savoir si la démarche envoie des SMS ou non
     *
     * @return
     */
    public boolean getDemarcheCanSendSms() {
        return demarchesDataProvider.getDemarcheCanSendSms();
    }

    /**
     * Permet de parser une string en un objet Date au format déclaré dans AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT
     *
     * @throws ParseException
     *         en cas d'erreurs de parsing du SimpleDateFormat
     */
    public static Date convertDate(String dateStr, boolean endDate) throws ParseException {
        Date date = null;
        if (StringUtils.isNotEmpty(dateStr)) {
            SimpleDateFormat sdf = new SimpleDateFormat(AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT);
            date = sdf.parse(dateStr);
            if (endDate) {
                // On applique le dernier instant de la journée
                Calendar cal = Calendar.getInstance();
                cal.setTime(date);
                cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
                cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
                cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
                date = cal.getTime();
            }
        }
        return date;
    }

    /**
     * Permet de savoir si la démarche prend en charge la gestion des agents (DENJS)
     *
     * @return
     */
    public boolean getDemarcheCanHandleDenjsGestionAgents() {
        return demarchesDataProvider.getDemarcheCanHandleDenjsGestionAgents();
    }

    public static String escapeChars(String str) {
        String result = " ";
        if (StringUtils.isNotBlank(str)) {
            if (str.contains("&#28;")) {
                str = Strings.CS.replace(str, "&#28;", " ");
            }
            if (str.contains("\u001C")) {
                str = Strings.CS.replace(str, "\u001C", " ");
            }
            if (str.contains("\u001A")) {
                str = Strings.CS.replace(str, "\u001A", " ");
            }
            if (str.contains("\u0017")) {
                str = Strings.CS.replace(str, "\u0017", " ");
            }
            result = str;
        }
        return result;
    }

    /**
     * Echappe les caractères posant problèmes dans les logs selon la règle Sonar javasecurity:S5145
     */
    public static String logSafe(String str) {
        return str != null ? str.replaceAll(SharedMessages.UNSAFE_CHARS, "_") : null;
    }

    /**
     * Retourne le nom d'un utilisateur à partir de son matricule
     * <br>
     * Attention cette méthode est appelée dans les pages HTML avec thymeleaf, bien vérifier les appels lors d'une
     * suppression
     *
     * @deprecated : Utiliser la méthode de {@link UtilisateursUtils}
     */
    @Deprecated
    public String getUserNameFromID(String matricule) {
        return utilisateursUtils.getUserNameFromID(matricule);
    }

    @SuppressWarnings("unchecked")
    public static Map<String, String> getListFromDemProperty(String demPropertyValue) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(demPropertyValue, Map.class);
        } catch (JacksonException e) {
            LOGGER.error("Erreur lors de AfBackUtils.getListFromDemProperty()", e);
        }
        return Collections.emptyMap();
    }

    public static PropertiesListEntityDTO[] parserPropertiesListJson(String json) {
        ObjectMapper mapper = new ObjectMapper();
        try {
            return mapper.readValue(json, PropertiesListEntityDTO[].class);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de AfBackUtils.parserPropertiesListJson()", e);
        }
        return new PropertiesListEntityDTO[0];
    }

    /**
     * Utilisé dans certains exports excel
     */
    public static String convertTelIndicateur(String indicateur) {
        return Strings.CS.replace(indicateur, "t", "+");
    }

    public static Double parseDoubleSafe(String texte) {
        double parsed = 0.0;
        if (StringUtils.isNotEmpty(texte)) {
            String safe = Strings.CS.replace(texte, ",", ".", -1);
            try {
                parsed = Double.parseDouble(safe);
            } catch (NumberFormatException e) {
                LOGGER.error("Impossible de parser ce nombre en double : {}", e.getMessage());
            }
        }
        return parsed;
    }

    public static String formatDoubleToCurrency(Double number) {
        return formatDoubleToCurrency(number, "fr");
    }

    public static String formatDoubleToCurrency(Double number, String langue) {
        Locale local = Strings.CS.equals("fr", langue) ? Locale.FRANCE : Locale.US;
        NumberFormat formatter = NumberFormat.getCurrencyInstance(local);
        DecimalFormatSymbols decimalFormatSymbols = ((DecimalFormat) formatter).getDecimalFormatSymbols();
        decimalFormatSymbols.setCurrencySymbol("€");
        ((DecimalFormat) formatter).setDecimalFormatSymbols(decimalFormatSymbols);
        return formatter.format(number);
    }

    public static String getSectionFromMetaFichier(String meta) {
        if (StringUtils.isBlank(meta)) {
            return null;
        }
        for (String token : meta.split(";")) {
            if (token.startsWith(META_FICHIER_SECTION_PREFIX)) {
                token = token.replace(META_FICHIER_SECTION_PREFIX, "");
                return token;
            }
        }
        return null;
    }

    /*
     * Retourne le texte tronqué avec "(...)" à la fin (pour affichage)
     */
    public static String tronquerTextePourAffichage(String texte, Integer nbChars) {
        if (texte != null) {
            String ret = texte.substring(0, (texte.length() > nbChars ? nbChars : texte.length()));
            if (ret.length() > 3000) {
                ret += " (...)";
            }
            return ret;
        }
        return null;
    }

    public static String mConnectDateToString(Date date) {
        return new SimpleDateFormat(MCONNECT_DATE_AND_TIME_FORMAT).format(date);
    }

    /**
     * Permet de savoir si la démarche prend en charge des propriétés
     *
     * @return
     */
    public boolean isTypedocApplicable(String typedoc) {
        return demarchesDataProvider.isTypedocApplicable(typedoc);
    }

    public AfApiClient getAfApiClient2Tiers() {
        if (afApiClient2Tiers == null) {
            afApiClient2Tiers = new AfApiClient(gouvPropertiesResolver.get2TiersBoUrl(),
                    gouvPropertiesResolver.get2TiersBoJwt());
        }
        return afApiClient2Tiers;
    }

    public Object getMarqueurValue(JsonNode contenu, String path, Map<String, MarqueurDTO> marqueursMap) {
        if (path == null) {
            return "";
        }

        JsonNode node = getNodeFromPath(contenu, path);
        if (node == null || (node.isString() && "null".equals(node.asString()))) {
            return "";
        }

        // Si c'est un texte simple
        if (node.isString()) {
            return node.asString();
        }

        // Si c'est un array
        if (node.isArray()) {
            // Si l'array contient uniquement des chaînes de caractères, c'est un type choixMultiple
            if (!node.isEmpty() && node.get(0).isString()) {
                List<String> choices = new ArrayList<>(node.size());
                node.forEach(arrayElement -> {
                    if (arrayElement.isString()) {
                        choices.add(arrayElement.asString());
                    }
                });
                return choices;
            }

            // Sinon, c'est un type tableau
            List<Map<String, String>> list = new ArrayList<>(node.size());

            node.forEach(arrayElement -> {
                Map<String, String> map = new HashMap<>();
                arrayElement.properties().forEach(tableauDonnee -> {
                    String donneeTableauPath = path + "." + tableauDonnee.getKey();
                    // Récupération directe du marqueur
                    MarqueurDTO marqueur = marqueursMap.get(donneeTableauPath);
                    if (marqueur != null) {
                        putMarqueurTableau(map, tableauDonnee.getValue(), marqueur);
                    } else {
                        // Vérifier si le chemin a un suffixe connu
                        String[] suffixes = { "ligne1", "ligne2", "ligne3", "ville", "pays", "codePostal", "bic",
                                "iban", "titulaire", "indicatif", "numero" };
                        for (String suffixe : suffixes) {
                            String suffixedPath = donneeTableauPath + "." + suffixe;
                            marqueur = marqueursMap.get(suffixedPath);
                            if (marqueur != null) {
                                var suffixNode = tableauDonnee.getValue().get(suffixe);
                                if (suffixNode != null) {
                                    putMarqueurTableau(map, suffixNode, marqueur);
                                }
                            }
                        }
                    }
                });
                list.add(map);
            });
            return list;
        }

        return "";
    }

    private void putMarqueurTableau(Map<String, String> map, JsonNode tableauDonneeNode, MarqueurDTO marqueurFound) {
        String donneeTableauValue = "";
        if (tableauDonneeNode != null && !tableauDonneeNode.isNull()) {
            if (tableauDonneeNode.isString() && !"null".equals(tableauDonneeNode.asString())) {
                // cas texte simple dans tableau
                donneeTableauValue = tableauDonneeNode.asString();
            } else if (tableauDonneeNode.isArray()) {
                // cas choix multiple dans tableau, on stocke sous format "VALEUR1, VALEUR2"
                donneeTableauValue = StreamSupport.stream(tableauDonneeNode.spliterator(), false)
                        .filter(JsonNode::isString).map(JsonNode::asString).collect(Collectors.joining(", "));
            }
        }
        map.put(marqueurFound.getIdentifiant(), donneeTableauValue);
    }

    public static JsonNode getNodeFromPath(JsonNode contenu, String path) {
        String chemin = getCheminRelatif(path);
        return contenu.at(chemin);
    }

    private static String getCheminRelatif(String path) {
        return path.replace("contenu.", "/").replace(".", "/");
    }

    public static void setNodeValue(JsonNode contenu, String path, String nouvelleValeur) {
        // [contenu,donnee,demandeur,prenom]
        List<String> donneeExterneKeyArray = new ArrayList<>(Arrays.asList(path.split("\\.")));
        // [donnee,demandeur,prenom]
        donneeExterneKeyArray.removeFirst();
        //	 "[donnee,demandeur]" / field = prenom
        String field = donneeExterneKeyArray.removeLast();
        // "/donnee/demandeur"
        String p = "/" + String.join("/", donneeExterneKeyArray);
        // Vérifier si le nœud existe
        JsonNode targetNode = "/".equals(p) ? contenu : contenu.at(p);
        if (!targetNode.isMissingNode()) {
            ((ObjectNode) targetNode).put(field, nouvelleValeur);
        }
    }

    public static void setNodeValueArray(JsonNode contenu, String path, ArrayNode nouvelleValeur) {
        // [contenu,donnee,demandeur,prenom]
        List<String> donneeExterneKeyArray = new ArrayList<>(Arrays.asList(path.split("\\.")));
        // [donnee,demandeur,prenom]
        donneeExterneKeyArray.removeFirst();
        //     "[donnee,demandeur]" / field = prenom
        String field = donneeExterneKeyArray.removeLast();

        JsonNode parentNode;
        if (donneeExterneKeyArray.isEmpty()) {
            parentNode = contenu;
        } else {
            // "/donnee/demandeur"
            String parentPath = "/" + String.join("/", donneeExterneKeyArray);
            parentNode = contenu.at(parentPath);
        }
        if (!parentNode.isMissingNode()) {
            ((ObjectNode) parentNode).set(field, nouvelleValeur);
        }
    }

    /**
     * Permet de convertir une ligne d'historique DEM en une ligne d'historique TS avec tous les détails spécifiques au
     * TS.
     *
     * @param demHisto
     * @return
     */
    public DemandeHistoriqueAffichageDTO histoDem2Ts(DemandeHistoriqueDTO demHisto) {
        DemandeHistoriqueAffichageDTO tsHisto = new DemandeHistoriqueAffichageDTO();
        tsHisto.setDemHistorique(demHisto);
        DemandeHistoriqueContenuDTO contenu = null;
        ObjectMapper mapper = new ObjectMapper();
        try {
            contenu = mapper.treeToValue(demHisto.getContenu(), DemandeHistoriqueContenuDTO.class);
        } catch (JacksonException e) {
            LOGGER.error("Erreur", e);
        }
        tsHisto.setContenu(contenu);
        return tsHisto;
    }

    /**
     * Permet de convertir un ensemble de lignes d'historique DEM en un ensemble de lignes d'historique TS avec tous les
     * détails spécifiques au TS.
     *
     * @param demHistos
     * @return
     */
    public List<DemandeHistoriqueAffichageDTO> histoDem2Ts(List<DemandeHistoriqueDTO> demHistos) {

        // Trier l'historique, au cas où (#9597)
        demHistos.sort(new DemandeHistoriqueComparator());

        List<DemandeHistoriqueAffichageDTO> tsHistos = new ArrayList<>();
        for (DemandeHistoriqueDTO demHisto : demHistos) {
            tsHistos.add(histoDem2Ts(demHisto));
        }
        return tsHistos;
    }

    public String getUtilisateurAffecte(DemandeDTO demande) {
        String utilisateurAffecte = StringUtils.EMPTY;
        String agentId = demande.getAgent() != null ? demande.getAgent().getId() : null;
        if (StringUtils.isNotBlank(agentId)) {
            try {
                User u = utilisateursCache.get(agentId);
                if (u != null) {
                    utilisateurAffecte = u.getNom();
                }
            } catch (Exception exception) {
                LOGGER.error(
                        "Erreur de recuperation de l'utilisateur affecté à la demande {} à partir de son matricule {}",
                        demande.getPkDemandes(), agentId, exception);
            }
        }

        return utilisateurAffecte;
    }

    public static String genererAdresseComplete(DemandeDTO demande, String marqueurIdentifiant) {
        return genererAdresseComplete(demande, marqueurIdentifiant, "\n");
    }

    public static String genererAdresseComplete(DemandeDTO demande, String marqueurIdentifiant, String separateur) {
        String codePostal = demande.getMarqueur(marqueurIdentifiant + "CodePostal");
        String ville = demande.getMarqueur(marqueurIdentifiant + "Ville");
        String adresseComplete = genererAdresse(demande, marqueurIdentifiant, separateur);
        if (!StringUtils.isEmpty(codePostal) && !StringUtils.isEmpty(ville)) {
            adresseComplete += separateur + escapeChars(codePostal) + " " + escapeChars(ville);
        }
        return adresseComplete;
    }

    public static String genererAdresse(DemandeDTO demandeDTO, String marqueurIdentifiant) {
        return genererAdresse(demandeDTO, marqueurIdentifiant, "\n");
    }

    public static String genererAdresseInline(DemandeDTO demandeDTO, String marqueurIdentifiant) {
        return genererAdresse(demandeDTO, marqueurIdentifiant, " - ");
    }

    public static String genererAdresseCompleteInline(DemandeDTO demandeDTO, String marqueurIdentifiant) {
        return genererAdresseComplete(demandeDTO, marqueurIdentifiant, " - ");
    }

    private static String genererAdresse(DemandeDTO demandeDTO, String marqueurIdentifiant, String separateur) {
        StringBuilder adresse = new StringBuilder();

        String ligne1 = escapeChars(demandeDTO.getMarqueur(marqueurIdentifiant + "Ligne1"));
        String ligne2 = demandeDTO.getMarqueur(marqueurIdentifiant + "Ligne2");
        String ligne3 = demandeDTO.getMarqueur(marqueurIdentifiant + "Ligne3");

        if (!StringUtils.isEmpty(ligne1)) {
            adresse.append(ligne1);
        }
        if (!StringUtils.isEmpty(ligne2)) {
            if (!adresse.isEmpty()) {
                adresse.append(separateur);
            }
            adresse.append(escapeChars(ligne2));
        }
        if (!StringUtils.isEmpty(ligne3)) {
            if (!adresse.isEmpty()) {
                adresse.append(separateur);
            }
            adresse.append(escapeChars(ligne3));
        }

        return adresse.toString();
    }

    /**
     * Utilisé dans les template doc
     *
     * @param date
     * @param pattern
     * @return
     */
    public static String formatDate(String date, String pattern) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(pattern, Locale.FRANCE);
        DateFormat inputFormat = new SimpleDateFormat(DEFAULT_FRENCH_DATE_FORMAT);
        try {
            return outputFormat.format(inputFormat.parse(date));
        } catch (ParseException e) {
            return "";
        }
    }

    public static String getSimpleDate(String fullDate) {
        if (StringUtils.isBlank(fullDate)) {
            return "";
        }

        String dateRegex = "(\\d{4})-(\\d{2})-(\\d{2})";
        String simpleDate = "";
        Matcher matcher = Pattern.compile(dateRegex).matcher(fullDate);

        if (matcher.find()) {
            simpleDate = String.format("%s/%s/%s", matcher.group(3), matcher.group(2), matcher.group(1));
        }

        return StringUtils.isNotBlank(simpleDate) ? simpleDate : fullDate;
    }

    /**
     * Utilisé dans les template doc
     *
     * @param pattern
     * @return
     */
    public static String dateCourante(String pattern) {
        SimpleDateFormat outputFormat = new SimpleDateFormat(pattern, Locale.FRANCE);
        return outputFormat.format(new Date());
    }

    /**
     * Permets de déterminer le type de connexion à partir d'une demande
     *
     * @param demande
     * @return
     */
    public static TypeConnexionUsagerEnum getTypeConnexion(DemandeDTO demande) {
        if (demande == null) {
            return null;
        }
        if (demande.getDonneesMConnect() != null) {
            return TypeConnexionUsagerEnum.MCONNECT;
        }
        //Par défaut, c'est AUTHENTIFICATION_FAIBLE
        return COURRIER.equals(demande.getCanal()) || GUICHET_PHYSIQUE.equals(demande.getCanal())
                ? TypeConnexionUsagerEnum.AGENT
                : TypeConnexionUsagerEnum.AUTHENTIFICATION_FAIBLE;
    }

    public static boolean isDocumentsValidesActif(DemandeDTO demande) {
        List<DemandeFileDTO> fichiers = FileUtils.getAllFileDemande(demande);
        return fichiers.stream().anyMatch(
                demandeFileDTO -> StringUtils.isNotBlank(demandeFileDTO.getTypedoc()) && !"NON_APPLICABLE".equals(
                        demandeFileDTO.getTypedoc()));
    }

    public static boolean hasRole(final String role) {
        var authentication = SecurityContextHolder.getContext().getAuthentication();

        return authentication != null && authentication.getAuthorities().stream()
                .anyMatch(authority -> role.equals(authority.getAuthority()));
    }

    /**
     * Remplacement des sauts de ligne par des balises <br> pour un affichage HTML correct
     *
     * @param commentaire
     * @return
     */
    public static String formatCommentaire(String commentaire) {
        if (StringUtils.isBlank(commentaire)) {
            return commentaire;
        }
        return commentaire.replaceAll("\\r?\\n", "<br/>");
    }

    public static String getInfoBulle(DemandeFileDTO file, List<TypedocDTO> liste) {
        if (file == null || liste == null) {
            return StringUtils.EMPTY;
        }
        String libelle = liste.stream().filter(f -> f.getKey().equals(file.getTypedoc())).map(TypedocDTO::getValue)
                .findFirst().orElse("");
        String[] metas = file.getMeta().split(";");
        String agent = Arrays.stream(metas).filter(e -> e.startsWith("AGENT_NAME_")).findFirst().orElse("");
        String agentName = StringUtils.substringAfter(agent, "AGENT_NAME_");
        String date = new SimpleDateFormat(DEFAULT_FRENCH_DATE_HOURS_FORMAT).format(file.getDate());
        return String.format("%s importé le %s par %s sous la qualification de %s", file.getName(), date, agentName,
                libelle);
    }
}
