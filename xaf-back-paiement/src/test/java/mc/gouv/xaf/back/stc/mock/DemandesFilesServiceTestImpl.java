package mc.gouv.xaf.back.stc.mock;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesFilesService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;
import org.springframework.stereotype.Component;

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
    public boolean updateTypedocs(Map<String, String> changes) {
        return false;
    }
}
