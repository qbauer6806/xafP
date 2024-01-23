package mc.gouv.xaf.servlet.properties;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import mc.gouv.Static;
import mc.gouv.xaf.servlet.util.AppFactoryServletUtils;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * 
 * Classe permettant de récupérer les propriétés externalisées dans les fichiers .properties du serveur
 * 
 * @author qdeme
 * 
 */
public class AfServletGouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(AfServletGouvPropertiesResolver.class);
    private static final String APPFACTORY_PREFIX = "mc.gouv.appfactory";
    private static final String MC_GOUV_PREFIX = "mc.gouv";

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

    private AfServletGouvPropertiesResolver() {
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

    public static final String GICHKEY_REDIRECT_URL = APPFACTORY_PREFIX + applicationPrefix + ".gichkey.redirect.url";

    public static String getGichkeyRedirectUrl() {
        return Static.getValue(GICHKEY_REDIRECT_URL).replace("<redirect_uri>", getGichkeyKeycloakRedirectUri());
    }
    
    public static final String GICHKEY_KEYCLOAK_REIRECT_URI = APPFACTORY_PREFIX + applicationPrefix + ".gichkey.keycloak.redirect.uri";

    public static String getGichkeyKeycloakRedirectUri() {
        return Static.getValue(GICHKEY_KEYCLOAK_REIRECT_URI);
    }

    public static final String GICHUNI_PROFIL_INDIVIDUAL_URL = "mc.gouv.appfactory.front.gichuni.profil.individual.url";

    public static String getGichuniProfilIndividualUrl() {
        return Static.getValue(GICHUNI_PROFIL_INDIVIDUAL_URL);
    }
    
    public static final String GICHUNI_PROFIL_COMPANY_URL = "mc.gouv.appfactory.front.gichuni.profil.company.url";

    public static String getGichuniProfilCompanyUrl() {
        return Static.getValue(GICHUNI_PROFIL_COMPANY_URL);
    }
    
    public static final String GICHUNI_USAGER_PARTICULER_URL_FR = "mc.gouv.gichuni.particulier.url.fr";
    public static final String GICHUNI_USAGER_PARTICULER_URL_EN = "mc.gouv.gichuni.particulier.url.en";
    public static final String GICHUNI_USAGER_ENTREPRISE_URL_FR = "mc.gouv.gichuni.entreprise.url.fr";
    public static final String GICHUNI_USAGER_ENTREPRISE_URL_EN = "mc.gouv.gichuni.entreprise.url.en";
    
	public static final String getSuiviDemarcheParticulierUrlFr() {
		String value = getGichuniUrl();
		String path = Static.getValue("mc.gouv.gichuni.particulier.uri.fr", "N/D");
		return StringUtils.isBlank(value) ? "vide" : value + path;
	}

	public static final String getSuiviDemarcheParticulierUrlEn() {
		String value = getGichuniUrl();
		String path = Static.getValue("mc.gouv.gichuni.particulier.uri.en", "N/D");
		return StringUtils.isBlank(value) ? "vide" : value + path;
	}

	public static final String getSuiviDemarcheEntrepriseUrlFr() {
		String value = getGichuniUrl();
		String path = Static.getValue("mc.gouv.gichuni.entreprise.uri.fr", "N/D");
		return StringUtils.isBlank(value) ? "vide" : value + path;
	}

	public static final String getSuiviDemarcheEntrepriseUrlEn() {
		String value = getGichuniUrl();
		String path = Static.getValue("mc.gouv.gichuni.entreprise.uri.en", "N/D");
		return StringUtils.isBlank(value) ? "vide" : value + path;
	}
	
	// #58041 - [BO] Clé BO pour lien vers le formulaire de révocation des certificats électroniques
	public static final String LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_FR = "mc.gouv.mconnect.revocation.certificats.url.fr";
	public static final String LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_EN = "mc.gouv.mconnect.revocation.certificats.url.en";
	public static final String getLienRevocationCertifsElectroniquesFr() {
		String path = Static.getValue(LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_FR, "N/D");
		return StringUtils.isBlank(path) ? "vide" : path;
	}
	public static final String getLienRevocationCertifsElectroniquesEn() {
		String path = Static.getValue(LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_EN, "N/D");
		return StringUtils.isBlank(path) ? "vide" : path;
	}


    /* Properties propres à la démarche */

    public static final String API_URL = APPFACTORY_PREFIX + applicationPrefix + ".api.url";

    public static String getApiUrl() {
        return Static.getValue(API_URL);
    }

    public static final String BACKOFFICE_URL = APPFACTORY_PREFIX + applicationPrefix + ".url";

    public static String getBackOfficeUrl() {
        return Static.getValue(BACKOFFICE_URL);
    }

    public static final String BACKOFFICE_DEMANDE_URL = APPFACTORY_PREFIX + applicationPrefix + ".demande.url";

    public static String getBackOfficeDemandeUrl() {
        return Static.getValue(BACKOFFICE_DEMANDE_URL);
    }

    public static final String SHARED_KEY = MC_GOUV_PREFIX + applicationPrefix + ".frontserver.key";

    public static String getSharedKey() {
        return Static.getValue(SHARED_KEY);
    }

    public static final String FILE_JWT = MC_GOUV_PREFIX + applicationPrefix + ".frontserver.file.jwt";

    public static String getFileJwt() {
        return Static.getValue(FILE_JWT);
    }

    public static final String API_JWT = MC_GOUV_PREFIX + applicationPrefix + ".frontserver.jwt";

    public static String getApiJwt() {
        return Static.getValue(API_JWT);
    }

    public static final String TGFAPI_URL = "mc.gouv.appfactory.tgfapi.url";

    public static String getTgfApiUrl() {
        return StringUtils.isBlank(Static.getValue(TGFAPI_URL)) ? "vide" : Static.getValue(TGFAPI_URL);
    }

    public static final String TGFAPI_JWT = "mc.gouv.appfactory.tgfapi.jwt";

    public static String getTgfApiJwt() {
        return StringUtils.isBlank(Static.getValue(TGFAPI_JWT)) ? "vide" : Static.getValue(TGFAPI_JWT);
    }

    public static final String VSCAN_URL = "mc.gouv.appfactory.external.vscan.url";

    public static String getVscanUrl() {
        return Static.getValue(VSCAN_URL);
    }

    public static final String VSCAN_JWT = MC_GOUV_PREFIX + applicationPrefix + ".frontserver.vscan.jwt";

    public static String getVscanJwt() {
        return Static.getValue(VSCAN_JWT);
    }

    public static final String MAX_UPLOAD_PAR_INTERVALLE = MC_GOUV_PREFIX + applicationPrefix + ".frontserver.maxUploadParIntervalle";

    public static String getMaxUploadParIntervalle() {
        return Static.getValue(MAX_UPLOAD_PAR_INTERVALLE);
    }

    public static final String TEMPS_INTERVALLE_UPLOAD = MC_GOUV_PREFIX + applicationPrefix + ".frontserver.tempsIntervalleUpload";

    public static String getTempsIntervalleUpload() {
        return Static.getValue(TEMPS_INTERVALLE_UPLOAD);
    }

    public static final String FRONTOFFICE_CONTACT_URL = APPFACTORY_PREFIX + applicationPrefix + ".front.login.contact.url";

    public static String getFrontofficeContactUrl() {
        return Static.getValue(FRONTOFFICE_CONTACT_URL);
    }

    public static final String FRONTOFFICE_COPYRIGHT_YEARS = APPFACTORY_PREFIX + applicationPrefix + ".front.copyright.years";

    public static String getFrontofficeCopyrightYears() {
        return Static.getValue(FRONTOFFICE_COPYRIGHT_YEARS);
    }

    public static final String FRONTOFFICE_PIWIK_SITE_ID = "mc.gouv.piwik.external." + applicationName + ".piwikSiteId";

    public static String getFrontofficePiwikSiteId() {
        return Static.getValue(FRONTOFFICE_PIWIK_SITE_ID);
    }

    public static final String FRONTOFFICE_PIWIK_URL = "mc.gouv.piwik.external.piwikUrl";

    public static String getFrontofficePiwikURL() {
        return Static.getValue(FRONTOFFICE_PIWIK_URL);
    }
    
    public static final String GICHKEY_URL = "mc.gouv.appfactory.front.gichkey.url";

    public static String getGichkeyUrl() {
        return Static.getValue(GICHKEY_URL);
    }
    
    public static final String GICHUNI_URL = "mc.gouv.appfactory.front.gichuni.url";

    public static String getGichuniUrl() {
        return Static.getValue(GICHUNI_URL);
    }
    
    public static final String GICHKEY_CLIENT_ID = APPFACTORY_PREFIX + applicationPrefix + ".gichkey.client_id";

    public static String getGichkeyClientId() {
        return Static.getValue(GICHKEY_CLIENT_ID);
    }
    
    public static final String GICHKEY_CLIENT_SECRET = APPFACTORY_PREFIX + applicationPrefix + ".gichkey.client_secret";

    public static String getGichkeyClientSecret() {
        return Static.getValue(GICHKEY_CLIENT_SECRET);
    }

    public static final String PAIEMENT_PROVIDER = APPFACTORY_PREFIX + applicationPrefix + ".paiement.provider";

    public static String getPaiementProvider() {
        String value = Static.getValue(PAIEMENT_PROVIDER);
        return StringUtils.isBlank(value) ? "vide" : value;
    }

    public static final String MONETICO_URL = APPFACTORY_PREFIX + applicationPrefix + ".monetico.url";

    public static String getMoneticoUrl() {
        String value = Static.getValue(MONETICO_URL);
        return StringUtils.isBlank(value) ? "vide" : value;
    }

    public static String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }

    static {
        //Vérification que chaque propriété a bien été configurée
        List<String> propertiesNotFound = new ArrayList<>();
        try {
            Method[] methods = AfServletGouvPropertiesResolver.class.getDeclaredMethods();
            for (Method m : methods) {
                if (!StringUtils.equals(m.getName(), "getFrontProperties")) {
                    LOGGER.info("Vérification de la propriété {}", m.getName());
                    String value = (String) m.invoke(null);
                    if (StringUtils.isBlank(value)) {
                        propertiesNotFound.add(m.getName());
                    }
                }
            }

        } catch (InvocationTargetException | IllegalAccessException e) {
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
        propertiesDTOS.add(new PropertiesDTO(GICHKEY_REDIRECT_URL, getGichkeyRedirectUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_PROFIL_INDIVIDUAL_URL, getGichuniProfilIndividualUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_PROFIL_COMPANY_URL, getGichuniProfilCompanyUrl()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_CONTACT_URL, getFrontofficeContactUrl()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_COPYRIGHT_YEARS, getFrontofficeCopyrightYears()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_PIWIK_SITE_ID, getFrontofficePiwikSiteId()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_PIWIK_URL, getFrontofficePiwikURL()));
        propertiesDTOS.add(new PropertiesDTO(PAIEMENT_PROVIDER, getPaiementProvider()));
        propertiesDTOS.add(new PropertiesDTO(MONETICO_URL, getMoneticoUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_URL, getGichuniUrl()));
        propertiesDTOS.add(new PropertiesDTO(FILE_JWT, getFileJwt()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_PARTICULER_URL_FR, getSuiviDemarcheParticulierUrlFr()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_PARTICULER_URL_EN, getSuiviDemarcheParticulierUrlEn()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_ENTREPRISE_URL_FR, getSuiviDemarcheEntrepriseUrlFr()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_ENTREPRISE_URL_EN, getSuiviDemarcheEntrepriseUrlEn()));
        propertiesDTOS.add(new PropertiesDTO(LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_FR, getLienRevocationCertifsElectroniquesFr()));
        propertiesDTOS.add(new PropertiesDTO(LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_EN, getLienRevocationCertifsElectroniquesEn()));
        return propertiesDTOS;
    }
}
