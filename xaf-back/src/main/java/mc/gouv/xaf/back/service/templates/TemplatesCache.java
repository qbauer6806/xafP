package mc.gouv.xaf.back.service.templates;

import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.caching.GouvCache;

/**
 * Interface pour le cache des templates d'e-mail
 *
 * @author qdeme
 */
public interface TemplatesCache extends GouvCache<Integer, TemplateDTO> {

    TemplateDTO getTemplate(String codeTemplate, String langue);

}
