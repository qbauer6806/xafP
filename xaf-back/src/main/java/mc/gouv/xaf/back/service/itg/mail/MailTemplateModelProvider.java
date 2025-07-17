package mc.gouv.xaf.back.service.itg.mail;

import java.util.List;
import java.util.Map;
import mc.gouv.xaf.shared.dto.DemandeDTO;

/**
 * Interface permettant à la démarche de spécifier à xaf-back son modèle pour le templating
 *
 * @author qdeme
 */
public interface MailTemplateModelProvider {

    default void setModel(Map<String, Object> model, String bodyTemplateCode, Map<String, Object> bpmVariables, DemandeDTO demandeDTO) {
    }
    default void setModelDesinscriptionUsager(Integer usagerId, Map<String, Object> model, List<DemandeDTO> demandes) {
    }

    String getMailTemplateCodeForAction(String action);
}
