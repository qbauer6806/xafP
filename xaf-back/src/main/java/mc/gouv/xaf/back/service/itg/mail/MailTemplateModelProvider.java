package mc.gouv.xaf.back.service.itg.mail;

import mc.gouv.xaf.shared.dto.DemandeDTO;

import java.util.Map;
import java.util.Map.Entry;

/**
 * Interface permettant à la démarche de spécifier à xaf-back son modèle pour le templating
 *
 * @author qdeme
 */
public interface MailTemplateModelProvider {

    Entry<String, String> getMailTemplateCodesForAction(String action, Integer pkDemande);

    Map<String, Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande,
            Map<String, Object> bpmVariables, String codeMotif, String commentaire);

    Map<String, Object> getGenericModelDemande(DemandeDTO demande, String codeMotif, String commentaire,
            Map<String, Object> bpmVariables);

    Map<String, Object> getGenericModelDemande(DemandeDTO demande);

    Map<String, Object> getGenericModel();

}
