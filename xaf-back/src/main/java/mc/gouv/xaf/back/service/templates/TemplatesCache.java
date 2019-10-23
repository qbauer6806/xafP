package mc.gouv.xaf.back.service.templates;

import mc.gouv.xaf.back.shared.dto.TemplateDTO;
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
