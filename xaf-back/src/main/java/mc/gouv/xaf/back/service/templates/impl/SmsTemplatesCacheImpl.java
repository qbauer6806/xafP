package mc.gouv.xaf.back.service.templates.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.templates.SmsTemplatesCache;
import mc.gouv.xaf.caching.GouvMemoryCache;
import mc.gouv.xaf.shared.dto.SmsTemplateDTO;

/**
 * Composant permettant de gérer un cache des templates SMS de la démarche courante
 *
 * @author qdeme
 */
@Profile("gouv")
@Component
public class SmsTemplatesCacheImpl extends GouvMemoryCache<Integer, SmsTemplateDTO> implements SmsTemplatesCache {

    // 3 heures
    private static final long CACHE_DURATION = 3 * 60 * 60 * 1000L;

    public SmsTemplatesCacheImpl(SmsTemplatesCacheDataProvider gouvCacheDataProvider) {
        super(gouvCacheDataProvider, CACHE_DURATION);
    }

    @Override
    public SmsTemplateDTO getTemplate(String codeTemplate, String langue) {
        for (SmsTemplateDTO template : getValues()) {
            if (template.getCode().equals(codeTemplate) && template.getLangue().equals(langue)) {
                return template;
            }
        }
        return null;
    }

}
