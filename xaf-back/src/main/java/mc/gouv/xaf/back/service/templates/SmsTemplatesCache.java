package mc.gouv.xaf.back.service.templates;

import mc.gouv.xaf.caching.GouvCache;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;

/**
 * Interface pour le cache des templates de SMS
 *
 * @author qdeme
 */
public interface SmsTemplatesCache extends GouvCache<Integer, SmsTemplateDTO> {

	SmsTemplateDTO getTemplate(String codeTemplate, String langue);

}
