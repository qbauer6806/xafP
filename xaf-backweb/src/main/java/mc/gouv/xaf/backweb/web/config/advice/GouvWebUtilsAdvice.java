package mc.gouv.xaf.backweb.web.config.advice;

import mc.gouv.xaf.backweb.properties.BackGouvPropertiesResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

@ControllerAdvice
public class GouvWebUtilsAdvice {

    private static final Logger LOGGER = LoggerFactory.getLogger(GouvWebUtilsAdvice.class);

    @Value("${maven.build.date}")
    private String buildDate;

    @Value("${maven.version}")
    private String version;

    @Value("${maven.name}")
    private String name;

    @Autowired
    private BackGouvPropertiesResolver gouvPropertiesResolver;

    private long buildTimestamp = 0;

    public static final String DATE_FORMAT_TS_MAVEN = "yyyy-MM-dd'T'HH:mm:ss'Z'";

    @ModelAttribute(name = "applicationVersion")
    public String getVersion() {
        return version;
    }

    @ModelAttribute(name = "gouvEnvironmentLibelle")
    public String getGouvEnvironmentLibelle() {
        return gouvPropertiesResolver.getGouvSharedEnv();
    }

    @ModelAttribute(name = "gouvEnvironmentColor")
    public String getGouvEnvironmentColor() {
    	return gouvPropertiesResolver.getGouvSharedEnvColor();
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

    @ModelAttribute(name = "contactSupportUrl")
    public String getContactSupportUrl() {
        return gouvPropertiesResolver.getContactSupportUrl();
    }

}
