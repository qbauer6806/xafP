package mc.gouv.xaf.back.service.itg.mail;

import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Interface permettant à la démarche de spécifier à xaf-back son modèle pour le templating
 *
 * @author qdeme
 */
public interface MailTemplateModelProvider {

    default void setModel(Map<String, Object> model, String bodyTemplateCode, Map<String, Object> bpmVariables) {
    }
    default void setModelDesinscriptionUsager(Integer usagerId, Map<String, Object> model, List<DemandeDTO> demandes) {
    }

    Entry<String, String> getMailTemplateCodesForAction(String action);
}
