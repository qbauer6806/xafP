package mc.gouv.af.servlet.properties;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.Static;
import mc.gouv.af.servlet.util.AppFactoryServletUtils;

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

    public static final String DEM_ACCESSES_URL = "mc.gouv.appfactory.demarchesws.accesses.url";

    public static String getDemAccessUrl() {
        return Static.getValue(DEM_ACCESSES_URL);
    }

    public static final String DEM_DEMANDES_URL = "mc.gouv.appfactory.demarchesws.demandes.url";

    public static String getDemDemandesUrl() {
        return Static.getValue(DEM_ACCESSES_URL);
    }

    public static final String DEM_MOTIFS_URL = "mc.gouv.appfactory.demarchesws.motifs.url";

    public static String getDemMotifsUrl() {
        return Static.getValue(DEM_MOTIFS_URL);
    }

    public static final String DEM_URL = "mc.gouv.appfactory.demarchesws.url";

    public static String getDemUrl() {
        return Static.getValue(DEM_URL);
    }

    public static final String LOGIN_REST_URL = "mc.gouv.appfactory.external.login.url";

    public static String getLoginRestUrl() {
        return Static.getValue(LOGIN_REST_URL);
    }

    public static final String PAYS_URL = "mc.gouv.appfactory.external.pays.url";

    public static String getPaysUrl() {
        return Static.getValue(PAYS_URL);
    }

    public static final String FILE_URL = "mc.gouv.appfactory.filews.file.url";

    public static String getFileUrl() {
        return Static.getValue(FILE_URL);
    }

    public static final String MAIL_URL = "mc.gouv.appfactory.mailws.mail.url";

    public static String getMailUrl() {
        return Static.getValue(MAIL_URL);
    }

    public static final String CAPTCHA_PRIVATE_KEY = "mc.gouv.appfactory.captcha.privatekey";

    public static String getCaptchaPrivateKey() {
        return Static.getValue(CAPTCHA_PRIVATE_KEY);
    }

    /**
     * Properties propres à la démarche
     */

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

    public static final String MAIL_JWT = "mc.gouv" + applicationPrefix + ".frontserver.mail.jwt";

    public static String getMailJwt() {
        return Static.getValue(MAIL_JWT);
    }

    public static final String API_USER = "mc.gouv" + applicationPrefix + ".frontserver.user";

    public static String getApiUser() {
        return Static.getValue(API_USER);
    }

    public static final String API_PWD = "mc.gouv" + applicationPrefix + ".frontserver.pwd";

    public static String getApiPwd() {
        return Static.getValue(API_PWD);
    }

    public static final String DEMARCHES_JWT = "mc.gouv" + applicationPrefix + ".frontserver.dem.jwt";

    public static String getDemarchesJwt() {
        return Static.getValue(DEMARCHES_JWT);
    }

    public static final String GOUV_CONTACT_EMAIL_EXPEDITEUR_ADRESSE = "mc.gouv" + applicationPrefix
            + ".frontserver.mail.contact.expediteur.adresse";

    public static String getGouvContactEmailExpediteurAdresse() {
        return Static.getValue(GOUV_CONTACT_EMAIL_EXPEDITEUR_ADRESSE);
    }

    public static final String GOUV_CONTACT_EMAIL_EXPEDITEUR_NOM = "mc.gouv" + applicationPrefix
            + ".frontserver.mail.contact.expediteur.nom";

    public static String getGouvContactEmailExpediteurNom() {
        return Static.getValue(GOUV_CONTACT_EMAIL_EXPEDITEUR_NOM);
    }

    public static final String GOUV_CONTACT_EMAIL_SERVICE_ADRESSE = "mc.gouv" + applicationPrefix
            + ".frontserver.mail.contact.service.adresse";

    public static String getGouvContactEmailServiceAdresse() {
        return Static.getValue(GOUV_CONTACT_EMAIL_SERVICE_ADRESSE);
    }

    public static final String GOUV_CONTACT_EMAIL_SERVICE_NOM = "mc.gouv" + applicationPrefix
            + ".frontserver.mail.contact.service.nom";

    public static String getGouvContactEmailServiceNom() {
        return Static.getValue(GOUV_CONTACT_EMAIL_SERVICE_NOM);
    }

    static {
        //Vérification que chaque propriété a bien été configurée
        List<String> propertiesNotFound = new ArrayList<String>();
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

            LOGGER.error("Des propriétés n'ont pas été trouvée : {}", propertiesNotFound);
            System.exit(1);
        }
    }

}
