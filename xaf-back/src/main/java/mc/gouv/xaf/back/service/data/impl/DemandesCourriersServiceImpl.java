package mc.gouv.xaf.back.service.data.impl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.SAXException;

import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;

/**
 * Service permettant la manipulation des courriers liés à une demande.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class DemandesCourriersServiceImpl implements DemandesCourriersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCourriersServiceImpl.class);

    @Autowired
    private DemandesCourriersRepository demandesCourriersRepository;

    @Autowired
    private DemandesRepository demandesRepository;

    @Autowired
    private DemandesService demandesService;

    @Autowired
    private DemarchesService demarchesService;

    /**
     * {@inheritDoc}
     * @throws SAXException 
     * @throws IOException 
     */
    @Override
    public DemandeCourrierDTO saveCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {

        LOGGER.info("Récupération de la demande associée...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande associée introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Constitution du nouveau courrier et sauvegarde en base...");

        DemandesCourriersBO bo = DemandesCourriersTransformer.dto2Bo(courrierDto);
        bo.setFkDemandes(demandeBo);
        // Statut associé au courrier : le dernier statut de la demande
        bo.setFkDemandesStatuts(demandeBo.getDernierStatut());
        bo.setDateCreation(new Date());

        DemandesCourriersBO retourBo = demandesCourriersRepository.save(bo);
        updateDemandeCourrier(demandeBo, bo);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesCourriersTransformer.bo2Dto(retourBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeCourrierDTO getCourrier(String demarcheId, Integer pkDemande, Integer pkCourrier) {

        DemandesCourriersBO courrierBo = getCourrierBo(demarcheId, pkDemande, pkCourrier);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesCourriersTransformer.bo2Dto(courrierBo);

    }

    private DemandesCourriersBO getCourrierBo(String demarcheId, Integer pkDemande, Integer pkCourrier) {

        LOGGER.info("Récupération de la demande associée...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande associée introuvable", HttpStatus.NOT_FOUND);
        }

        Optional<DemandesCourriersBO> courrierBoOp = demandesCourriersRepository.findById(pkCourrier);

        if (!courrierBoOp.isPresent()) {
            throw new DemarchesServiceException("Courrier introuvable", HttpStatus.NOT_FOUND);
        }

        return courrierBoOp.get();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeCourrierDTO> getCourriers(String demarcheId, Integer pkDemande) {

        LOGGER.info("Récupération de la demande associée...");

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);

        if (demandeBo == null) {
            throw new DemarchesServiceException("Demande associée introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesCourriersTransformer.bo2Dto(new ArrayList<DemandesCourriersBO>(demandeBo.getCourriers()));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<DemandeCourrierDTO> getCourriersPourDemarche(String demarcheId) {

        LOGGER.info("Récupération de la démarche associée...");

        demarchesService.getCheckDemarche(demarcheId);

        List<DemandesCourriersBO> courriers = demandesCourriersRepository
                .findByFkDemandesFkAccessDemarcheId(demarcheId);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesCourriersTransformer.bo2Dto(courriers);

    }

    private void updateDemandeCourrier(DemandeBO demandeBo, DemandesCourriersBO courrierBo) {

        if (demandeBo.getCourriers() != null) {
            demandeBo.getCourriers().add(courrierBo);
        } else {
            Set<DemandesCourriersBO> courriers = new HashSet<>();
            courriers.add(courrierBo);
            demandeBo.setCourriers(courriers);
        }

        demandesRepository.save(demandeBo);
    }

    /**
     * {@inheritDoc}
     * @throws Exception 
     */
    @Override
    public DemandeCourrierDTO updateCourrier(String demarcheId, Integer pkDemande, DemandeCourrierDTO courrierDto)
            throws Exception {

        DemandesCourriersBO courrierBo = getCourrierBo(demarcheId, pkDemande, courrierDto.getPkCourrier());

        LOGGER.info("Mise à jour du courrier...");

        courrierBo.setName(courrierDto.getName());
        courrierBo.setUrl(courrierDto.getUrl());
        courrierBo.setMeta(courrierDto.getMeta());
        courrierBo.setIdentifiant(courrierDto.getIdentifiant());
        courrierBo.setDatePrinted(courrierDto.getDatePrinted());
        courrierBo = demandesCourriersRepository.save(courrierBo);

        DemandeBO demandeBo = demandesService.getCheckDemarcheDemandeBO(demarcheId, pkDemande, true);
        updateDemandeCourrier(demandeBo, courrierBo);

        LOGGER.info("Transformation bo -> dto ...");
        return DemandesCourriersTransformer.bo2Dto(courrierBo);

    }

}
