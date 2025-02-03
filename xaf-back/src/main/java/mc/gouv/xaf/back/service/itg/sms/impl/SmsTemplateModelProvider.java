package mc.gouv.xaf.back.service.itg.sms.impl;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Interface permettant à la démarche de spécifier à xaf-back son modèle pour le templating
 *
 * @author qdeme
 */
public interface SmsTemplateModelProvider {

    void setModel(Map<String, Object> model, String bodyTemplateCode, Map<String, Object> bpmVariables);

    Entry<String, String> getSmsTemplateCodesForAction(String action);
}
