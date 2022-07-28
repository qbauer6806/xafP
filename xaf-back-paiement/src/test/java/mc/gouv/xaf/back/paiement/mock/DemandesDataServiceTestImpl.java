package mc.gouv.xaf.back.paiement.mock;

import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.DemandesDataService;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class DemandesDataServiceTestImpl implements DemandesDataService {
    @Override
    public DemandeDataDTO getDemandeData(String demarcheId, Integer demandeId, String key) {
        return null;
    }

    @Override
    public List<DemandeDataDTO> getDemandeDatas(String demarcheId, Integer demandeId) {
        return null;
    }

    @Override
    public List<DemandeDataDTO> getDemandeDatasByKeyAndValue(String key, String value) {
        return null;
    }

    @Override
    public List<DemandeDataDTO> getDemandeDatasByKeyAndValueAndfkDemandes(String key, String value, List<DemandeBO> demandes) {
        return null;
    }

    @Override
    public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value) {
        return null;
    }

    @Override
    public DemandeDataDTO updateDemandeData(DemandeDataDTO dataDTO) {
        return null;
    }

    @Override
    public void deleteDemandeData(String demarcheId, Integer demandeId, String key) {

    }

    @Override
    public void saveOrUpdateDemandeDatas(String demarcheId, Integer demandeId, Map<String, String> datas) {

    }
}
