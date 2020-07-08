package mc.gouv.xaf.back.service.data.impl;

import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemarchesRepository;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.back.data.transformer.DemarchesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.shared.dto.DemarcheDTO;

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

        LOGGER.info("Récupération en base de la démarche...");

        DemarchesBO demarcheBo = getCheckDemarche(demarcheId);

        LOGGER.info("Transformation bo -> dto ...");
        
        DemarcheDTO demarcheDto = DemarchesTransformer.bo2Dto(demarcheBo);

        return demarcheDto;

    }

    @Override
    public DemarcheDTO updateDemarche(DemarcheDTO demarche) {

        LOGGER.info("Récupération en base de la démarche...");

        DemarchesBO demarcheBo = getCheckDemarche(demarche.getPkDemarches());

        LOGGER.info("Mise à jour de la démarche...");

        demarcheBo.setEmailService(demarche.getEmailService());
        demarcheBo = demarchesRepository.save(demarcheBo);

        LOGGER.info("Transformation bo -> dto ...");

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
