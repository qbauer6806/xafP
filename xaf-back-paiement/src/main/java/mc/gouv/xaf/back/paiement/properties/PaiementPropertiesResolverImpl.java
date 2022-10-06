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
    private static final String XAF_MONETICO_3DSV2_SCENARIO = "XAF_MONETICO_3DSV2_SCENARIO";

    private static final String XAF_CODE_PAIEMENT = "XAF_CODE_PAIEMENT";
    private static final String XAF_CODE_ECHANGE = "XAF_CODE_ECHANGE";
    private static final String XAF_TARIF_ECHANGE = "XAF_TARIF_ECHANGE";
    private static final String XAF_CODE_P_INTER = "XAF_CODE_P_INTER";

    private static final String XAF_RETRY_INITIAL_DELAY = "XAF_RETRY_INITIAL_DELAY";
    private static final String XAF_RETRY_COUNT = "XAF_RETRY_COUNT";
    private static final String XAF_RETRY_MULTIPLIER = "XAF_RETRY_MULTIPLIER";

    private static final String XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE = "XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE";
    private static final String PAIEMENT_IMMEDIAT_HEURE_DIFFERE_DEFAULT = "23:58:00";


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

    /**
     * <p>Permet de récupérer la valeur de la propriété XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE</p>
     * <p>Si cette propriété n'est pas ajoutée dans la BDD, on utilise une valeur par défault</p>
     * @return une chaine contenant l'heure d'arrêt du paiement immédiat
     */
    @Override
    public String getXafPaiementImmediatHeureDiffere() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE);
        return (propertiesDTO != null) ? propertiesDTO.getValue() : PAIEMENT_IMMEDIAT_HEURE_DIFFERE_DEFAULT;
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
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_3DSV2_SCENARIO);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodePaiement() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_CODE_PAIEMENT);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodeEchange() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_CODE_ECHANGE);
        return propertiesDTO.getValue();
    }

    @Override
    // TODO prendre en compte les changements de tarifs
    public double getTarifEchange() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_TARIF_ECHANGE);
        return propertiesDTO != null ? Double.parseDouble(propertiesDTO.getValue()) : null;
    }

    @Override
    public String getCodePermisInternational() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_CODE_P_INTER);
        return propertiesDTO.getValue();
    }

}
