package mc.gouv.xaf.back.service.motifs;

import mc.gouv.xaf.shared.dto.MotifDTO;
import mc.gouv.xaf.caching.GouvCache;

import java.util.List;

/**
 * Implémentation de l'interface MotifsCache
 *
 * @author qdeme
 */
public interface MotifsCache extends GouvCache<Integer, MotifDTO> {

    MotifDTO getMotif(String codeMotif, String langue);

    MotifDTO getMotif(String codeMotif, String langue, String statut);

    List<MotifDTO> getMotifs(String langue);

    List<MotifDTO> getMotifs(String langue, String statut);

    List<MotifDTO> getFilteredMotifs(String langue, List<String> codes);

}
