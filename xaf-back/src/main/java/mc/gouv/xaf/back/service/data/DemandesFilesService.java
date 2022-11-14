package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

import java.util.List;
import java.util.Map;

/**
 * Service permettant la manipulation des fichiers joints aux demandes.
 *
 * @author qdeme
 */
public interface DemandesFilesService {

	void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo);

	void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande);

	void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande, boolean checkActive);

	boolean updateTypedocs(Map<String, String> changes, Map<String, Boolean> checkboxes);

	List<DemandeFileDTO> getFileByDemandeIdAndMeta(Integer pkDemande, String meta);

}
