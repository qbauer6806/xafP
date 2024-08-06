package mc.gouv.xaf.back.service.itg.logon;

import mc.gouv.xaf.back.service.itg.logon.dto.User;
import mc.gouv.xaf.caching.GouvCache;

public interface UtilisateursCache extends GouvCache<String, User> {

}
