package mc.gouv.xaf.rio.utils;

import mc.gouv.xaf.shared.dto.TacheDTO;
import org.apache.commons.lang3.StringUtils;

import java.util.function.Predicate;

/**
 * Classe utilitaire pour l'archivage des fichiers d'une demande
 */
public class ArchivageUtils {

    public static final String ARCHIVAGE_RIO_COMPLETED = "ARCHIVAGE_RIO_COMPLETED";
    public static final String NOMBRE_FICHIERS_ERREUR_ARCHIVAGE = "NOMBRE_FICHIERS_ERREUR_ARCHIVAGE";
    public static final String CODE_TYPE_PERMIS = "PERMIS";
    public static final String CODE_TYPE_IMMAT = "IMMAT";

    private ArchivageUtils() {
        //DO NOTHING
    }

    /**
     * Permets de trouver une tâche à partir d'une référence
     *
     * @param ref référence de la tâche à chercher
     * @return
     */
    public static Predicate<TacheDTO> filtrerTache(String ref) {
        return tacheDTO -> {
            if (CODE_TYPE_PERMIS.equals(tacheDTO.getCodeType())) {
                return StringUtils.equals(ref, tacheDTO.getContenu().at("/numPermis").asText());
            } else if (CODE_TYPE_IMMAT.equals(tacheDTO.getCodeType())) {
                return StringUtils.equals(ref, tacheDTO.getContenu().at("/numRegistre").asText());
            }
            return false;
        };
    }
}
