package mc.gouv.xaf.back.service.data.impl;

import java.util.Date;
import java.util.List;
import java.util.Optional;
import mc.gouv.xaf.back.data.dao.PeriodesOuvertureRepository;
import mc.gouv.xaf.back.data.entity.PeriodesOuvertureBO;
import mc.gouv.xaf.back.data.transformer.PeriodeOuvertureTransformer;
import mc.gouv.xaf.back.exception.DemarchesServiceException;
import mc.gouv.xaf.back.service.data.PeriodesOuvertureService;
import mc.gouv.xaf.shared.SharedMessages;
import mc.gouv.xaf.shared.dto.PeriodeOuvertureDTO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Service permettant la manipulation des périodes d'ouverture
 *
 * @author qdeme
 */
@Component
@Transactional(rollbackFor = Exception.class)
public class PeriodesOuvertureServiceImpl implements PeriodesOuvertureService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PeriodesOuvertureServiceImpl.class);

    @Autowired
    private PeriodesOuvertureRepository periodesOuvertureRepository;

    @Override
    public List<PeriodeOuvertureDTO> getPeriodesOuverture() {
        LOGGER.info("Récupération en base des périodes d'ouverture...");
        List<PeriodesOuvertureBO> periodesOuvertureBos = periodesOuvertureRepository.findAll();
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PeriodeOuvertureTransformer.bo2Dto(periodesOuvertureBos);
    }

    @Override
    public Page<PeriodeOuvertureDTO> getPeriodesOuverturePageable(Pageable pageable) {
        LOGGER.info("Récupération en base des périodes d'ouverture...");
        Page<PeriodesOuvertureBO> periodesOuvertureBos = periodesOuvertureRepository.findAll(pageable);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return periodesOuvertureBos.map(PeriodeOuvertureTransformer::bo2Dto);
    }

    @Override
    public PeriodeOuvertureDTO getDernierePeriodeOuvertureTerminee() {
        Date date = new Date();
        LOGGER.info("Récupération en base des périodes d'ouvertures avant le {} ...", date);
        List<PeriodesOuvertureBO> periodesOuvertureBOS = periodesOuvertureRepository.findByDateFinBeforeOrderByDateFinDesc(
                date);
        PeriodeOuvertureDTO dto = null;
        if (!periodesOuvertureBOS.isEmpty()) {
            PeriodesOuvertureBO bo = periodesOuvertureBOS.get(0);
            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
            dto = PeriodeOuvertureTransformer.bo2Dto(bo);
        } else {
            LOGGER.info("Aucune période n'a été trouvée.");
        }
        return dto;
    }

    @Override
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureFutures() {
        Date date = new Date();
        LOGGER.info("Récupération en base des périodes d'ouverture commençant après le {} ...", date);
        List<PeriodesOuvertureBO> periodesOuvertureBos = periodesOuvertureRepository.findByDateDebutAfter(date);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PeriodeOuvertureTransformer.bo2Dto(periodesOuvertureBos);
    }

    @Override
    public List<PeriodeOuvertureDTO> getPeriodesOuvertureEnCours() {
        Date date = new Date();
        LOGGER.info("Récupération en base des périodes d'ouverture en cours ...");
        List<PeriodesOuvertureBO> periodesOuvertureBos = periodesOuvertureRepository.findByDateDebutLessThanEqualAndDateFinGreaterThanEqual(
                date, date);
        LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);
        return PeriodeOuvertureTransformer.bo2Dto(periodesOuvertureBos);
    }

    @Override
    public PeriodeOuvertureDTO saveOrUpdatePeriodeOuverture(PeriodeOuvertureDTO periodeOuverture) {
        // Création
        if (periodeOuverture.getPkPeriodesOuverture() == null) {
            LOGGER.info("Création d'une période d'ouverture");
            LOGGER.info("Transformation dto -> bo");

            PeriodesOuvertureBO bo = PeriodeOuvertureTransformer.dto2Bo(periodeOuverture);

            bo = periodesOuvertureRepository.save(bo);

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

            return PeriodeOuvertureTransformer.bo2Dto(bo);
        }
        // Mise à jour
        else {
            LOGGER.info("Mise à jour de la période d'ouverture");

            Optional<PeriodesOuvertureBO> periodeOuvertureBoOpt = periodesOuvertureRepository.findById(
                    periodeOuverture.getPkPeriodesOuverture());
            if (periodeOuvertureBoOpt.isEmpty()) {
                throw new DemarchesServiceException("La période d'ouverture spécifiée est introuvable",
                        HttpStatus.NOT_FOUND);
            }

            PeriodesOuvertureBO periodeOuvertureBo = periodeOuvertureBoOpt.get();

            periodeOuvertureBo.setDateDebut(periodeOuverture.getDateDebut());
            periodeOuvertureBo.setDateFin(periodeOuverture.getDateFin());

            periodeOuvertureBo = periodesOuvertureRepository.save(periodeOuvertureBo);

            LOGGER.info(SharedMessages.TRANSFORMATION_BO_DTO);

            return PeriodeOuvertureTransformer.bo2Dto(periodeOuvertureBo);

        }
    }

    @Override
    public void deletePeriodeOuverture(Integer pkPeriodeOuverture) {
        Optional<PeriodesOuvertureBO> periodeOuvertureBoOpt = periodesOuvertureRepository.findById(pkPeriodeOuverture);
        if (periodeOuvertureBoOpt.isEmpty()) {
            throw new DemarchesServiceException("La période d'ouverture spécifiée est introuvable",
                    HttpStatus.NOT_FOUND);
        }
        LOGGER.info("Suppression de la période d'ouverture...");
        periodesOuvertureRepository.delete(periodeOuvertureBoOpt.get());
    }

    @Override
    public void deleteAllPeriodeOuverture() {
        LOGGER.info("Récupération des périodes à supprimer ...");
        List<PeriodesOuvertureBO> periodesOuvertureBos = periodesOuvertureRepository.findAll();
        if (periodesOuvertureBos.isEmpty()) {
            LOGGER.info("Aucune période à supprimer");
        } else {
            LOGGER.info("{} périodes vont être supprimées ...", periodesOuvertureBos.size());
            periodesOuvertureRepository.deleteAll(periodesOuvertureBos);
        }
    }

}
