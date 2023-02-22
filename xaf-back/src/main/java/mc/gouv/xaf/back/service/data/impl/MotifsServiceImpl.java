package mc.gouv.xaf.back.service.data.impl;

import mc.gouv.xaf.back.data.dao.MotifsRepository;
import mc.gouv.xaf.back.data.entity.MotifBO;
import mc.gouv.xaf.back.data.transformer.MotifTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.DemarchesService;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.MotifDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Service permettant la manipulation des motifs.
 * 
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class MotifsServiceImpl implements MotifsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifsServiceImpl.class);

    @Autowired
    private MotifsRepository motifsRepository;

    @Autowired
    private DemarchesService demarchesService;

    private MotifBO getMotifBO(String demarcheId, Integer pkMotif) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        MotifBO motifBo = motifsRepository.findByDemarcheIdAndPkMotifs(demarcheId, pkMotif);
        if (motifBo == null) {
            throw new DemarchesServiceException(SharedMessages.DONNEE_INTROUVABLE, HttpStatus.NOT_FOUND);
        }
        return motifBo;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MotifDTO getMotif(String demarcheId, Integer pkMotif) {
        MotifBO motifBo = getMotifBO(demarcheId, pkMotif);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return MotifTransformer.bo2Dto(motifBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Map<String, MotifDTO> getMotifs(String demarcheId, String statut) {
        LOGGER.info("getMotifs({},{})...", demarcheId, statut);
        List<MotifBO> motifBos = motifsRepository.findByDemarcheIdAndStatut(demarcheId, statut);
        return motifBos.stream().collect(Collectors.toMap(m -> m.getCode() + '_' + m.getLangue().toUpperCase(), MotifTransformer::bo2Dto));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MotifDTO> getMotifs(String demarcheId) {
        LOGGER.info(SharedMessages.RECUPERATION_EN_BASE);
        List<MotifBO> motifBos = motifsRepository.findByDemarcheId(demarcheId);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return MotifTransformer.bo2Dto(motifBos);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MotifDTO saveOrUpdateMotif(String demarcheId, MotifDTO motif) {
        if (motif.getPkMotifs() != null) {
            // PkMotifs fourni, il faut donc mettre à jour un motif
            return updateMotif(demarcheId, motif);
        } else {
            // Pas de PkMotifs fourni, il faut donc créer un nouveau motif
            return saveMotif(demarcheId, motif);
        }
    }

    public MotifDTO saveMotif(String demarcheId, MotifDTO motif) {
        // Vérification préalable de l'existence de la démarche indiquée
        demarchesService.getCheckDemarche(demarcheId);
        LOGGER.info(SharedMessages.TRANSFORMATION_DTO_BO);
        MotifBO bo = MotifTransformer.dto2Bo(motif);
        bo.setDemarcheId(demarcheId);
        bo = motifsRepository.save(bo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return MotifTransformer.bo2Dto(bo);
    }

    public MotifDTO updateMotif(String demarcheId, MotifDTO motif) {
        MotifBO motifBo = getMotifBO(demarcheId, motif.getPkMotifs());
        LOGGER.info("Mise à jour du motif...");
        motifBo.setLangue(motif.getLangue());
        motifBo.setLibelle(motif.getLibelle());
        motifBo.setStatut(motif.getStatut());
        motifBo.setCommentairePrerempli(motif.getCommentairePrerempli());
        motifBo.setCode(motif.getCode());
        // Seul l'appel DELETE permet l'inscription d'une DATE_ARCHIVE
        // La mise à jour permet seulement la mise à null afin de réactiver le motif
        if (motif.getDateArchive() == null) {
            motifBo.setDateArchive(null);
        }
        motifBo = motifsRepository.save(motifBo);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        MotifDTO ret = MotifTransformer.bo2Dto(motifBo);
        ret.setUpdated(true);
        return ret;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMotif(String demarcheId, Integer pkMotif) {
        MotifBO motifBo = getMotifBO(demarcheId, pkMotif);
        motifBo.setDateArchive(new Date());
        LOGGER.info("Définition de la date d'archivage du motif...");
        motifsRepository.save(motifBo);
    }

}
