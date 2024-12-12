package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.DynamicJSService;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class AbstractDynamicJSServiceImpl implements DynamicJSService {

    protected static final String DEBUT = "\n(\"";
    protected static final String FIN = "\";\n}\n";
    protected static final String RETURN = "return \"";
    protected static final String RETURN_INCONNU = "return \"INCONNU\";\n}\n";

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    @Autowired
    private AfBackUtils afBackUtils;

    String js = null;

    @Override
    public String getResponse() {
        StringBuilder builder = new StringBuilder();
        if (js == null) {
            statusColorClass(builder);
            traductionCanal(builder);
            js = builder.toString();
        }
        return js;
    }

    private boolean ifElse(StringBuilder builder, boolean first) {
        if (first) {
            builder.append("\nif ");
        } else {
            builder.append("\nelse if ");
        }
        return false;
    }

    private boolean appendStatuts(StringBuilder builder, boolean first, String name) {
        boolean result = ifElse(builder, first);
        builder.append(DEBUT).append(name).append("\" === statutName) {\n");
        builder.append(RETURN).append(afBackUtils.getStatusColorClass(name));
        builder.append(FIN);
        return result;
    }

    protected void traductionCanal(StringBuilder builder) {
        builder.append("APP.getTraductionCanal = function(canal) {\n");
        boolean first = true;
        for (DemandeCanalEnum canal : DemandeCanalEnum.values()) {
            first = ifElse(builder, first);
            builder.append(DEBUT).append(canal.name()).append("\" === canal) {\n");
            builder.append(RETURN).append(canal).append(FIN);
        }
        builder.append(RETURN_INCONNU);
    }

    protected void statusColorClass(StringBuilder builder) {
        builder.append("APP.getStatusColorClass = function(statutName) {\n");
        boolean first = true;
        for (Map.Entry<String, String> status : demarchesDataProvider.getStatusMap().entrySet()) {
            first = appendStatuts(builder, first, status.getKey());
        }
        builder.append("return \"default-status-color\";\n}\n");
    }

}
