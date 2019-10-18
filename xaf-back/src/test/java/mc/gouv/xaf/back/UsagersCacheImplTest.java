package mc.gouv.xaf.back;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.cache.UsagersCache;
import mc.gouv.servicerest.usager.model.UsagerBean;

@Component
@Profile("test")

public class UsagersCacheImplTest implements UsagersCache {

    @Override
    public Map<Integer, UsagerBean> getAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UsagerBean get(Integer key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public UsagerBean get(Integer key, boolean forceUpdate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void add(Integer key, UsagerBean value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<UsagerBean> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Integer> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

}
