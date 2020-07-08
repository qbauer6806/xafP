package mc.gouv.xaf.back.service.motifs.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import mc.gouv.xaf.back.service.motifs.MotifsCache;
import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xboot.caching.GouvMemoryCache;

/**
 * 
 * Implémentation de l'interface MotifsCache
 * 
 * @author qdeme
 *
 */
@Profile("gouv")
@Component
public class MotifsCacheImpl extends GouvMemoryCache<Integer, MotifDTO> implements MotifsCache {
    
    // 3 heures
    private static final long CACHE_DURATION = 3*60*60*1000;

    public MotifsCacheImpl(MotifsCacheDataProvider gouvCacheDataProvider) {
        super(gouvCacheDataProvider, CACHE_DURATION);
    }

    public MotifDTO getMotif(String codeMotif, String langue) {
        for (MotifDTO motif : getValues()) {
            if (motif.getCode().equals(codeMotif) && motif.getLangue().equals(langue)) {
                return motif;
            }
        }
        return null;
    }
    
    public List<MotifDTO> getMotifs(String langue) {
        List<MotifDTO> motifs = new ArrayList<MotifDTO>();
        for (MotifDTO motif : getValues()) {
            if (motif.getLangue().equals(langue) && motif.getDateArchive() == null) {
                motifs.add(motif);
            }
        }
        return motifs;
    }
    
    public List<MotifDTO> getMotifs(String langue, String statut) {
        List<MotifDTO> ret = new ArrayList<MotifDTO>();
        for (MotifDTO motif : getValues()) {
            if (motif.getLangue().equals(langue) && motif.getStatut().equals(statut) && motif.getDateArchive() == null) {
                ret.add(motif);
            }
        }
        return ret;
    }

    @Override
    public List<MotifDTO> getFilteredMotifs(String langue, List<String> codes) {
        List<MotifDTO> filteredMotifs = new ArrayList<>();
        for (MotifDTO motif : getMotifs(langue)) {
            if (codes.contains(motif.getCode())) {
                filteredMotifs.add(motif);
            }
        }
        return filteredMotifs;
    }
}
