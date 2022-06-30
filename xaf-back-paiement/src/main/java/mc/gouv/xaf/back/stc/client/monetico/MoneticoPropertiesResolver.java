package mc.gouv.xaf.back.stc.client.monetico;

import mc.gouv.Static;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class MoneticoPropertiesResolver {

    @Autowired
    GouvPropertiesResolver gouvPropertiesResolver;

    public String getVersion() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.version");
    }

    public String getServiceUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.serviceUrl");
    }

    public String getTpe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.tpe");
    }


    public String key() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.key");
    }

    public String getCompanyCode() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.companyCode");
    }


    public String getCurrency() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.currency");
    }


}
