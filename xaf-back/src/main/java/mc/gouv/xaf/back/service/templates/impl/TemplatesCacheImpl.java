package mc.gouv.xaf.back.service.templates.impl;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.templates.TemplatesCache;
import mc.gouv.xaf.shared.dto.TemplateDTO;
import mc.gouv.xaf.caching.GouvMemoryCache;

/**
 * Composant permettant de gérer un cache des templates de la démarche courante
 * 
 * @author qdeme
 *
 */
@Profile("gouv")
@Component
public class TemplatesCacheImpl extends GouvMemoryCache<Integer, TemplateDTO> implements TemplatesCache {

    // 3 heures
    private static final long CACHE_DURATION = 3*60*60*1000L;

    public TemplatesCacheImpl(TemplatesCacheDataProvider gouvCacheDataProvider) {
        super(gouvCacheDataProvider, CACHE_DURATION);
    }

    @Override
    public TemplateDTO getTemplate(String codeTemplate, String langue) {
        for (TemplateDTO template : getValues()) {
            if (template.getCode().equals(codeTemplate) && template.getLangue().equals(langue)) {
                return template;
            }
        }
        return null;
    }

}
