package mc.gouv.xaf.back.service.impl;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.DynamicJSService;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import mc.gouv.xaf.shared.enums.DemandeCanalEnum;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public abstract class AbstractDynamicJSServiceImpl implements DynamicJSService {

    protected static final String DEBUT = "\n(\"";
    protected static final String FIN = "\";\n}\n";
    protected static final String RETURN = "return \"";
    protected static final String RETURN_INCONNU = "return \"INCONNU\";\n}\n";

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    protected boolean ifElse(StringBuilder builder, boolean first) {
        if (first) {
            builder.append("\nif ");
        } else {
            builder.append("\nelse if ");
        }
        return false;
    }

    protected boolean appendStatuts(StringBuilder builder, boolean first, String name, String libelle) {
        boolean result = ifElse(builder, first);
        builder.append(DEBUT).append(name).append("\" === statutPublicOuInterne) {\n");
        StatutPublicOuInterneDTO statutPublicOuInterne = new StatutPublicOuInterneDTO();
        statutPublicOuInterne.setName(name);
        statutPublicOuInterne.setLibelle(libelle);
        builder.append(RETURN).append(demarchesDataProvider.getStatusColorClass(statutPublicOuInterne));
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

}
