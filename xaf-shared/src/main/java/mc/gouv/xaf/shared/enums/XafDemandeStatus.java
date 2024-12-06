package mc.gouv.xaf.shared.enums;

import java.util.HashMap;
import java.util.Map;

public interface XafDemandeStatus {

    String getCouleur();

    String getLibelle();

    boolean isStatutPublic();

    static <T extends Enum<T> & XafDemandeStatus> Map<String, String> getPrivateStatuts(Class<T> statutClass) {
        Map<String, String> privateStatuts = new HashMap<>();
        for (T statut : statutClass.getEnumConstants()) {
            if (!statut.isStatutPublic()) {
                privateStatuts.put(statut.name(), statut.getLibelle());
            }
        }
        return privateStatuts;
    }

}
