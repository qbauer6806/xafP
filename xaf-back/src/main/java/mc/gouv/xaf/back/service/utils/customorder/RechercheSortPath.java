package mc.gouv.xaf.back.service.utils.customorder;

import java.util.List;
import java.util.stream.Stream;

/**
 * Lors du tri d'un tableau datatable, possibilité de trier sur un ou plusieurs autres champs lors lorsque la valeur du
 * champ par défaut est null (voir par exemple Absenex)
 *
 * @param defaultPath
 * @param alternativePaths
 */
public record RechercheSortPath(String defaultPath, List<String> alternativePaths) {

    public List<String> getAllPaths() {
        return Stream.concat(Stream.of(defaultPath), alternativePaths.stream()).toList();
    }

}
