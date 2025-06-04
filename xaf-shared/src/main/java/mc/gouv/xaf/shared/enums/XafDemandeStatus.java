package mc.gouv.xaf.shared.enums;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public interface XafDemandeStatus {

    String getLibelle();

    StatutSimplifieEnum getStatutSimplifie();

    boolean isStatutPublic();

    static <T extends Enum<T> & XafDemandeStatus> Map<String, String> getPrivateStatuts(Class<T> statutClass) {
        return Arrays.stream(statutClass.getEnumConstants()).filter(statut -> !statut.isStatutPublic())
                .collect(Collectors.toMap(Enum::name, XafDemandeStatus::getLibelle));
    }

    static <T extends Enum<T> & XafDemandeStatus> Map<String, String> getMap(Class<T> statutClass) {
        Map<String, String> statuts = new LinkedHashMap<>();
        for (T statut : statutClass.getEnumConstants()) {
            statuts.put(statut.name(), statut.getLibelle());
        }
        return statuts;
    }

    static <T extends Enum<T> & XafDemandeStatus> List<String> getStatutsAPurger(Class<T> statutClass) {
        return Arrays.stream(statutClass.getEnumConstants())
                .filter(statut -> statut.getStatutSimplifie().equals(StatutSimplifieEnum.TERMINEE)).map(Enum::name)
                .toList();
    }


}
