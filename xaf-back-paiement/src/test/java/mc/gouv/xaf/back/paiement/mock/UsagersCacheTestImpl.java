package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.service.itg.rest.UsagersCache;
import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Map;

@Component
public class UsagersCacheTestImpl implements UsagersCache {
    @Override
    public Map<Integer, GichuniUsagerDTO> getAll() {
        return null;
    }

    @Override
    public GichuniUsagerDTO get(Integer key) {
        GichuniUsagerDTO gichuniUsagerDTO = new GichuniUsagerDTO();
        gichuniUsagerDTO.setEmail("edouard.germain@gmail.com");
        gichuniUsagerDTO.setNom("germain");
        gichuniUsagerDTO.setPrenom("edouard");
        return gichuniUsagerDTO;
    }

    @Override
    public GichuniUsagerDTO get(Integer key, boolean forceUpdate) {
        return null;
    }

    @Override
    public void refresh() {

    }

    @Override
    public void add(Integer key, GichuniUsagerDTO value) {

    }

    @Override
    public Collection<GichuniUsagerDTO> getValues() {
        return null;
    }

    @Override
    public Collection<Integer> getKeys() {
        return null;
    }
}
