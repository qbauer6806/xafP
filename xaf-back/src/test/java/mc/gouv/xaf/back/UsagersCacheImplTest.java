package mc.gouv.xaf.back;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;

@Component
@Profile("test")

public class UsagersCacheImplTest implements UsagersCache {

    @Override
    public Map<Integer, GichuniUsagerDTO> getAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GichuniUsagerDTO get(Integer key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public GichuniUsagerDTO get(Integer key, boolean forceUpdate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void add(Integer key, GichuniUsagerDTO value) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public Collection<GichuniUsagerDTO> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<Integer> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

}
