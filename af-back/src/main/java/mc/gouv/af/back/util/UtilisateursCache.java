package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.logon.model.User;

public interface UtilisateursCache {

    List<User> getAll();

    User getUtilisateur(String matricule);

    void clearCache();

}
