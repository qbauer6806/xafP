package mc.gouv.xaf.front.properties;

import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/**
 * Classe permettant de récupérer les propriétés externalisées dans les fichiers .properties du serveur
 *
 * @author qdeme
 */
@Getter
@Component
public class FrontGouvPropertiesResolver {

    @Value("${application.name}")
    private String demarcheId;

    // GLOBAL

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
    @Value("${mc.gouv.appli.frontserver.copyright.years}")
    private String copyrightYears;

    @Value("${mc.gouv.appli.frontserver.matomo.site_id}")
    private String matomoSiteId;

    @Value("${mc.gouv.appli.frontserver.key}")
    private String frontserverKey;

    @Value("${mc.gouv.appli.frontserver.back.url}")
    private String backUrl;

    @Value("${mc.gouv.appli.frontserver.redirectToBo.url}")
    private String demandeUrl;

    @Value("${mc.gouv.appli.frontserver.api.url}")
    private String apiUrl;

    @Value("${mc.gouv.appli.frontserver.jwt}")
    private String frontserverJwt;

    @Value("${mc.gouv.appli.frontserver.file.jwt}")
    private String fileJwt;

    @Value("${mc.gouv.appli.frontserver.tgf.jwt:}")
    private String tgfApiJwt;

    @Value("${mc.gouv.appli.frontserver.vscan.jwt}")
    private String vscanJwt;

    @Value("${mc.gouv.frontserver.max.upload.intervalle}")
    private String maxUploadParIntervalle;

    @Value("${mc.gouv.frontserver.temps.upload.intervalle}")
    private String tempsIntervalleUpload;

    @Value("${mc.gouv.appli.frontserver.gichkey.redirect.url}")
    private String gichkeyRedirectUrl;

    @Value("${mc.gouv.appli.frontserver.gichkey.keycloak.redirect.uri}")
    private String gichkeyKeycloakRedirectUrl;

    @Value("${mc.gouv.gichuni.profil.particulier.url}")
    private String gichuniProfilIndividualUrl;

    @Value("${mc.gouv.gichuni.profil.entreprise.url}")
    private String gichuniProfilCompanyUrl;

    @Value("${mc.gouv.appli.frontserver.gichkey.client_id}")
    private String gichkeyClientId;

    @Value("${mc.gouv.appli.frontserver.gichkey.client_secret}")
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

    @Value("${mc.gouv.appli.frontserver.2tiers.activation:false}")
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
    @Value("${mc.gouv.appfactory.external.lyra.javascript.url:}")
    private String lyraJavascriptUrl;
    @Value("${mc.gouv.appfactory.external.lyra.additional.css.url:}")
    private String lyraJavascriptCssUrl;
    @Value("${mc.gouv.appfactory.external.lyra.additional.javascript.url:}")
    private String lyraJavascriptAdditionalUrl;
    @Value("${mc.gouv.appfactory.external.lyra.javascript.sri.url:}")
    private String lyraJavascriptSriUrl;
    @Value("${mc.gouv.appfactory.appli.external.lyra.public.key:}")
    private String lyraPublicKey;
    @Value("${mc.gouv.appli.frontserver.api.url}/paiement")
    private String mwpaymtCallbackUri;

    /* Properties propres à la démarche */

    public String getPorteDocUrl() {
        String value = getGichuniUrl();
        return StringUtils.isBlank(value) ? "vide" : value + "/public/doc-holder";
    }

    public List<PropertiesDTO> getFrontProperties() {

        List<PropertiesDTO> propertiesDTOS = new ArrayList<>();

        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appli.frontserver.gichkey.redirect.url",
                getGichkeyRedirectUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appli.frontserver.copyright.years", getCopyrightYears()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appli.frontserver.matomo.site_id", getMatomoSiteId()));
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
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.javascript.url", getLyraJavascriptUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.additional.css.url", getLyraJavascriptCssUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.additional.javascript.url", getLyraJavascriptAdditionalUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.external.lyra.javascript.sri.url", getLyraJavascriptSriUrl()));
        propertiesDTOS.add(new PropertiesDTO("mc.gouv.appfactory.appli.external.lyra.public.key", getLyraPublicKey()));
        return propertiesDTOS;
    }
}
