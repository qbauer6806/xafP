package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
public class DemandesFilesServiceTestImpl implements DemandesFilesService {

    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) {

    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, Integer pkDemande) {

    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, Integer pkDemande, boolean checkActive) {

    }

    @Override
    public boolean updateTypedocs(Map<String, String> changes, Map<String, Boolean> checkboxes) {
        return false;
    }

    @Override
    public List<DemandeFileDTO> getFileByDemandeIdAndTypedoc(Integer pkDemande, String typedoc) {
        return List.of();
    }

    @Override
    public List<DemandeFileDTO> getFileByDemandeIdAndMeta(Integer pkDemande, String meta) {
        return List.of();
    }

    @Override
    public void clonerDesPiecesJointes(DemandeBO demandeBO, DemandeBO newDemandeBO) {

    }

    @Override
    public void clonerDesFichiersInternes(DemandeBO demandeBO, DemandeBO newDemandeBO) {

    }

    @Override
    public void updateFichiers(DemandeBO demandeBo, DemandeFileDTO[] fichiers) {

    }

    @Override
    public void suppressionDesFichiers(DemandeDTO demandeDTO) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Optional<DemandeFileDTO> getFileByDemandeFileId(Integer pkDemandesFiles) {
        return Optional.empty();
    }

    @Override
    public void deleteFileByFileUrlAndId(String fileName, Integer fileId) {

    }


}
