package mc.gouv.xaf.back.service.es.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.service.data.impl.DemandeFilesServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.back.shared.dto.DemandeFileDTO;

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
        indexedDemandeService.sendToTopic(demandeFiles, demandeBo.getFkAccess().getDemarcheId(),
                demandeBo.getIdentifiant());
    }

    @Override
    public void saveFile(DemandeFileDTO demandeFile, String demarcheId, Integer pkDemande) throws Exception {
        super.saveFile(demandeFile, demarcheId, pkDemande);
        DemandeBO demandeBo = indexedDemandeService.getDemandeBo(demarcheId, pkDemande);
        indexedDemandeService.sendToTopic(demandeFile, demarcheId, demandeBo.getIdentifiant());

    }

}
