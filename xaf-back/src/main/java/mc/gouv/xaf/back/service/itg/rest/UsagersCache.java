package mc.gouv.xaf.back.service.itg.rest;

import mc.gouv.xaf.shared.dto.GichuniUsagerDTO;
import mc.gouv.xaf.caching.GouvCache;

/**
 * Cache des usagers
 *
 * @author qdeme
 */
public interface UsagersCache extends GouvCache<Integer, GichuniUsagerDTO> {

}
