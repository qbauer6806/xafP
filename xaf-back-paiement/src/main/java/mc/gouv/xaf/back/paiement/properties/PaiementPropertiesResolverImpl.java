package mc.gouv.xaf.back.paiement.properties;

import mc.gouv.Static;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class PaiementPropertiesResolverImpl implements PaiementPropertiesResolver{

    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    public String getFactureUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.serviceUrl");
    }

    public String getFactureToken() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.token");
    }

    public int getRegistre() {
        return Integer.parseInt(Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.registre"));
    }

    public String getCodeTarif() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".cir.codeTarif");
    }

    public String getTpe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.tpe");
    }

    public String getVersion() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.version");
    }

    public String getPaiementUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.serviceUrl");
    }

    public String getPaiementKey() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.key");
    }

    public String getCompanyCode() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.companyCode");
    }


    public String getCurrency() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationPrefix() + ".monetico.currency");
    }
}
