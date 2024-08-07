package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DemandesFilesServiceTestImpl implements DemandesFilesService {
    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) {

    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) {

    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande, boolean checkActive) {

    }

    @Override
    public boolean updateTypedocs(Map<String, String> changes, Map<String, Boolean> checkboxes) {
        return false;
    }

    @Override
    public List<DemandeFileDTO> getFileByDemandeIdAndTypedoc(Integer pkDemande, String typedoc) {
        return null;
    }

    @Override
    public List<DemandeFileDTO> getFileByDemandeIdAndMeta(Integer pkDemande, String meta) {
        return null;
    }

    @Override
    public void clonerDesPiecesJointes(DemandeBO demandeBO, DemandeBO newDemandeBO) {

    }

    @Override
    public void updateFichiers(DemandeBO demandeBo, DemandeFileDTO[] fichiers) {

    }

    @Override
    public void suppressionDesFichiers(DemandeDTO demandeDTO, boolean statutCheck, List<String> statuts, int jours) {

    }

    /**
     *
     */
    @Override
    public void deleteAllOrphans() {

    }

    @Override
    public int updateContenuFiles() {
        return 0;
    }
}
