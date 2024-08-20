package mc.gouv.xaf.xaf12batch.logon;

import mc.gouv.xaf.xaf12batch.logon.dto.User;
import mc.gouv.xboot.caching.GouvCache;

public interface UtilisateursCache extends GouvCache<String, User> {

}
