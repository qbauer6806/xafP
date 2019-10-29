package mc.gouv.xaf.back.service.es.impl;

import java.util.Map;

import javax.transaction.Transactional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.service.data.impl.DemandesDataServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.DemandeDataDTO;

@Primary
@Service
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackOn = Exception.class)
public class IndexedEsDemandesDataServiceImpl extends DemandesDataServiceImpl {

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    @Override
    public DemandeDataDTO saveOrUpdateDemandeData(String demarcheId, Integer demandeId, String key, String value)
            throws Exception {
        DemandeDataDTO demandeDataDto = super.saveOrUpdateDemandeData(demarcheId, demandeId, key, value);
        indexDemandeData(demarcheId, demandeId);
        return demandeDataDto;

    }

    @Override
    public void saveOrUpdateDemandeDatas(String demarcheId, Integer demandeId, Map<String, String> datas)
            throws Exception {
        super.saveOrUpdateDemandeDatas(demarcheId, demandeId, datas);
        indexDemandeData(demarcheId, demandeId);

    }

    @Override
    public void deleteDemandeData(String demarcheId, Integer demandeId, String key) throws Exception {
        super.deleteDemandeData(demarcheId, demandeId, key);
        indexDemandeData(demarcheId, demandeId);

    }

    private void indexDemandeData(String demarcheId, Integer demandeId) throws Exception {
        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, demandeId);
        indexedDemandeService.sendToTopic(demandeDTO, false);
    }
}
