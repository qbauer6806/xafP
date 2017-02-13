package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.joda.time.Days;
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

import mc.gouv.af.back.service.properties.AfGouvProperty;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeDTO;
import mc.gouv.dem.apishared.model.DemandeDataDTO;
import mc.gouv.dem.apishared.model.DemarcheDTO;
import mc.gouv.dem.apishared.model.UsagerCourrierDTO;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.model.User;
import mc.gouv.servicerest.pays.ReferentielPaysClient;
import mc.gouv.servicerest.usager.ReferentielUsagersClient;
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

    private String DEM_URL = null;

    private String DEM_USER = null;

    private String DEM_PWD = null;
    
    private String DEM_FRONTUSER = null;
    
    private String DEM_FRONTPWD = null;

    private String DEMARCHE_ID = null;

    private String PROCESS_DEFINITION_KEY = null;

    private String USAGERS_REST_URL = null;

    private String PAYS_REST_URL = null;

    private String FILE_URL = null;
    
    private String FILE_USER = null;
    
    private String FILE_PWD = null;
    
    private String DEM_JMS_HOST = null;
    
    private int DEM_JMS_PORT;
    
    private String MAIL_URL = null;
    
    private String MAIL_USER = null;
    
    private String MAIL_PWD = null;
    
    private String FRONT_URL = null;

    private static String APPFACTORYID = "appfactory";
    
    public static final String MAIL_METADATA_DEMANDEID = "MC_DEMANDEID";

    private final static String version = AfBackUtils.class.getPackage().getImplementationVersion();

    private static RestTemplate restTemplate;
    
    /**
     * Version en cache des infos de la démarche
     */
    private DemarcheDTO demarche = null;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private LogonProxy logonProxy;
    
    private DemClient demClient;
    
    private ReferentielPaysClient referentielPaysClient;
    
    private ReferentielUsagersClient referentielUsagersClient;
    
    public static final int USAGERID_OFFSET = 1000000000;

    @PostConstruct
    public void postConstruct() {
        LOGGER.info("AfBackUtils - Récupération des paramètres...");
        DEM_URL = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_URL);
        DEM_USER = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_USER);
        DEM_PWD = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_PWD);
        DEM_FRONTUSER = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_FRONTUSER);
        DEM_FRONTPWD = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_FRONTPWD);
        DEMARCHE_ID = gouvPropertiesResolver.getValue(AfGouvProperty.DEMARCHE_ID);
        PROCESS_DEFINITION_KEY = gouvPropertiesResolver.getValue(AfGouvProperty.PROCESS_DEFINITION_KEY);
        USAGERS_REST_URL = gouvPropertiesResolver.getValue(AfGouvProperty.USAGERS_REST_URL);
        FILE_URL = gouvPropertiesResolver.getValue(AfGouvProperty.FILE_URL);
        FILE_USER = gouvPropertiesResolver.getValue(AfGouvProperty.FILE_USER);
        FILE_PWD = gouvPropertiesResolver.getValue(AfGouvProperty.FILE_PWD);
        PAYS_REST_URL = gouvPropertiesResolver.getValue(AfGouvProperty.PAYS_REST_URL);
        DEM_JMS_HOST = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_JMS_HOST);
        DEM_JMS_PORT = Integer.parseInt(gouvPropertiesResolver.getValue(AfGouvProperty.DEM_JMS_PORT));
        MAIL_URL = gouvPropertiesResolver.getValue(AfGouvProperty.MAIL_URL);
        MAIL_USER = gouvPropertiesResolver.getValue(AfGouvProperty.MAIL_USER);
        MAIL_PWD = gouvPropertiesResolver.getValue(AfGouvProperty.MAIL_PWD);
        FRONT_URL = gouvPropertiesResolver.getValue(AfGouvProperty.FRONT_URL);

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

    public static Integer calculerDureeTraitement(Date dateCreationDemande) {
        // TODO : compléter ! Spec "durée en jours ouvrés depuis la création de la demande"
        return Days.daysBetween(new DateTime(dateCreationDemande), new DateTime(new Date())).getDays();
    }

    public static String getAuthenticatedAgentId() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMatricule();
    }

    public static String getAuthenticatedAgentName() {
        return ((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getNom();
    }

    public static String getVersion() {
        return version;
    }

    public String getDemUrl() {
        return DEM_URL;
    }

    public String getDemUser() {
        return DEM_USER;
    }

    public String getDemPwd() {
        return DEM_PWD;
    }

    public String getFileUrl() {
        return FILE_URL;
    }
    
    public String getFileUser() {
        return FILE_USER;
    }
    
    public String getFilePwd() {
        return FILE_PWD;
    }
    
    public String getMailUrl() {
        return MAIL_URL;
    }
    
    public String getMailUser() {
        return MAIL_USER;
    }
    
    public String getMailPwd() {
        return MAIL_PWD;
    }
    
    public String getDemJmsHost() {
        return DEM_JMS_HOST;
    }
    
    public int getDemJmsPort() {
        return DEM_JMS_PORT;
    }

    public static String getYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }

    public String getDemarcheId() {
        return DEMARCHE_ID;
    }

    public String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }

    public static String getAppFactoryId() {
        return APPFACTORYID;
    }

    public String getUsagersRestUrl() {
        return USAGERS_REST_URL;
    }

    public String getPaysRestUrl() {
        return PAYS_REST_URL;
    }
    
    public String getFrontUrl() {
        return FRONT_URL;
    }

    /**
     * Retourne le nom d'un usager à partir de son ID
     * 
     * @param usagerId
     * @return
     */
    public String getUsagerNameFromID(Integer usagerId) {
        LOGGER.debug("getUsagerNameFromID() : Appel au référentiel Usagers...");
        UsagerBean usager = getUsagerFromID(usagerId);
        if (usager != null) {
            return usager.getPrenom() + " " + usager.getNom();
        }
        return null;
    }
    
    /**
     * Retourne les informations d'un usager à partir de son ID
     * 
     * @param usagerId
     * @return
     */
    public UsagerBean getUsagerFromID(Integer usagerId) {
        
        if (!isUsagerCourrier(usagerId)) {
            LOGGER.debug("getUsagerFromID(" + usagerId + ") : Appel au référentiel Usagers...");
            return getReferentielUsagersClient().getUsager(usagerId);
        }
        else {
            LOGGER.debug("getUsagerFromID(" + usagerId + ") : Appel à DEM car usager courrier...");
            UsagerCourrierDTO uc = getUsagerCourrierFromID(usagerId);
            UsagerBean ub = new UsagerBean();
            ub.setAdresse1(uc.getAdresse1());
            ub.setAdresse2(uc.getAdresse2());
            ub.setCodePostal(uc.getCodePostal());
            ub.setComplementAdresse(uc.getAdresseComplement());
            ub.setDateCreation(uc.getDateCreation());
            ub.setEmail(uc.getEmail());
            ub.setId(uc.getPkUsagersCourrier());
            ub.setLogin(uc.getLogin());
            ub.setNom(uc.getNom());
            ub.setPrenom(uc.getPrenom());
            ub.setNomPays(uc.getPays());
            ub.setRaisonSociale(uc.getRaisonSociale());
            ub.setTitre(uc.getTitre().shortValue());
            ub.setVille(uc.getVille());
            return ub;
        }
    }
    
    /**
     * Retourne les informations d'un usager courrier à partir de son ID
     * 
     * @param usagerId
     * @return
     */
    public UsagerCourrierDTO getUsagerCourrierFromID(Integer usagerId) {
        LOGGER.debug("getUsagerCourrierFromID() : Appel à DEM...");
        return getDemClient().getUsagerCourrier(getDemarcheId(), usagerId);
    }

    /**
     * Retourne le nom d'un utilisateur à partir de son ID
     * 
     * @param userId
     * @return
     * @throws RestException
     */
    public String getUserNameFromID(String userId) throws RestException {
        LOGGER.debug("getUserNameFromID() : Appel à Logon...");
        User user = logonProxy.getUserByMatricule(userId);
        if (user != null) {
            return user.getNom();
        }
        return null;
    }
    
    /**
     * Génère un UUID version 1 (time+location based UUID)
     * TODO copié de afservlet, supprimer dans l'un des deux
     * 
     * @return
     */
    public static UUID generateUUID() {
        EthernetAddress addr = EthernetAddress.fromInterface();
        TimeBasedGenerator uuidGenerator = Generators.timeBasedGenerator(addr);
        UUID uuid = uuidGenerator.generate();
        return uuid;
    }
    
    public DemClient getDemClient() {
        if (demClient == null) {
            demClient = new DemClient(getDemUrl(), getDemUser(), getDemPwd());
        }
        return demClient;
    }
    
    public ReferentielPaysClient getReferentielPaysClient() {
        if (referentielPaysClient == null) {
            referentielPaysClient = new ReferentielPaysClient(getPaysRestUrl(), null, null);
        }
        return referentielPaysClient;
    }
    
    public ReferentielUsagersClient getReferentielUsagersClient() {
        if (referentielUsagersClient == null) {
            referentielUsagersClient = new ReferentielUsagersClient(getUsagersRestUrl(), null, null);
        }
        return referentielUsagersClient;
    }
    
    /**
     * Retourne une version "cachée" des informations de la démarche
     * @return
     */
    public DemarcheDTO getDemarcheInfos() {
        if (demarche == null) {
            demarche = getDemClient().getDemarche(getDemarcheId());
        }
        return demarche;
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
    
    /**
     * Indique si l'usager correspond à un usager courrier ou pas. Si l'usagerId est supérieur à un milliard, alors il
     * s'agit d'un usager courrier.
     * 
     * @param usagerId
     * @return
     */
    public static boolean isUsagerCourrier(Integer usagerId) {
        return usagerId > USAGERID_OFFSET;
    }

    public String getDemFrontUser() {
        return DEM_FRONTUSER;
    }

    public String getDemFrontPwd() {
        return DEM_FRONTPWD;
    }
}
