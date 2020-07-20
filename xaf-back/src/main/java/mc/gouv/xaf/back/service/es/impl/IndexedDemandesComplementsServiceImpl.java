package mc.gouv.xaf.back.service.es.impl;

import javax.inject.Inject;

import org.springframework.context.annotation.Conditional;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.config.es.IndexationEnabledCondition;
import mc.gouv.xaf.back.service.data.impl.DemandesComplementsServiceImpl;
import mc.gouv.xaf.back.service.es.IndexedDemandeService;
import mc.gouv.xaf.shared.dto.DemandeComplementsDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsQuestionDTO;
import mc.gouv.xaf.shared.dto.DemandeComplementsReponseDTO;
import mc.gouv.xaf.shared.dto.DemandeDTO;

@Service
@Primary
@Conditional(IndexationEnabledCondition.class)
@Transactional(rollbackFor = Exception.class)
public class IndexedDemandesComplementsServiceImpl extends DemandesComplementsServiceImpl {

    @Inject
    IndexedDemandeService indexedDemandeService;

    @Override
    @Transactional
    public DemandeComplementsDTO saveDemandeComplements(String demarcheId, Integer demandeId,
            DemandeComplementsQuestionDTO demandeComplements) throws Exception {

        DemandeComplementsDTO DemandeComplementsDTO = super.saveDemandeComplements(demarcheId, demandeId,
                demandeComplements);
        indexedDemandeService.indexDemande(demarcheId, demandeId);
        return DemandeComplementsDTO;

    }

    @Override
    public DemandeComplementsDTO repondreDemandeComplements(String demarcheId, Integer pkDemande,
            Integer pkDemandeComplements, DemandeComplementsReponseDTO demandeComplementsReponse) throws Exception {

        DemandeComplementsDTO demandeComplementsDTO = super.repondreDemandeComplements(demarcheId, pkDemande,
                pkDemandeComplements, demandeComplementsReponse);

        DemandeDTO demandeDTO = indexedDemandeService.getDemande(demarcheId, pkDemande);

        indexedDemandeService.indexElement(demandeDTO, true);

        return demandeComplementsDTO;

    }

}
