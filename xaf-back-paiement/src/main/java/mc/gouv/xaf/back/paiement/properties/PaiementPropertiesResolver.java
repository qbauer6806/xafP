package mc.gouv.xaf.back.paiement.properties;

public interface PaiementPropertiesResolver {

    String getFactureUrl();

    String getFactureToken();

    int getRegistre();

    String getCodeTarif();

    String getTpe();

    String getVersion();

    String getPaiementUrl();

    String getPaiementKey();

    String getCompanyCode();

    String getCurrency();
}
