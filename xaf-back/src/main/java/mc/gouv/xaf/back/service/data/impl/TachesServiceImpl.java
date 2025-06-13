package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.DemandesRepository;
import mc.gouv.xaf.back.data.dao.TachesRepository;
import mc.gouv.xaf.back.data.entity.DemandeBO;
import mc.gouv.xaf.back.data.entity.TacheBO;
import mc.gouv.xaf.back.data.transformer.TachesTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.TachesService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.DemandeDTO;
import mc.gouv.xaf.shared.dto.TacheDTO;
import mc.gouv.xaf.shared.enums.StatutTachesEnum;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * @author mboutelier.ext
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class TachesServiceImpl implements TachesService {

    private static final Logger LOGGER = LoggerFactory.getLogger(TachesServiceImpl.class);

    @Autowired
    protected TachesRepository tachesRepository;

    @Autowired
    protected DemandesRepository demandesRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public TacheDTO getTacheByID(Integer pkTaches) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE, pkTaches);
        Optional<TacheBO> tacheBO = tachesRepository.findById(pkTaches);
        if (tacheBO.isEmpty()) {
            throw new DemarchesServiceException("La tâche spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TachesTransformer.bo2dto(tacheBO.get());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<TacheDTO> getTachesByDemandeID(Integer demandeId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE, demandeId);
        List<TacheBO> tachesBOS = tachesRepository.findByPkDemandes(demandeId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TachesTransformer.bos2Dtos(tachesBOS);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TacheDTO saveOrUpdate(TacheDTO toSave) {
        // Vérification préalable de l'existence de la demande
        Optional<DemandeBO> demandesBoOptional = demandesRepository.findById(toSave.getFkDemande());
        if (demandesBoOptional.isEmpty()) {
            throw new DemarchesServiceException("La demande spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }

        if (toSave.getPkTaches() == null) {
            LOGGER.info("Création d'une nouvelle tâche ...");
        } else {
            LOGGER.info("Mise à jour d'une tâche ...");
            Optional<TacheBO> tacheBOOptional = tachesRepository.findById(toSave.getPkTaches());
            if (tacheBOOptional.isEmpty()) {
                throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
            }
        }

        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        TacheBO bo = TachesTransformer.dto2bo(toSave);
        bo.setDemande(demandesBoOptional.get());
        bo = tachesRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return TachesTransformer.bo2dto(bo);
    }

    /**
     * {@inheritDoc}
     */
    public List<TacheDTO> creerListeDeTaches(DemandeDTO demande) {
        return new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTachesPourRetourGuichet(Integer pkDemandes) {
        // Vérification préalable de l'existence de la demande
        Optional<DemandeBO> demandesBoOptional = demandesRepository.findById(pkDemandes);
        if (demandesBoOptional.isEmpty()) {
            throw new DemarchesServiceException("La demande spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Retour au Guichet : Récupération des tâches de la demande {}...", pkDemandes);
        List<TacheBO> taches = tachesRepository.findByPkDemandes(pkDemandes);
        for (TacheBO tacheBO : taches) {
            if (StatutTachesEnum.RETOUR_GUICHET.name().equals(tacheBO.getCodeStatutValideur())) {
                tacheBO.setCodeStatutAgent(null);
                tacheBO.setCodeStatutValideur(null);
            } else {
                tacheBO.setCodeStatutAgent(tacheBO.getCodeStatutValideur());
                tacheBO.setLocked(true);
            }
        }

        LOGGER.info("Retour au Guichet : Sauvegarde en base des tâches...");
        tachesRepository.saveAll(taches);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void updateTachesLock(Integer pkDemandes) {
        LOGGER.info("Bloquage des tâches validées et refusées...");
        tachesRepository.updateTachesLock(pkDemandes);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteTaches(Integer pkDemande) {
        LOGGER.info("Purge des tâches liées à la demande : {}...", pkDemande);
        tachesRepository.deleteByDemande_PkDemandes(pkDemande);
    }
}
