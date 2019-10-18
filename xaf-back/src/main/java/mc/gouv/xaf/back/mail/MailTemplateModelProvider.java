package mc.gouv.xaf.back.mail;

import java.util.Map;
import java.util.Map.Entry;

import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Interface permettant à la démarche de spécifier à xaf-back son modèle pour le templating
 *  
 * @author qdeme
 *
 */
public interface MailTemplateModelProvider {
    
    public Entry<String, String> getMailTemplateCodesForAction(String action);
    
    public Map<String,Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande, Map<String, Object> bpmVariables, String codeMotif, String commentaire);

}
