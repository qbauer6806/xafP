package mc.gouv.xaf.backweb.web.config.advice;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.DemarchesService;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

@ControllerAdvice
@RequiredArgsConstructor
public class AfBackWebControllerAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfBackWebControllerAdvice.class);

    @Value("${mc.gouv.backserver.help.url}")
    private String helpUrl;

    @Value("${mc.gouv.backserver.contact.support.url}")
    private String contactSupportUrl;

    @Value("${maven.build.date}")
    private String buildDate;

    @Value("${maven.version}")
    private String version;

    @Value("${maven.name}")
    private String name;

    @Value("${mc.gouv.backserver.env.name}")
    private String sharedEnv;

    @Value("${mc.gouv.backserver.env.color}")
    private String sharedEnvColor;

    @Value("${application.name}")
    private String applicationName;

    @Value("${mc.gouv.logon.url:}")
    private String logonUrl;

    private long buildTimestamp = 0;
    private static final String DATE_FORMAT_TS_MAVEN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    private final DemarchesService demarchesService;

    @ModelAttribute(name = "helpUrl")
    public String addHelpUrl() {
        return helpUrl;
    }

    @ModelAttribute(name = "contactSupportUrl")
    public String getContactSupportUrl() {
        return contactSupportUrl;
    }

    @ModelAttribute(name = "applicationVersion")
    public String getVersion() {
        return version;
    }

    @ModelAttribute(name = "gouvEnvironmentLibelle")
    public String getGouvEnvironmentLibelle() {
        return sharedEnv;
    }

    @ModelAttribute(name = "gouvEnvironmentColor")
    public String getGouvEnvironmentColor() {
        return sharedEnvColor;
    }

    @ModelAttribute(name = "applicationBuildTimestamp")
    public long getBuildTimestamp() {
        if (buildTimestamp == 0) {
            String dateStr = buildDate;
            var df = new SimpleDateFormat(DATE_FORMAT_TS_MAVEN);
            Date dateBuild;
            try {
                dateBuild = df.parse(dateStr);
                buildTimestamp = dateBuild.getTime();
            } catch (ParseException e) {
                LOGGER.error("Error lors du parsing de la date {}", dateStr, e);
            }
        }
        return buildTimestamp;
    }

    @ModelAttribute(name = "applicationBuildDate")
    public String getBuildDate() {
        return buildDate;
    }

    @ModelAttribute(name = "applicationName")
    public String getName() {
        return name;
    }

    @ModelAttribute(name = "demarcheId")
    public String addDemarcheId() {
        return StringUtils.upperCase(applicationName);
    }

    @ModelAttribute(name = "logonUrl")
    public String getGouvSharedLogonUrl() {
        return logonUrl;
    }

    /**
     * Permets de récupérer le nom de la demarche de façon global.
     * On retourne une chaine vide en cas d'exception pour éviter d'afficher la stacktrace à l'usager.
     * Il s'agit d'une Expression EL de Thymeleaf
     *
     * @return
     */
    @ModelAttribute("nomDemarche")
    public String globalNomDemarche() {
        try {
            return demarchesService.getDemarche().getNom();
        } catch (Exception ex) {
            LOGGER.error("Erreur de recuperation du nom de la demarche", ex);
            return StringUtils.EMPTY;
        }
    }
}
