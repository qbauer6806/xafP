package mc.gouv.xaf.back;

import java.util.Collection;
import java.util.Map;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.itg.nomen.PaysCache;
import mc.gouv.xaf.shared.dto.PaysDTO;

@Component
@Profile("test")
public class PaysCacheImplMock implements PaysCache {

    @Override
    public Map<String, PaysDTO> getAll() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PaysDTO get(String key) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public PaysDTO get(String key, boolean forceUpdate) {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public void refresh() {
        // TODO Auto-generated method stub

    }

    @Override
    public void add(String key, PaysDTO value) {
        // TODO Auto-generated method stub

    }

    @Override
    public Collection<PaysDTO> getValues() {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Collection<String> getKeys() {
        // TODO Auto-generated method stub
        return null;
    }

}
