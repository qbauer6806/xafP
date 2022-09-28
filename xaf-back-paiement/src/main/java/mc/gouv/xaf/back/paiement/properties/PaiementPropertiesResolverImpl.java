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

    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_NAME = "XAF_MONETICO_CONTEXTE_COMMANDE_NAME";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_FIRSTNAME = "XAF_MONETICO_CONTEXTE_COMMANDE_FIRSTNAME";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE = "XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_VILLE = "XAF_MONETICO_CONTEXTE_COMMANDE_VILLE";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL = "XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_PAYS = "XAF_MONETICO_CONTEXTE_COMMANDE_PAYS";

    private static final String XAF_RETRY_INITIAL_DELAY = "XAF_RETRY_INITIAL_DELAY";
    private static final String XAF_RETRY_COUNT = "XAF_RETRY_COUNT";
    private static final String XAF_RETRY_MULTIPLIER = "XAF_RETRY_MULTIPLIER";

    private static final String XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE = "XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE";


    @Override
    public String getXafMoneticoTexteAller() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_TEXTE_ALLER);
        return propertiesDTO.getValue();
    }

    @Override
    public int getValiditeMaxMoyenPaiement() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_DUREE_CARTE);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public String getFactureUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.serviceUrl");
    }

    @Override
    public String getFactureToken() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.token");
    }

    @Override
    public int getRegistre() {
        return Integer.parseInt(Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.registre"));
    }

    @Override
    public String getNomParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_NAME);
        return propertiesDTO.getValue();
    }

    @Override
    public String getPrenomParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_FIRSTNAME);
        return propertiesDTO.getValue();
    }

    @Override
    public String getAdresseParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE);
        return propertiesDTO.getValue();
    }

    @Override
    public String getVilleParDefaut() {

        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_VILLE);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodePostalParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodePaysParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CONTEXTE_COMMANDE_PAYS);
        return propertiesDTO.getValue();
    }

    @Override
    public int getXafRetryInitialDelay() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_INITIAL_DELAY);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public int getXafRetryCount() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_COUNT);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public int getXafRetryMultiplier() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_MULTIPLIER);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public String getXafPaiementImmediatHeureDiffere() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCurrency() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.currency");
    }

    @Override
    public String getTpe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.tpe");
    }

    @Override
    public String getPaiementClef() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.cleSceau");
    }

    @Override
    public String getVersionAller() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.versionAller");
    }

    @Override
    public String getVersionCapture() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.versionCapture");
    }

    @Override
    public String getCodeSiteStandard() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.codeSiteStandard");
    }

    @Override
    public String getXafMoneticoCodeSiteIframe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.codeSiteIframe");
    }

    @Override
    public String getCaptureUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.captureUrl");
    }

    @Override
    public String getSuccesUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.successUrl");
    }

    @Override
    public String getEchecUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.echecUrl");
    }

    @Override
    public String getXafMonetico3dsv2Scenario() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.3dvs2Scenario");
    }

}
