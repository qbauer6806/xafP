package mc.gouv.xaf.front.properties;

import lombok.Getter;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
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

    // GLOBAL
    @Getter
    @Value("${mc.gouv.servicerest.api.pays.url}")
    private String paysUrl;

    @Getter
    @Value("${mc.gouv.file.api.url}")
    private String fileUrl;

    @Value("${mc.gouv.tgf.api.url}")
    private String tgfApiUrl;

    @Getter
    @Value("${mc.gouv.vscan.api.url}")
    private String vscanUrl;

    @Getter
    @Value("${mc.gouv.gichkey.url}")
    private String gichkeyUrl;

    @Getter
    @Value("${mc.gouv.gichuni.api.url}")
    private String gichuniUrl;

    @Value("${mc.gouv.matomo.api.url}")
    private String piwikUrl;

    @Value("${mc.gouv.monetico.url:}")
    private String moneticoUrl;


    // FRONT
    @Value("${mc.gouv.${application.name}.frontserver.copyright.years}")
    private String copyrightYears;

    @Value("${mc.gouv.${application.name}.frontserver.matomo.site_id}")
    private String piwikSiteId;

    @Value("${mc.gouv.${application.name}.frontserver.key}")
    private String frontserverKey;

    @Value("${mc.gouv.${application.name}.frontserver.back.url}")
    private String backUrl;

    @Value("${mc.gouv.${application.name}.frontserver.redirectToBo.url}")
    private String demandeUrl;

    @Getter
    @Value("${mc.gouv.${application.name}.frontserver.api.url}")
    private String apiUrl;

    @Value("${mc.gouv.${application.name}.frontserver.jwt}")
    private String frontserverJwt;

    @Getter
    @Value("${mc.gouv.${application.name}.frontserver.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.${application.name}.frontserver.tgf.jwt}")
    private String tgfApiJwt;

    @Getter
    @Value("${mc.gouv.${application.name}.frontserver.vscan.jwt}")
    private String vscanJwt;

    @Getter
    @Value("${mc.gouv.frontserver.max.upload.intervalle}")
    private String maxUploadParIntervalle;

    @Getter
    @Value("${mc.gouv.frontserver.temps.upload.intervalle}")
    private String tempsIntervalleUpload;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.redirect.url}")
    private String gichkeyRedirectUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.keycloak.redirect.uri}")
    private String gichkeyKeycloakRedirectUrl;

    @Getter
    @Value("${mc.gouv.gichuni.profil.particulier.url}")
    private String gichuniProfilIndividualUrl;

    @Getter
    @Value("${mc.gouv.gichuni.profil.entreprise.url}")
    private String gichuniProfilCompanyUrl;

    @Getter
    @Value("${mc.gouv.${application.name}.frontserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Getter
    @Value("${mc.gouv.${application.name}.frontserver.gichkey.client_secret}")
    private String gichkeyClientSecret;

    @Value("${mc.gouv.${application.name}.frontserver.paiement.provider:}")
    private String paiementProvider;

    @Getter
    @Value("${mc.gouv.gichuni.front.url}")
    private String gichuniFrontUrl;

    @Value("${mc.gouv.mconnect.revocation.certificats.url.fr:}")
    private String lienRevocationCertifsElectroniquesFr;
    @Value("${mc.gouv.mconnect.revocation.certificats.url.en:}")
    private String lienRevocationCertifsElectroniquesEn;

    @Value("${mc.gouv.gichuni.pub.mconnect.url.fr:}")
    private String pubMconnectUrlFr;
    @Value("${mc.gouv.gichuni.pub.mconnect.url.en:}")
    private String pubMconnectUrlEn;

    @Value("${mc.gouv.${application.name}.frontserver.2tiers.activation:false}")
    private String proxy2tiersActivation;

    @Getter
    @Value("${mc.gouv.gichuni.demarche.particulier.url.fr:OPTIONAL}")
    private String gichuniDemarcheParticulierUrlFr;
    @Getter
    @Value("${mc.gouv.gichuni.demarche.particulier.url.en:OPTIONAL}")
    private String gichuniDemarcheParticulierUrlEn;
    @Getter
    @Value("${mc.gouv.gichuni.demarche.entreprise.url.fr:OPTIONAL}")
    private String gichuniDemarcheEntrepriseUrlFr;
    @Getter
    @Value("${mc.gouv.gichuni.demarche.entreprise.url.en:OPTIONAL}")
    private String gichuniDemarcheEntrepriseUrlEn;

    @PostConstruct
    private void initPrefix() throws IntrospectionException, IllegalAccessException, InvocationTargetException,
            GouvPropertyNotFoundException {

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
            if (value instanceof String s) {
                if (StringUtils.isBlank(s)) {
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

    public String getGichkeyRedirectUrl() {
        return gichkeyRedirectUrl.replace("<redirect_uri>", getGichkeyKeycloakRedirectUri());
    }

    public String getGichkeyKeycloakRedirectUri() {
        return gichkeyKeycloakRedirectUrl;
    }

    /* Properties propres à la démarche */

    public String getBackOfficeUrl() {
        return backUrl;
    }

    public String getBackOfficeDemandeUrl() {
        return demandeUrl;
    }

    public String getSharedKey() {
        return frontserverKey;
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

    public String getFrontofficeCopyrightYears() {
        return copyrightYears;
    }

    public String getFrontofficePiwikSiteId() {
        return piwikSiteId;
    }

    public String getFrontofficePiwikURL() {
        return piwikUrl;
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

    public String getLienRevocationCertifsElectroniquesFr() {
        return StringUtils.isBlank(lienRevocationCertifsElectroniquesFr) ? "vide" : lienRevocationCertifsElectroniquesFr;
    }
    public String getLienRevocationCertifsElectroniquesEn() {
        return StringUtils.isBlank(lienRevocationCertifsElectroniquesEn) ? "vide" : lienRevocationCertifsElectroniquesEn;
    }
    public String getPubMconnectUrlFr() {
        return StringUtils.isBlank(pubMconnectUrlFr) ? "vide" : pubMconnectUrlFr;
    }

    public String getPubMconnectUrlEn() {
        return StringUtils.isBlank(pubMconnectUrlEn) ? "vide" : pubMconnectUrlEn;
    }

    public boolean getProxy2TiersActivation() {
        String value = proxy2tiersActivation;
        return StringUtils.isNotBlank(value) && value.equals("true");
    }

    public List<PropertiesDTO> getFrontProperties() {
        // TODO refactor le nom de ces properties une fois que le premier WYSI xaf12 sera prêt
        final String LOGIN_KEEP_ALIVE = APPFACTORY_PREFIX + ".front.login.keepalive.url";
        final String GICHKEY_REDIRECT_URL = APPFACTORY_PREFIX + "." + applicationName + ".gichkey.redirect.url";
        final String GICHUNI_PROFIL_INDIVIDUAL_URL = APPFACTORY_PREFIX + ".front.gichuni.profil.individual.url";
        final String GICHUNI_PROFIL_COMPANY_URL = APPFACTORY_PREFIX + ".front.gichuni.profil.company.url";
        final String FRONTOFFICE_CONTACT_URL = APPFACTORY_PREFIX + "." + applicationName + ".front.login.contact.url";
        final String FRONTOFFICE_COPYRIGHT_YEARS = APPFACTORY_PREFIX + "." + applicationName + ".front.copyright.years";
        final String FRONTOFFICE_PIWIK_SITE_ID = "mc.gouv.piwik.external." + applicationName + ".piwikSiteId";
        final String FRONTOFFICE_PIWIK_URL = "mc.gouv.piwik.external.piwikUrl";
        final String PAIEMENT_PROVIDER = APPFACTORY_PREFIX + "." + applicationName + ".paiement.provider";
        final String MONETICO_URL = APPFACTORY_PREFIX + "." + applicationName + ".monetico.url";
        final String GICHUNI_FRONT_URL = APPFACTORY_PREFIX + ".front.gichuni.url";

        // #58046 - Ajout de propriétés partagées par tous les Front office
        final String GICHUNI_USAGER_PARTICULER_URL_FR = "mc.gouv.gichuni.particulier.url.fr";
        final String GICHUNI_USAGER_PARTICULER_URL_EN = "mc.gouv.gichuni.particulier.url.en";
        final String GICHUNI_USAGER_ENTREPRISE_URL_FR = "mc.gouv.gichuni.entreprise.url.fr";
        final String GICHUNI_USAGER_ENTREPRISE_URL_EN = "mc.gouv.gichuni.entreprise.url.en";

        // #58041 - [BO] Clé BO pour lien vers le formulaire de révocation des certificats électroniques
        final String LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_FR = "mc.gouv.mconnect.revocation.certificats.url.fr";
        final String LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_EN = "mc.gouv.mconnect.revocation.certificats.url.en";

        final String PUB_MCONNECT_URL_FR = "mc.gouv.gichuni.pub.mconnect.url.fr";
        final String PUB_MCONNECT_URL_EN = "mc.gouv.gichuni.pub.mconnect.url.en";

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

        //merge depuis la 11.3.0
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_FRONT_URL, getGichuniFrontUrl()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_PARTICULER_URL_FR, getGichuniDemarcheParticulierUrlFr()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_PARTICULER_URL_EN, getGichuniDemarcheParticulierUrlEn()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_ENTREPRISE_URL_FR, getGichuniDemarcheEntrepriseUrlFr()));
        propertiesDTOS.add(new PropertiesDTO(GICHUNI_USAGER_ENTREPRISE_URL_EN, getGichuniDemarcheEntrepriseUrlEn()));
        propertiesDTOS.add(new PropertiesDTO(LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_FR, getLienRevocationCertifsElectroniquesFr()));
        propertiesDTOS.add(new PropertiesDTO(LIEN_REVOCATION_CERTIFS_ELECTRONIQUES_EN, getLienRevocationCertifsElectroniquesEn()));

        propertiesDTOS.add(new PropertiesDTO(PUB_MCONNECT_URL_FR, getPubMconnectUrlFr()));
        propertiesDTOS.add(new PropertiesDTO(PUB_MCONNECT_URL_EN, getPubMconnectUrlEn()));

        return propertiesDTOS;
    }
}
