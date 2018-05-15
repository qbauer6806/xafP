package mc.gouv.af.back.cache;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.af.back.properties.GouvPropertiesResolver;
import mc.gouv.dem.service.MotifsService;
import mc.gouv.dem.shared.model.MotifDTO;
import mc.gouv.xboot.caching.GouvCacheDataProvider;

@Profile("gouv")
@Component
public class MotifsCacheDataProvider implements GouvCacheDataProvider<Integer, MotifDTO> {
    
    @Autowired
    private MotifsService motifsService;
    
    @Autowired
    private GouvPropertiesResolver gouvPropertiesResolver;

    @Override
    public ConcurrentHashMap<Integer, MotifDTO> getAll() {
        ConcurrentHashMap<Integer, MotifDTO> ret = new ConcurrentHashMap<Integer, MotifDTO>();
        List<MotifDTO> motifs = motifsService.getMotifs(gouvPropertiesResolver.getDemarcheId());
        for (MotifDTO motif : motifs) {
            ret.put(motif.getPkMotifs(), motif);
        }
        return ret;
    }

    @Override
    public MotifDTO get(Integer key) {
        return motifsService.getMotif(gouvPropertiesResolver.getDemarcheId(), key);
    }
    
}