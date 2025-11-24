package mc.gouv.xaf.back.service.demande;

import mc.gouv.xaf.shared.dto.DemandeDTO;
import java.util.Map;

@FunctionalInterface
public interface CreateDemandeBpmnVariablesProvider {

    Map<String, String> getVariablesBpmn(final DemandeDTO demandeDTO);
}
