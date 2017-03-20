package mc.gouv.af.back.util;

import java.util.List;

import mc.gouv.logon.shared.User;

public interface UtilisateursCache {

    List<User> getAll();

    User getUtilisateur(String matricule);

    void clearCache();

    void updateUtilisateur(User u);

}
