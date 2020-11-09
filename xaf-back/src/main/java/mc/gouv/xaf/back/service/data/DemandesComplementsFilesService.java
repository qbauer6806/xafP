package mc.gouv.xaf.back.service.data;

import java.util.Map;

/**
 * Service permettant la manipulation des fichiers joints aux  d'informations complémentaires.
 *
 * @author mboutelier.ext
 */
public interface DemandesComplementsFilesService {

	void updateTypedocs(Map<String, String> changes);
}
