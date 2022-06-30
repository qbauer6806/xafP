package mc.gouv.xaf.back.stc.client.cir;

import mc.gouv.Static;
import mc.gouv.xaf.back.properties.GouvPropertiesResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CirPropertiesResolver {

    GouvPropertiesResolver gouvPropertiesResolver;

    public CirPropertiesResolver(GouvPropertiesResolver gouvPropertiesResolver) {
        this.gouvPropertiesResolver = gouvPropertiesResolver;
    }

    public String getUrl() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.cir.serviceUrl");
    }

    public String getToken() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.cir.token");
    }

    public int getRegistre() {
        return Integer.parseInt(Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.cir.registre"));
    }

    public String getCodeTarif() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.cir.codeTarif");
    }

    public String getTpe() {
        return Static.getValue("mc.gouv" + gouvPropertiesResolver.getApplicationName() + ".backserver.monetico.tpe");
    }
}
