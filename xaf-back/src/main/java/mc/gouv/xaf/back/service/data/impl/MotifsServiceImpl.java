package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import mc.gouv.xaf.back.data.dao.DemarchesRepository;
import mc.gouv.xaf.back.data.dao.MotifsRepository;
import mc.gouv.xaf.back.data.entity.DemarchesBO;
import mc.gouv.xaf.back.data.entity.MotifBO;
import mc.gouv.xaf.back.data.transformer.MotifTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.MotifsService;
import mc.gouv.xaf.back.shared.dto.MotifDTO;

/**
 * Service permettant la manipulation des motifs.
 * 
 * @author qdeme
 *
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class MotifsServiceImpl implements MotifsService {

    private static final Logger LOGGER = LoggerFactory.getLogger(MotifsServiceImpl.class);

    @Autowired
    private MotifsRepository motifsRepository;

    @Autowired
    private DemarchesRepository demarchesRepository;

    /**
     * {@inheritDoc}
     */
    @Override
    public MotifDTO getMotif(String demarcheId, Integer pkMotif) {

        LOGGER.info("Récupération en base du motif...");

        MotifBO motifBo = motifsRepository.findByDemarcheIdAndPkMotifs(demarcheId, pkMotif);

        if (motifBo == null) {
            throw new DemarchesServiceException("Motif introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation bo -> dto ...");

        return MotifTransformer.bo2Dto(motifBo);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public List<MotifDTO> getMotifs(String demarcheId) {

        LOGGER.info("Récupération en base des motifs...");

        List<MotifBO> motifBos = motifsRepository.findByDemarcheId(demarcheId);

        LOGGER.info("Transformation bo -> dto ...");

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
        Optional<DemarchesBO> demarcheBo = demarchesRepository.findById(demarcheId);
        if (!demarcheBo.isPresent()) {
            throw new DemarchesServiceException("La démarche spécifiée est introuvable", HttpStatus.NOT_FOUND);
        }

        LOGGER.info("Transformation dto -> bo");

        MotifBO bo = MotifTransformer.dto2Bo(motif);
        bo.setDemarcheId(demarcheId);

        bo = motifsRepository.save(bo);

        LOGGER.info("Transformation bo -> dto ...");

        return MotifTransformer.bo2Dto(bo);
    }

    public MotifDTO updateMotif(String demarcheId, MotifDTO motif) {

        LOGGER.info("Récupération en base du motif...");

        MotifBO motifBo = motifsRepository.findByDemarcheIdAndPkMotifs(demarcheId, motif.getPkMotifs());

        if (motifBo == null) {
            throw new DemarchesServiceException("Motif introuvable", HttpStatus.NOT_FOUND);
        }

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

        LOGGER.info("Transformation bo -> dto ...");

        MotifDTO ret = MotifTransformer.bo2Dto(motifBo);
        ret.setUpdated(true);

        return ret;
    }

    // Ancienne suppression : suppression réelle
    // /**
    // * {@inheritDoc}
    // */
    // @Override
    // public void deleteMotif(MotifDTO motif) {
    //
    // LOGGER.info("Récupération en base du motif...");
    //
    // MotifBO motifBo = motifsRepository.findOne(motif.getPkMotifs());
    //
    // if (motifBo == null) {
    // throw new DemarchesServiceException("Motif introuvable",
    // HttpStatus.NOT_FOUND);
    // }
    //
    // LOGGER.info("Suppression du motif...");
    //
    // motifsRepository.delete(motifBo);
    // }

    // Nouvelle suppression, suppression logique
    /**
     * {@inheritDoc}
     */
    @Override
    public void deleteMotif(String demarcheId, Integer pkMotif) {

        LOGGER.info("Récupération en base du motif...");

        MotifBO motifBo = motifsRepository.findByDemarcheIdAndPkMotifs(demarcheId, pkMotif);

        if (motifBo == null) {
            throw new DemarchesServiceException("Motif introuvable", HttpStatus.NOT_FOUND);
        }

        motifBo.setDateArchive(new Date());

        LOGGER.info("Définition de la date d'archivage du motif...");

        motifsRepository.save(motifBo);
    }

}
