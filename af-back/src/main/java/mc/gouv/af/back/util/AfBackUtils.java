package mc.gouv.af.back.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import com.fasterxml.uuid.EthernetAddress;
import com.fasterxml.uuid.Generators;
import com.fasterxml.uuid.impl.TimeBasedGenerator;

import mc.gouv.af.back.cache.UsagersCache;
import mc.gouv.af.back.cache.UtilisateursCache;
import mc.gouv.af.back.dto.StatutPublicOuInterneDTO;
import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.af.back.service.DemarchesDataProvider;
import mc.gouv.dem.service.DemarchesService;
import mc.gouv.dem.shared.model.DemandeDTO;
import mc.gouv.dem.shared.model.DemandeDataDTO;
import mc.gouv.dem.shared.model.DemandeFlatDTO;
import mc.gouv.dem.shared.model.DemarcheDTO;
import mc.gouv.file.apiclient.FileClient;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.shared.User;
import mc.gouv.mail.apiclient.client.MailClient;
import mc.gouv.servicerest.usager.model.UsagerBean;

/**
 * Classe utilitaire pour le projet af-back
 * 
 * @author qdeme
 *
 */
@Component
public class AfBackUtils {

	private static final Logger LOGGER = LoggerFactory.getLogger(AfBackUtils.class);

	public static final String MAIL_METADATA_DEMANDEID = "MC_DEMANDEID";

	public static final String FILE_METADATA_DEMANDEID = "X-MC-DEMANDEID";

	public static final String FILE_METADATA_DEMANDESTATUT = "X-MC-DEMANDESTATUT";

	private final static String version = AfBackUtils.class.getPackage().getImplementationVersion();

	private static RestTemplate restTemplate;

	private static String envName;

	private static String envColor;

	public static DateFormat sdf_JJ_MM_AAAA = new SimpleDateFormat("dd/MM/yyyy");

	public static final String MESSAGE_ERREURS_FORMULAIRE = "Le formulaire contient des erreurs.";

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
		mediaTypes.add(new MediaType("application", "json", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
		mediaTypes.add(new MediaType("text", "html", MappingJackson2HttpMessageConverter.DEFAULT_CHARSET));
		conv.setSupportedMediaTypes(mediaTypes);
		list.add(conv);
		restTemplate.setMessageConverters(list);
	}

	public static String getVersion() {
		return version;
	}

	public static String getAuthenticatedAgentId() {
		Object o = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (o instanceof User) {
			return ((User) o).getMatricule();
		}

		return null;
	}

	public static String getAuthenticatedAgentName() {
		Object o = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
		if (o instanceof User) {
			return ((User) o).getNom();
		}

		return null;
	}

	/**
	 * Retourne le nom d'un usager à partir de son ID
	 * 
	 * @param usagerId
	 * @return
	 */
	public String getUsagerNameFromID(Integer usagerId) {
		UsagerBean u = usagersCache.get(usagerId);
		if (u == null) {
			return null;
		}
		String prenomNom = StringUtils.EMPTY;
		if (u.getPrenom() != null) {
			prenomNom += u.getPrenom();
		}
		if (u.getNom() != null) {
			prenomNom += " " + u.getNom();
		}
		return prenomNom;
	}

	/**
	 * Retourne le nom d'un utilisateur à partir de son matricule
	 * 
	 * @param userId
	 * @return
	 * @throws RestException
	 */
	public String getUserNameFromID(String matricule) throws RestException {
		LOGGER.debug("getUserNameFromID() : Appel à Logon...");
		User user = utilisateursCache.get(matricule);
		if (user != null) {
			return user.getNom();
		}
		return null;
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

	public MailClient getMailClient() {
		if (mailClient == null) {
			mailClient = new MailClient(gouvPropertiesResolver.getMailUrl(), gouvPropertiesResolver.getMailJwt());
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
	public DemandeFlatDTO getDemandeFlatDTO(DemandeDTO demande) {
		DemandeFlatDTO flat = new DemandeFlatDTO();
		flat.setAgentAffecteId(demande.getAgentAffecteId());
		if (!StringUtils.isBlank(demande.getAgentAffecteId())) {
			try {
				flat.setAgentAffecteNom(getUserNameFromID(demande.getAgentAffecteId()));
			} catch (RestException e) {
				LOGGER.error("Erreur lors de la récupération du nom de l'agent affecté à la demande", e);
			}
		}
		flat.setCanal(demande.getCanal().libelle);
		flat.setCourrierDateReception(demande.getCourrierDateReception());
		flat.setCourrierRefInterne(demande.getCourrierRefInterne());
		flat.setDateCreation(demande.getDateCreation());
		flat.setDernierStatut(demarchesDataProvider.getStatusLibelle(demande.getDernierStatut().getLibelle()));
		flat.setIdentifiant(demande.getIdentifiant());
		flat.setLangue(demande.getLangue());
		flat.setObservations(demande.getObservations());
		flat.setPkDemandes(demande.getPkDemandes());
		flat.setUsagerId(demande.getUsagerId());
		flat.setUsagerNom(getUsagerNameFromID(demande.getUsagerId()));
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

}
