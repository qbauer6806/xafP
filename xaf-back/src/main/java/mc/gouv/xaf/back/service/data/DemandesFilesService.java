package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.Map;

/**
 * Service permettant la manipulation des fichiers joints aux demandes.
 *
 * @author qdeme
 */
public interface DemandesFilesService {

	void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) throws Exception;

	void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception;

	boolean updateTypedocs(Map<String, String> changes);

    void deleteAllOrphans();

}
