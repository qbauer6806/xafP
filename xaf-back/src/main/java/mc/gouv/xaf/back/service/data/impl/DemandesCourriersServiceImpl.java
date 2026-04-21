package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import mc.gouv.xaf.back.data.dao.DemandesCourriersRepository;
import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.DemandesCourriersBO;
import mc.gouv.xaf.back.data.model.ErrorEventDTO;
import mc.gouv.xaf.back.data.transformer.DemandesCourriersTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemandesCourriersService;
import mc.gouv.xaf.back.service.data.DemandesService;
import mc.gouv.xaf.back.service.handlers.TransactionErrorsHandler;
import mc.gouv.xaf.back.service.utils.RechercheCourriersUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeCourrierDTO;
import mc.gouv.xaf.shared.dto.DemandeCourrierRechercheDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des courriers liés à une demande.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
@RequiredArgsConstructor
public class DemandesCourriersServiceImpl implements DemandesCourriersService {

    private static final Logger LOGGER = LoggerFactory.getLogger(DemandesCourriersServiceImpl.class);

    private final DemandesCourriersRepository demandesCourriersRepository;
    private final DemandesRepository demandesRepository;
    private final RechercheCourriersUtils rechercheCourriersUtils;
    private final TransactionErrorsHandler transactionErrorsHandler;
    private final ApplicationEventPublisher applicationEventPublisher;
    private final DemandesHelperService demandesHelperService;

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeCourrierDTO saveCourrier(Integer pkDemande, DemandeCourrierDTO courrierDto) {
        try {
            DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, true);
            if (demandeBo == null) {
                throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
            }

            LOGGER.info("Constitution du nouveau courrier et sauvegarde en base...");

            DemandesCourriersBO bo = DemandesCourriersTransformer.dto2Bo(courrierDto);
            bo.setFkDemandes(demandeBo);
            // Statut associé au courrier : le dernier statut de la demande
            bo.setFkDemandesStatuts(demandeBo.getDernierStatut());
            bo.setDateCreation(new Date());

            DemandesCourriersBO retourBo = demandesCourriersRepository.save(bo);
            updateDemandeCourrier(demandeBo, bo);

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            return DemandesCourriersTransformer.bo2Dto(retourBo);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de la sauvegarde courrier");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesCourriersServiceImpl - méthode saveCourrier()", pkDemande, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DemandeCourrierDTO getCourrier(Integer pkDemande, Integer pkCourrier) {
        DemandesCourriersBO courrierBo = getCourrierBo(pkDemande, pkCourrier);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return DemandesCourriersTransformer.bo2Dto(courrierBo);
    }

    private DemandesCourriersBO getCourrierBo(Integer pkDemande, Integer pkCourrier) {

        DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, true);
        if (demandeBo == null) {
            throw new DemarchesServiceException(SharedMessages.DEMANDE_ASSOCIEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        Optional<DemandesCourriersBO> courrierBoOp = demandesCourriersRepository.findById(pkCourrier);
        if (courrierBoOp.isEmpty()) {
            throw new DemarchesServiceException("Courrier introuvable", HttpStatus.NOT_FOUND);
        }

        return courrierBoOp.get();
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
     */
    @Override
    public DemandeCourrierDTO updateCourrier(Integer pkDemande, DemandeCourrierDTO courrierDto) {
        try {
            DemandesCourriersBO courrierBo = getCourrierBo(pkDemande, courrierDto.getPkCourrier());

            LOGGER.info("Mise à jour du courrier...");

            courrierBo.setName(courrierDto.getName());
            courrierBo.setUrl(courrierDto.getUrl());
            courrierBo.setMeta(courrierDto.getMeta());
            courrierBo.setIdentifiant(courrierDto.getIdentifiant());
            courrierBo.setDatePrinted(courrierDto.getDatePrinted());
            courrierBo = demandesCourriersRepository.save(courrierBo);

            DemandeBO demandeBo = demandesHelperService.getCheckDemarcheDemandeBO(pkDemande, true);
            updateDemandeCourrier(demandeBo, courrierBo);

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            return DemandesCourriersTransformer.bo2Dto(courrierBo);
        } catch (Exception e) {
            LOGGER.error("Erreur lors de l'update courrier");
            ErrorEventDTO esErrorEventDTO = transactionErrorsHandler.createErrorEvent(
                    "DemandesCourriersServiceImpl - méthode updateCourrier()", pkDemande, e);
            applicationEventPublisher.publishEvent(esErrorEventDTO);
            throw new DemarchesServiceException(e.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    public Page<DemandeCourrierDTO> getDemandesCourriers(DemandeCourrierRechercheDTO demandeRecherche,
            Pageable pageable, String[] fields) {
        //count query
        Long totalCount = rechercheCourriersUtils.getCourriersCount(demandeRecherche);

        List<DemandesCourriersBO> demandes = rechercheCourriersUtils.getCourriers(demandeRecherche, pageable);

        List<DemandeCourrierDTO> demandesDto = DemandesCourriersTransformer.bo2Dto(demandes);

        return new PageImpl<>(demandesDto, pageable, totalCount);
    }

}
