package mc.gouv.xaf.back.service.impl;

import static mc.gouv.xaf.back.service.js.DynamicJSBuilderUtils.DEBUT;
import static mc.gouv.xaf.back.service.js.DynamicJSBuilderUtils.FIN;
import static mc.gouv.xaf.back.service.js.DynamicJSBuilderUtils.RETURN;
import static mc.gouv.xaf.back.service.js.DynamicJSBuilderUtils.RETURN_INCONNU;

import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.DynamicJSService;
import mc.gouv.xaf.back.service.js.CustomDynamicJSProvider;
import mc.gouv.xaf.back.service.js.DynamicJSBuilderUtils;
import mc.gouv.xaf.back.service.utils.AfBackUtils;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DynamicJSServiceImpl implements DynamicJSService {

    private final DemarchesDataProvider demarchesDataProvider;
    private final AfBackUtils afBackUtils;
    private final Optional<CustomDynamicJSProvider> customDynamicJSProvider;

    protected String js = null;

    @Override
    public String getResponse() {
        if (js == null) {
            StringBuilder builder = new StringBuilder();
            statusColorClass(builder);
            traductionCanal(builder);
            customDynamicJSProvider.ifPresent(provider -> provider.appendCustomJs(builder));
            js = builder.toString();
        }
        return js;
    }

    private boolean appendStatuts(StringBuilder builder, boolean first, String name) {
        boolean result = DynamicJSBuilderUtils.ifElse(builder, first);
        builder.append(DEBUT).append(name).append("\" === statutName) {\n");
        builder.append(RETURN).append(afBackUtils.getStatusColorClass(name));
        builder.append(FIN);
        return result;
    }

    protected void traductionCanal(StringBuilder builder) {
        builder.append("APP.getTraductionCanal = function(canal) {\n");
        boolean first = true;
        for (DemandeCanalEnum canal : DemandeCanalEnum.values()) {
            first = DynamicJSBuilderUtils.ifElse(builder, first);
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
