package mc.gouv.xaf.back.service.motifs.impl;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.caching.GouvCacheDataProvider;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MotifsCacheDataProvider implements GouvCacheDataProvider<Integer, MotifDTO> {

    private final MotifsService motifsService;

    @Override
    public ConcurrentHashMap<Integer, MotifDTO> getAll() {
        ConcurrentHashMap<Integer, MotifDTO> ret = new ConcurrentHashMap<>();
        List<MotifDTO> motifs = motifsService.getMotifs();
        for (MotifDTO motif : motifs) {
            ret.put(motif.getPkMotifs(), motif);
        }
        return ret;
    }

    @Override
    public MotifDTO get(Integer key) {
        return motifsService.getMotif(key);
    }

}
