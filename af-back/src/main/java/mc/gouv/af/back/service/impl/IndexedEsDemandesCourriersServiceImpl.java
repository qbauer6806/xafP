package mc.gouv.af.back.service.impl;

import javax.inject.Inject;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.af.back.config.es.IndexationEnabledCondition;
import mc.gouv.af.back.service.IndexedDemandeService;
import mc.gouv.dem.service.impl.DemandesCourriersServiceImpl;
import mc.gouv.dem.shared.model.DemandeCourrierDTO;
import mc.gouv.dem.shared.model.DemandeDTO;

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
        indexCourrier(demarcheId, pkDemande, demandeCourrierDTO);
        return demandeCourrierDTO;
    }

    @Override
    public DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {
        DemandeCourrierDTO demandeCourrierDTO = super.updateCourrier(demarcheId, pkDemande, courrierDto);
        indexCourrier(demarcheId, pkDemande, demandeCourrierDTO);
        return demandeCourrierDTO;
    }

    private void indexCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto) throws Exception {
        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, pkDemande);

        indexedDemandeService.sendToTopic(demandeDTO);
    }
}
