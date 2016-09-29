package mc.gouv.af.back.util;

import java.util.Calendar;
import java.util.Date;

import javax.annotation.PostConstruct;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

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
    
    private static String DEM_URL = null;
    
    private static String DEM_USER = null;
    
    private static String DEM_PWD = null;
    
    private static String DEMARCHE_ID = null;
    
    private static String PROCESS_DEFINITION_KEY = null;
    
    private static String DEMARCHE_LANGUE = null;
    
    private final static String version = AfBackUtils.class.getPackage().getImplementationVersion();
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;
    
    @PostConstruct
    public void fillConstants() {
        LOGGER.info("AfBackUtils - Récupération des paramètres...");
        DEM_URL = gouvPropertiesResolver.getValue("mc.gouv.af.back.dem.url");
        DEM_USER = gouvPropertiesResolver.getValue("mc.gouv.af.back.dem.user");
        DEM_PWD = gouvPropertiesResolver.getValue("mc.gouv.af.back.dem.pwd");
        DEMARCHE_ID = gouvPropertiesResolver.getValue("mc.gouv.af.back.demarcheId");
        PROCESS_DEFINITION_KEY = gouvPropertiesResolver.getValue("mc.gouv.af.back.processDefinitionKey");
        DEMARCHE_LANGUE = gouvPropertiesResolver.getValue("mc.gouv.af.back.langue");
    }
    
    public static Integer calculerDureeTraitement(Date dateCreationDemande) {
        // TODO : compléter ! Spec "durée en jours ouvrés depuis la création de la demande"
        return Days.daysBetween(new DateTime(dateCreationDemande), new DateTime(new Date())).getDays();
    }
    
    public static String getAuthenticatedAgentId() {
        return ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getMatricule();
    }
    
    public static String getAuthenticatedAgentName() {
        return ((User)SecurityContextHolder.getContext().getAuthentication().getPrincipal()).getNom();
    }

    public static String getVersion() {
        return version;
    }
    
    public static String getDemUrl() {
        return DEM_URL;
    }
    
    public static String getDemUser() {
        return DEM_USER;
    }
    
    public static String getDemPwd() {
        return DEM_PWD;
    }
    
    public static String getYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }
    
    public static String getDemarcheId() {
        return DEMARCHE_ID;
    }

    public static String getProcessDefinitionKey() {
        return PROCESS_DEFINITION_KEY;
    }
    
    public static String getDemarcheLangue() {
        return DEMARCHE_LANGUE;
    }

}
