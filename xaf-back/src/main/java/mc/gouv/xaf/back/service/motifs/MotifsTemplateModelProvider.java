package mc.gouv.xaf.back.service.motifs;

import java.util.Map;
import mc.gouv.xaf.shared.dto.DemandeDTO;

public interface MotifsTemplateModelProvider {

    default void setModel(Map<String, Object> model, DemandeDTO demandeDTO) {
    }
}
