package mc.gouv.af.back.cache;

import mc.gouv.dem.shared.model.TemplateDTO;
import mc.gouv.xboot.caching.GouvCache;

/**
 * 
 * Implémentation de l'interface UsagersCache
 * 
 * @author qdeme
 *
 */
public interface TemplatesCache extends GouvCache<Integer, TemplateDTO> {
    
    public TemplateDTO getTemplate(String codeTemplate, String langue);
    
}
