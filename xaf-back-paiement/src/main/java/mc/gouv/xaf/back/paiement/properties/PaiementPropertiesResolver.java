package mc.gouv.xaf.back.paiement.properties;

public interface PaiementPropertiesResolver {

    int getValiditeMaxMoyenPaiement();

    String getTpe();

    String getPaiementClef();

    String getCodeSiteStandard();

    String getXafMoneticoCodeSiteIframe();

    String getXafMoneticoTexteAller();

    String getFactureUrl();

    String getFactureToken();

    int getRegistre();

    String getVersionAller();

    String getVersionCapture();

    String getCaptureUrl();

    String getSuccesUrl();

    String getEchecUrl();

    String getCurrency();

    String getNomParDefaut();

    String getPrenomParDefaut();

    String getAdresseParDefaut();

    String getVilleParDefaut();

    String getCodePostalParDefaut();

    String getCodePaysParDefaut();

    int getXafRetryInitialDelay();

    int getXafRetryCount();

    int getXafRetryMultiplier();

    String getXafMonetico3dsv2Scenario();

    String getXafPaiementImmediatHeureDiffere();

    String getCodePaiement();
    
    String getXafMoneticoLibelleSociete();
    
    String getXafMoneticoLibelleLieu();

}
