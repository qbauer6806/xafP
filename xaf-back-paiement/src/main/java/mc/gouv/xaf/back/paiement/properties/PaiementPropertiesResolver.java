package mc.gouv.xaf.back.paiement.properties;

public interface PaiementPropertiesResolver {

    String getTpe();

    String getPaiementClef();

    String getCodeSiteStandard();

    String getXafMoneticoCodeSiteIframe();

    String getXafMoneticoTexteAller();

    int getValiditeMaxMoyenPaiement();

    String getFactureUrl();

    String getFactureToken();

    int getRegistre();

    int getPermisParDefaut();

    int getImmatParDefaut();

    String getVersionAller();

    String getVersionCapture();

    String getAllerUrl();

    String getRetourUrl();

    String getMenuUrl();

    String getCaptureUrl();

    String getSuccesUrl();


    String getEchecUrl();

    String getPaiementKey();


    String getCurrency();

    String getAdresseParDefaut();

    String getVilleParDefaut();

    String getCodePostalParDefaut();

    String getCodePaysParDefaut();


    String getAdressesMailAdminMetier();

    String getAdressesMailSupportTechniqueCir();

    String getAdressesMailSupportTechniqueRio();


    int getXafRetryInitialDelay();

    int getXafRetryCount();

    int getXafRetryMultiplier();

    String getXafPaiementImmediatHeureDiffere();

    String getXafMonetico3dsv2Scenario();
}
