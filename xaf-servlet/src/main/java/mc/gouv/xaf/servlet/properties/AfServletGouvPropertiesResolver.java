package mc.gouv.xaf.servlet.properties;

import mc.gouv.Static;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class AfServletGouvPropertiesResolver {

    private static Logger LOGGER = LoggerFactory.getLogger(AfServletGouvPropertiesResolver.class);

    /*
     * hab
     */
    private static String applicationName = "";
    /*
     * .hab
     * Sert à gérer s'il n'y a pas de fichier config.properties alors
     * nous prenons en compte les properties sans prefix ex : mc.gouv.appfactory.url et non pas mc.gouv.appfactory.hab.url
     */
    private static String applicationPrefix = "";

    static {
        Properties prop = new Properties();
        String propFileName = "config.properties";

        InputStream inputStream = AppFactoryServletUtils.class.getClassLoader().getResourceAsStream(propFileName);

        if (inputStream != null) {
            try {
                prop.load(inputStream);
                applicationName = prop.getProperty("application.name");
                LOGGER.info("application.name = {}", applicationName);
                if (StringUtils.isBlank(applicationName)) {
                    LOGGER.warn("Impossible de charger la propriété application.name");
                } else {
                    applicationPrefix = "." + applicationName.toLowerCase();
                    LOGGER.info("Prefix = {}", applicationPrefix);
                }
            } catch (IOException e) {
                LOGGER.warn(
                        "Impossible de charger config.properties dans le classpath pour avoir la propriété application.name",
                        e);
            }
        } else {
            LOGGER.warn(
                    "Impossible de charger config.properties dans le classpath pour avoir la propriété application.name");
        }

    }

    public static final String LOGIN_REST_URL = "mc.gouv.appfactory.external.login.url";

    public static String getLoginRestUrl() {
        return Static.getValue(LOGIN_REST_URL);
    }

    public static final String LOGIN_SERVICEREST_URL = "mc.gouv.appfactory.external.login.servicerest.url";

    public static String getLoginServiceRestUrl() {
        return Static.getValue(LOGIN_SERVICEREST_URL);
    }

    public static final String PAYS_URL = "mc.gouv.appfactory.external.pays.url";

    public static String getPaysUrl() {
        return Static.getValue(PAYS_URL);
    }

    public static final String FILE_URL = "mc.gouv.appfactory.filews.file.url";

    public static String getFileUrl() {
        return Static.getValue(FILE_URL);
    }

    public static final String LOGIN_KEEP_ALIVE = "mc.gouv.appfactory.front.login.keepalive.url";

    public static String getLoginKeepAlive() {
        return Static.getValue(LOGIN_KEEP_ALIVE);
    }

    public static final String LOGIN_URL = "mc.gouv.appfactory.front.login.url";

    public static String getLoginUrl() {
        return Static.getValue(LOGIN_URL);
    }

    public static final String LOGIN_PROFIL_URL = "mc.gouv.appfactory.front.login.profil.url";

    public static String getLoginProfilUrl() {
        return Static.getValue(LOGIN_PROFIL_URL);
    }

    /* Properties propres à la démarche */

    public static final String API_URL = "mc.gouv.appfactory" + applicationPrefix + ".api.url";

    public static String getApiUrl() {
        return Static.getValue(API_URL);
    }

    public static final String BACKOFFICE_URL = "mc.gouv.appfactory" + applicationPrefix + ".url";

    public static String getBackOfficeUrl() {
        return Static.getValue(BACKOFFICE_URL);
    }

    public static final String BACKOFFICE_DEMANDE_URL = "mc.gouv.appfactory" + applicationPrefix + ".demande.url";

    public static String getBackOfficeDemandeUrl() {
        return Static.getValue(BACKOFFICE_DEMANDE_URL);
    }

    public static final String SHARED_KEY = "mc.gouv" + applicationPrefix + ".frontserver.key";

    public static String getSharedKey() {
        return Static.getValue(SHARED_KEY);
    }

    public static final String FILE_JWT = "mc.gouv" + applicationPrefix + ".frontserver.file.jwt";

    public static String getFileJwt() {
        return Static.getValue(FILE_JWT);
    }

    public static final String API_JWT = "mc.gouv" + applicationPrefix + ".frontserver.jwt";

    public static String getApiJwt() {
        return Static.getValue(API_JWT);
    }

    public static final String TGFAPI_URL = "mc.gouv.appfactory.tgfapi.url";

    public static String getTgfApiUrl() {
        return Static.getValue(TGFAPI_URL);
    }

    public static final String TGFAPI_JWT = "mc.gouv.appfactory.tgfapi.jwt";

    public static String getTgfApiJwt() {
        return Static.getValue(TGFAPI_JWT);
    }

    public static final String VSCAN_URL = "mc.gouv.appfactory.external.vscan.url";

    public static String getVscanUrl() {
        return Static.getValue(VSCAN_URL);
    }

    public static final String VSCAN_JWT = "mc.gouv" + applicationPrefix + ".frontserver.vscan.jwt";

    public static String getVscanJwt() {
        return Static.getValue(VSCAN_JWT);
    }

    public static final String FRONTOFFICE_CONTACT_URL = "mc.gouv.appfactory" + applicationPrefix + ".front.login.contact.url";

    public static String getFrontofficeContactUrl() {
        return Static.getValue(FRONTOFFICE_CONTACT_URL);
    }

    public static final String FRONTOFFICE_COPYRIGHT_YEARS = "mc.gouv.appfactory" + applicationPrefix + ".front.copyright.years";

    public static String getFrontofficeCopyrightYears() {
        return Static.getValue(FRONTOFFICE_COPYRIGHT_YEARS);
    }

    public static final String FRONTOFFICE_PIWIK_SITE_ID = "mc.gouv.piwik.external" + applicationPrefix + ".piwikSiteId";

    public static String getFrontofficePiwikSiteId() {
        return Static.getValue(FRONTOFFICE_PIWIK_SITE_ID);
    }

    static {
        //Vérification que chaque propriété a bien été configurée
        List<String> propertiesNotFound = new ArrayList<>();
        try {
            Method m = Static.class.getDeclaredMethod("getValue", String.class);

            Field[] fields = AfServletGouvPropertiesResolver.class.getDeclaredFields();
            for (Field f : fields) {
                if (Modifier.isStatic(f.getModifiers()) && !f.getName().equals("LOGGER")
                        && !f.getName().equals("applicationName") && !f.getName().equals("applicationPrefix")) {
                    LOGGER.info("Vérification de la propriété {}", f.getName());
                    String propertyName = (String) f.get(null);
                    String value = (String) m.invoke(null, f.get(null));

                    if (StringUtils.isBlank(value)) {
                        propertiesNotFound.add(propertyName);
                    }
                }
            }

        } catch (NoSuchMethodException e) {
            LOGGER.error("Erreur lors de l'introspection", e);
        } catch (SecurityException e) {
            LOGGER.error("Erreur lors de l'introspection", e);
        } catch (IllegalAccessException e) {
            LOGGER.error("Erreur lors de l'introspection", e);
        } catch (IllegalArgumentException e) {
            LOGGER.error("Erreur lors de l'introspection", e);
        } catch (InvocationTargetException e) {
            LOGGER.error("Erreur lors de l'introspection", e);
        }

        if (!propertiesNotFound.isEmpty()) {

            LOGGER.error("Des propriétés n'ont pas été trouvées : {}", propertiesNotFound);
            System.exit(1);
        }
    }

    public static List<PropertiesDTO> getFrontProperties() {
        List<PropertiesDTO> propertiesDTOS = new ArrayList<>();
        propertiesDTOS.add(new PropertiesDTO(LOGIN_KEEP_ALIVE, getLoginKeepAlive()));
        propertiesDTOS.add(new PropertiesDTO(LOGIN_URL, getLoginUrl()));
        propertiesDTOS.add(new PropertiesDTO(LOGIN_PROFIL_URL, getLoginProfilUrl()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_CONTACT_URL, getFrontofficeContactUrl()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_COPYRIGHT_YEARS, getFrontofficeCopyrightYears()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_PIWIK_SITE_ID, getFrontofficePiwikSiteId()));
        return propertiesDTOS;
    }
}
