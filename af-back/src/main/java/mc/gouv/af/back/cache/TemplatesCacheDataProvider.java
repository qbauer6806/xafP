package mc.gouv.af.back.cache;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.TemplatesService;
import mc.gouv.dem.shared.model.TemplateDTO;
import mc.gouv.xboot.caching.GouvCacheDataProvider;

@Profile("gouv")
@Component
public class TemplatesCacheDataProvider implements GouvCacheDataProvider<Integer, TemplateDTO> {
    
    @Autowired
    private TemplatesService templatesService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public ConcurrentHashMap<Integer, TemplateDTO> getAll() {
        ConcurrentHashMap<Integer, TemplateDTO> ret = new ConcurrentHashMap<Integer, TemplateDTO>();
        List<TemplateDTO> templates = templatesService.getTemplates(gouvPropertiesResolver.getDemarcheId());
        for (TemplateDTO template : templates) {
            ret.put(template.getPkTemplates(), template);
        }
        return ret;
    }

    @Override
    public TemplateDTO get(Integer key) {
        return templatesService.getTemplate(gouvPropertiesResolver.getDemarcheId(), key);
    }
    
}
