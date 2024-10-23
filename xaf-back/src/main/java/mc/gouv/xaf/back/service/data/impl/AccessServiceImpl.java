package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.AccessRepository;
import mc.gouv.xaf.back.data.dao.UsagersCourrierRepository;
import mc.gouv.xaf.back.data.entity.AccessBO;
import mc.gouv.xaf.back.data.entity.UsagersCourrierBO;
import mc.gouv.xaf.back.data.transformer.AccessTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.AccessService;
import mc.gouv.xaf.back.service.itg.gichuni.kafka.GUKafkaProducer;
import mc.gouv.xaf.back.service.utils.DemarchesUtils;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.AccessDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des accès.
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class AccessServiceImpl implements AccessService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AccessServiceImpl.class);

    @Autowired
    private AccessRepository accessRepository;

    @Autowired
    private UsagersCourrierRepository usagerCourrierRepository;

    @Autowired
    private GUKafkaProducer guKafkaProducer;

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessDTO saveOrUpdateAccess(Integer usagerId, AccessDTO access) {

        LOGGER.info("Vérification de l'unicité...");
        AccessDTO dto = AccessTransformer.bo2Dto(getAccessBO(usagerId, true));

        if (dto != null) {
            // Accès déjà existant, le mettre à jour
            dto.setContenu(access.getContenu());
            dto.setDateDerModif(new Date());

            LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);

            AccessBO bo = AccessTransformer.dto2Bo(dto);

            bo.setActive(true); // existait déjà, donc true
            bo = accessRepository.save(bo);

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

            dto = AccessTransformer.bo2Dto(bo);
            dto.setUpdated(true);

            return dto;
        }
        // Accès non existant, le créer

        boolean isUsagerCourrier = DemarchesUtils.isUsagerCourrier(access.getUsagerId());
        LOGGER.info("Usager courrier : {}", isUsagerCourrier);

        if (isUsagerCourrier) {
            // Vérifier que l'usagerId existe dans la table USAGERS_COURRIER s'il s'agit d'un usager courrier
            // Vérification faite à la main, pas de FK en base car cela serait devenu techniquement très compliqué à
            // maintenir
            // étant donné qu'une seule colonne peut correspondre à un usager dans Login ou dans DEM
            Optional<UsagersCourrierBO> usagerCourrierOp = usagerCourrierRepository.findById(access.getUsagerId());
            if (usagerCourrierOp.isEmpty()) {
                throw new DemarchesServiceException("L'usager courrier spécifié est introuvable", HttpStatus.NOT_FOUND);
            }
        }

        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);

        access.setDateCreation(new Date());
        access.setDateDerModif(access.getDateCreation());

        AccessBO bo = AccessTransformer.dto2Bo(access);

        // Nouvel accès, donc actif
        bo.setActive(true);

        LOGGER.info("Sauvegarder en base...");

        bo = accessRepository.save(bo);

        if (!isUsagerCourrier) {
            LOGGER.info("Envoi d'un message au GU via Kafka...");
            guKafkaProducer.sendCreationAccesTSMessage(usagerId);
        }
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

        return AccessTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessDTO getAccessActive(Integer usagerId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        AccessBO bo = getAccessBOActive(usagerId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return AccessTransformer.bo2Dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessDTO getAccess(Integer pkAccess) {

        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);

        Optional<AccessBO> boOp = getAccessBO(pkAccess);

        if (boOp.isEmpty()) {
            LOGGER.error(SharedMessages.DONNEE_INTROUVABLE);
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }

        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

        return AccessTransformer.bo2Dto(boOp.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public AccessBO getAccessBOActive(Integer usagerId) {
        AccessBO accessBO = getAccessBO(usagerId, true);
        if (accessBO == null) {
            throw new DemarchesServiceException("Accès correspondant introuvable", HttpStatus.NOT_FOUND);
        }
        return accessBO;
    }

    @Override
    public AccessBO getAccessBO(Integer usagerId, boolean active) {
        AccessBO bo = null;

        Optional<AccessBO> accessBOOptional = accessRepository.findFirstByUsagerIdAndActive(usagerId, active);
        if (accessBOOptional.isPresent()) {
            bo = accessBOOptional.get();
        }

        // Gérer les accès désactivés
        if (active && bo != null && !bo.isActive()) {
            bo = null;
        }

        return bo;
    }

    private Optional<AccessBO> getAccessBO(Integer pkAccess) {

        Optional<AccessBO> boOp = accessRepository.findById(pkAccess);

        // Gérer les accès désactivés
        if (boOp.isPresent() && !boOp.get().isActive()) {
            boOp = Optional.empty();
        }

        return boOp;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteAccess(Integer usagerId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        AccessDTO dto = getAccessActive(usagerId);
        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        AccessBO bo = AccessTransformer.dto2Bo(dto);
        bo.setActive(false);
        accessRepository.save(bo);
    }

    @Override
    public List<Integer> getUsagersIds() {

        LOGGER.info("Récupération de tous les usagersIds présents en base...");

        return accessRepository.findDistinctUsagerId();
    }

    @Override
    public boolean isAccessActive(Integer pkAccess) {
        Optional<AccessBO> boOp = accessRepository.findById(pkAccess);
        return boOp.map(AccessBO::isActive).orElse(false);
    }

}
