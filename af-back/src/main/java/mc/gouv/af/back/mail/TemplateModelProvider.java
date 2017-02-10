package mc.gouv.af.back.mail;

import java.util.Map;

import org.activiti.engine.delegate.DelegateExecution;

/**
 * 
 * Interface permettant à la démarche de spécifier à af-back son modèle pour le templating
 *  
 * @author qdeme
 *
 */
public interface TemplateModelProvider {
    
    public Map<String,Object> getModel(DelegateExecution execution);

}
