package mc.gouv.af.back.util;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import javax.annotation.PostConstruct;

import org.apache.commons.lang.StringUtils;
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

import mc.gouv.af.back.service.properties.GouvPropertiesResolver;
import mc.gouv.dem.apiclient.DemClient;
import mc.gouv.dem.apishared.model.DemandeDTO;
import mc.gouv.dem.apishared.model.DemandeDataDTO;
import mc.gouv.dem.apishared.model.DemarcheDTO;
import mc.gouv.logon.apiclient.RestException;
import mc.gouv.logon.shared.User;
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

    /**
     * Version en cache des infos de la démarche
     */
    private DemarcheDTO demarche = null;

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private DemClient demClient;

    @Autowired
    UsagersCache usagersCache;

    @Autowired
    UtilisateursCache utilisateursCache;

    @PostConstruct
    public void postConstruct() {

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

    /**
     * Retourne le nom d'un usager à partir de son ID
     * 
     * @param usagerId
     * @return
     */
    public String getUsagerNameFromID(Integer usagerId) {
        UsagerBean u = usagersCache.getUsager(usagerId);
        String prenomNom = StringUtils.EMPTY;
        if (u.getPrenom() != null) {
            prenomNom += u.getPrenom();
        }
        if (u.getNom() != null) {
            prenomNom += u.getNom();
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
        User user = utilisateursCache.getUtilisateur(matricule);
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
            demClient = new DemClient(gouvPropertiesResolver.getDemUrl(), gouvPropertiesResolver.getDemUser(),
                    gouvPropertiesResolver.getDemPwd());
        }
        return demClient;
    }

    /**
     * Retourne une version "cachée" des informations de la démarche
     * @return
     */
    public DemarcheDTO getDemarcheInfos() {
        if (demarche == null) {
            demarche = getDemClient().getDemarche(gouvPropertiesResolver.getDemarcheId());
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

    public static String getYear() {
        return String.valueOf(Calendar.getInstance().get(Calendar.YEAR));
    }
}
