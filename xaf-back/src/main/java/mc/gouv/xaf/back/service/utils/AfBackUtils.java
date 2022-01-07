package mc.gouv.xaf.back.service.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;
import com.google.gson.Gson;
import mc.gouv.file.apiclient.FileClient;
import mc.gouv.logon.shared.Droit;
import mc.gouv.logon.shared.Role;
import mc.gouv.logon.shared.User;
import mc.gouv.mail.apiclient.client.MailClient;
import mc.gouv.servicerest.usager.model.UsagerBean;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.motifs.MotifTemplateService;
import mc.gouv.xaf.shared.dto.*;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * Classe utilitaire pour le projet xaf-back
 *
 * @author qdeme
 *
 */
@Component
public class AfBackUtils {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBackUtils.class);

    public static final String MAIL_METADATA_DEMANDEID = "MC_DEMANDEID";

    public static final String STATUT_PUBLIC_SUPPRIMEE = "SUPPRIMEE";

    private static RestTemplate restTemplate;

    private static String envName;

    private static String envColor;

    public static String DEFAULT_FRENCH_DATE_FORMAT = "dd/MM/yyyy";

    // 24 hours time format
    public static String DEFAULT_FRENCH_TIME_FORMAT = "HH:mm";

    // French date format with 24 hours
    public static String DEFAULT_FRENCH_DATE_HOURS_FORMAT = "dd/MM/yyyy HH:mm";

    public static DateFormat SDF_JJ_MM_AAAA = new SimpleDateFormat(DEFAULT_FRENCH_DATE_FORMAT);

    public static DateFormat SDF_JJ_MM_AAAA_HH_MM = new SimpleDateFormat(DEFAULT_FRENCH_DATE_HOURS_FORMAT);

    public static DateFormat FILE_DATE_SUFFIX = new SimpleDateFormat("HHmmssSSS");

    public static DateFormat FILE_DATE_AND_TIME_SUFFIX = new SimpleDateFormat("YYYYMMddHHmmssSS");

    public static final String MESSAGE_ERREURS_FORMULAIRE = "Le formulaire contient des erreurs.";
    
    // Préfix de la meta d'un fichier indiquant l'ID de la section correspondante
    public static String META_FICHIER_SECTION_PREFIX = "SECTION_ID_";

    /**
     * Version en cache des infos de la démarche
     */
    private DemarcheDTO demarche = null;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    private MailClient mailClient = null;

    private FileClient fileClient = null;

    @Autowired
    private UsagersCache usagersCache;

    @Autowired
    private UtilisateursCache utilisateursCache;

    @Autowired
    private DemarchesService demarchesService;

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private MessageSource messageSource;

    @Autowired
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    private MotifTemplateService motifTemplateService;

    public static final short GENDER_MR_INDEX = 0;
    public static final short GENDER_MME_INDEX = 1;
    public static final short GENDER_MLLE_INDEX = 2;

    @PostConstruct
    public void postConstructEnv() {
        String env = gouvPropertiesResolver.getGouvSharedEnv();
        // Si production, ne rien afficher
        if ("prod".equals(env)) {
            envName = "";
        } else if ("sup".equals(env)) {
            envName = "Support";
        } else if ("pre".equals(env)) {
            envName = "Pré-production";
        } else if ("rec".equals(env)) {
            envName = "Recette";
        } else if ("dev".equals(env)) {
            envName = "Développement";
        } else if ("loc".equals(env)) {
            envName = "Local";
        } else {
            envName = "Environnement inconnu";
        }

        // Fond noir si environnement de production, et non pas rouge
        if ("prod".equals(env)) {
            envColor = "#000000";
        } else {
            envColor = gouvPropertiesResolver.getGouvSharedEnvColor();
        }
    }

    @PostConstruct
    public void postConstructRestTemplate() {
        restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> list = new ArrayList<HttpMessageConverter<?>>();
        MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<MediaType>();
        mediaTypes.add(new MediaType("application", "json", StandardCharsets.UTF_8));
        mediaTypes.add(new MediaType("text", "html", StandardCharsets.UTF_8));
        conv.setSupportedMediaTypes(mediaTypes);
        list.add(conv);
        restTemplate.setMessageConverters(list);
    }

    public static String getAuthenticatedAgentId() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object o = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (o instanceof User) {
                return ((User) o).getMatricule();
            }
        }

        return null;
    }

    public static String getAuthenticatedAgentName() {
        if (SecurityContextHolder.getContext() != null
                && SecurityContextHolder.getContext().getAuthentication() != null) {
            Object o = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (o instanceof User) {
                return ((User) o).getNom();
            }
        }

        return null;
    }

    public String getLogonUrl() {
        return gouvPropertiesResolver.getGouvSharedLogonUrl();
    }

    /**
     * Retourne le nom d'un usager à partir de son ID
     *
     * @param usagerId une String contenant l'id de l'usager
     * @return une Sring composer de son prénom et son nom
     */
    public String getUsagerNameFromID(Integer usagerId) {
        UsagerBean u = usagersCache.get(usagerId);
        StringBuilder builder = new StringBuilder();
        if (null != u) {
            if (StringUtils.isNotBlank(u.getPrenom())) {
                builder.append(AfBackUtils.escapeChars(u.getPrenom())).append(' ');
            }
            if (StringUtils.isNotBlank(u.getNom())) {
                builder.append(AfBackUtils.escapeChars(u.getNom()));
            }
        }
        return builder.toString();
    }

    /**
     * Génère un UUID version 1 (time+location based UUID) TODO copié de
     * afservlet, supprimer dans l'un des deux
     *
     * @return
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        UUID uuid = uuidGenerator.generate();
        return uuid;
    }

    /**
     * Génère un suffixe de fichier en fonction de la date de génération conformément au
     * pattern suivant: HHmmssSSS
     */
    public static String generateFileDateSuffix() {
        return FILE_DATE_SUFFIX.format(new Date());
    }

    /**
     * Génère un suffixe de fichier en fonction de la date de génération conformément au
     * pattern suivant: YYYYMMDDHHmmssSS
     */
    public static String generateFileDateAndTimeSuffix() {
        return FILE_DATE_AND_TIME_SUFFIX.format(new Date());
    }

    public MailClient getMailClient() {
        if (mailClient == null) {
            String mailUrl = gouvPropertiesResolver.getMailUrl();
            String mailJwt = gouvPropertiesResolver.getMailJwt();
            mailClient = new MailClient(mailUrl, mailJwt);
        }
        return mailClient;
    }

    public FileClient getFileClient() {
        if (fileClient == null) {
            fileClient = new FileClient(gouvPropertiesResolver.getFileUrl(), gouvPropertiesResolver.getFileJwt());
        }
        return fileClient;
    }

    /**
     * Retourne une version "cachée" des informations de la démarche
     *
     * @return
     */
    public DemarcheDTO getDemarcheInfos() {
        if (demarche == null) {
            demarche = demarchesService.getDemarche(gouvPropertiesResolver.getDemarcheId());
        }
        return demarche;
    }

    /**
     * Retourne le nom complet de la démarche
     *
     * @return
     */
    public String getDemarcheNom() {
        return getDemarcheInfos().getNom();
    }

    /**
     * Permet de récupérer une donnée d'une demande
     *
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

    public static String getEnvName() {
        return envName;
    }

    public static String getEnvColor() {
        return envColor;
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
        String nomAgent = utilisateursUtils.getUserNameFromID(demande.getAgentAffecteId());
        flat.setAgentAffecteNom(getSafeString(nomAgent));
        flat.setCanal(demande.getCanal().libelle);
        flat.setCourrierDateReception(convertDateToString(demande.getCourrierDateReception()));
        flat.setCourrierRefInterne(getSafeString(demande.getCourrierRefInterne()));
        flat.setDateCreation(convertDateToString(demande.getDateCreation()));
        flat.setDernierStatut(demarchesDataProvider.getStatusLibelle(demande.getDernierStatut().getLibelle()));
        flat.setIdentifiant(getSafeString(demande.getIdentifiant()));
        flat.setLangue(getSafeString(demande.getLangue()));
        flat.setObservations(getSafeString(demande.getObservations()));
        flat.setPkDemandes(demande.getPkDemandes());
        flat.setUsagerId(demande.getUsagerId());
        flat.setUsagerNom(getSafeString(demande.getUsagerNom()));
        flat.setUsagerPrenom(getSafeString(demande.getUsagerPrenom()));
        flat.setUsagerEmail(getSafeString(demande.getUsagerEmail()));
        flat.setBuildId(demande.getBuildId());
        return flat;
    }

    public static String getTitreStr(Short titre) {
        if (titre == 0) {
            return "Monsieur";
        } else if (titre == 1) {
            return "Madame";
        } else if (titre == 2) {
            return "Mademoiselle";
        } else {
            return "";
        }
    }

    public String getStatusLibelleFromName(String status) {
        return demarchesDataProvider.getStatusLibelle(status);
    }

    /**
     * Retourne la classe CSS de la couleur associée à un statut
     * Attention, changer la fonction js getStatusColorClass
     * @param statutPublicOuInterne
     * @return
     */
    public String getStatusColorClass(StatutPublicOuInterneDTO statutPublicOuInterne) {
        return demarchesDataProvider.getStatusColorClass(statutPublicOuInterne);
    }

    /**
     * Permet de récupérer le d'un demandeur (ici, la raison sociale de l'entreprise)
     */
    public String getDemandeur(Object contenuDemandeDTO) {
        return demarchesDataProvider.getDemandeur(contenuDemandeDTO);
    }

    public StatutPublicOuInterneDTO getStatutPublicOuInterne(DemandeDTO demandeDto) {
        return demarchesDataProvider.getStatutPublicOuInterne(demandeDto);
    }

    public String getCivilite(Short titre, String locale) {
        return messageSource.getMessage("civilite." + titre, null, new Locale(locale));
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
            boolean toAdd = false;
            Set<Role> agentRoles = agent.getRoles();
            for (Role role : agentRoles) {
                if (role.getAppli().getCode().equals(codeAppli)) {
                    for (Droit droit : role.getDroits()) {
                        for (String roleFromList : rolesList) {
                            if (roleFromList.trim().equals(droit.getCode())) {
                                toAdd = true;
                            }
                        }
                    }

                }
            }
            if (toAdd) {
                destinataires.add(agent);
            }
        }
        return destinataires;
    }

    public static String convertDateToString(final Date date) {
        if (date == null) {
            return "";
        }
        return SDF_JJ_MM_AAAA.format(date);
    }

    public static String convertDateTimeToString(final Date date) {
        if (date == null) {
            return "";
        }
        return SDF_JJ_MM_AAAA_HH_MM.format(date);
    }

    public static String changeDateStringFormat(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .format(DateTimeFormatter.ofPattern(DEFAULT_FRENCH_DATE_FORMAT));
    }

    public static String changeTimeStringFormat(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .format(DateTimeFormatter.ofPattern(DEFAULT_FRENCH_TIME_FORMAT));
    }

    public static String changeDateTimeStringFormat(final String dateString) {
        if (StringUtils.isBlank(dateString)) {
            return " ";
        }
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                .format(DateTimeFormatter.ofPattern(DEFAULT_FRENCH_DATE_HOURS_FORMAT));
    }

    public static String getSafeString(final String value) {
        return StringUtils.isBlank(value) ? "" : value;
    }

    public static BigDecimal convertStringToBigDecimal(String decimalStr) {
        final String regexDecimal = "[0-9]*\\,?[0-9]*";
        final String regexInteger = "[0-9]*";

        if (StringUtils.isBlank(decimalStr)) {
            return null;
        }

        if (decimalStr.matches(regexInteger) || decimalStr.matches(regexDecimal)) {
            return new BigDecimal(decimalStr.replace(",", "."));
        }

        return null;
    }

    public String getDernierCodeMotif(DemandeDTO demande) {
        String codeDernierMotif = demande.getDernierStatut().getCodeMotif();
        String motif = codeDernierMotif;

        try {
            if (codeDernierMotif != null) {
                motif = motifTemplateService.getMotif(demande, codeDernierMotif, "fr").getLibelle();
            }
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la récupération du motif", e);
        }

        return motif;
    }

    /**
     * Permet de récupérer le flag indiquant que la démarche peut générer des
     * courriers
     *
     * @return
     */
    public boolean getDemarcheCanGenerateCourriers() {
        return demarchesDataProvider.getDemarcheCanGenerateCourriers();
    }

    /**
     * Permet de savoir si la démarche prend en charge les périodes d'ouverture
     * @return
     */
    public boolean getDemarcheCanHandlePeriodesOuverture() {
        return demarchesDataProvider.getDemarcheCanHandlePeriodesOuverture();
    }

    /**
     * Permet de savoir si la démarche prend en charge des propriétés
     * @return
     */
    public boolean getDemarcheCanHandleProperties() {
        return demarchesDataProvider.getDemarcheCanHandleProperties();
    }

    public static Date convertStartDate(String startDate) throws ParseException {
        return SDF_JJ_MM_AAAA.parse(startDate);
    }

    public static Date convertEndDate(String plainEndDate) throws ParseException {
        Date endDate = SDF_JJ_MM_AAAA.parse(plainEndDate);

        // Last moment of days
        Calendar cal = Calendar.getInstance();
        cal.setTime(endDate);
        cal.set(Calendar.HOUR_OF_DAY, cal.getMaximum(Calendar.HOUR_OF_DAY));
        cal.set(Calendar.MINUTE, cal.getMaximum(Calendar.MINUTE));
        cal.set(Calendar.SECOND, cal.getMaximum(Calendar.SECOND));
        endDate = cal.getTime();

        return endDate;
    }
    
    /**
     * Permet de savoir si la démarche prend en charge la gestion des agents (DENJS)
     * @return
     */
    public boolean getDemarcheCanHandleDenjsGestionAgents() {
    	return demarchesDataProvider.getDemarcheCanHandleDenjsGestionAgents();
    }

    public static String escapeChars(String str) {
        String result = " ";
        if (StringUtils.isNotBlank(str)) {
            if (str.contains("&#28;")) {
                str = StringUtils.replace(str, "&#28;", " ");
            }
            if (str.contains("\u001C")) {
                str = StringUtils.replace(str, "\u001C", " ");
            }
            if (str.contains("\u001A")) {
                str = StringUtils.replace(str, "\u001A", " ");
            }
            result = str;
        }
        return result;
    }

    /**
     * Retourne le nom d'un utilisateur à partir de son matricule
     * <br>
     * deprecated : Utiliser la méthode de {@link UtilisateursUtils}
     * <br>
     * Attention cette méthode est appelée dans les pages HTML avec thymeleaf, bien vérifier les appels lors d'une suppression
     */
    @Deprecated
    public String getUserNameFromID(String matricule) {
        return utilisateursUtils.getUserNameFromID(matricule);
    }
    
    @SuppressWarnings("unchecked")
	public static Map<String, String> getListFromDemProperty(String demPropertyValue) {
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			Map<String, String> map = mapper.readValue(demPropertyValue, Map.class);
			return map;
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors de AfBackUtils.getListFromDemProperty()", e);
		}
    	return null;
    }

    public static PropertiesListEntityDTO[] parserPropertiesListJson(String json) {
        Gson gson = new Gson();
        return gson.fromJson(json, PropertiesListEntityDTO[].class);
    }
    
    public static String convertTelIndicateur(String indicateur) {
        return StringUtils.replace(indicateur, "t", "+");
    }

    public static Double parseDoubleSafe(String texte) {
        Double parsed = 0.0;
        if (StringUtils.isNotEmpty(texte)) {
            String safe = StringUtils.replace(texte, ",", ".", -1);
            try {
                parsed = Double.parseDouble(safe);
            } catch (NumberFormatException e) {
                LOGGER.error("Impossible de parser ce nombre en double : {}", e.getMessage());
            }
        }
        return parsed;
    }

    public static String formatDoubleToCurrency(Double number) {
        NumberFormat formatter = NumberFormat.getCurrencyInstance(Locale.FRANCE);
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
}
