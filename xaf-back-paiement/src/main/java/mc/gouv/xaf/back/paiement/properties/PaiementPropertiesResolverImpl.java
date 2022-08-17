package mc.gouv.xaf.back.paiement.properties;

import mc.gouv.Static;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaiementPropertiesResolverImpl implements PaiementPropertiesResolver {

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Autowired
    private PropertiesService propertiesService;

    private static final String XAF_MONETICO_DUREE_CARTE = "XAF_MONETICO_DUREE_CARTE";

    private static final String XAF_MONETICO_TEXTE_ALLER = "XAF_MONETICO_TEXTE_ALLER";

    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE = "XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_VILLE = "XAF_MONETICO_CONTEXTE_COMMANDE_VILLE";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL = "XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_PAYS = "XAF_MONETICO_CONTEXTE_COMMANDE_PAYS";

    private static final String XAF_RETRY_INITIAL_DELAY = "XAF_RETRY_INITIAL_DELAY";
    private static final String XAF_RETRY_COUNT = "XAF_RETRY_COUNT";
    private static final String XAF_RETRY_MULTIPLIER = "XAF_RETRY_MULTIPLIER";

    private static final String XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE = "XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE";


    public String getXafMoneticoTexteAller() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_TEXTE_ALLER);
        return propertiesDTO.getValue();
    }

    public int getValiditeMaxMoyenPaiement() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_DUREE_CARTE);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public String getFactureUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.serviceUrl");
    }

    public String getFactureToken() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.token");
    }

    public int getRegistre() {
        return Integer.parseInt(Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.registre"));
    }

    public String getAdresseParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE);
        return propertiesDTO.getValue();
    }

    public String getVilleParDefaut() {

        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_VILLE);
        return propertiesDTO.getValue();
    }

    public String getCodePostalParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL);
        return propertiesDTO.getValue();
    }

    public String getCodePaysParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_PAYS);
        return propertiesDTO.getValue();
    }

    public int getXafRetryInitialDelay() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_INITIAL_DELAY);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public int getXafRetryCount() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_COUNT);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public int getXafRetryMultiplier() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_MULTIPLIER);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public String getXafPaiementImmediatHeureDiffere() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE);
        return propertiesDTO.getValue();
    }

    public String getApiRioUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".rio.url");
    }

    public String getApiRioJwt() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".rio.jwt");
    }

    public String getCurrency() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.currency");
    }

    public String getTpe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.tpe");
    }

    public String getPaiementClef() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.cleSceau");
    }

    public String getVersionAller() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.versionAller");
    }

    public String getVersionCapture() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.versionCapture");
    }

    public String getCodeSiteStandard() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.codeSiteStandard");
    }

    public String getXafMoneticoCodeSiteIframe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.codeSiteIframe");
    }

    public String getCaptureUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.captureUrl");
    }

    public String getSuccesUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.successUrl");
    }

    public String getEchecUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.echecUrl");
    }

    public String getXafMonetico3dsv2Scenario() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.3dvs2Scenario");
    }

}
