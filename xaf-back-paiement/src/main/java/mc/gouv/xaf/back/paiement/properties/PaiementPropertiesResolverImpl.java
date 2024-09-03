package mc.gouv.xaf.back.paiement.properties;

import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import mc.gouv.xaf.back.service.data.PropertiesService;
import mc.gouv.xaf.shared.dto.PropertiesDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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
    private static final String XAF_MONETICO_LIBELLE_SOCIETE = "XAF_MONETICO_LIBELLE_SOCIETE";
    private static final String XAF_MONETICO_LIBELLE_LIEU = "XAF_MONETICO_LIBELLE_LIEU";

    private static final String XAF_CODE_PAIEMENT = "XAF_CODE_PAIEMENT";
    private static final String XAF_RETRY_INITIAL_DELAY = "XAF_RETRY_INITIAL_DELAY";
    private static final String XAF_RETRY_COUNT = "XAF_RETRY_COUNT";
    private static final String XAF_RETRY_MULTIPLIER = "XAF_RETRY_MULTIPLIER";

    private static final String XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE = "XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE";
    private static final String PAIEMENT_IMMEDIAT_HEURE_DIFFERE_DEFAULT = "23:58:00";

    @Value("mc.gouv.cir.api.url")
    private String cirServiceUrl;

    @Value("mc.gouv.${application.name}.shared.backapi.cir.api.jwt")
    private String cirServiceToken;

    @Value("mc.gouv.${application.name}.shared.backapi.cir.registre:0")
    private String cirRegistre;

    @Value("mc.gouv.${application.name}.shared.backapi.cir.permis:0")
    private String cirPermis;

    @Value("mc.gouv.${application.name}.shared.backapi.cir.immat:")
    private String cirImmat;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.currency")
    private String moneticoCurrency;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.tpe")
    private String moneticoTpe;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.clesceau")
    private String moneticoCleSceau;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.versionaller")
    private String moneticoVersionAller;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.versioncapture")
    private String moneticoVersionCapture;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.codesitestandard")
    private String moneticoCodeSiteStandard;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.codesiteiframe")
    private String moneticoCodeSiteIframe;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.captureurl")
    private String moneticoCaptureUrl;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.successurl")
    private String moneticoSuccessUrl;

    @Value("mc.gouv.${application.name}.shared.backapi.monetico.echecurl")
    private String moneticoEchecUrl;


    @Override
    public String getXafMoneticoTexteAller() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_TEXTE_ALLER);
        return propertiesDTO.getValue();
    }

    @Override
    public int getValiditeMaxMoyenPaiement() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_DUREE_CARTE);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public String getFactureUrl() {
        return cirServiceUrl;
    }

    @Override
    public String getFactureToken() {
        return cirServiceToken;
    }

    /**
     * Permet de récupérer la valeur par défaut pour les numéros de registres.
     * <br>
     * On force une valeur par défaut à 0 si la propriété n'existe pas ou est vide.
     */
    @Override
    public int getRegistre() {
        return Integer.parseInt(cirRegistre);
    }

    /**
     * Permet de récupérer la valeur par défaut pour les numéros de permis.
     * <br>
     * On force une valeur par défaut à 0 si la propriété n'existe pas ou est vide.
     */
    @Override
    public int getPermis() {
        return Integer.parseInt(cirPermis);
    }

    /**
     * Permet de récupérer la valeur par défaut pour les immatriculations.
     * <br>
     * On force une chaîne vide par défaut si la propriété n'est pas présente ou null.
     */
    @Override
    public String getImmat() {
        return cirImmat;
    }

    @Override
    public String getNomParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_CONTEXTE_COMMANDE_NAME);
        return propertiesDTO.getValue();
    }

    @Override
    public String getPrenomParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_CONTEXTE_COMMANDE_FIRSTNAME);
        return propertiesDTO.getValue();
    }

    @Override
    public String getAdresseParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_CONTEXTE_COMMANDE_ADRESSE);
        return propertiesDTO.getValue();
    }

    @Override
    public String getVilleParDefaut() {

        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_CONTEXTE_COMMANDE_VILLE);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodePostalParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_CONTEXTE_COMMANDE_CODE_POSTAL);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodePaysParDefaut() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_CONTEXTE_COMMANDE_PAYS);
        return propertiesDTO.getValue();
    }

    @Override
    public int getXafRetryInitialDelay() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_RETRY_INITIAL_DELAY);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public int getXafRetryCount() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_RETRY_COUNT);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    @Override
    public int getXafRetryMultiplier() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_RETRY_MULTIPLIER);
        return Integer.parseInt(propertiesDTO.getValue());
    }

    /**
     * <p>Permet de récupérer la valeur de la propriété XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE</p>
     * <p>Si cette propriété n'est pas ajoutée dans la BDD, on utilise une valeur par défault</p>
     *
     * @return une chaine contenant l'heure d'arrêt du paiement immédiat
     */
    @Override
    public String getXafPaiementImmediatHeureDiffere() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_PAIEMENT_IMMEDIAT_HEURE_DIFFERE);
        return (propertiesDTO != null) ? propertiesDTO.getValue() : PAIEMENT_IMMEDIAT_HEURE_DIFFERE_DEFAULT;
    }

    @Override
    public String getCurrency() {
        return moneticoCurrency;
    }

    @Override
    public String getTpe() {
        return moneticoTpe;
    }

    @Override
    public String getPaiementClef() {
        return moneticoCleSceau;
    }

    @Override
    public String getVersionAller() {
        return moneticoVersionAller;
    }

    @Override
    public String getVersionCapture() {
        return moneticoVersionCapture;
    }

    @Override
    public String getCodeSiteStandard() {
        return moneticoCodeSiteStandard;
    }

    @Override
    public String getXafMoneticoCodeSiteIframe() {
        return moneticoCodeSiteIframe;
    }

    @Override
    public String getCaptureUrl() {
        return moneticoCaptureUrl;
    }

    @Override
    public String getSuccesUrl() {
        return moneticoSuccessUrl;
    }

    @Override
    public String getEchecUrl() {
        return moneticoEchecUrl;
    }

    @Override
    public String getXafMonetico3dsv2Scenario() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_3DSV2_SCENARIO);
        return propertiesDTO.getValue();
    }

    @Override
    public String getCodePaiement() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_CODE_PAIEMENT);
        return propertiesDTO.getValue();
    }

    @Override
    public String getXafMoneticoLibelleSociete() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_LIBELLE_SOCIETE);
        return propertiesDTO.getValue();
    }

    @Override
    public String getXafMoneticoLibelleLieu() {
        PropertiesDTO propertiesDTO = propertiesService.getProperty(XAF_MONETICO_LIBELLE_LIEU);
        return propertiesDTO.getValue();
    }

}
