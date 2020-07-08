package mc.gouv.xaf.back.service.data;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

/**
 * 
 * Service permettant la manipulation des fichiers joints aux demandes.
 * 
 * @author qdeme
 *
 */
public interface DemandesFilesService {

    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) throws Exception;

    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception;

}
