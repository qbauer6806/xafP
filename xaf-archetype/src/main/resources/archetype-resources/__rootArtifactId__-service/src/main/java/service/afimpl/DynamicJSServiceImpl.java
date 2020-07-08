#set( $symbol_pound = '#' )
#set( $symbol_dollar = '$' )
#set( $symbol_escape = '\' )
package ${groupId}.service.afimpl;

import mc.gouv.xaf.back.service.DemarchesDataProvider;
import mc.gouv.xaf.back.service.DynamicJSService;
import mc.gouv.xaf.shared.dto.DemandeCanalEnum;
import mc.gouv.xaf.shared.dto.StatutPublicOuInterneDTO;
import ${groupId}.shared.enums.${artifactIdCamelCase}DemandeStatutEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DynamicJSServiceImpl implements DynamicJSService {

    @Autowired
    private DemarchesDataProvider demarchesDataProvider;

    String js = null;

    @Override
    public String getResponse() {
        if (js == null) {
            js = "APP.getStatusColorClass = function(statutPublicOuInterne) {${symbol_escape}n";
            boolean first = true;
            for (${artifactIdCamelCase}DemandeStatutEnum status : ${artifactIdCamelCase}DemandeStatutEnum.values()) {
                if (first) {
                    js += "${symbol_escape}nif ";
                    first = false;
                } else {
                    js += "${symbol_escape}nelse if ";
                }
                js += "${symbol_escape}n(${symbol_escape}"" + status.name() + "${symbol_escape}" === statutPublicOuInterne) {${symbol_escape}n";
                StatutPublicOuInterneDTO statutPublicOuInterne = new StatutPublicOuInterneDTO();
                statutPublicOuInterne.setName(status.name());
                statutPublicOuInterne.setLibelle(status.libelle);
                js += "return ${symbol_escape}"" + demarchesDataProvider.getStatusColorClass(statutPublicOuInterne) + "${symbol_escape}";${symbol_escape}n";
                js += "}${symbol_escape}n";
            }
            js += "return ${symbol_escape}"default-status-color${symbol_escape}";${symbol_escape}n";
            js += "}${symbol_escape}n";

            js += "APP.getTraductionStatut = function(statut) {${symbol_escape}n";
            first = true;
            for (${artifactIdCamelCase}DemandeStatutEnum status : ${artifactIdCamelCase}DemandeStatutEnum.values()) {
                if (first) {
                    js += "${symbol_escape}nif ";
                    first = false;
                } else {
                    js += "${symbol_escape}nelse if ";
                }
                js += "${symbol_escape}n(${symbol_escape}"" + status.name() + "${symbol_escape}" === statut) {${symbol_escape}n";
                js += "return ${symbol_escape}"" + status.libelle + "${symbol_escape}";${symbol_escape}n";
                js += "}${symbol_escape}n";
            }
            js += "return ${symbol_escape}"INCONNU${symbol_escape}";${symbol_escape}n";
            js += "}${symbol_escape}n";

            js += "APP.getTraductionCanal = function(canal) {${symbol_escape}n";
            first = true;
            for (DemandeCanalEnum canal : DemandeCanalEnum.values()) {
                if (first) {
                    js += "${symbol_escape}nif ";
                    first = false;
                } else {
                    js += "${symbol_escape}nelse if ";
                }
                js += "${symbol_escape}n(${symbol_escape}"" + canal.name() + "${symbol_escape}" === canal) {${symbol_escape}n";
                js += "return ${symbol_escape}"" + canal.libelle + "${symbol_escape}";${symbol_escape}n";
                js += "}${symbol_escape}n";
            }
            js += "return ${symbol_escape}"INCONNU${symbol_escape}";${symbol_escape}n";
            js += "}${symbol_escape}n";

            return js;
        }
        return js;
    }

}
