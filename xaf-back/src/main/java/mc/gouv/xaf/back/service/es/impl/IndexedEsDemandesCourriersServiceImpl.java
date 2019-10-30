package mc.gouv.xaf.back.service.es.impl;

import javax.inject.Inject;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.service.data.impl.DemandesCourriersServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedEsDemandesCourriersServiceImpl extends DemandesCourriersServiceImpl {

    @Inject
    IndexedDemandeService indexedDemandeService;

    @Override
    public DemandeCourrierDTO saveCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {

        DemandeCourrierDTO demandeCourrierDTO = super.saveCourrier(demarcheId, pkDemande, courrierDto);
        indexCourrier(demarcheId, pkDemande);
        return demandeCourrierDTO;
    }

    @Override
    public DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {
        DemandeCourrierDTO demandeCourrierDTO = super.updateCourrier(demarcheId, pkDemande, courrierDto);
        indexCourrier(demarcheId, pkDemande);
        return demandeCourrierDTO;
    }

    private void indexCourrier(String demarcheId, Integer pkDemande) throws Exception {
        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, pkDemande);

        indexedDemandeService.sendToTopic(demandeDTO, false);
    }
}
