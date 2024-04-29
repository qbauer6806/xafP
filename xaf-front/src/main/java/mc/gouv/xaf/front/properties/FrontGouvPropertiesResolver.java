package mc.gouv.xaf.front.properties;

import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

/**
 * 
 * Classe permettant de récupérer les propriétés externalisées dans les fichiers .properties du serveur
 * 
 * @author qdeme
 * 
 */
@Component
public class FrontGouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontGouvPropertiesResolver.class);

    private static final String APPFACTORY_PREFIX = "mc.gouv.appfactory";

    @Value("${application.name}")
    private String applicationName;

    private String applicationPrefix = "";
    private String demarcheId;

    // GLOBAL
    @Value("${mc.gouv.servicerest.api.pays.url}")
    private String paysUrl;

    @Value("${mc.gouv.file.api.url}")
    private String fileUrl;

    @Value("${mc.gouv.tgf.api.url}")
    private String tgfApiUrl;

    @Value("${mc.gouv.vscan.api.url}")
    private String vscanUrl;

    @Value("${mc.gouv.gichkey.url}")
    private String gichkeyUrl;

    @Value("${mc.gouv.gichuni.api.url}")
    private String gichuniUrl;

    @Value("${mc.gouv.matomo.api.url}")
    private String piwikUrl;

    @Value("${mc.gouv.monetico.url:}")
    private String moneticoUrl;


    // FRONT
    @Value("${mc.gouv.${application.name}.frontserver.copyright.years}")
    private String copyrightYears;

    @Value("${mc.gouv.${application.name}.frontserver.matomo.matomoSiteId}")
    private String piwikSiteId;

    @Value("${mc.gouv.${application.name}.frontserver.key}")
    private String frontserverKey;

    @Value("${mc.gouv.${application.name}.frontserver.url}")
    private String backUrl;

    @Value("${mc.gouv.${application.name}.frontserver.redirectToBo.url}")
    private String demandeUrl;

    @Value("${mc.gouv.${application.name}.frontserver.api.url}")
    private String apiUrl;

    @Value("${mc.gouv.${application.name}.frontserver.jwt}")
    private String frontserverJwt;

    @Value("${mc.gouv.${application.name}.frontserver.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.pocts.frontserver.tgf.jwt}")
    private String tgfApiJwt;

    @Value("${mc.gouv.${application.name}.frontserver.vscan.jwt}")
    private String vscanJwt;

    @Value("${mc.gouv.${application.name}.frontserver.maxUploadParIntervalle}")
    private String maxUploadParIntervalle;

    @Value("${mc.gouv.${application.name}.frontserver.tempsIntervalleUpload}")
    private String tempsIntervalleUpload;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.redirect.url}")
    private String gichkeyRedirectUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.keycloak.redirect.uri}")
    private String gichkeyKeycloakRedirectUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichuni.profil.individual.url}")
    private String gichuniProfilIndividualUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichuni.profil.company.url}")
    private String gichuniProfilCompanyUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.client_secret}")
    private String gichkeyClientSecret;

    @Value("${mc.gouv.${application.name}.frontserver.paiement.provider:}")
    private String paiementProvider;


    @PostConstruct
    private void initPrefix() throws IntrospectionException, IllegalAccessException, InvocationTargetException,
            GouvPropertyNotFoundException {

        if (StringUtils.isNotBlank(applicationName)) {
            applicationPrefix = "." + applicationName;
            demarcheId = StringUtils.upperCase(applicationName);
        }

        //Vérification que chaque propriété a bien été configurée
        List<String> propertiesNotFound = new ArrayList<>();
        try {

            for (PropertyDescriptor propertyDescriptor : Introspector
                    .getBeanInfo(FrontGouvPropertiesResolver.class, Object.class).getPropertyDescriptors()) {

                Method method = getMethod(propertyDescriptor);

                checkProperties(propertiesNotFound, method, propertyDescriptor);
            }

        } catch (IntrospectionException e) {
            LOGGER.error("Erreur lors de l'introspection");
            throw e;
        }

        if (!propertiesNotFound.isEmpty()) {
            throw new GouvPropertyNotFoundException(propertiesNotFound);
        }

    }

    private Method getMethod(PropertyDescriptor propertyDescriptor) {
        Method method;
        try {
            LOGGER.info("Vérification de la propriété via le get : {}", propertyDescriptor.getReadMethod());
            method = propertyDescriptor.getReadMethod();
        } catch (SecurityException e) {
            LOGGER.error("Erreur lors de la récupération de la méthode");
            throw e;
        }
        return method;
    }

    private void checkProperties(List<String> propertiesNotFound, Method method, PropertyDescriptor propertyDescriptor) throws InvocationTargetException, IllegalAccessException {
        try {
            Object value = method.invoke(this);
            if (value instanceof String) {
                if (StringUtils.isBlank((String) value)) {
                    propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
                }
            } else if (value == null) {
                propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
            }
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Erreur lors de l'invocation de la méthode");
            throw e;
        }
    }

    public String getDemarcheId() {
        return applicationName;
    }

    public String getPaysUrl() {
        return paysUrl;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public String getGichkeyRedirectUrl() {
        return gichkeyRedirectUrl.replace("<redirect_uri>", getGichkeyKeycloakRedirectUri());
    }

    public String getGichkeyKeycloakRedirectUri() {
        return gichkeyKeycloakRedirectUrl;
    }

    public String getGichuniProfilIndividualUrl() {
        return gichuniProfilIndividualUrl;
    }

    public String getGichuniProfilCompanyUrl() {
        return gichuniProfilCompanyUrl;
    }

    /* Properties propres à la démarche */

    public String getApiUrl() {
        return apiUrl;
    }

    public String getBackOfficeUrl() {
        return backUrl;
    }

    public String getBackOfficeDemandeUrl() {
        return demandeUrl;
    }

    public String getSharedKey() {
        return frontserverKey;
    }

    public String getFileJwt() {
        return fileJwt;
    }

    public String getApiJwt() {
        return frontserverJwt;
    }

    public String getTgfApiUrl() {
        return StringUtils.isBlank(tgfApiUrl) ? "vide" : tgfApiUrl;
    }

    public String getTgfApiJwt() {
        return StringUtils.isBlank(tgfApiJwt) ? "vide" : tgfApiJwt;
    }

    public String getVscanUrl() {
        return vscanUrl;
    }

    public String getVscanJwt() {
        return vscanJwt;
    }

    public String getMaxUploadParIntervalle() {
        return maxUploadParIntervalle;
    }

    public String getTempsIntervalleUpload() {
        return tempsIntervalleUpload;
    }


    public String getFrontofficeCopyrightYears() {
        return copyrightYears;
    }

    public String getFrontofficePiwikSiteId() {
        return piwikSiteId;
    }

    public String getFrontofficePiwikURL() {
        return piwikUrl;
    }

    public String getGichkeyUrl() {
        return gichkeyUrl;
    }

    public String getGichuniUrl() {
        return gichuniUrl;
    }

    public String getGichkeyClientId() {
        return gichkeyClientId;
    }

    public String getGichkeyClientSecret() {
        return gichkeyClientSecret;
    }

    public String getPaiementProvider() {
        String value = paiementProvider;
        return StringUtils.isBlank(value) ? "vide" : value;
    }

    public String getMoneticoUrl() {
        String value = moneticoUrl;
        return StringUtils.isBlank(value) ? "vide" : value;
    }

    public String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }

    public List<PropertiesDTO> getFrontProperties() {
        // TODO refactor le nom de ces properties une fois que le premier WYSI xaf12 sera prêt
        final String LOGIN_KEEP_ALIVE = "mc.gouv.appfactory.front.login.keepalive.url";
        final String GICHKEY_REDIRECT_URL = APPFACTORY_PREFIX + "." + applicationName + ".gichkey.redirect.url";
        final String GICHUNI_PROFIL_INDIVIDUAL_URL = "mc.gouv.appfactory.front.gichuni.profil.individual.url";
        final String GICHUNI_PROFIL_COMPANY_URL = "mc.gouv.appfactory.front.gichuni.profil.company.url";
        final String FRONTOFFICE_CONTACT_URL = APPFACTORY_PREFIX + "." + applicationName + ".front.login.contact.url";
        final String FRONTOFFICE_COPYRIGHT_YEARS = APPFACTORY_PREFIX + "." + applicationName + ".front.copyright.years";
        final String FRONTOFFICE_PIWIK_SITE_ID = "mc.gouv.piwik.external." + applicationName + ".piwikSiteId";
        final String FRONTOFFICE_PIWIK_URL = "mc.gouv.piwik.external.piwikUrl";
        final String PAIEMENT_PROVIDER = APPFACTORY_PREFIX + "." + applicationName + ".paiement.provider";
        final String MONETICO_URL = APPFACTORY_PREFIX + "." + applicationName + ".monetico.url";
        final String GICHUNI_URL = "mc.gouv.appfactory.front.gichuni.url";

        List<PropertiesDTO> propertiesDTOS = new ArrayList<>();
        propertiesDTOS.add(new PropertiesDTO(LOGIN_KEEP_ALIVE, ""));
        propertiesDTOS.add(new PropertiesDTO(GICHKEY_REDIRECT_URL, getGichkeyRedirectUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_PROFIL_INDIVIDUAL_URL, getGichuniProfilIndividualUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_PROFIL_COMPANY_URL, getGichuniProfilCompanyUrl()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_CONTACT_URL, ""));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_COPYRIGHT_YEARS, getFrontofficeCopyrightYears()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_PIWIK_SITE_ID, getFrontofficePiwikSiteId()));
        propertiesDTOS.add(new PropertiesDTO(FRONTOFFICE_PIWIK_URL, getFrontofficePiwikURL()));
        propertiesDTOS.add(new PropertiesDTO(PAIEMENT_PROVIDER, getPaiementProvider()));
        propertiesDTOS.add(new PropertiesDTO(MONETICO_URL, getMoneticoUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_URL, getGichuniUrl()));
        // TODO A REVOIR
        //propertiesDTOS.add(new PropertiesDTO(FILE_JWT, getFileJwt()));
        return propertiesDTOS;
    }
}
