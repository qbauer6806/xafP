package mc.gouv.af.back.mail;

import java.util.Map;

import mc.gouv.dem.shared.model.DemandeDTO;

/**
 * 
 * Interface permettant à la démarche de spécifier à af-back son modèle pour le templating
 *  
 * @author qdeme
 *
 */
public interface TemplateModelProvider {
    
    public Map<String,Object> getModel(String subjectTemplateCode, String bodyTemplateCode, DemandeDTO demande, Map<String, Object> bpmVariables, String codeMotif, String commentaire);

}
