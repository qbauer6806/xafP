package mc.gouv.xaf.back.service.itg.mail;

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

    Entry<String, String> getMailTemplateCodesForAction(String action);
}
