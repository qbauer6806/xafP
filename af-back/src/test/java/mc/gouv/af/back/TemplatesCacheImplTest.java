package mc.gouv.af.back;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.cache.TemplatesCache;
import mc.gouv.dem.shared.model.TemplateDTO;

@Component
@Profile("test")
public class TemplatesCacheImplTest implements TemplatesCache {

    @Override
    public Map<Integer, TemplateDTO> getAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TemplateDTO get(Integer key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TemplateDTO get(Integer key, boolean forceUpdate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void add(Integer key, TemplateDTO value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<TemplateDTO> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Integer> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public TemplateDTO getTemplate(String codeTemplate, String langue) {
        // TODO Auto-generated method stub
        return null;
    }

}
