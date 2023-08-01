package mc.gouv.xaf.back.service.utils;

import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.annotation.PostConstruct;

import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
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
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.back.service.itg.logon.UtilisateursCache;
import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.back.service.motifs.MotifTemplateService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import mc.gouv.xaf.shared.dto.DemandeFlatDTO;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import mc.gouv.xaf.shared.dto.PropertiesListEntityDTO;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;

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

    public static final String XAF_EMAIL_HTML_ENABLED = "XAF_EMAIL_HTML_ENABLED";

    /**
     * Version en cache des infos de la démarche
     */
    private DemarcheDTO demarche = null;

    @Autowired
    @Lazy
    private GouvPropertiesResolver gouvPropertiesResolver;

    private MailClient mailClient = null;

    private FileClient fileClient = null;

    @Autowired
    @Lazy
    private UsagersCache usagersCache;

    @Autowired
    @Lazy
    private UtilisateursCache utilisateursCache;

    @Autowired
    @Lazy
    private DemarchesService demarchesService;

    @Autowired
    @Lazy
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    @Lazy
    private MessageSource messageSource;

    @Autowired
    @Lazy
    private UtilisateursUtils utilisateursUtils;

    @Autowired
    @Lazy
    private MotifTemplateService motifTemplateService;
    
    @Autowired
    @Lazy
    private DemandesService demandesService;

    @Autowired
    @Lazy
    private PropertiesService propertiesService;

    @Autowired
    @Lazy
    private MotifsCache motifsCache;

    public static final short GENDER_MR_INDEX = 0;
    public static final short GENDER_MME_INDEX = 1;
    public static final short GENDER_MLLE_INDEX = 2;

    @PostConstruct
    // TODO sonar n'aime cette méthode, car elle n'est pas static
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
    // TODO utile ?
    public void postConstructRestTemplate() {
        restTemplate = new RestTemplate();
        List<HttpMessageConverter<?>> list = new ArrayList<>();
        MappingJackson2HttpMessageConverter conv = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>();
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
    
    /**
	 * Retourne le code alpha2 de la nationalitée en fonction du code alpha3 donné en paramètre
	 * @param alpha3Code
	 * @return
	 */
	public static String getAlpha2Code(String alpha3Code) {
		Map<String, String> isoCodeMap = new HashMap<>();
		for (String currentCountry : Locale.getISOCountries()) {
			Locale currentCountryLocaleFr = new Locale("fr", currentCountry);
			isoCodeMap.put(currentCountryLocaleFr.getISO3Country().toUpperCase(), currentCountry);
		}
		return isoCodeMap.get(alpha3Code);
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
    	GichuniUsagerDTO u = usagersCache.get(usagerId);
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
     * Génère un UUID version 1 (time+location based UUID)
     * TODO copié de afservlet, supprimer dans l'un des deux
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        return uuidGenerator.generate();
    }

    /**
     * Génère un suffixe de fichier en fonction de la date de génération conformément au pattern suivant: HHmmssSSS
     */
    public static String generateFileDateSuffix() {
        return new SimpleDateFormat(FILE_DATE_SUFFIX_FORMAT).format(new Date());
    }

    /**
     * Génère un suffixe de fichier en fonction de la date de génération conformément au pattern suivant: YYYYMMDDHHmmssSS
     */
    public static String generateFileDateAndTimeSuffix() {
        return new SimpleDateFormat(FILE_DATE_AND_TIME_SUFFIX_FORMAT).format(new Date());
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
     * Retourne le nom complet de la démarche en Anglais
     *
     * @return
     */
    public String getDemarcheNomEn() {
        return getDemarcheInfos().getNomEn();
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
        // motif
        if (demande.getDernierStatut() != null && demande.getDernierStatut().getCodeMotif() != null) {
            MotifDTO motif = motifsCache.getMotif(demande.getDernierStatut().getCodeMotif(), "fr");
            flat.setMotif(motif != null ? motif.getLibelle() : null);
        }
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

    public String getStatusLibelleFromName(String status) {
        return demarchesDataProvider.getStatusLibelle(status);
    }

    public String getExportLibelle() {
        return demarchesDataProvider.getExportLibelle() != null ? demarchesDataProvider.getExportLibelle() : "Export Anonymisé";
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

    /**
     * Permet de parser une string en un objet Date au format déclaré dans AfBackUtils.DEFAULT_FRENCH_DATE_FORMAT
     * @throws ParseException en cas d'erreurs de parsing du SimpleDateFormat
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
            if (str.contains("\u0017")) {
                str = StringUtils.replace(str, "\u0017", " ");
            }
            result = str;
        }
        return result;
    }

    /**
     * Echappe les caractères posant problèmes dans les logs selon la règle Sonar javasecurity:S5145
     */
    public static String logSafe(String str) {
        if (StringUtils.isEmpty(str)) {
            return str;
        }
        return str.replaceAll(SharedMessages.UNSAFE_CHARS, "_");
    }

    /**
     * Retourne le nom d'un utilisateur à partir de son matricule
     * <br>
     * Attention cette méthode est appelée dans les pages HTML avec thymeleaf, bien vérifier les appels lors d'une suppression
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
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur lors de AfBackUtils.getListFromDemProperty()", e);
		}
    	return Collections.emptyMap();
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
        return formatDoubleToCurrency(number, "fr");
    }

    public static String formatDoubleToCurrency(Double number, String langue) {
        Locale local = StringUtils.equals("fr", langue) ? Locale.FRANCE : Locale.US;
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
    
    public static List<String> donneesCertifieesJsonToList(String json) {
    	if (json != null) {
	    	try {
	    		ObjectMapper mapper = new ObjectMapper();
				return mapper.readValue(json, new TypeReference<List<String>>(){});
			} catch (JsonProcessingException e) {
				LOGGER.error("Erreur dans donneesCertifieesJsonToList()", e);
			}
    	}
    	return new ArrayList<>();
    }
    
    public static String donneesCertifieesListToJson(List<String> list) {
    	ObjectMapper mapper = new ObjectMapper();
    	try {
			return mapper.writeValueAsString(list);
		} catch (JsonProcessingException e) {
			LOGGER.error("Erreur dans donneesCertifieesListToJson()", e);
		}
    	return null;
    }
    
	public static String addDonneeCertifiee(String donneesCertifiees, String path) {
		List<String> donneesCertifieesList = donneesCertifieesJsonToList(donneesCertifiees);
		donneesCertifieesList.add(path);
		return donneesCertifieesListToJson(donneesCertifieesList);
	}
	
	public static String mConnectDateToString(Date date) {
		return new SimpleDateFormat(MCONNECT_DATE_AND_TIME_FORMAT).format(date);
	}
	
	public String getIdentifiantFromPkDemande(Integer pkDemande) {
		DemandeDTO demande = demandesService.getDemande(gouvPropertiesResolver.getDemarcheId(), pkDemande);
		return demande.getIdentifiant();
	}
    
	public boolean isEmailHtmlEnabled() {
        PropertiesDTO emailHtmlEnabledProp = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_EMAIL_HTML_ENABLED);
        if (emailHtmlEnabledProp == null || StringUtils.isBlank(emailHtmlEnabledProp.getValue())) {
        	return false;
        }
        return Boolean.valueOf(emailHtmlEnabledProp.getValue());
	}

}
