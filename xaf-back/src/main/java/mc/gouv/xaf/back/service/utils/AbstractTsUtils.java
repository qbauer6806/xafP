package mc.gouv.xaf.back.service.utils;

import java.lang.reflect.Field;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Classe utilitaire à extends pour les classes TSNAMEUtils
 *
 * @author uek
 *
 */

public abstract class AbstractTsUtils {

    /**
     * Retourne le dernier buildId
     */
    public String getLastBuildId() {
        // Récupère les champs de la classe TSNAMEUtils
        Field[] fields = this.getClass().getDeclaredFields();
        SortedSet<String> buildIds = new TreeSet<>();
        for (Field field : fields) {
            // Récupère le nom du champs en enlevant le V (exemple V1678700504013)
            String buildId = field.getName().substring(1);
            // Vérifie qu'il y a bien 13 chiffres
            if (buildId.matches("\\d{13}")) {
                buildIds.add(buildId);
            }
        }
        return buildIds.last();
    }

}
