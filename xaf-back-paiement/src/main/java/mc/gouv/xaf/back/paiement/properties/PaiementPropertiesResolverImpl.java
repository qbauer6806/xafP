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

    private static final String XAF_MONETICO_TPE = "XAF_MONETICO_TPE";
    private static final String XAF_MONETICO_DUREE_CARTE = "XAF_MONETICO_DUREE_CARTE";
    private static final String XAF_MONETICO_CLE_SCEAU = "XAF_MONETICO_CLE_SCEAU";
    private static final String XAF_MONETICO_VERSION_ALLER = "XAF_MONETICO_VERSION_ALLER";
    private static final String XAF_MONETICO_VERSION_CAPTURE = "XAF_MONETICO_VERSION_CAPTURE";

    private static final String XAF_MONETICO_TEXTE_ALLER = "XAF_MONETICO_TEXTE_ALLER";

    private static final String XAF_MONETICO_LIBELLE_SOCIETE = "XAF_MONETICO_LIBELLE_SOCIETE";
    private static final String XAF_MONETICO_LIBELLE_LIEU = "XAF_MONETICO_LIBELLE_LIEU";

    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE = "XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_VILLE = "XAF_MONETICO_CONTEXTE_COMMANDE_VILLE";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL = "XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL";
    private static final String XAF_MONETICO_CONTEXTE_COMMANDE_PAYS = "XAF_MONETICO_CONTEXTE_COMMANDE_PAYS";
    private static final String XAF_MONETICO_CODE_SITE_STANDARD = "XAF_MONETICO_CODE_SITE_STANDARD";
    private static final String XAF_MONETICO_CODE_SITE_IFRAME = "XAF_MONETICO_CODE_SITE_IFRAME";


    private static final String XAF_MONETICO_ALLER_URL = "XAF_MONETICO_ALLER_URL";
    private static final String XAF_MONETICO_RETOUR_URL = "XAF_MONETICO_RETOUR_URL";
    private static final String XAF_MONETICO_PAGE_MENU_URL = "XAF_MONETICO_PAGE_MENU_URL";
    private static final String XAF_MONETICO_CAPTURE_URL = "XAF_MONETICO_CAPTURE_URL";
    private static final String XAF_PAGE_SUCCES_URL = "XAF_PAGE_SUCCES_URL";
    private static final String XAF_PAGE_ECHEC_URL = "XAF_PAGE_ECHEC_URL";

    private static final String XAF_ADRESSES_MAIL_ADMIN_METIER = "XAF_ADRESSES_MAIL_ADMIN_METIER";
    private static final String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR";
    private static final String XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RIO = "XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RIO";

    private static final String XAF_RETRY_INITIAL_DELAY = "XAF_RETRY_INITIAL_DELAY";
    private static final String XAF_RETRY_COUNT = "XAF_RETRY_COUNT";
    private static final String XAF_RETRY_MULTIPLIER = "XAF_RETRY_MULTIPLIER";


    private static final String XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE = "XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE";

    private static final String XAF_CIR_PERMIS = "XAF_CIR_PERMIS";
    private static final String XAF_CIR_REGISTRE = "XAF_CIR_REGISTRE";
    private static final String XAF_CIR_IMMAT = "XAF_CIR_IMMAT";


    public String getTpe() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_TPE);
        return propertiesDTO.getValue();
    }

    public String getPaiementClef() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CLE_SCEAU);
        return propertiesDTO.getValue();
    }

    public String getCodeSiteStandard() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CODE_SITE_STANDARD);
        return propertiesDTO.getValue();
    }

    public String getXafMoneticoCodeSiteIframe() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CODE_SITE_IFRAME);
        return propertiesDTO.getValue();
    }

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
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_CIR_REGISTRE);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public int getPermisParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_CIR_PERMIS);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public int getImmatParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_CIR_IMMAT);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    public String getVersionAller() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_VERSION_ALLER);
        return propertiesDTO.getValue();
    }

    public String getVersionCapture() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_VERSION_CAPTURE);
        return propertiesDTO.getValue();
    }

    public String getAllerUrl() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_ALLER_URL);
        return propertiesDTO.getValue();
    }

    public String getRetourUrl() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_RETOUR_URL);
        return propertiesDTO.getValue();
    }

    public String getMenuUrl() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_PAGE_MENU_URL);
        return propertiesDTO.getValue();
    }

    public String getCaptureUrl() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_MONETICO_CAPTURE_URL);
        return propertiesDTO.getValue();
    }

    public String getSuccesUrl() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PAGE_SUCCES_URL);
        return propertiesDTO.getValue();
    }


    public String getEchecUrl() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PAGE_ECHEC_URL);
        return propertiesDTO.getValue();
    }

    public String getPaiementKey() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.key");
    }


    public String getCurrency() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.currency");
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


    public String getAdressesMailAdminMetier() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_ADMIN_METIER);
        return propertiesDTO.getValue();
    }

    public String getAdressesMailSupportTechniqueCir() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_CIR);
        return propertiesDTO.getValue();
    }

    public String getAdressesMailSupportTechniqueRio() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_ADRESSES_MAIL_SUPPORT_TECHNIQUE_RIO);
        return propertiesDTO.getValue();
    }

    public String getXafRetryInitialDelay() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_INITIAL_DELAY);
        return propertiesDTO.getValue();
    }

    public String getXafRetryCount() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_COUNT);
        return propertiesDTO.getValue();
    }

    public String getXafRetryMultiplier() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_RETRY_MULTIPLIER);
        return propertiesDTO.getValue();
    }

    public String getXafPaiementImmediatHeureDiffere() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(gouvPropertiesResolver.getDemarcheId(), XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE);
        return propertiesDTO.getValue();
    }

}
