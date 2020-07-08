package mc.gouv.xaf.back.service.es.impl;

import mc.gouv.xaf.back.data.transformer.DemandesTransformer;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.impl.DemandeFilesServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.shared.dto.DemandeFileDTO;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandeFilesServiceImpl extends DemandeFilesServiceImpl {

    @Autowired
    private IndexedDemandeService indexedDemandeService;

    @Override
    public void saveFiles(DemandeFileDTO[] demandeFiles, DemandeBO demandeBo) throws Exception {

        super.saveFiles(demandeFiles, demandeBo);
        DemandeDTO demandeDto = DemandesTransformer.bo2Dto(demandeBo);
        indexedDemandeService.sendToTopic(demandeFiles, demandeDto);
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception {
        super.saveFile(demandeFile, demarcheId, pkDemande);
        DemandeBO demandeBo = indexedDemandeService.getDemandeBo(demarcheId, pkDemande);
        DemandeDTO demandeDto = DemandesTransformer.bo2Dto(demandeBo);
        indexedDemandeService.sendToTopic(demandeFile, demandeDto);

    }

}
