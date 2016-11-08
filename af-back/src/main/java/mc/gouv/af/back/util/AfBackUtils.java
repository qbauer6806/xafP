package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

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

import mc.gouv.af.back.service.properties.AfGouvProperty;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.model.User;

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

    private String DEMARCHE_ID = null;

    private String PROCESS_DEFINITION_KEY = null;

    private String USAGERS_REST_URL = null;

    private String PAYS_REST_URL = null;

    private String FILE_URL = null;

    private static String APPFACTORYID = "appfactory";

    private final static String version = AfBackUtils.class.getPackage().getImplementationVersion();

    private static RestTemplate restTemplate;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private LogonProxy logonProxy;

    @PostConstruct
    public void postConstruct() {
        LOGGER.info("AfBackUtils - Récupération des paramètres...");
        DEM_URL = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_URL);
        DEM_USER = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_USER);
        DEM_PWD = gouvPropertiesResolver.getValue(AfGouvProperty.DEM_PWD);
        DEMARCHE_ID = gouvPropertiesResolver.getValue(AfGouvProperty.DEMARCHE_ID);
        PROCESS_DEFINITION_KEY = gouvPropertiesResolver.getValue(AfGouvProperty.PROCESS_DEFINITION_KEY);
        USAGERS_REST_URL = gouvPropertiesResolver.getValue(AfGouvProperty.USAGERS_REST_URL);
        FILE_URL = gouvPropertiesResolver.getValue(AfGouvProperty.FILE_URL);
        PAYS_REST_URL = gouvPropertiesResolver.getValue(AfGouvProperty.PAYS_REST_URL);

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

    /**
     * Retourne le nom d'un usager à partir de son ID
     * 
     * @param usagerId
     * @return
     */
    public String getUsagerNameFromID(Integer usagerId) {
        LOGGER.debug("getUsagerNameFromID() : Appel au référentiel Usagers...");
        UsagerInfosDTO usager = restTemplate.getForObject(USAGERS_REST_URL + "/" + usagerId, UsagerInfosDTO.class);
        if (usager != null) {
            return usager.getPrenom() + " " + usager.getNom();
        }
        return null;
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

}
