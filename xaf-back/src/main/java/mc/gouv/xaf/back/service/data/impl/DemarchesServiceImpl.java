package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemarchesRepository;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.back.data.transformer.DemarchesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemarcheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

/**
 * Service permettant la manipulation des démarches.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemarchesServiceImpl implements DemarchesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemarchesServiceImpl.class);

    @Autowired
    private DemarchesRepository demarchesRepository;

    @Override
    public DemarcheDTO getDemarche(String demarcheId) {
        DemarchesBO demarcheBo = getCheckDemarche(demarcheId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemarchesTransformer.bo2Dto(demarcheBo);
    }

    @Override
    public DemarcheDTO updateDemarche(DemarcheDTO demarche) {
        LOGGER.info("Mise à jour de la démarche...");
        DemarchesBO demarcheBo = DemarchesTransformer.dto2Bo(demarche);
        demarcheBo = demarchesRepository.save(demarcheBo);

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemarchesTransformer.bo2Dto(demarcheBo);
    }

    @Override
    public DemarchesBO getCheckDemarche(String demarcheId) {
        LOGGER.info("Récupération en base de la démarche...");
        Optional<DemarchesBO> demarcheBoOp = demarchesRepository.findById(demarcheId);
        if (!demarcheBoOp.isPresent()) {
            throw new DemarchesServiceException("La démarche spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }
        return demarcheBoOp.get();
    }

}
