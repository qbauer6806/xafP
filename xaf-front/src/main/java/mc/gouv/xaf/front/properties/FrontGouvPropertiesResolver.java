package mc.gouv.xaf.front.properties;

import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.annotation.PostConstruct;
import lombok.Getter;
import mc.gouv.xaf.shared.dto.PropertiesDTO;

/**
 * Classe permettant de récupérer les propriétés externalisées dans les fichiers .properties du serveur
 *
 * @author qdeme
 */
@Getter
@Component
public class FrontGouvPropertiesResolver {

    private static final Logger LOGGER = LoggerFactory.getLogger(FrontGouvPropertiesResolver.class);

    @Value("${application.name}")
    private String demarcheId;

    // GLOBAL    
    @Value("${mc.gouv.nomen.api.url}")
    private String nomenUrl;

    @Value("${mc.gouv.file.api.url}")
    private String fileUrl;

    @Value("${mc.gouv.tgf.api.url:}")
    private String tgfApiUrl;

    @Value("${mc.gouv.vscan.api.url}")
    private String vscanUrl;

    @Value("${mc.gouv.vscan.activated}")
    private boolean vscanActivated;

    @Value("${mc.gouv.gichkey.url}")
    private String gichkeyUrl;

    @Value("${mc.gouv.gichuni.api.url}")
    private String gichuniUrl;

    @Value("${mc.gouv.matomo.api.url}")
    private String matomoUrl;

    @Value("${mc.gouv.monetico.url:}")
    private String moneticoUrl;

    // FRONT
    @Value("${mc.gouv.${application.name}.frontserver.copyright.years}")
    private String copyrightYears;

    @Value("${mc.gouv.${application.name}.frontserver.matomo.site_id}")
    private String matomoSiteId;

    @Value("${mc.gouv.${application.name}.frontserver.key}")
    private String frontserverKey;

    @Value("${mc.gouv.${application.name}.frontserver.back.url}")
    private String backUrl;

    @Value("${mc.gouv.${application.name}.frontserver.redirectToBo.url}")
    private String demandeUrl;

    @Value("${mc.gouv.${application.name}.frontserver.api.url}")
    private String apiUrl;

    @Value("${mc.gouv.${application.name}.frontserver.mwpaymnt.url}")
    private String mwpaymntUrl;

    @Value("${mc.gouv.${application.name}.frontserver.jwt}")
    private String frontserverJwt;

    @Value("${mc.gouv.${application.name}.frontserver.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.${application.name}.frontserver.tgf.jwt:}")
    private String tgfApiJwt;

    @Value("${mc.gouv.${application.name}.frontserver.vscan.jwt}")
    private String vscanJwt;

    @Value("${mc.gouv.${application.name}.frontserver.nomen.jwt}")
    private String nomenJwt;

    @Value("${mc.gouv.payscache.duration}")
    private String paysCacheDuration;

    @Value("${mc.gouv.frontserver.max.upload.intervalle}")
    private String maxUploadParIntervalle;

    @Value("${mc.gouv.frontserver.temps.upload.intervalle}")
    private String tempsIntervalleUpload;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.redirect.url}")
    private String gichkeyRedirectUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.keycloak.redirect.uri}")
    private String gichkeyKeycloakRedirectUrl;

    @Value("${mc.gouv.gichuni.profil.particulier.url}")
    private String gichuniProfilIndividualUrl;

    @Value("${mc.gouv.gichuni.profil.entreprise.url}")
    private String gichuniProfilCompanyUrl;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.${application.name}.frontserver.gichkey.client_secret}")
    private String gichkeyClientSecret;

    @Value("${mc.gouv.gichuni.front.url}")
    private String gichuniFrontUrl;

    @Value("${mc.gouv.mconnect.revocation.certificats.url.fr:}")
    private String lienRevocationCertifsElectroniquesFr;
    @Value("${mc.gouv.mconnect.revocation.certificats.url.en:}")
    private String lienRevocationCertifsElectroniquesEn;

    @Value("${mc.gouv.gichuni.pub.mconnect.url.fr}")
    private String pubMconnectUrlFr;
    @Value("${mc.gouv.gichuni.pub.mconnect.url.en}")
    private String pubMconnectUrlEn;

    @Value("${mc.gouv.${application.name}.frontserver.2tiers.activation:false}")
    private boolean proxy2tiersActivation;

    @Value("${spring.servlet.multipart.max-file-size}")
    private String maxFileSize;

    @Value("${mc.gouv.file.extensions.whitelist}")
    private String extensionsWhitelist;

    @Value("${mc.gouv.gichuni.demarche.particulier.url.fr}")
    private String gichuniDemarcheParticulierUrlFr;
    @Value("${mc.gouv.gichuni.demarche.particulier.url.en}")
    private String gichuniDemarcheParticulierUrlEn;
    @Value("${mc.gouv.gichuni.demarche.entreprise.url.fr}")
    private String gichuniDemarcheEntrepriseUrlFr;
    @Value("${mc.gouv.gichuni.demarche.entreprise.url.en}")
    private String gichuniDemarcheEntrepriseUrlEn;

    @Value("${mc.gouv.appfactory.servicepublic.particulier.url.fr}")
    private String servicePublicParticulierUrlFr;
    @Value("${mc.gouv.appfactory.servicepublic.particulier.url.en}")
    private String servicePublicParticulierUrlEn;
    @Value("${mc.gouv.appfactory.servicepublic.entreprise.url.fr}")
    private String servicePublicEntrepriseUrlFr;
    @Value("${mc.gouv.appfactory.servicepublic.entreprise.url.en}")
    private String servicePublicEntrepriseUrlEn;
    @Value("${mc.gouv.appfactory.cookies.particulier.url.fr}")
    private String cookiesParticulierUrlFr;
    @Value("${mc.gouv.appfactory.cookies.particulier.url.en}")
    private String cookiesParticulierUrlEn;
    @Value("${mc.gouv.appfactory.cookies.entreprise.url.fr}")
    private String cookiesEntrepriseUrlFr;
    @Value("${mc.gouv.appfactory.cookies.entreprise.url.en}")
    private String cookiesEntrepriseUrlEn;
    @Value("${mc.gouv.appfactory.mconnect.help.url.fr}")
    private String mconnectHelpUrlFr;
    @Value("${mc.gouv.appfactory.mconnect.help.url.en}")
    private String mconnectHelpUrlEn;
    @Value("${mc.gouv.appfactory.support.url.fr}")
    private String supportUrlFr;
    @Value("${mc.gouv.appfactory.support.url.en}")
    private String supportUrlEn;
    @Value("${mc.gouv.appfactory.compte.particulier.url.fr}")
    private String compteParticulierUrlFr;
    @Value("${mc.gouv.appfactory.compte.particulier.url.en}")
    private String compteParticulierUrlEn;
    @Value("${mc.gouv.appfactory.compte.entreprise.url.fr}")
    private String compteEntrepriseUrlFr;
    @Value("${mc.gouv.appfactory.compte.entreprise.url.en}")
    private String compteEntrepriseUrlEn;
    @Value("${mc.gouv.appfactory.monguichet.cgu.url.fr}")
    private String monguichetCguUrlFr;
    @Value("${mc.gouv.appfactory.monguichet.cgu.url.en}")
    private String monguichetCguUrlEn;
    @Value("${mc.gouv.appfactory.external.lyra.javascript.url}")
    private String lyraJavascriptUrl;
    @Value("${mc.gouv.appfactory.external.lyra.additional.css.url}")
    private String lyraJavascriptCssUrl;
    @Value("${mc.gouv.appfactory.external.lyra.additional.javascript.url}")
    private String lyraJavascriptAdditionalUrl;
    @Value("${mc.gouv.appfactory.rescart.external.lyra.public.key}")
    private String lyraPublicKey;
    @Value("${mc.gouv.rescart.frontserver.mwpaymnt.paiement.redirect.uri}")
    private String mwpaymntRedirectUri;

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
            LOGGER.debug("Vérification de la propriété via le get : {}", propertyDescriptor.getReadMethod());
            method = propertyDescriptor.getReadMethod();
        } catch (SecurityException e) {
            LOGGER.error("Erreur lors de la récupération de la méthode");
            throw e;
        }
        return method;
    }

    private void checkProperties(List<String> propertiesNotFound, Method method, PropertyDescriptor propertyDescriptor)
            throws InvocationTargetException, IllegalAccessException {
        try {
            Object value = method.invoke(this);
            if (value == null) {
                propertiesNotFound.add(propertyDescriptor.getReadMethod().toString());
            }
        } catch (IllegalArgumentException | InvocationTargetException | IllegalAccessException e) {
            LOGGER.error("Erreur lors de l'invocation de la méthode");
            throw e;
        }
    }

    /* Properties propres à la démarche */

    public String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }

    public List<PropertiesDTO> getFrontProperties() {

        List<PropertiesDTO> propertiesDTOS = new ArrayList<>();

        propertiesDTOS.add(new PropertiesDTO("mc.gouv." + demarcheId + ".frontserver.gichkey.redirect.url",
                getGichkeyRedirectUrl()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv." + demarcheId + ".frontserver.copyright.years", getCopyrightYears()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv." + demarcheId + ".frontserver.matomo.site_id", getMatomoSiteId()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.matomo.api.url", getMatomoUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.monetico.url", getMoneticoUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.mconnect.revocation.certificats.url.fr",
                getLienRevocationCertifsElectroniquesFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.mconnect.revocation.certificats.url.en",
                getLienRevocationCertifsElectroniquesEn()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.gichuni.profil.particulier.url", getGichuniProfilIndividualUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.gichuni.profil.entreprise.url", getGichuniProfilCompanyUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.gichuni.front.url", getGichuniFrontUrl()));
        propertiesDTOS.add(
                new PropertiesDTO("mc.gouv.gichuni.demarche.particulier.url.fr", getGichuniDemarcheParticulierUrlFr()));
        propertiesDTOS.add(
                new PropertiesDTO("mc.gouv.gichuni.demarche.particulier.url.en", getGichuniDemarcheParticulierUrlEn()));
        propertiesDTOS.add(
                new PropertiesDTO("mc.gouv.gichuni.demarche.entreprise.url.fr", getGichuniDemarcheEntrepriseUrlFr()));
        propertiesDTOS.add(
                new PropertiesDTO("mc.gouv.gichuni.demarche.entreprise.url.en", getGichuniDemarcheEntrepriseUrlEn()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.gichuni.pub.mconnect.url.fr", getPubMconnectUrlFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.gichuni.pub.mconnect.url.en", getPubMconnectUrlEn()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.servicepublic.particulier.url.fr",
                getServicePublicParticulierUrlFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.servicepublic.particulier.url.en",
                getServicePublicParticulierUrlEn()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.servicepublic.entreprise.url.fr",
                getServicePublicEntrepriseUrlFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.servicepublic.entreprise.url.en",
                getServicePublicEntrepriseUrlEn()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.cookies.particulier.url.fr", getCookiesParticulierUrlFr()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.cookies.particulier.url.en", getCookiesParticulierUrlEn()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.cookies.entreprise.url.fr", getCookiesEntrepriseUrlFr()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.cookies.entreprise.url.en", getCookiesEntrepriseUrlEn()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.mconnect.help.url.fr", getMconnectHelpUrlFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.mconnect.help.url.en", getMconnectHelpUrlEn()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.support.url.fr", getSupportUrlFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.support.url.en", getSupportUrlEn()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.compte.particulier.url.fr", getCompteParticulierUrlFr()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.compte.particulier.url.en", getCompteParticulierUrlEn()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.compte.entreprise.url.fr", getCompteEntrepriseUrlFr()));
        propertiesDTOS
                .add(new PropertiesDTO("mc.gouv.appfactory.compte.entreprise.url.en", getCompteEntrepriseUrlEn()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.monguichet.cgu.url.fr", getMonguichetCguUrlFr()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.monguichet.cgu.url.en", getMonguichetCguUrlEn()));

        // TODO a voir si on bouge cette partie ailleurs
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.javascript.url", getLyraJavascriptUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.additional.css.url", getLyraJavascriptCssUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.additional.javascript.url", getLyraJavascriptAdditionalUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory." + demarcheId + ".external.lyra.public.key", getLyraPublicKey()));
        return propertiesDTOS;
    }
}
